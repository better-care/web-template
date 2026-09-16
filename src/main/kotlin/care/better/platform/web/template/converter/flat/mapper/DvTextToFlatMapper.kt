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

import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ENCODING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FORMATTING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MAPPING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.OTHER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [DvText] to FLAT format.
 */
internal object DvTextToFlatMapper : RmObjectToFlatMapper<DvText> {
    private val dvCodedTextRmType = RmUtils.getRmTypeName(DvCodedText::class.java)

    override fun map(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: DvText,
        webTemplatePath: String,
        flatConversionContext: FlatMappingContext
    ) {
        rmObject.mappings.forEachIndexed { index, termMapping ->
            TermMappingToFlatMapper.map(webTemplateNode, valueConverter, termMapping, "$webTemplatePath/_$MAPPING:$index", flatConversionContext)
        }

        if (webTemplateNode.rmType == dvCodedTextRmType) {
            flatConversionContext["$webTemplatePath|$OTHER"] = rmObject.value
        } else {
            flatConversionContext["$webTemplatePath${if (rmObject.mappings.isEmpty() && rmObject.formatting.isNullOrBlank()) "" else "|$VALUE"}"] = rmObject.value
        }

        rmObject.formatting?.also {
            flatConversionContext["$webTemplatePath|$FORMATTING"] = it
        }

        rmObject.language?.also {
            CodePhraseToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_$LANGUAGE", flatConversionContext)
        }

        rmObject.encoding?.also {
            CodePhraseToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_$ENCODING", flatConversionContext)
        }
    }

    override fun mapFormatted(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: DvText,
        webTemplatePath: String,
        formattedFlatConversionContext: FormattedFlatMappingContext
    ) {
        rmObject.mappings.forEachIndexed { index, termMapping ->
            TermMappingToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                termMapping,
                "$webTemplatePath/_$MAPPING:$index",
                formattedFlatConversionContext)
        }

        if (webTemplateNode.rmType == dvCodedTextRmType) {
            formattedFlatConversionContext["$webTemplatePath|$OTHER"] = rmObject.value
        } else {
            formattedFlatConversionContext["$webTemplatePath${if (rmObject.mappings.isEmpty() && rmObject.formatting.isNullOrBlank()) "" else "|$VALUE"}"] = rmObject.value
        }

        rmObject.formatting?.also {
            formattedFlatConversionContext["$webTemplatePath|$FORMATTING"] = it
        }

        rmObject.language?.also {
            CodePhraseToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_$LANGUAGE", formattedFlatConversionContext)
        }

        rmObject.encoding?.also {
            CodePhraseToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_$ENCODING", formattedFlatConversionContext)
        }
    }
}
