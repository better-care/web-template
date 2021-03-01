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
import org.openehr.rm.datatypes.DvOrdered

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [RmObjectToFlatMapper] that maps [DvOrdered] to FLAT format.
 *
 * @constructor Creates a new instance of [DvOrderedToFlatMapper]
 */
internal abstract class DvOrderedToFlatMapper<T : DvOrdered> : RmObjectToFlatMapper<T> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.normalRange?.also {
            NormalRangeToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_normal_range", flatConversionContext)
        }

        rmObject.otherReferenceRanges.forEachIndexed { index, referenceRange ->
            ReferenceRangeToFlatMapper.map(
                webTemplateNode,
                valueConverter,
                referenceRange,
                "$webTemplatePath/_other_reference_ranges:$index",
                flatConversionContext)
        }

        flatConversionContext["$webTemplatePath|normal_status"] = rmObject.normalStatus?.codeString
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.normalRange?.also {
            NormalRangeToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_normal_range", formattedFlatConversionContext)
        }

        rmObject.otherReferenceRanges.forEachIndexed { index, referenceRange ->
            ReferenceRangeToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                referenceRange,
                "$webTemplatePath/_other_reference_ranges:$index",
                formattedFlatConversionContext)
        }

        formattedFlatConversionContext["$webTemplatePath|normal_status"] = rmObject.normalStatus?.codeString
    }
}
