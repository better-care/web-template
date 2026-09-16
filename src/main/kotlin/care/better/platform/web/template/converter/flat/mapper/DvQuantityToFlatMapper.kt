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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MAGNITUDE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UNIT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UNIT_DISPLAY_NAME
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UNIT_SYSTEM
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.datatypes.DvQuantity

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToFlatMapper] that maps [DvQuantity] to FLAT format.
 */
internal object DvQuantityToFlatMapper : DvQuantifiedToFlatMapper<DvQuantity>() {

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvQuantity,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        flatConversionContext["$webTemplatePath|$MAGNITUDE"] = rmObject.magnitude
        flatConversionContext["$webTemplatePath|$UNIT"] = rmObject.units
        flatConversionContext["$webTemplatePath|$UNIT_SYSTEM"] = rmObject.unitsSystem
        flatConversionContext["$webTemplatePath|$UNIT_DISPLAY_NAME"] = rmObject.unitsDisplayName
        flatConversionContext["$webTemplatePath|$ACCURACY"] = rmObject.accuracy
        flatConversionContext["$webTemplatePath|$ACCURACY_IS_PERCENT"] = rmObject.accuracyIsPercent

        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvQuantity,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        formattedFlatConversionContext["$webTemplatePath|$MAGNITUDE"] = valueConverter.formatDouble(rmObject.magnitude)
        formattedFlatConversionContext["$webTemplatePath|$UNIT"] = rmObject.units
        formattedFlatConversionContext["$webTemplatePath|$UNIT_SYSTEM"] = rmObject.unitsSystem
        formattedFlatConversionContext["$webTemplatePath|$UNIT_DISPLAY_NAME"] = rmObject.unitsDisplayName
        formattedFlatConversionContext["$webTemplatePath|$ACCURACY"] = rmObject.accuracy?.toString()
        formattedFlatConversionContext["$webTemplatePath|$ACCURACY_IS_PERCENT"] = rmObject.accuracyIsPercent?.toString()

        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
    }
}
