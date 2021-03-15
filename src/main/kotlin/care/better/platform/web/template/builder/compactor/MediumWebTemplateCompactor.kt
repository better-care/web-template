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
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.datastructures.*

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@Suppress("SpellCheckingInspection")
internal class MediumWebTemplateCompactor : MinimalWebTemplateCompactor() {
    companion object {
        private val ALWAYS_COMPACTABLE_CLASSES: Set<String> = setOf(
            RmUtils.getRmTypeName(ItemTree::class.java),
            RmUtils.getRmTypeName(ItemList::class.java),
            RmUtils.getRmTypeName(ItemSingle::class.java),
            RmUtils.getRmTypeName(ItemTable::class.java),
            RmUtils.getRmTypeName(ItemStructure::class.java),
            RmUtils.getRmTypeName(History::class.java))

        private val SINGLE_COMPACTABLE_CLASSES: Set<String> = setOf(
            RmUtils.getRmTypeName(PointEvent::class.java),
            RmUtils.getRmTypeName(IntervalEvent::class.java),
            RmUtils.getRmTypeName(Event::class.java))
    }

    override fun compactNode(node: WebTemplateNode): WebTemplateNode? =
        with(getCompacted(node, node.children)) {
            node.children.clear()
            node.children.addAll(this)
            super.compactNode(node)
        }

    private fun getCompacted(node: WebTemplateNode, children: List<WebTemplateNode>): List<WebTemplateNode> =
        with(mutableListOf<WebTemplateNode>()) {
            children.forEach {
                if (isCompactable(it, children)) {
                    if (it.dependsOn != null) {
                        copyDependsOnToChildren(it)
                    }
                    if (it.cardinalities != null) {
                        copyCardinalities(it.cardinalities, node)
                    }
                    this.addAll(getCompacted(it, it.children))
                } else {
                    this.add(it)
                }
            }
            this.toList()
        }

    private fun copyDependsOnToChildren(node: WebTemplateNode) {
        node.children.forEach { copyDependsOn(node, it) }
    }

    private fun isCompactable(child: WebTemplateNode, children: List<WebTemplateNode>): Boolean =
        if (ALWAYS_COMPACTABLE_CLASSES.contains(child.rmType))
            true
        else
            1 == child.occurences?.getJsonMax() && SINGLE_COMPACTABLE_CLASSES.contains(child.rmType) && children.none { it != child && typesMatch(child, it) }

    private fun typesMatch(firstNode: WebTemplateNode, secondNode: WebTemplateNode): Boolean =
        firstNode.rmType == secondNode.rmType || firstNode.rmType.endsWith("EVENT") && secondNode.rmType.endsWith("EVENT")

    override fun isSkippable(node: WebTemplateNode): Boolean = super.isSkippable(node) || "ELEMENT" == node.rmType
}
