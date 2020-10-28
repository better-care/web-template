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
 * Singleton instance of [PostProcessor] that post-processes [MutableList].
 */
internal object MutableListPostProcessor : PostProcessor<MutableList<Any>> {
    private val supportedClass = MutableList::class.java

    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode, instance: MutableList<Any>, webTemplatePath: WebTemplatePath) {

        val nameSuffixIndexMap: MutableMap<Pair<String, String>, Int> = mutableMapOf()
        val iterator = instance.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
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

        sortCollection(amNode, instance)
    }

    /**
     * Sorts [MutableList] using [AmNode].
     *
     * @param amNode [AmNode]
     * @param list [MutableList]
     */
    private fun sortCollection(amNode: AmNode, list: MutableList<Any>) {
        AmUtils.attributeOf(amNode.parent!!, amNode)?.also { attribute ->
            val sortedList = mutableListOf<Any>()
            attribute.children.forEach { child -> moveMatching(child, list, sortedList) }
            sortedList.addAll(list)
            list.clear()
            list.addAll(sortedList)

        }
    }

    private fun moveMatching(child: AmNode, list: MutableList<Any>, sorted: MutableList<Any>) {
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next is Locatable && AmUtils.matches(child, next)) {
                sorted.add(next)
                iterator.remove()
            }
        }
    }

    override fun getType(): Class<*> = supportedClass
}
