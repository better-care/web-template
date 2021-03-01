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

import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.ObjectRef
import org.openehr.rm.common.Participation
import org.openehr.rm.common.PartyIdentified

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
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Participation): JsonNode =
        map(webTemplateNode, valueConverter, rmObject)

    private fun mapInternal(
            rmObject: Participation,
            objectNode: ObjectNode) {
        objectNode.putIfNotNull("|function", rmObject.function?.value)
        objectNode.putIfNotNull("|mode", rmObject.mode?.value)

        val performer = rmObject.performer
        if (performer is PartyIdentified) {
            objectNode.putIfNotNull("|name", performer.name)
            performer.externalRef?.also { mapPartyRef(it, objectNode) }

            objectNode.putCollectionAsArray(
                "|identifiers_assigner",
                performer.identifiers.filter { !it.assigner.isNullOrBlank() }) {
                it.assigner?.let { assigner -> TextNode.valueOf(assigner) }
            }
            objectNode.putCollectionAsArray(
                "|identifiers_issuer",
                performer.identifiers.filter { !it.issuer.isNullOrBlank() }) { it.issuer?.let { issuer -> TextNode.valueOf(issuer) } }
            objectNode.putCollectionAsArray(
                "|identifiers_type",
                performer.identifiers.filter { !it.type.isNullOrBlank() }) { it.type?.let { type -> TextNode.valueOf(type) } }
            objectNode.putCollectionAsArray(
                "|identifiers_id",
                performer.identifiers.filter { !it.id.isNullOrBlank() }) { it.id?.let { id -> TextNode.valueOf(id) } }
        }
    }

    private fun mapPartyRef(objectRef: ObjectRef, objectNode: ObjectNode) {
        objectNode.putIfNotNull("|id", objectRef.id?.value)
        objectNode.putIfNotNull("|id_namespace", objectRef.namespace)
        if (objectRef.id is GenericId) {
            objectNode.putIfNotNull("|id_scheme", (objectRef.id as GenericId).scheme)
        }
    }
}
