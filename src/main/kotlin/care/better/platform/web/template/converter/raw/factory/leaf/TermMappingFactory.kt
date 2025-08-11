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
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.rm.datatypes.TermMapping

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [TermMapping].
 *
 * @constructor Creates a new instance of [TermMappingFactory]
 */
internal object TermMappingFactory : RmObjectLeafNodeFactory<TermMapping>() {
    override fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): TermMapping =
        throw ConversionException("${amNode.rmType} can not be created from simple value", webTemplatePath.toString())

    override fun createInstance(attributes: Set<AttributeDto>): TermMapping = TermMapping()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: TermMapping,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        when (attribute.attribute) {
            "match" -> {
                rmObject.match = jsonNode.asText()
                true
            }
            "target" -> {
                rmObject.target = jsonNode.mapNotNull {
                    CodePhraseFactory.create(
                        conversionContext,
                        AmNode(amNode, "CODE_PHRASE"),
                        it,
                        webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            "purpose" -> {
                rmObject.purpose = jsonNode.mapNotNull {
                    DvCodedTextFactory.getInstance().create(
                        conversionContext,
                        AmNode(amNode, "DV_CODED_TEXT"),
                        it,
                        webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            else -> false
        }

    override fun afterPropertiesSet(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: TermMapping) {
        super.afterPropertiesSet(conversionContext, amNode, jsonNode, rmObject)
        if (rmObject.match.isNullOrBlank()) {
            rmObject.match = "?"
        }
    }
}
