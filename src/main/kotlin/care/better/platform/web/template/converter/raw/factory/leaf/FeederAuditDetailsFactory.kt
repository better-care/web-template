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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOCATION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PROVIDER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PROVIDER_RELATED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SUBJECT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SUBJECT_RELATED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SUBJECT_SELF
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SYSTEM_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TIME
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VERSION_ID
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
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
            SYSTEM_ID -> {
                rmObject.systemId = jsonNode.asText()
                true
            }

            VERSION_ID -> {
                rmObject.versionId = jsonNode.asText()
                true
            }

            TIME -> {
                rmObject.time = DvDateTimeFactory.create(
                        conversionContext,
                        amNode,
                        when (jsonNode) {
                            is ArrayNode -> jsonNode.firstOrNull() ?: NullNode.instance
                            else -> jsonNode
                        },
                        WebTemplatePath(attribute.originalAttribute, webTemplatePath))
                true
            }

            LOCATION -> {
                rmObject.location = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }

            PROVIDER -> {
                rmObject.provider = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }

            PROVIDER_RELATED -> {
                rmObject.provider = jsonNode.mapNotNull {
                    PartyRelatedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }

            SUBJECT -> {
                rmObject.subject = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }

            SUBJECT_RELATED -> {
                rmObject.subject = jsonNode.mapNotNull {
                    PartyRelatedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }

            SUBJECT_SELF -> {
                rmObject.subject = jsonNode.mapNotNull {
                    PartyIdentifiedFactory.create(conversionContext, amNode, it, webTemplatePath + attribute.originalAttribute)
                }.firstOrNull()
                true
            }

            else -> false
        }
}
