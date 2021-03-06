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
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvScale

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvOrderedToStructuredMapper] that maps [DvScale] to STRUCTURED format.
 */
internal object DvScaleToStructuredMapper : DvOrderedToStructuredMapper<DvScale>() {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvScale): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("|code", rmObject.symbol?.definingCode?.codeString)
            this.putIfNotNull("|value", rmObject.symbol?.value)
            this.putIfNotNull("|scale", rmObject.value)
            DvScaleToStructuredMapper.map(webTemplateNode, valueConverter, rmObject, this)
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvScale): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("|code", rmObject.symbol?.definingCode?.codeString)
            this.putIfNotNull("|value", rmObject.symbol?.value)
            this.putIfNotNull("|scale", rmObject.value.toString())
            DvScaleToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            this
        }
}
