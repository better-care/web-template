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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FEEDER_SYSTEM_AUDIT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FEEDER_SYSTEM_ITEM_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINAL_CONTENT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINAL_CONTENT_MULTIMEDIA
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINATING_SYSTEM_AUDIT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINATING_SYSTEM_ITEM_ID
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.rm.common.FeederAudit
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [FeederAudit].
 */
internal object FeederAuditFactory : RmObjectLeafNodeFactory<FeederAudit>() {

    override fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): FeederAudit =
        throw ConversionException("${amNode.rmType} can not be created from simple value", webTemplatePath.toString())

    override fun createInstance(attributes: Set<AttributeDto>): FeederAudit = FeederAudit()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: FeederAudit,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        when {
            attribute.attribute.startsWith(ORIGINAL_CONTENT) -> {
                handleOriginalContent(attribute.attribute, conversionContext, amNode, jsonNode, rmObject, webTemplatePath)
                true
            }
            attribute.attribute == ORIGINATING_SYSTEM_ITEM_ID -> {
                rmObject.originatingSystemItemIds = jsonNode.mapIndexedNotNull { index, node ->
                    DvIdentifierFactory.create(conversionContext, amNode, node, WebTemplatePath(attribute.originalAttribute, webTemplatePath, index))
                }.toMutableList()
                true
            }
            attribute.attribute == FEEDER_SYSTEM_ITEM_ID -> {
                rmObject.feederSystemItemIds = jsonNode.mapIndexedNotNull { index, node ->
                    DvIdentifierFactory.create(conversionContext, amNode, node, WebTemplatePath(attribute.originalAttribute, webTemplatePath, index))
                }.toMutableList()
                true
            }
            attribute.attribute == ORIGINATING_SYSTEM_AUDIT -> {
                rmObject.originatingSystemAudit = jsonNode.mapNotNull {
                    FeederAuditDetailsFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            attribute.attribute == FEEDER_SYSTEM_AUDIT -> {
                rmObject.feederSystemAudit = jsonNode.mapNotNull {
                    FeederAuditDetailsFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            else -> false
        }

    /**
     * Sets original content to [FeederAudit] from [JsonNode] "original_content" or "original_content_multimedia" entry value.
     *
     * @param jsonNode [JsonNode]
     * @param rmObject [DvCodedText]
     */
    private fun handleOriginalContent(
            attributeName: String,
            conversionContext: ConversionContext,
            amNode: AmNode,
            jsonNode: JsonNode,
            rmObject: FeederAudit,
            webTemplatePath: WebTemplatePath) {
        if (attributeName == ORIGINAL_CONTENT_MULTIMEDIA) {
            rmObject.originalContent = jsonNode.mapNotNull {
                DvMultimediaFactory.create(conversionContext, amNode, it, webTemplatePath + attributeName)
            }.firstOrNull()
        } else {
            rmObject.originalContent = jsonNode.mapNotNull {
                DvParsableFactory.create(conversionContext, amNode, it, webTemplatePath + attributeName)
            }.firstOrNull()
        }
    }
}
