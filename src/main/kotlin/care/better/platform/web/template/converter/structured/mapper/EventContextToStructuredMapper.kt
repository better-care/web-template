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
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.TextNode
import com.marand.thinkehr.web.build.WebTemplateNode
import org.openehr.rm.composition.EventContext

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [EventContext] to STRUCTURED format.
 */
internal object EventContextToStructuredMapper : RmObjectToStructuredMapper<EventContext> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: EventContext): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putCollectionAsArray("_participation", rmObject.participations) { ParticipationToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            rmObject.healthCareFacility?.also {
                this.putSingletonAsArray("_health_care_facility") { PartyIdentifiedToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.endTime?.also {
                this.putSingletonAsArray("_end_time") { DvDateTimeToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.location?.also { location -> this.putSingletonAsArray("_location") { TextNode.valueOf(location) } }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: EventContext): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putCollectionAsArray("_participation", rmObject.participations) {
                ParticipationToStructuredMapper.mapFormatted(
                    webTemplateNode,
                    valueConverter,
                    it)
            }
            rmObject.healthCareFacility?.also {
                this.putSingletonAsArray("_health_care_facility") { PartyIdentifiedToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.endTime?.also {
                this.putSingletonAsArray("_end_time") { DvDateTimeToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.location?.also { location -> this.putSingletonAsArray("_location") { TextNode.valueOf(location) } }
            this
        }
}
