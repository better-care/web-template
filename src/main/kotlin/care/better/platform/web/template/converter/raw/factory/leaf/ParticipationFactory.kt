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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PARTICIPATION_MODE_GROUP_NAME
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import care.better.platform.web.template.converter.raw.extensions.createGenericId
import care.better.platform.web.template.converter.raw.extensions.createPartyRef
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.PartyRef
import org.openehr.rm.common.Participation
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvIdentifier
import org.openehr.rm.datatypes.DvInterval
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [Participation].
 */
internal object ParticipationFactory : RmObjectLeafNodeFactory<Participation>() {

    override fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): Participation =
        throw ConversionException("${amNode.rmType} can not be created from simple value", webTemplatePath.toString())

    override fun createInstance(attributes: Set<AttributeDto>): Participation = Participation()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: Participation,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean {
        return when {
            attribute.attribute == "function" -> {
                rmObject.function = DvText(jsonNode.asText())
                true
            }
            attribute.attribute == "name" -> {
                getOrCreatePerformer(rmObject).name = jsonNode.asText()
                true
            }
            attribute.attribute == "id" -> {
                handleIdAttribute(getOrCreatePerformer(rmObject), jsonNode, conversionContext)
                true
            }
            attribute.attribute == "id_scheme" -> {
                handleIdSchemeAttribute(getOrCreatePerformer(rmObject), jsonNode, conversionContext)
                true
            }
            attribute.attribute == "id_namespace" || attribute.attribute == "namespace" -> {
                handleIdNamespaceAttribute(getOrCreatePerformer(rmObject), jsonNode, conversionContext)
                true
            }
            attribute.attribute == "mode" -> {
                rmObject.mode = DvCodedText.createFromOpenEhrTerminology(PARTICIPATION_MODE_GROUP_NAME, jsonNode.asText())
                true
            }
            attribute.attribute.startsWith("identifiers_assigner") -> {
                mapToIdentifier(rmObject, jsonNode) { dvIdentifier, value -> dvIdentifier.assigner = value }
                true
            }
            attribute.attribute.startsWith("identifiers_issuer") -> {
                mapToIdentifier(rmObject, jsonNode) { dvIdentifier, value -> dvIdentifier.issuer = value }
                true
            }
            attribute.attribute.startsWith("identifiers_type") -> {
                mapToIdentifier(rmObject, jsonNode) { dvIdentifier, value -> dvIdentifier.type = value }
                true
            }
            attribute.attribute.startsWith("identifiers_id") -> {
                mapToIdentifier(rmObject, jsonNode) { dvIdentifier, value -> dvIdentifier.id = value }
                true
            }
            else -> false
        }
    }

    override fun afterPropertiesSet(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: Participation) {
        if (rmObject.mode == null) {
            rmObject.mode = DvCodedText.createWithOpenEHRTerminology("193", "not specified")
        }
        if (rmObject.function == null) {
            rmObject.function = DvText("unknown")
        }
    }

    /**
     * Sets identifiers attribute to [PartyIdentified] [List] of [DvInterval] from [JsonNode]
     * "identifiers_assigner", "identifiers_issuer", "identifiers_type" or "identifiers_id" entry value.
     *
     * @param participation [Participation]
     * @param jsonNode [JsonNode]
     * @param dvIdentifierSetter Consumer that sets value to the [DvIdentifier]
     */
    private fun mapToIdentifier(participation: Participation, jsonNode: JsonNode, dvIdentifierSetter: (DvIdentifier, String) -> Unit) {
        val dvIdentifiers = getOrCreatePerformer(participation).identifiers
        if (jsonNode.isArray) {
            jsonNode.forEachIndexed { index, node ->
                val dvIdentifier = if (index > dvIdentifiers.size - 1) DvIdentifier().apply { dvIdentifiers.add(this) } else dvIdentifiers[index]
                dvIdentifierSetter.invoke(dvIdentifier, node.asText())
            }
        } else {
            val dvIdentifier = if (dvIdentifiers.isEmpty()) DvIdentifier().apply { dvIdentifiers.add(this) } else dvIdentifiers.first()
            dvIdentifierSetter.invoke(dvIdentifier, jsonNode.asText())
        }
    }

    /**
     * Retrieves or creates performer from [Participation].
     *
     * @param rmObject [Participation]
     * @return Performer as [PartyIdentified]
     */
    private fun getOrCreatePerformer(rmObject: Participation): PartyIdentified =
        if (rmObject.performer == null)
            PartyIdentified().also { rmObject.performer = it }
        else
            rmObject.performer as PartyIdentified

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
