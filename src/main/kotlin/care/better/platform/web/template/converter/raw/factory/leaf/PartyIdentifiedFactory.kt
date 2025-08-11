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
import care.better.platform.web.template.converter.raw.extensions.createGenericId
import care.better.platform.web.template.converter.raw.extensions.createPartyRef
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.PartyRef
import org.openehr.rm.common.PartyIdentified

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [PartyIdentified].
 */
internal object PartyIdentifiedFactory : RmObjectLeafNodeFactory<PartyIdentified>() {

    override fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): PartyIdentified =
        throw ConversionException("${amNode.rmType} can not be created from simple value", webTemplatePath.toString())

    override fun createInstance(attributes: Set<AttributeDto>): PartyIdentified = PartyIdentified()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: PartyIdentified,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean {
        return when (attribute.attribute) {
            "name" -> {
                rmObject.name = jsonNode.asText()
                true
            }
            "id" -> {
                handleIdAttribute(rmObject, jsonNode, conversionContext)
                true
            }
            "_identifier" -> {
                rmObject.identifiers = jsonNode.mapIndexedNotNull { index, node ->
                    DvIdentifierFactory.create(conversionContext, amNode, node, WebTemplatePath(attribute.originalAttribute, webTemplatePath, index))
                }.toMutableList()
                true
            }
            "id_scheme" -> {
                handleIdSchemeAttribute(rmObject, jsonNode, conversionContext)
                true
            }
            "id_namespace", "namespace" -> {
                handleIdNamespaceAttribute(rmObject, jsonNode, conversionContext)
                true
            }
            else -> {
                false
            }
        }
    }

    /**
     * Sets ID to [PartyIdentified] from [JsonNode] "|id" entry value.
     *
     * @param rmObject [PartyIdentified]
     * @param jsonNode [JsonNode]
     * @param conversionContext [ConversionContext]
     */
    private fun handleIdAttribute(rmObject: PartyIdentified, jsonNode: JsonNode, conversionContext: ConversionContext) {
        if (rmObject.externalRef == null) {
            rmObject.externalRef = PartyRef.createPartyRef(jsonNode.asText(), conversionContext.idScheme, conversionContext.idNamespace)
        } else {
            if (rmObject.externalRef?.id == null) {
                rmObject.externalRef?.also {
                    it.id = GenericId.createGenericId(jsonNode.asText(), conversionContext.idScheme)
                }
            } else {
                rmObject.externalRef?.id?.also { it.value = jsonNode.asText() }
            }
        }
    }

    /**
     * Sets id_scheme to [PartyIdentified] from [JsonNode] "|id_scheme" entry value.
     *
     * @param rmObject [PartyIdentified]
     * @param jsonNode [JsonNode]
     * @param conversionContext [ConversionContext]
     */
    private fun handleIdSchemeAttribute(rmObject: PartyIdentified, jsonNode: JsonNode, conversionContext: ConversionContext) {
        if (rmObject.externalRef == null) {
            rmObject.externalRef = PartyRef.createPartyRef(null, jsonNode.asText(), conversionContext.idNamespace)
        } else {
            if (rmObject.externalRef?.id == null) {
                rmObject.externalRef?.also {
                    it.id = GenericId.createGenericId(null, jsonNode.asText())
                }
            } else {
                rmObject.externalRef?.id?.also { (it as GenericId).scheme = jsonNode.asText() }
            }
        }
    }

    /**
     * Sets id_namespace to [PartyIdentified] from [JsonNode] "|id_namespace" entry value.
     *
     * @param rmObject [PartyIdentified]
     * @param jsonNode [JsonNode]
     * @param conversionContext [ConversionContext]
     */
    private fun handleIdNamespaceAttribute(rmObject: PartyIdentified, jsonNode: JsonNode, conversionContext: ConversionContext) {
        if (rmObject.externalRef == null) {
            rmObject.externalRef = PartyRef.createPartyRef(null, conversionContext.idScheme, jsonNode.asText())
        } else {
            rmObject.externalRef?.namespace = jsonNode.asText()
        }
    }
}
