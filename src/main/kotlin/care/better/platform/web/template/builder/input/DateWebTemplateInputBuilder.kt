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
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.range.WebTemplateTemporalRange
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.apache.commons.lang3.StringUtils
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import org.openehr.am.aom.CDate
import org.openehr.rm.datatypes.DvDate

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object DateWebTemplateInputBuilder : TemporalWebTemplateInputBuilder<CDate>() {
    override fun build(amNode: AmNode, validator: CDate?, context: WebTemplateBuilderContext): WebTemplateInput =
        with(
            if (validator == null)
                build(WebTemplateInputType.DATE)
            else
                build(WebTemplateInputType.DATE, WebTemplateTemporalRange(validator.range), validator.pattern)) {

            this.defaultValue = getDefaultValue(amNode, validator)
            this
        }

    private fun getDefaultValue(amNode: AmNode, item: CDate?): String? {
        if (item != null && StringUtils.isNotBlank(item.assumedValue)) {
            return item.assumedValue
        }
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(amNode, DvDate::class.java)
        return defaultValue?.value
    }

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val cDate = getPrimitiveItem(node.amNode, CDate::class.java, "value")
        val input = build(node.amNode, cDate, context)
        input.defaultValue = getTypedDefaultValue(node.amNode, cDate?.assumedValue)
        node.inputs.add(input)
    }

    private fun getTypedDefaultValue(amNode: AmNode, assumedValue: String?): LocalDate? {
        val defaultValue: LocalDate? = if (assumedValue == null) null else ISODateTimeFormat.dateParser().parseLocalDate(assumedValue)
        val date = WebTemplateBuilderUtils.getDefaultValue(amNode, DvDate::class.java)
        if (date != null) {
            return ISODateTimeFormat.dateParser().parseLocalDate(date.value)
        }
        return defaultValue
    }
}
