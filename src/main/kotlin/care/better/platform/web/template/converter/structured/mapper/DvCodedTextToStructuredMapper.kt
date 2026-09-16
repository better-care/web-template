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

package care.better.platform.web.template.converter.structured.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CODE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ENCODING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FORMATTING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MAPPING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PREFERRED_TERM
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TERMINOLOGY
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [DvCodedText] to STRUCTURED format.
 */
internal object DvCodedTextToStructuredMapper : RmObjectToStructuredMapper<DvCodedText> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvCodedText): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putCollectionAsArray("_$MAPPING", rmObject.mappings) { TermMappingToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            this.putIfNotNull("|$CODE", rmObject.definingCode?.codeString)
            this.putIfNotNull("|$VALUE", rmObject.value)
            this.putIfNotNull("|$TERMINOLOGY", rmObject.definingCode?.terminologyId?.value)
            this.putIfNotNull("|$PREFERRED_TERM", rmObject.definingCode?.preferredTerm)
            this.putIfNotNull("|$FORMATTING", rmObject.formatting)
            rmObject.language?.also {
                this.putSingletonAsArray("_$LANGUAGE") { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.encoding?.also {
                this.putSingletonAsArray("_$ENCODING") { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvCodedText): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putCollectionAsArray("_$MAPPING", rmObject.mappings) { TermMappingToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            this.putIfNotNull("|$CODE", rmObject.definingCode?.codeString)
            this.putIfNotNull("|$VALUE", rmObject.value)
            this.putIfNotNull("|$TERMINOLOGY", rmObject.definingCode?.terminologyId?.value)
            this.putIfNotNull("|$PREFERRED_TERM", rmObject.definingCode?.preferredTerm)
            this.putIfNotNull("|$FORMATTING", rmObject.formatting)
            rmObject.language?.also {
                this.putSingletonAsArray("_$LANGUAGE") { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.encoding?.also {
                this.putSingletonAsArray("_$ENCODING") { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            this
        }
}
