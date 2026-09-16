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

package care.better.platform.web.template.converter.flat.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MATH_FUNCTION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SAMPLE_COUNT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.WIDTH
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.datastructures.IntervalEvent

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [IntervalEvent] to FLAT format.
 */
internal object IntervalEventToFlatMapper : RmObjectToFlatMapper<IntervalEvent> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: IntervalEvent,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.width?.also {
            DvDurationToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$WIDTH", flatConversionContext)
        }
        rmObject.mathFunction?.also {
            DvCodedTextToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$MATH_FUNCTION", flatConversionContext)
        }
        flatConversionContext["$webTemplatePath|$SAMPLE_COUNT"] = rmObject.sampleCount
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: IntervalEvent,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.width?.also {
            DvDurationToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$WIDTH", formattedFlatConversionContext)
        }
        rmObject.mathFunction?.also {
            DvCodedTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$MATH_FUNCTION", formattedFlatConversionContext)
        }
        formattedFlatConversionContext["$webTemplatePath|$SAMPLE_COUNT"] = rmObject.sampleCount?.toString()
    }
}
