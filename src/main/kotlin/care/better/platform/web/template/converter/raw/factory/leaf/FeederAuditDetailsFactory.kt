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
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.rm.common.FeederAuditDetails

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [FeederAuditDetails].
 */
internal object FeederAuditDetailsFactory : RmObjectLeafNodeFactory<FeederAuditDetails>() {

    override fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): FeederAuditDetails =
        throw ConversionException("${amNode.rmType} can not be created from simple value", webTemplatePath.toString())

    override fun createInstance(attributes: Set<AttributeDto>): FeederAuditDetails = FeederAuditDetails()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: FeederAuditDetails,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        when (attribute.attribute) {
            "system_id" -> {
                rmObject.systemId = jsonNode.asText()
                true
            }
            "version_id" -> {
                rmObject.versionId = jsonNode.asText()
                true
            }
            "time" -> {
                rmObject.time = DvDateTimeFactory.create(conversionContext, amNode, jsonNode, WebTemplatePath(attribute.originalAttribute, webTemplatePath))
                true
            }
            "location" -> {
                rmObject.location = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            "provider" -> {
                rmObject.provider = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            "provider_related" -> {
                rmObject.provider = jsonNode.mapNotNull {
                    PartyRelatedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            "subject" -> {
                rmObject.subject = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            "subject_related" -> {
                rmObject.subject = jsonNode.mapNotNull {
                    PartyRelatedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            "subject_self" -> {
                rmObject.subject = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }
            else -> false
        }
}
