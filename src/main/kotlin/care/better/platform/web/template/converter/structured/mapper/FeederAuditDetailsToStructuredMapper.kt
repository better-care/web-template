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
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.common.FeederAuditDetails
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartyRelated

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [FeederAuditDetails] to STRUCTURED format.
 */
internal object FeederAuditDetailsToStructuredMapper : RmObjectToStructuredMapper<FeederAuditDetails> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: FeederAuditDetails): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.time?.also { time ->
                this.replace("|time", DvDateTimeToStructuredMapper.map(webTemplateNode, valueConverter, time))
            }
            rmObject.subject?.also {
                if (it is PartyIdentified) {
                    val attributeName = if (it is PartyRelated) "subject_related" else "subject"
                    this.putSingletonAsArray(attributeName) { PartyIdentifiedToStructuredMapper.map(webTemplateNode, valueConverter, it) }
                }
            }
            rmObject.provider?.also {
                val attributeName = if (it is PartyRelated) "provider_related" else "provider"
                this.putSingletonAsArray(attributeName) { PartyIdentifiedToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.location?.also {
                this.putSingletonAsArray("location") { PartyIdentifiedToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }

            this.putIfNotNull("|system_id", rmObject.systemId)
            this.putIfNotNull("|version_id", rmObject.versionId)
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: FeederAuditDetails): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.time?.also { time ->
                this.replace("|time", DvDateTimeToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, time))
            }
            rmObject.subject?.also {
                if (it is PartyIdentified) {
                    val attributeName = if (it is PartyRelated) "subject_related" else "subject"
                    this.putSingletonAsArray(attributeName) { PartyIdentifiedToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
                }
            }
            rmObject.provider?.also {
                val attributeName = if (it is PartyRelated) "provider_related" else "provider"
                this.putSingletonAsArray(attributeName) { PartyIdentifiedToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.location?.also {
                this.putSingletonAsArray("location") { PartyIdentifiedToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }

            this.putIfNotNull("|system_id", rmObject.systemId)
            this.putIfNotNull("|version_id", rmObject.versionId)
            this
        }
}
