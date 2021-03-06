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

package care.better.platform.web.template.converter.raw.special

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.composition.Observation

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [SpecialCaseRmObjectHandler] that handles "data_history_origin" attribute.
 */
internal object HistoryDataOriginHandler : SpecialCaseRmObjectHandler {
    const val specialAttribute: String = "data_history_origin"

    private val observationClass = Observation::class.java

    override fun getAcceptableClass(): Class<*> = observationClass

    override fun getAttributeName(): String = specialAttribute

    override fun handleOnRmObject(
            conversionContext: ConversionContext,
            amNode: AmNode,
            jsonNode: JsonNode,
            rmObject: RmObject,
            webTemplatePath: WebTemplatePath) {
        (rmObject as Observation).data?.also {
            it.origin = createDvDateTime(conversionContext, amNode, jsonNode, webTemplatePath + specialAttribute)
        }
    }
}
