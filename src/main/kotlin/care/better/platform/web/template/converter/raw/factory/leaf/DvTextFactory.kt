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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CODE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ENCODING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FORMATTING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MAPPING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.OTHER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TERMINOLOGY
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
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
        if (map[AttributeDto.forAttribute(CODE)] != null || map[AttributeDto.forAttribute(TERMINOLOGY)] != null)
            DvCodedTextFactory.getInstance().removeDependentValues(map)
        else
            false

    override fun createInstance(attributes: Set<AttributeDto>): DvText =
        if (attributes.contains(AttributeDto.forAttribute(CODE)) || attributes.contains(AttributeDto.forAttribute(TERMINOLOGY)))
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
            DvCodedTextFactory.getInstance().handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath)
        } else {
            when {
                attribute.attribute.isBlank() || attribute.attribute == VALUE || attribute.attribute == OTHER -> {
                    rmObject.value = jsonNode.asText()
                    true
                }
                attribute.attribute == "_$MAPPING" -> {
                    rmObject.mappings = jsonNode.mapIndexedNotNull { index, node ->
                        TermMappingFactory.create(conversionContext, amNode, node, WebTemplatePath(attribute.originalAttribute, webTemplatePath, index))
                    }.toMutableList()
                    true
                }
                attribute.attribute == FORMATTING -> {
                    rmObject.formatting = jsonNode.asText()
                    true
                }
                attribute.attribute == "_$LANGUAGE" -> {
                    jsonNode.forEach { node -> CodePhraseFactory.create(conversionContext, amNode, node, webTemplatePath)?.also { rmObject.language = it } }
                    true
                }
                attribute.attribute == "_$ENCODING" -> {
                    jsonNode.forEach { node -> CodePhraseFactory.create(conversionContext, amNode, node, webTemplatePath)?.also { rmObject.encoding = it } }
                    true
                }
                else -> false
            }
        }
    }
}
