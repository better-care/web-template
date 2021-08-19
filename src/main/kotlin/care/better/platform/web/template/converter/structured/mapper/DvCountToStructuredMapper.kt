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
import care.better.platform.web.template.converter.mapper.resolve
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.datatypes.DvCount

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToStructuredMapper] that maps [DvCount] to STRUCTURED format.
 */
internal object DvCountToStructuredMapper : DvQuantifiedToStructuredMapper<DvCount>() {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvCount): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("", rmObject.magnitude)
            map(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvCount): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("", rmObject.magnitude.toString())
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun supportsValueNode(): Boolean = true

    override fun defaultValueNodeAttribute(): String = "|magnitude"
}
