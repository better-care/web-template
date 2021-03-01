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

package care.better.platform.web.template.builder.input

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils.getPrimitiveItem
import care.better.platform.utils.DateTimeConversionUtils
import care.better.platform.utils.JSR310ConversionUtils
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.range.WebTemplateTemporalRange
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.apache.commons.lang3.StringUtils
import org.openehr.am.aom.CDateTime
import org.openehr.rm.datatypes.DvDateTime
import java.time.OffsetDateTime

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object DateTimeWebTemplateInputBuilder : TemporalWebTemplateInputBuilder<CDateTime>() {

    override fun build(amNode: AmNode, validator: CDateTime?, context: WebTemplateBuilderContext): WebTemplateInput =
        with(
            if (validator == null)
                build(WebTemplateInputType.DATETIME)
            else
                build(WebTemplateInputType.DATETIME, WebTemplateTemporalRange(validator.range), validator.pattern)) {

            this.defaultValue = getDefaultValue(amNode, validator)
            this
        }

    private fun getDefaultValue(amNode: AmNode, item: CDateTime?): String? {
        val defaultValue: String? =
            if (item != null && StringUtils.isNotBlank(item.assumedValue))
                item.assumedValue
            else
                null

        val dateTime = WebTemplateBuilderUtils.getDefaultValue(amNode, DvDateTime::class.java)
        if (dateTime != null) {
            return dateTime.value
        }
        return defaultValue
    }

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val cDateTime = getPrimitiveItem(node.amNode, CDateTime::class.java, "value")
        val input = build(node.amNode, cDateTime, context)
        input.defaultValue = getTypedDefaultValue(node.amNode, cDateTime?.assumedValue)
        node.inputs.add(input)
    }

    private fun getTypedDefaultValue(amNode: AmNode, assumedValue: String?): OffsetDateTime? {
        val defaultValue: OffsetDateTime? = if (assumedValue == null) null else DateTimeConversionUtils.toOffsetDateTime(assumedValue)
        val dateTime = WebTemplateBuilderUtils.getDefaultValue(amNode, DvDateTime::class.java)
        if (dateTime != null) {
            return JSR310ConversionUtils.toOffsetDateTime(dateTime)
        }
        return defaultValue
    }
}
