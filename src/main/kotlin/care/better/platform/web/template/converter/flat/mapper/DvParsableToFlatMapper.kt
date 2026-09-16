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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CHARSET
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FORMALISM
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.datatypes.DvParsable

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [DvParsable] to FLAT format.
 */
internal object DvParsableToFlatMapper : RmObjectToFlatMapper<DvParsable> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvParsable,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        flatConversionContext["$webTemplatePath|$VALUE"] = rmObject.value
        flatConversionContext["$webTemplatePath|$FORMALISM"] = rmObject.formalism
        rmObject.charset?.also {
            CodePhraseToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_$CHARSET", flatConversionContext)
        }
        rmObject.language?.also {
            CodePhraseToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_$LANGUAGE", flatConversionContext)
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvParsable,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        formattedFlatConversionContext["$webTemplatePath|$VALUE"] = rmObject.value
        formattedFlatConversionContext["$webTemplatePath|$FORMALISM"] = rmObject.formalism
        rmObject.charset?.also {
            CodePhraseToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_$CHARSET", formattedFlatConversionContext)
        }
        rmObject.language?.also {
            CodePhraseToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_$LANGUAGE", formattedFlatConversionContext)
        }
    }
}
