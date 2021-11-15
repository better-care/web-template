/* Copyright 2021 Better Ltd (www.better.care)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package care.better.platform.web.template.builder.id

import care.better.platform.template.AmNode
import care.better.platform.web.template.builder.exception.BuilderException
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import com.google.common.collect.Multimap
import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.regex.Pattern

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@Suppress("SpellCheckingInspection")
internal class WebTemplateIdBuilder {
    companion object {
        private val INTERVAL_PATTERN = Pattern.compile("DV_INTERVAL<DV_([^>]+)>")

        private val TO_JSON_ID: (WebTemplateNode) -> String = { it.jsonId }
    }

    private val segments: Deque<WebTemplateNode> = ArrayDeque()
    private val idDeduplicator: IdDeduplicator = NumericSuffixIdDeduplicator()

    fun buildIds(node: WebTemplateNode, nodes: Multimap<AmNode, WebTemplateNode>): WebTemplateNode {
        val parent = segments.peek()
        if (parent != null) {
            WebTemplateBuilderUtils.buildChain(node, parent)
        }
        buildId(node)
        updateNodesMap(node, nodes)
        segments.push(node)
        if (node.children.isNotEmpty()) {
            handleChildren(node, nodes)
            updateDependsOn(node.children)
        }
        if (node.cardinalities != null) {
            updateCardinalities(node)
        }
        segments.pop()
        return node
    }

    private fun handleChildren(node: WebTemplateNode, nodes: Multimap<AmNode, WebTemplateNode>) {
        val isChoice = node.rmType == "ELEMENT" && node.children.size > 1
        if (isChoice) { // make sure coded text is before text
            fixPolymorphicOrder(node.children)
        }
        node.children.forEachIndexed { index, childWebTemplateNode ->
            if (isChoice) {
                val key = node.amNode.attributes.asSequence()
                    .firstOrNull { attribute ->  attribute.value.children.any { it == childWebTemplateNode.amNode } }
                    ?.key ?: throw BuilderException("AM node for ${childWebTemplateNode.path} not found.")

                val type = childWebTemplateNode.rmType
                if (StringUtils.isNotBlank(type) && type.startsWith("DV_") && "value" == key) {
                    childWebTemplateNode.jsonId = buildTypedId(type)
                    childWebTemplateNode.alternativeId = "${segments.peek().id}/value${if (index > 0) (index + 1).toString() else ""}"
                    childWebTemplateNode.alternativeJsonId = "value${if (index > 0) (index + 1).toString() else ""}"
                } else {
                    childWebTemplateNode.jsonId = key
                }
            }
            buildIds(childWebTemplateNode, nodes)
        }
    }

    private fun buildTypedId(type: String): String =
        with(INTERVAL_PATTERN.matcher(type)) {
            if (this.matches())
                "interval_of_${this.group(1).lowercase()}_value"
            else
                "${type.substring(3).lowercase()}_value"
        }

    private fun fixPolymorphicOrder(children: MutableList<WebTemplateNode>) {
        val codedText = children.firstOrNull { it.rmType == "DV_CODED_TEXT" }?.let { children.indexOf(it) }
        val text = children.firstOrNull { it.rmType == "DV_TEXT" }?.let { children.indexOf(it) }

        if (codedText != null && text != null && codedText > text) { // swap if text is before coded_text
            children.add(text, children.removeAt(codedText))
        }
    }

    private fun updateNodesMap(node: WebTemplateNode, nodes: Multimap<AmNode, WebTemplateNode>) {
        nodes.put(node.amNode, node)
        node.chain.forEach {
            if (!nodes.containsEntry(it, node)) {
                nodes.put(it, node)
            }
        }
    }

    private fun updateCardinalities(node: WebTemplateNode) {
        val cardinalities = node.cardinalities
        if (cardinalities != null) {
            val iterator = cardinalities.iterator()
            while (iterator.hasNext()) {
                val cardinality = iterator.next()
                val ids = node.children.asSequence().filter(PathPrefixPredicate(cardinality.path!!)).map(TO_JSON_ID).toList()
                cardinality.ids = ids
                if (ids.isEmpty()) {
                    iterator.remove()
                }
            }
        }
    }

    private fun updateDependsOn(children: List<WebTemplateNode>) {
        children.forEach {
            if (it.dependsOn != null) {
                updateDependsOn(children, it)
            }
        }
    }

    private fun updateDependsOn(children: List<WebTemplateNode>, child: WebTemplateNode) {
        val dependsOn: MutableSet<String> = hashSetOf()
        child.dependsOn?.forEach {
            dependsOn.addAll(children.asSequence().filter(DependsOnPredicate(it)).map(TO_JSON_ID).toList())
        }

        child.dependsOn = if (dependsOn.isEmpty()) null else dependsOn.toMutableList()
    }

    private fun buildId(node: WebTemplateNode) {
        val baseId = with(getBaseId(node)) {
            when {
                this.isEmpty() -> "id"
                Character.isDigit(this.codePointAt(0)) -> "a$this"
                else -> this
            }
        }
        val parentId = if (segments.isEmpty()) "" else "${segments.peek().id}/"
        val id = idDeduplicator.getUniqueBaseId(parentId, baseId)
        node.id = "${parentId}${id}"
        node.jsonId = id
    }

    private fun getBaseId(node: WebTemplateNode): String {
        val name: String =
            when {
                node.isJsonIdInitialized() -> WebTemplateConversionUtils.getWebTemplatePathSegmentForName(node.jsonId)
                node.name.isNullOrBlank() -> {
                    if ("ELEMENT" == segments.peek().rmType && !node.rmType.startsWith("DV_INTERVAL")) { // multiple ored data values
                        "value"
                    } else {
                        getLastPathElement(node)
                    }
                }
                else -> node.name!!
            }
        return WebTemplateConversionUtils.getWebTemplatePathSegmentForName(name)
    }

    private fun getLastPathElement(node: WebTemplateNode): String {
        val path = node.path
        val lastSlash = path.lastIndexOf('/')
        return if (lastSlash == -1) path else path.substring(lastSlash + 1)
    }

    class PathPrefixPredicate internal constructor(private val pathPrefix: String) : (WebTemplateNode) -> Boolean {
        override fun invoke(node: WebTemplateNode): Boolean = node.path.startsWith(pathPrefix)
    }

    class DependsOnPredicate internal constructor(private val pathPrefix: String) : (WebTemplateNode) -> Boolean {
        override fun invoke(node: WebTemplateNode): Boolean = true != node.inContext && node.path.startsWith(pathPrefix)
    }
}
