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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.RELATIONSHIP
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.common.PartyRelated

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [PartyRelated] to FLAT format.
 */
internal object PartyRelatedToFlatMapper : RmObjectToFlatMapper<PartyRelated> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: PartyRelated,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        PartyIdentifiedToFlatMapper.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
        rmObject.relationship?.also {
            DvCodedTextToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$RELATIONSHIP", flatConversionContext)
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: PartyRelated,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        PartyIdentifiedToFlatMapper.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
        rmObject.relationship?.also {
            DvCodedTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$RELATIONSHIP", formattedFlatConversionContext)
        }
    }
}
