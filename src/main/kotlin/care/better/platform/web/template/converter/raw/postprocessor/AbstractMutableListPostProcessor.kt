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

package care.better.platform.web.template.converter.raw.postprocessor

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.openehr.rm.common.Locatable

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * [PostProcessor] that post-processes [MutableList].
 */
abstract class AbstractMutableListPostProcessor : PostProcessor<MutableList<Any>> {
    private val supportedClass = MutableList::class.java

    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: MutableList<Any>, webTemplatePath: WebTemplatePath?) {

        val nameSuffixIndexMap: MutableMap<Pair<String, String>, Int> = mutableMapOf()
        val iterator = instance.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (mustRemove(next)) {
                iterator.remove()
            } else {
                if (next is Locatable) {
                    val name = next.name?.value!!
                    val pair = Pair(next.archetypeNodeId!!, name)
                    val index = nameSuffixIndexMap[pair]

                    val suffixIndex = if (index == null) {
                        1
                    } else {
                        val i = index + 1
                        next.name?.value = "$name #$i"
                        i
                    }
                    nameSuffixIndexMap[pair] = suffixIndex
                }
            }
        }

        if (amNode != null) {
            sortCollection(amNode, instance)
        }
    }

    abstract fun mustRemove(element: Any): Boolean

    /**
     * Sorts [MutableList] using [AmNode].
     *
     * @param amNode [AmNode]
     * @param list [MutableList]
     */
    private fun sortCollection(amNode: AmNode, list: MutableList<Any>) {
        AmUtils.attributeOf(amNode.parent!!, amNode)?.also { attribute ->
            val constrainedMap = attribute.children.asSequence()
                .filter { AmUtils.isNameConstrained(it) }
                .associateBy({ it }, { getMatching(it, list) })

            val unconstrainedMap = attribute.children.asSequence()
                .filter { !AmUtils.isNameConstrained(it) }
                .associateBy({ it }, { getMatching(it, list) })

            val sortedList = mutableListOf<Any>()

            attribute.children.forEach { sortedList.addAll(constrainedMap[it] ?: requireNotNull(unconstrainedMap[it])) }

            sortedList.addAll(list)
            list.clear()
            list.addAll(sortedList)
        }
    }

    private fun getMatching(amNode: AmNode, originalList: MutableList<Any>): MutableList<Any> {
        val list: MutableList<Any> = mutableListOf()
        val iterator = originalList.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next is Locatable && AmUtils.matches(amNode, next)) {
                list.add(next)
                iterator.remove()
            }
        }
        return list
    }

    override fun getType(): Class<*> = supportedClass
}
