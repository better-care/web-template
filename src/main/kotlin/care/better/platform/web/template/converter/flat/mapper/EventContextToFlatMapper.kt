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

package care.better.platform.web.template.converter.flat.mapper

import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.composition.EventContext

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [EventContext] to FLAT format.
 */
internal object EventContextToFlatMapper : RmObjectToFlatMapper<EventContext> {

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: EventContext,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.participations.forEachIndexed { index, participation ->
            ParticipationToFlatMapper.map(webTemplateNode, valueConverter, participation, "$webTemplatePath/_participation:$index", flatConversionContext)
        }
        rmObject.healthCareFacility?.also {
            PartyIdentifiedToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_health_care_facility", flatConversionContext)
        }
        rmObject.endTime?.also {
            DvDateTimeToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_end_time", flatConversionContext)
        }
        flatConversionContext["$webTemplatePath/_location"] = rmObject.location
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: EventContext,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.participations.forEachIndexed { index, participation ->
            ParticipationToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                participation,
                "$webTemplatePath/_participation:$index",
                formattedFlatConversionContext)
        }
        rmObject.healthCareFacility?.also {
            PartyIdentifiedToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/_health_care_facility",
                formattedFlatConversionContext)
        }
        rmObject.endTime?.also {
            DvDateTimeToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_end_time", formattedFlatConversionContext)
        }
        formattedFlatConversionContext["$webTemplatePath/_location"] = rmObject.location
    }
}
