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

package care.better.platform.web.template.converter.structured.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FUNCTION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.IDENTIFIERS_ASSIGNER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.IDENTIFIERS_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.IDENTIFIERS_ISSUER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.IDENTIFIERS_TYPE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ID_NAMESPACE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ID_SCHEME
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MODE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NAME
import care.better.platform.web.template.converter.constant.WebTemplateConstants.RELATIONSHIP
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.ObjectRef
import org.openehr.rm.common.Participation
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartyRelated

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [Participation] to STRUCTURED format.
 */
internal object ParticipationToStructuredMapper : RmObjectToStructuredMapper<Participation> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Participation): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapInternal(rmObject, this)
            (rmObject.performer as? PartyRelated)?.relationship?.also {
                putSingletonAsArray(RELATIONSHIP) { DvCodedTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Participation): JsonNode =
        map(webTemplateNode, valueConverter, rmObject)

    private fun mapInternal(
            rmObject: Participation,
            objectNode: ObjectNode) {
        objectNode.putIfNotNull("|$FUNCTION", rmObject.function?.value)
        objectNode.putIfNotNull("|$MODE", rmObject.mode?.value)

        val performer = rmObject.performer
        if (performer is PartyIdentified) {
            objectNode.putIfNotNull("|$NAME", performer.name)
            performer.externalRef?.also { mapPartyRef(it, objectNode) }

            objectNode.putCollectionAsArray(
                "|$IDENTIFIERS_ASSIGNER",
                performer.identifiers.filter { !it.assigner.isNullOrBlank() }) {
                it.assigner?.let { assigner -> TextNode.valueOf(assigner) }
            }
            objectNode.putCollectionAsArray(
                "|$IDENTIFIERS_ISSUER",
                performer.identifiers.filter { !it.issuer.isNullOrBlank() }) { it.issuer?.let { issuer -> TextNode.valueOf(issuer) } }
            objectNode.putCollectionAsArray(
                "|$IDENTIFIERS_TYPE",
                performer.identifiers.filter { !it.type.isNullOrBlank() }) { it.type?.let { type -> TextNode.valueOf(type) } }
            objectNode.putCollectionAsArray(
                "|$IDENTIFIERS_ID",
                performer.identifiers.filter { !it.id.isNullOrBlank() }) { it.id?.let { id -> TextNode.valueOf(id) } }
        }
    }

    private fun mapPartyRef(objectRef: ObjectRef, objectNode: ObjectNode) {
        objectNode.putIfNotNull("|$ID", objectRef.id?.value)
        objectNode.putIfNotNull("|$ID_NAMESPACE", objectRef.namespace)
        if (objectRef.id is GenericId) {
            objectNode.putIfNotNull("|$ID_SCHEME", (objectRef.id as GenericId).scheme)
        }
    }
}
