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
import com.fasterxml.jackson.databind.node.BooleanNode
import org.openehr.rm.datatypes.DvInterval

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [SpecialCaseRmObjectHandler] that handles "upper_unbounded" attribute.
 */
internal object DvIntervalUpperUnboundedHandler : SpecialCaseRmObjectHandler {
    const val specialAttribute: String = "|upper_unbounded"

    private val dvIntervalClass = DvInterval::class.java


    override fun handleOnRmObject(
            conversionContext: ConversionContext,
            amNode: AmNode,
            jsonNode: JsonNode,
            rmObject: RmObject,
            webTemplatePath: WebTemplatePath) {
        (rmObject as DvInterval).upperUnbounded = if (jsonNode.isBoolean) (jsonNode as BooleanNode).booleanValue() else jsonNode.asText().toBoolean()
    }

    override fun getAcceptableClass(): Class<*> = dvIntervalClass

    override fun getAttributeName(): String = specialAttribute
}
