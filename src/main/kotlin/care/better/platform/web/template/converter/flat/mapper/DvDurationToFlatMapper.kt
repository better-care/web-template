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

import care.better.platform.utils.JodaConversionUtils
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateDurationField
import org.joda.time.Period
import org.openehr.rm.datatypes.DvDuration

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToFlatMapper] that maps [DvDuration] to FLAT format.
 */
internal object DvDurationToFlatMapper : DvQuantifiedToFlatMapper<DvDuration>() {

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvDuration,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        flatConversionContext[webTemplatePath] = JodaConversionUtils.toPeriod(rmObject)
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvDuration,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        val period: Period = JodaConversionUtils.toPeriod(rmObject)

        WebTemplateDurationField.values().forEach {
            val key = it.name.lowercase()
            val value = period[it.durationFieldType]
            if (value > 0) {
                formattedFlatConversionContext["$webTemplatePath|$key"] = value.toString()
            }
        }
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
    }
}
