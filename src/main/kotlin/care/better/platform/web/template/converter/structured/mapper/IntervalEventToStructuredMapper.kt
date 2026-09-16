/* Copyright 2026 Better Ltd (www.better.care)
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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MATH_FUNCTION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SAMPLE_COUNT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.WIDTH
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datastructures.IntervalEvent

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [IntervalEvent] to STRUCTURED format.
 */
internal object IntervalEventToStructuredMapper : RmObjectToStructuredMapper<IntervalEvent> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: IntervalEvent): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.width?.also {
                this.putSingletonAsArray(WIDTH) { DvDurationToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.mathFunction?.also {
                this.putSingletonAsArray(MATH_FUNCTION) { DvCodedTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.sampleCount?.also { this.putIfNotNull("|$SAMPLE_COUNT", it) }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: IntervalEvent): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.width?.also {
                this.putSingletonAsArray(WIDTH) { DvDurationToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.mathFunction?.also {
                this.putSingletonAsArray(MATH_FUNCTION) { DvCodedTextToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.sampleCount?.also { this.putIfNotNull("|$SAMPLE_COUNT", it.toString()) }
            this
        }
}