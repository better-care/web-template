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

import care.better.platform.utils.DateTimeConversionUtils
import care.better.platform.utils.JSR310ConversionUtils
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.resolve
import care.better.platform.web.template.converter.value.ValueConverter
import care.better.platform.web.template.date.partial.PartialDate
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvDate

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToStructuredMapper] that maps [DvDate] to STRUCTURED format.
 */
internal object DvDateToStructuredMapper : DvQuantifiedToStructuredMapper<DvDate>() {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvDate): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            val value = requireNotNull(rmObject.value) { "DV_DATE value must not be null!" }
            if (DateTimeConversionUtils.isPartialDate(value)) {
                this.putIfNotNull("", value)
            } else {
                this.putIfNotNull("", JSR310ConversionUtils.toLocalDate(rmObject).toString())
            }
            map(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvDate): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            val value = requireNotNull(rmObject.value) { "DV_DATE value must not be null!" }
            if (DateTimeConversionUtils.isPartialDate(value)) {
                this.putIfNotNull("", valueConverter.formatPartialDate(PartialDate.from(value)))
            } else {
                this.putIfNotNull("", valueConverter.formatDate(rmObject))
            }
            map(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun supportsValueNode(): Boolean = true

    override fun defaultValueNodeAttribute(): String = "|value"
}
