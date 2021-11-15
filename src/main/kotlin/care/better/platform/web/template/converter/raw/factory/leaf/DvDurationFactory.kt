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
import care.better.platform.template.AmUtils
import care.better.platform.utils.JodaConversionUtils
import care.better.platform.web.template.builder.model.input.WebTemplateDurationField
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.utils.DurationUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.joda.time.Period
import org.joda.time.format.ISOPeriodFormat
import org.joda.time.format.PeriodFormatter
import org.openehr.am.aom.CDuration
import org.openehr.rm.datatypes.DvDuration

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvDuration].
 */
internal object DvDurationFactory : DvQuantifiedFactory<DvDuration>() {

    private val PERIOD_FORMATTER: PeriodFormatter = ISOPeriodFormat.standard()
    private val durationFields: Set<String> = WebTemplateDurationField.values().map { it.name }.toSet()

    override fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvDuration, webTemplateInput: WebTemplateInput) {
        val item = AmUtils.getPrimitiveItem(amNode, CDuration::class.java, "value")
        val max = DurationUtils.getMax(item!!)
        val min = DurationUtils.getMin(item)

        if (min != Period.ZERO && max == min) {
            rmObject.value = PERIOD_FORMATTER.print(min)
        }
    }

    override fun createInstance(attributes: Set<AttributeDto>): DvDuration = DvDuration()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvDuration,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                when {
                    durationFields.contains(attribute.attribute.uppercase()) -> {
                        handleDurationEnumAttribute(attribute, jsonNode, rmObject)
                        true
                    }
                    attribute.attribute.isBlank() || attribute.attribute == "value" -> {
                        rmObject.value = PERIOD_FORMATTER.print(JodaConversionUtils.toPeriod(jsonNode.asText()))
                        true
                    }
                    else -> false
                }


    /**
     * Handles the attribute that has the same name as [WebTemplateDurationField].
     *
     * @param attribute [AttributeDto]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvDuration]
     */
    private fun handleDurationEnumAttribute(attribute: AttributeDto, jsonNode: JsonNode, rmObject: DvDuration) {
        val currentPeriod = if (rmObject.value == null) Period.ZERO else JodaConversionUtils.toPeriod(rmObject)

        val field: WebTemplateDurationField = WebTemplateDurationField.valueOf(attribute.attribute.uppercase())

        val fieldValue: Int = try {
            Integer.valueOf(jsonNode.asText())
        } catch (e: NumberFormatException) {
            throw ConversionException("Invalid value of duration field '${attribute.originalAttribute}': ${jsonNode.asText()}", e)
        }

        rmObject.value = PERIOD_FORMATTER.print(currentPeriod.withField(field.durationFieldType, fieldValue))
    }
}
