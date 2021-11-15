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

package care.better.platform.web.template.converter.raw.factory.leaf

import care.better.openehr.terminology.OpenEhrTerminology
import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvQuantified

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [DvOrderedFactory] that creates a new instance of [DvQuantified].
 *
 * @constructor Creates a new instance of [DvQuantifiedFactory]
 */
internal abstract class DvQuantifiedFactory<T : DvQuantified> : DvOrderedFactory<T>() {
    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: T,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                if (attribute.attribute == "magnitude_status") {
                    val textValue = jsonNode.asText()
                    if (textValue != null) {
                        if (OpenEhrTerminology.getMagnitudeStatusCodes().contains(textValue.uppercase()))
                            rmObject.magnitudeStatus = textValue
                        else
                            throw ConversionException("Invalid MAGNITUDE_STATUS: $textValue", webTemplatePath.toString())
                    }
                    true
                } else {
                    false
                }
}
