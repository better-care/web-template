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

import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import care.better.platform.web.template.builder.model.WebTemplateNode
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
            this.putIfNotNull("|value", rmObject.uri?.value)
            this.putIfNotNull("|mediatype", rmObject.mediaType?.codeString)
            this.putIfNotNull("|alternatetext", rmObject.alternateText)
            if (rmObject.size > 0) {
                this.putIfNotNull("|size", rmObject.size)
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvMultimedia): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("|value", rmObject.uri?.value)
            this.putIfNotNull("|mediatype", rmObject.mediaType?.codeString)
            this.putIfNotNull("|alternatetext", rmObject.alternateText)
            if (rmObject.size > 0) {
                this.putIfNotNull("|size", rmObject.size.toString())
            }
            this
        }
}
