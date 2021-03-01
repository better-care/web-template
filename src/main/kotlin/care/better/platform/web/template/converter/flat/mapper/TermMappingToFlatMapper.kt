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
import org.openehr.rm.datatypes.TermMapping

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [TermMapping] to FLAT format.
 */
internal object TermMappingToFlatMapper : RmObjectToFlatMapper<TermMapping> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: TermMapping,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        flatConversionContext["$webTemplatePath|match"] = rmObject.match
        rmObject.target?.also { CodePhraseToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/target", flatConversionContext) }
        rmObject.purpose?.also { DvCodedTextToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/purpose", flatConversionContext) }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: TermMapping,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        formattedFlatConversionContext["$webTemplatePath|match"] = rmObject.match
        rmObject.target?.also {
            CodePhraseToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/target",
                formattedFlatConversionContext)
        }
        rmObject.purpose?.also {
            DvCodedTextToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/purpose",
                formattedFlatConversionContext)
        }
    }
}
