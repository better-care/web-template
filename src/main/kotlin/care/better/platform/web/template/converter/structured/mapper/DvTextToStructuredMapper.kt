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

import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ENCODING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FORMATTING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MAPPING
import care.better.platform.web.template.converter.constant.WebTemplateConstants.OTHER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.mapper.resolve
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [DvText] to STRUCTURED format.
 */
internal object DvTextToStructuredMapper : RmObjectToStructuredMapper<DvText> {
    private val dvCodedTextRmType = RmUtils.getRmTypeName(DvCodedText::class.java)

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvText): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putCollectionAsArray("_$MAPPING", rmObject.mappings) { TermMappingToStructuredMapper.map(webTemplateNode, valueConverter, it) }

            if (webTemplateNode.rmType == dvCodedTextRmType) {
                this.putIfNotNull("|$OTHER", rmObject.value)
            } else {
                this.putIfNotNull(if (rmObject.mappings.isEmpty() && rmObject.formatting.isNullOrBlank()) "" else "|$VALUE", rmObject.value)
            }
            this.putIfNotNull("|$FORMATTING", rmObject.formatting)

            rmObject.language?.also {
                this.putSingletonAsArray("_$LANGUAGE") { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }

            rmObject.encoding?.also {
                this.putSingletonAsArray("_$ENCODING") { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }

            this.resolve()
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvText): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putCollectionAsArray("_$MAPPING", rmObject.mappings) { TermMappingToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }

            if (webTemplateNode.rmType == dvCodedTextRmType) {
                this.putIfNotNull("|$OTHER", rmObject.value)
            } else {
                this.putIfNotNull(if (rmObject.mappings.isEmpty() && rmObject.formatting.isNullOrBlank()) "" else "|$VALUE", rmObject.value)
            }
            this.putIfNotNull("|$FORMATTING", rmObject.formatting)

            rmObject.language?.also {
                this.putSingletonAsArray("_$LANGUAGE") { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }

            rmObject.encoding?.also {
                this.putSingletonAsArray("_$ENCODING") { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }

            this.resolve()
        }

    override fun supportsValueNode(): Boolean = true

    override fun defaultValueNodeAttribute(): String = "|$VALUE"
}
