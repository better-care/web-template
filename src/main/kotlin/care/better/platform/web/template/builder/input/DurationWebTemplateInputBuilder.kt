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
import care.better.platform.utils.JodaConversionUtils
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateDurationField
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.WebTemplateValidation
import care.better.platform.web.template.builder.model.input.range.WebTemplateValidationIntegerRange
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import com.google.common.base.Splitter
import com.google.common.collect.ImmutableSet
import org.joda.time.Period
import org.openehr.am.aom.CDuration
import org.openehr.rm.datatypes.DvDuration
import java.util.*

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object DurationWebTemplateInputBuilder : WebTemplateInputBuilder<CDuration> {
    private val FULL_DURATION: Set<WebTemplateDurationField> = ImmutableSet.of(
        WebTemplateDurationField.YEAR,
        WebTemplateDurationField.MONTH,
        WebTemplateDurationField.DAY,
        WebTemplateDurationField.WEEK,
        WebTemplateDurationField.HOUR,
        WebTemplateDurationField.MINUTE,
        WebTemplateDurationField.SECOND)

    override fun build(amNode: AmNode, validator: CDuration?, context: WebTemplateBuilderContext): WebTemplateInput {
        val input = WebTemplateInput(WebTemplateInputType.DURATION)
        val fields: Set<WebTemplateDurationField>
        val min: Period
        val max: Period
        if (validator == null) {
            fields = FULL_DURATION
            min = Period.ZERO
            max = Period.ZERO
        } else {
            fields = if (validator.pattern == null) FULL_DURATION else getAllowedFields(validator.pattern!!)
            min = if (validator.range?.lower != null) JodaConversionUtils.toPeriod(validator.range?.lower!!) else Period.ZERO
            max = if (validator.range?.upper != null) JodaConversionUtils.toPeriod(validator.range?.upper!!) else Period.ZERO
            if (validator.assumedValue != null) {
                input.defaultValue = validator.assumedValue
            }
        }
        for (field in fields) {
            val codedValue = WebTemplateCodedValue(field.name.toLowerCase(), field.name.toLowerCase())
            codedValue.validation = WebTemplateValidation()
            val fieldMin = min[field.durationFieldType]
            val fieldMax = max[field.durationFieldType]

            codedValue.validation?.apply {
                this.range = WebTemplateValidationIntegerRange(fieldMin, if (fieldMax == 0) null else fieldMax)
            }
            input.list.add(codedValue)
        }
        input.fixed = min != Period.ZERO && min == max
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(amNode, DvDuration::class.java)
        if (defaultValue != null) {
            input.defaultValue = defaultValue.value
        }
        return input
    }

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val item = getPrimitiveItem(node.amNode, CDuration::class.java, "value")
        val defaultDuration = WebTemplateBuilderUtils.getDefaultValue(node.amNode, DvDuration::class.java)
        var defaultValue = defaultDuration?.value
        val fields: Set<WebTemplateDurationField>
        val min: Period
        val max: Period
        if (item == null) {
            fields = FULL_DURATION
            min = Period.ZERO
            max = Period.ZERO
        } else {
            if (defaultValue == null && item.assumedValue != null) {
                defaultValue = item.assumedValue
            }
            fields = if (item.pattern == null) FULL_DURATION else getAllowedFields(item.pattern!!)
            min = getMin(item)
            max = getMax(item)
        }
        val defaultPeriod = if (defaultValue == null) null else Period.parse(defaultValue)
        for (field in fields) {
            val input = WebTemplateInput(WebTemplateInputType.INTEGER, field.name.toLowerCase())
            input.validation = WebTemplateValidation()
            val fieldMin = min[field.durationFieldType]
            val fieldMax = max[field.durationFieldType]
            input.validation?.apply {
                this.range = WebTemplateValidationIntegerRange(fieldMin, if (fieldMax == 0) null else fieldMax)
            }
            input.fixed = min != Period.ZERO && min == max
            if (defaultPeriod != null) {
                input.defaultValue = defaultPeriod[field.durationFieldType]
            }
            node.inputs.add(input)
        }
    }

    private fun getAllowedFields(pattern: String): Set<WebTemplateDurationField> {
        val iterator: Iterator<String> = Splitter.on("T").split(pattern).iterator()
        val allowedFields: MutableSet<WebTemplateDurationField> = EnumSet.noneOf(WebTemplateDurationField::class.java)
        if (iterator.hasNext()) {
            val ymdw = iterator.next()
            if (ymdw.contains("Y")) {
                allowedFields.add(WebTemplateDurationField.YEAR)
            }
            if (ymdw.contains("M")) {
                allowedFields.add(WebTemplateDurationField.MONTH)
            }
            if (ymdw.contains("D")) {
                allowedFields.add(WebTemplateDurationField.DAY)
            }
            if (ymdw.contains("W")) {
                allowedFields.add(WebTemplateDurationField.WEEK)
            }
        }
        if (iterator.hasNext()) {
            val hms = iterator.next()
            if (hms.contains("H")) {
                allowedFields.add(WebTemplateDurationField.HOUR)
            }
            if (hms.contains("M")) {
                allowedFields.add(WebTemplateDurationField.MINUTE)
            }
            if (hms.contains("S")) {
                allowedFields.add(WebTemplateDurationField.SECOND)
            }
        }
        return allowedFields
    }

    fun getMin(item: CDuration): Period = if (item.range?.lower != null) JodaConversionUtils.toPeriod(item.range?.lower!!) else Period.ZERO


    fun getMax(item: CDuration): Period = if (item.range?.upper != null) JodaConversionUtils.toPeriod(item.range?.upper!!) else Period.ZERO

    fun getAssumedValue(item: CDuration): Period = if (item.assumedValue != null) JodaConversionUtils.toPeriod(item.assumedValue!!) else Period.ZERO
}
