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

import care.better.platform.template.AmUtils
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.resolve
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openehr.am.aom.CTime
import org.openehr.rm.datatypes.DvTime
import java.time.DateTimeException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToStructuredMapper] that maps [DvTime] to STRUCTURED format.
 */
internal object DvTimeToStructuredMapper : DvQuantifiedToStructuredMapper<DvTime>() {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvTime): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            putValue(this, rmObject, valueConverter, "")
            map(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvTime): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            val pattern = AmUtils.getPrimitiveItem(webTemplateNode.amNode, CTime::class.java, "value")?.pattern ?: ""
            putValue(this, rmObject, valueConverter, pattern)
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun supportsValueNode(): Boolean = true

    override fun defaultValueNodeAttribute(): String = "|value"

    private fun putValue(node: ObjectNode, rmObject: DvTime, valueConverter: ValueConverter, pattern: String) {
        val value = requireNotNull(rmObject.value) { "DV_TIME value must not be null!" }
        try {
            node.putIfNotNull("", valueConverter.formatOpenEhrTemporal(valueConverter.parseOpenEhrTime(value, pattern, false), pattern, false))
        } catch (_: ConversionException) {
            node.putIfNotNull("", value)
        } catch (_: DateTimeException) {
            node.putIfNotNull("", value)
        }
    }
}
