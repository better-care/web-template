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
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [DvCodedText] to FLAT format.
 */
internal object DvCodedTextToFlatMapper : RmObjectToFlatMapper<DvCodedText> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvCodedText,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.mappings.forEachIndexed { index, termMapping ->
            TermMappingToFlatMapper.map(webTemplateNode, valueConverter, termMapping, "$webTemplatePath/_mapping:$index", flatConversionContext)
        }

        flatConversionContext["$webTemplatePath|code"] = rmObject.definingCode?.codeString
        flatConversionContext["$webTemplatePath|value"] = rmObject.value
        flatConversionContext["$webTemplatePath|terminology"] = rmObject.definingCode?.terminologyId?.value
        flatConversionContext["$webTemplatePath|preferred_term"] = rmObject.definingCode?.preferredTerm
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvCodedText,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.mappings.forEachIndexed { index, termMapping ->
            TermMappingToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                termMapping,
                "$webTemplatePath/_mapping:$index",
                formattedFlatConversionContext)
        }

        formattedFlatConversionContext["$webTemplatePath|code"] = rmObject.definingCode?.codeString
        formattedFlatConversionContext["$webTemplatePath|value"] = rmObject.value
        formattedFlatConversionContext["$webTemplatePath|terminology"] = rmObject.definingCode?.terminologyId?.value
        formattedFlatConversionContext["$webTemplatePath|preferred_term"] = rmObject.definingCode?.preferredTerm
    }
}
