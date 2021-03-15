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

package care.better.platform.web.template.builder.compactor

import care.better.platform.utils.RmUtils
import care.better.platform.utils.exception.RmClassCastException
import care.better.platform.web.template.builder.model.WebTemplateCardinality
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.range.WebTemplateIntegerRange
import com.google.common.collect.Iterables
import org.apache.commons.lang3.StringUtils
import org.openehr.rm.datastructures.*
import org.openehr.rm.datatypes.DataValue
import java.util.*
import kotlin.collections.HashMap

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal open class MinimalWebTemplateCompactor : WebTemplateCompactor {

    companion object {
        private val SKIP_IF_EMPTY: Set<String> =
            setOf(
                "LIST",
                RmUtils.getRmTypeName(Cluster::class.java),
                RmUtils.getRmTypeName(Element::class.java),
                RmUtils.getRmTypeName(ItemTree::class.java),
                RmUtils.getRmTypeName(ItemList::class.java),
                RmUtils.getRmTypeName(ItemSingle::class.java),
                RmUtils.getRmTypeName(ItemTable::class.java),
                RmUtils.getRmTypeName(ItemStructure::class.java),
                RmUtils.getRmTypeName(History::class.java),
                RmUtils.getRmTypeName(PointEvent::class.java),
                RmUtils.getRmTypeName(IntervalEvent::class.java),
                RmUtils.getRmTypeName(Event::class.java),
                RmUtils.getRmTypeName(Item::class.java))
    }

    private val segments: Deque<WebTemplateNode> = ArrayDeque()

    override fun compact(node: WebTemplateNode): WebTemplateNode? {
        segments.push(node)
        val compactedNode = compactNode(node)
        segments.pop()
        return compactedNode
    }

    protected open fun compactNode(node: WebTemplateNode): WebTemplateNode? {
        val iterator = node.children.listIterator()
        while (iterator.hasNext()) {
            val compactedNode = compact(iterator.next())
            if (compactedNode == null)
                iterator.remove()
            else
                iterator.set(compactedNode)
        }
        return processChildren(node)
    }

    private fun processChildren(node: WebTemplateNode): WebTemplateNode? {
        compactChildren(node.children)
        return if (!node.hasInput() && node.children.size == 1 && segments.size > 1 && isSkippable(node)) {
            if (node.cardinalities != null) {
                copyCardinalities(node.cardinalities, Iterables.get(segments, 2))
            }
            val childNode = node.children[0]
            if ("ELEMENT" == node.rmType && (childNode.occurences?.min == null || 0 == childNode.occurences?.min)) {
                childNode.occurences?.also {
                    it.min = 1
                }
            }
            copyValues(node, childNode)
            childNode
        } else if (node.children.isEmpty() && SKIP_IF_EMPTY.contains(node.rmType)) {
            null
        } else {
            node
        }
    }

    @Suppress("SpellCheckingInspection")
    protected open fun isSkippable(node: WebTemplateNode): Boolean =
        try {
            DataValue::class.java.isAssignableFrom(RmUtils.getRmClass(node.rmType))
        } catch (ignored: RmClassCastException) {
            false
        }

    private fun copyValues(from: WebTemplateNode, to: WebTemplateNode) {
        to.name = from.name
        to.localizedName = from.localizedName
        to.localizedNames.clear()
        to.localizedNames.putAll(from.localizedNames)
        to.localizedDescriptions.putAll(from.localizedDescriptions)
        to.nodeId = from.nodeId
        to.nameCodeString = from.nameCodeString
        try {
            if (!DataValue::class.java.isAssignableFrom(RmUtils.getRmClass(to.rmType))) {
                to.rmType = from.rmType
            }
        } catch (ignored: RmClassCastException) {
            to.rmType = from.rmType
        }
        val fromOccurrences = from.occurences!!
        val toOccurrences = to.occurences!!
        if (toOccurrences.min == null || fromMinGtTo(fromOccurrences, toOccurrences) || fromElementToDataValue(to)) {
            toOccurrences.min = fromOccurrences.min
        }
        if (fromOccurrences.max == null || fromMaxLtTo(fromOccurrences, toOccurrences)) {
            toOccurrences.max = fromOccurrences.max
        }
        if (from.dependsOn != null) {
            copyDependsOn(from, to)
        }
        if (from.hasInput() && !to.hasInput()) {
            to.inputs.clear()
            to.inputs.addAll(from.inputs)
        }
        to.annotations.putAll(from.annotations)
        to.termBindings.putAll(from.termBindings)
    }

    private fun fromElementToDataValue(to: WebTemplateNode): Boolean =
        to.rmType.startsWith("DV_") && 1 == to.occurences?.min  // ELEMENT.value is always 1..1, so copy from ELEMENT occurrences


    private fun fromMaxLtTo(fromOccurrences: WebTemplateIntegerRange, toOccurrences: WebTemplateIntegerRange): Boolean =
        with(toOccurrences.max) {
            this != null && this < fromOccurrences.max!!
        }

    private fun fromMinGtTo(fromOccurrences: WebTemplateIntegerRange, toOccurrences: WebTemplateIntegerRange): Boolean =
        with(fromOccurrences.min) {
            this != null && toOccurrences.min!! > this
        }

    fun copyDependsOn(from: WebTemplateNode, to: WebTemplateNode) {
        val dependsOn = to.dependsOn
        if (dependsOn == null)
            to.dependsOn = from.dependsOn
        else
            from.dependsOn?.also { dependsOn.addAll(it) }
    }

    private fun compactChildren(currentChildren: MutableList<WebTemplateNode>) {
        compactCodedTextWithOther(currentChildren)
        compactMultipleCodedTexts(currentChildren)
    }

    protected fun copyCardinalities(cardinalities: MutableList<WebTemplateCardinality>?, node: WebTemplateNode) {
        val nodeCardinalities = node.cardinalities
        if (nodeCardinalities == null)
            node.cardinalities = cardinalities
        else
            cardinalities?.also { nodeCardinalities.addAll(it) }
    }

    private fun compactCodedTextWithOther(children: MutableList<WebTemplateNode>) {
        if (children.size == 2) {
            val node0 = children[0]
            val node1 = children[1]
            if (setOf("DV_TEXT", "DV_CODED_TEXT") == setOf(node0.rmType, node1.rmType)) {
                val difference = StringUtils.difference(node0.path, node1.path)
                if ("/defining_code" == difference) {
                    node1.inputs.add(WebTemplateInput(WebTemplateInputType.TEXT, "other"))
                    node1.getInput()?.listOpen = true
                    children.removeAt(0)
                } else if (difference.isEmpty()) {
                    node0.inputs.add(WebTemplateInput(WebTemplateInputType.TEXT, "other"))
                    node0.getInput()?.listOpen = true
                    children.removeAt(1)
                }
            }
        }
    }

    private fun compactMultipleCodedTexts(children: MutableList<WebTemplateNode>) {
        val matchingChildren = mergeChildrenWithMatchingPaths(children)
        for (webTemplateNodes in matchingChildren.values) {
            // are all for defining_code
            if (webTemplateNodes.size == 2 && webTemplateNodes[0].path.endsWith("defining_code")) {
                val child0 = webTemplateNodes[0]
                val constrained0 = isConstrained(child0)
                val child1 = webTemplateNodes[1]
                val constrained1 = isConstrained(child1)
                if (constrained0 && !constrained1) {
                    children.remove(child1)
                } else if (constrained1 && !constrained0) {
                    children.remove(child0)
                } else {
                    val input = mergeInputs(child0.getInput()!!, child1.getInput()!!)
                    if (input != null) {
                        child0.setInput(input)
                        children.remove(child1)
                    }
                }
            }
        }
    }

    private fun isConstrained(child: WebTemplateNode): Boolean =
        child.hasInput() && child.getInput()!!.validation != null

    private fun mergeChildrenWithMatchingPaths(children: List<WebTemplateNode>): Map<String, MutableList<WebTemplateNode>> {
        val matchingChildren: MutableMap<String, MutableList<WebTemplateNode>> = HashMap()
        for (child in children) {
            val rmPath = child.path
            if (!matchingChildren.containsKey(rmPath)) {
                matchingChildren[rmPath] = ArrayList()
            }
            matchingChildren[rmPath]?.add(child)
        }
        return matchingChildren
    }

    private fun mergeInputs(firstInput: WebTemplateInput, secondInput: WebTemplateInput): WebTemplateInput? {
        val input = WebTemplateInput(firstInput.type)
        when {
            firstInput.list.isEmpty() -> input.list.addAll(secondInput.list)
            secondInput.list.isEmpty() -> input.list.addAll(firstInput.list)
            else -> {
                input.list.addAll(firstInput.list)
                input.list.addAll(secondInput.list)
            }
        }
        when {
            firstInput.validation == null -> input.validation = secondInput.validation
            secondInput.validation == null -> input.validation = firstInput.validation
            else -> return null
        }

        input.fixed = false

        when {
            input.list.isNotEmpty() && input.validation != null -> input.listOpen = true
            true != firstInput.listOpen && secondInput.listOpen != null -> input.listOpen = secondInput.listOpen
            true != secondInput.listOpen && firstInput.listOpen != null -> input.listOpen = firstInput.listOpen
            else -> return null
        }
        return input
    }
}
