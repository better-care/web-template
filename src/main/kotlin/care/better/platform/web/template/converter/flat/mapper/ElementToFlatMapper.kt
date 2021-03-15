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
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.datastructures.Element

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [LocatableToFlatMapper] that maps [Element] to FLAT format.
 */
internal object ElementToFlatMapper : LocatableToFlatMapper<Element>() {

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Element,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)

        val nullFlavour = rmObject.nullFlavour
        val nullReason = rmObject.nullReason
        if (rmObject.value == null) {
            if (nullFlavour != null) {
                DvCodedTextToFlatMapper.map(webTemplateNode, valueConverter, nullFlavour, "$webTemplatePath/_null_flavour", flatConversionContext)
            }
            if (nullReason != null) {
                DvTextToFlatMapper.map(webTemplateNode, valueConverter, nullReason, "$webTemplatePath/_null_reason", flatConversionContext)
            }
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Element,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)

        val nullFlavour = rmObject.nullFlavour
        val nullReason = rmObject.nullReason
        if (rmObject.value == null) {
            if (nullFlavour != null) {
                DvCodedTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, nullFlavour, "$webTemplatePath/_null_flavour", formattedFlatConversionContext)
            }
            if (nullReason != null) {
                DvTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, nullReason, "$webTemplatePath/_null_reason", formattedFlatConversionContext)
            }
        }
    }
}
