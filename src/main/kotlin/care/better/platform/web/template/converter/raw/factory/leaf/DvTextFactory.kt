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
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [DvText].
 */
internal object DvTextFactory : RmObjectLeafNodeFactory<DvText>() {

    override fun removeDependentValues(map: MutableMap<AttributeDto, JsonNode>): Boolean =
        if (map[AttributeDto.forAttribute("code")] != null || map[AttributeDto.forAttribute("terminology")] != null)
            DvCodedTextFactory.getInstance().removeDependentValues(map)
        else
            false

    override fun createInstance(attributes: Set<AttributeDto>): DvText =
        if (attributes.contains(AttributeDto.forAttribute("code")) || attributes.contains(AttributeDto.forAttribute("terminology")))
            DvCodedText()
        else
            DvText()

    override fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvText, webTemplateInput: WebTemplateInput) {
        if (webTemplateInput.list.size == 1) {
            rmObject.value = webTemplateInput.list[0].value
        }
    }

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvText,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean {
        return if (rmObject is DvCodedText) {
            return DvCodedTextFactory.getInstance().handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath)
        } else {
            if (attribute.attribute.isBlank() || attribute.attribute == "value") {
                rmObject.value = jsonNode.asText()
                true
            } else if (attribute.attribute == "_mapping") {
                rmObject.mappings = jsonNode.mapIndexedNotNull { index, node ->
                    TermMappingFactory.create(conversionContext, amNode, node, WebTemplatePath(attribute.originalAttribute, webTemplatePath, index))
                }.toMutableList()
                true
            } else {
                false
            }
        }
    }
}
