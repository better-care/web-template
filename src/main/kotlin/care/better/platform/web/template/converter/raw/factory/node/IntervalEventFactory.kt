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

package care.better.platform.web.template.converter.raw.factory.node

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.openehr.am.aom.CCodePhrase
import org.openehr.rm.datastructures.IntervalEvent
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [EventFactory] that creates a new instance of [IntervalEvent].
 */
internal object IntervalEventFactory : EventFactory<IntervalEvent>() {
    private const val DEFAULT_CODE: String = "146"
    private val MATH_FUNCTIONS: Map<String, String> =
        mapOf(
            Pair("144", "maximum"),
            Pair("145", "minimum"),
            Pair(DEFAULT_CODE, "mean"),
            Pair("147", "change"),
            Pair("148", "total"),
            Pair("267", "mode"),
            Pair("268", "median"))


    override fun createEvent(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): IntervalEvent =
        IntervalEvent().apply {
            this.mathFunction = DvCodedText().apply {
                this.definingCode = CodePhrase.create("openehr", DEFAULT_CODE)
                this.value = MATH_FUNCTIONS[DEFAULT_CODE]
            }

            val node: AmNode? = amNode?.let { AmUtils.getAmNode(it, "math_function", "defining_code") }
            node?.also {
                val cObject = node.cObject
                if (cObject is CCodePhrase && cObject.codeList.isNotEmpty()) {
                    this.mathFunction = DvCodedText().apply {
                        this.definingCode = CodePhrase.create(cObject.terminologyId?.value!!, cObject.codeList[0])
                        this.value = MATH_FUNCTIONS[cObject.codeList[0]]
                    }
                }
            }
        }


}
