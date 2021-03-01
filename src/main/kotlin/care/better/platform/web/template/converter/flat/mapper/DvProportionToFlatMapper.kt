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
import org.openehr.rm.datatypes.DvProportion

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToFlatMapper] that maps [DvProportion] to FLAT format.
 */
internal object DvProportionToFlatMapper : DvQuantifiedToFlatMapper<DvProportion>() {

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvProportion,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        flatConversionContext["$webTemplatePath|numerator"] = rmObject.numerator
        flatConversionContext["$webTemplatePath|denominator"] = rmObject.denominator
        flatConversionContext["$webTemplatePath|type"] = rmObject.type
        flatConversionContext[webTemplatePath] = rmObject.numerator / rmObject.denominator
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvProportion,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        if (2 == rmObject.type) {
            formattedFlatConversionContext[webTemplatePath] = "${valueConverter.formatDouble(rmObject.numerator.toDouble())}%"
        } else {
            formattedFlatConversionContext[webTemplatePath] = "${valueConverter.formatDouble(rmObject.numerator.toDouble())}/${valueConverter.formatDouble(rmObject.denominator.toDouble())}"
            formattedFlatConversionContext["$webTemplatePath|numerator"] = valueConverter.formatDouble(rmObject.numerator.toDouble())
            formattedFlatConversionContext["$webTemplatePath|denominator"] = valueConverter.formatDouble(rmObject.denominator.toDouble())
            formattedFlatConversionContext["$webTemplatePath|type"] = rmObject.type.toString()
        }
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
    }
}
