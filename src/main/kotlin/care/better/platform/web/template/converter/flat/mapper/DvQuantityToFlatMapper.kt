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
        flatConversionContext["$webTemplatePath|magnitude"] = rmObject.magnitude
        flatConversionContext["$webTemplatePath|unit"] = rmObject.units
        flatConversionContext["$webTemplatePath|unit_system"] = rmObject.unitsSystem
        flatConversionContext["$webTemplatePath|unit_display_name"] = rmObject.unitsDisplayName

        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvQuantity,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        formattedFlatConversionContext["$webTemplatePath|magnitude"] = valueConverter.formatDouble(rmObject.magnitude)
        formattedFlatConversionContext["$webTemplatePath|unit"] = rmObject.units
        formattedFlatConversionContext["$webTemplatePath|unit_system"] = rmObject.unitsSystem
        formattedFlatConversionContext["$webTemplatePath|unit_display_name"] = rmObject.unitsDisplayName

        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
    }
}
