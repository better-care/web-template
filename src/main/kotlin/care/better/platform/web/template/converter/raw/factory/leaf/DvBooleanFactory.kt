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
import com.fasterxml.jackson.databind.node.BooleanNode
import org.openehr.rm.datatypes.DvBoolean

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [DvBoolean].
 */
internal object DvBooleanFactory : RmObjectLeafNodeFactory<DvBoolean>() {

    override fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvBoolean, webTemplateInput: WebTemplateInput) {
        if (webTemplateInput.fixed) {
            rmObject.value = webTemplateInput.list[0].value.toBoolean()
        }
    }

    override fun createInstance(attributes: Set<AttributeDto>): DvBoolean = DvBoolean()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvBoolean,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        if (attribute.attribute.isBlank() || attribute.attribute == "value") {
            if (jsonNode is BooleanNode) {
                rmObject.value = jsonNode.booleanValue()
            } else {
                val textValue = jsonNode.asText()
                rmObject.value = textValue.isNotBlank() && !"false".equals(textValue, ignoreCase = true) && !"0".equals(textValue, ignoreCase = true)
            }
            true
        } else {
            false
        }
}
