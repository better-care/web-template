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

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ACCURACY
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ACCURACY_IS_PERCENT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.DENOMINATOR
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NUMERATOR
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PRECISION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TYPE
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
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
        flatConversionContext["$webTemplatePath|$NUMERATOR"] = rmObject.numerator
        flatConversionContext["$webTemplatePath|$DENOMINATOR"] = rmObject.denominator
        flatConversionContext["$webTemplatePath|$TYPE"] = rmObject.type
        flatConversionContext[webTemplatePath] = rmObject.numerator / rmObject.denominator
        flatConversionContext["$webTemplatePath|$PRECISION"] = rmObject.precision
        flatConversionContext["$webTemplatePath|$ACCURACY"] = rmObject.accuracy
        flatConversionContext["$webTemplatePath|$ACCURACY_IS_PERCENT"] = rmObject.accuracyIsPercent
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvProportion,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        if (2 == rmObject.type) {
            formattedFlatConversionContext[webTemplatePath] = "${valueConverter.formatFloat(rmObject.numerator)}%"
        } else {
            formattedFlatConversionContext[webTemplatePath] = "${valueConverter.formatFloat(rmObject.numerator)}/${valueConverter.formatFloat(rmObject.denominator)}"
        }
        formattedFlatConversionContext["$webTemplatePath|$NUMERATOR"] = valueConverter.formatFloat(rmObject.numerator)
        formattedFlatConversionContext["$webTemplatePath|$DENOMINATOR"] = valueConverter.formatFloat(rmObject.denominator)
        formattedFlatConversionContext["$webTemplatePath|$TYPE"] = rmObject.type.toString()
        formattedFlatConversionContext["$webTemplatePath|$PRECISION"] = rmObject.precision?.toString()
        formattedFlatConversionContext["$webTemplatePath|$ACCURACY"] = rmObject.accuracy?.let { valueConverter.formatFloat(it) }
        formattedFlatConversionContext["$webTemplatePath|$ACCURACY_IS_PERCENT"] = rmObject.accuracyIsPercent?.toString()
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
    }
}
