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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ALTERNATETEXT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CHARSET
import care.better.platform.web.template.converter.constant.WebTemplateConstants.COMPRESSION_ALGORITHM
import care.better.platform.web.template.converter.constant.WebTemplateConstants.DATA
import care.better.platform.web.template.converter.constant.WebTemplateConstants.INTEGRITY_CHECK
import care.better.platform.web.template.converter.constant.WebTemplateConstants.INTEGRITY_CHECK_ALGORITHM
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MEDIATYPE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SIZE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.THUMBNAIL
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvMultimedia

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [DvMultimedia] to STRUCTURED format.
 */
internal object DvMultimediaToStructuredMapper : RmObjectToStructuredMapper<DvMultimedia> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvMultimedia): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("|$VALUE", rmObject.uri?.value)
            this.putIfNotNull("|$MEDIATYPE", rmObject.mediaType?.codeString)
            this.putIfNotNull("|$ALTERNATETEXT", rmObject.alternateText)
            if (rmObject.size > 0) {
                this.putIfNotNull("|$SIZE", rmObject.size)
            }
            this.putIfNotNull("|$COMPRESSION_ALGORITHM", rmObject.compressionAlgorithm?.codeString)
            this.putIfNotNull("|$INTEGRITY_CHECK_ALGORITHM", rmObject.integrityCheckAlgorithm?.codeString)
            rmObject.integrityCheck?.also { this.putIfNotNull("|$INTEGRITY_CHECK", String(it)) }
            rmObject.data?.also { this.putIfNotNull("|$DATA", String(it)) }
            rmObject.thumbnail?.also {
                this.putSingletonAsArray("_$THUMBNAIL") { map(webTemplateNode, valueConverter, it) }
            }
            rmObject.charset?.also {
                this.putSingletonAsArray("_$CHARSET") { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.language?.also {
                this.putSingletonAsArray("_$LANGUAGE") { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvMultimedia): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("|$VALUE", rmObject.uri?.value)
            this.putIfNotNull("|$MEDIATYPE", rmObject.mediaType?.codeString)
            this.putIfNotNull("|$ALTERNATETEXT", rmObject.alternateText)
            if (rmObject.size > 0) {
                this.putIfNotNull("|$SIZE", rmObject.size.toString())
            }
            this.putIfNotNull("|$COMPRESSION_ALGORITHM", rmObject.compressionAlgorithm?.codeString)
            this.putIfNotNull("|$INTEGRITY_CHECK_ALGORITHM", rmObject.integrityCheckAlgorithm?.codeString)
            rmObject.integrityCheck?.also { this.putIfNotNull("|$INTEGRITY_CHECK", String(it)) }
            rmObject.data?.also { this.putIfNotNull("|$DATA", String(it)) }
            rmObject.thumbnail?.also {
                this.putSingletonAsArray("_$THUMBNAIL") { mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.charset?.also {
                this.putSingletonAsArray("_$CHARSET") { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.language?.also {
                this.putSingletonAsArray("_$LANGUAGE") { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            this
        }
}
