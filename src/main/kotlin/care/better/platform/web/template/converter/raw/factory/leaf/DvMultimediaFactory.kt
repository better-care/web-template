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

package care.better.platform.web.template.converter.raw.factory.leaf

import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ALTERNATETEXT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CHARSET
import care.better.platform.web.template.converter.constant.WebTemplateConstants.COMPRESSION_ALGORITHM
import care.better.platform.web.template.converter.constant.WebTemplateConstants.DATA
import care.better.platform.web.template.converter.constant.WebTemplateConstants.INTEGRITY_CHECK
import care.better.platform.web.template.converter.constant.WebTemplateConstants.INTEGRITY_CHECK_ALGORITHM
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MEDIATYPE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MEDIA_TYPE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.MIMETYPE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SIZE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.THUMBNAIL
import care.better.platform.web.template.converter.constant.WebTemplateConstants.URI
import care.better.platform.web.template.converter.constant.WebTemplateConstants.URL
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvMultimedia
import org.openehr.rm.datatypes.DvUri

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [DvMultimedia].
 */
internal object DvMultimediaFactory : RmObjectLeafNodeFactory<DvMultimedia>() {

    override fun createInstance(attributes: Set<AttributeDto>): DvMultimedia = DvMultimedia()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvMultimedia,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        if (attribute.attribute.isBlank() || attribute.attribute == URL || attribute.attribute == URI || attribute.attribute == VALUE) {
            rmObject.uri = DvUri().apply {
                this.value = jsonNode.asText()
            }
            true
        } else if (attribute.attribute == ALTERNATETEXT) {
            rmObject.alternateText = jsonNode.asText()
            true
        } else if (attribute.attribute == MIMETYPE || attribute.attribute == MEDIATYPE || attribute.attribute == MEDIA_TYPE) {
            rmObject.mediaType = CodePhrase.create("IANA_media-types", jsonNode.asText())
            true
        } else if (attribute.attribute == SIZE) {
            if (jsonNode.isNumber) {
                rmObject.size = jsonNode.numberValue().toInt()
            } else {
                try {
                    rmObject.size = jsonNode.asText().toInt()
                } catch (ex: NumberFormatException) {
                    throw ConversionException(
                        "Invalid value for attribute 'size' of DV_MULTIMEDIA (numeric expected): ${jsonNode.asText()}",
                        ex,
                        webTemplatePath.toString())
                }
            }
            true
        } else if (attribute.attribute == COMPRESSION_ALGORITHM) {
            rmObject.compressionAlgorithm = CodePhrase.create("openehr_compression_algorithms", jsonNode.asText())
            true
        } else if (attribute.attribute == INTEGRITY_CHECK_ALGORITHM) {
            rmObject.integrityCheckAlgorithm = CodePhrase.create("openehr_integrity_check_algorithms", jsonNode.asText())
            true
        } else if (attribute.attribute == INTEGRITY_CHECK) {
            rmObject.integrityCheck = jsonNode.asText().toByteArray()
            true
        } else if (attribute.attribute == DATA) {
            rmObject.data = jsonNode.asText().toByteArray()
            true
        } else if (attribute.attribute == "_$CHARSET") {
            jsonNode.forEach { node -> CodePhraseFactory.create(conversionContext, amNode, node, webTemplatePath)?.also { rmObject.charset = it } }
            true
        } else if (attribute.attribute == "_$LANGUAGE") {
            jsonNode.forEach { node -> CodePhraseFactory.create(conversionContext, amNode, node, webTemplatePath)?.also { rmObject.language = it } }
            true
        } else if (attribute.attribute == "_$THUMBNAIL") {
            jsonNode.forEach { node -> create(conversionContext, amNode, node, webTemplatePath)?.also { rmObject.thumbnail = it } }
            true
        } else {
            false
        }
}
