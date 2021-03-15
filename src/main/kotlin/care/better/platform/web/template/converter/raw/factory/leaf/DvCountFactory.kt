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

import care.better.platform.template.AmNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvCount

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvCount].
 */
internal object DvCountFactory : DvQuantifiedFactory<DvCount>() {

    override fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvCount, webTemplateInput: WebTemplateInput) {
        if (webTemplateInput.fixed) {
            if (webTemplateInput.list.isNotEmpty()) {
                rmObject.magnitude = webTemplateInput.list[0].value.toLong()
            } else if (webTemplateInput.validation?.range != null) {
                val min = webTemplateInput.validation?.range?.getMinimal()
                if (min is Number) {
                    rmObject.magnitude = min.toLong()
                }
            }
        }
    }

    override fun createInstance(attributes: Set<AttributeDto>): DvCount = DvCount()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvCount,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                if (attribute.attribute.isBlank() || attribute.attribute == "magnitude" || attribute.attribute == "value") {
                    if (jsonNode.isNumber) {
                        rmObject.magnitude = jsonNode.numberValue().toLong()
                    } else {
                        val textValue = jsonNode.asText()
                        if (textValue != null) {
                            rmObject.magnitude = textValue.toLong()
                        }
                    }
                    true
                } else {
                    false
                }
}
