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
import care.better.platform.utils.DateTimeConversionUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.am.aom.CTime
import org.openehr.rm.datatypes.DvTime
import java.time.DateTimeException
import java.time.format.DateTimeFormatter

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvTime].
 */
internal object DvTimeFactory : DvQuantifiedFactory<DvTime>() {

    override fun createInstance(attributes: Set<AttributeDto>): DvTime = DvTime()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvTime,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                if (attribute.attribute.isBlank() || attribute.attribute == "value") {
                    val pattern = AmUtils.getPrimitiveItem(amNode, CTime::class.java, "value")?.pattern ?: ""
                    val textValue = jsonNode.asText()
                    if (isOffsetTime(textValue)) {
                        handleOffsetTime(conversionContext.valueConverter, rmObject, textValue, pattern)
                    } else {
                        handleLocalTime(conversionContext.valueConverter, rmObject, textValue, pattern)
                    }
                    true
                } else {
                    false
                }


    /**
     * Sets local time [String] to [DvTime].
     *
     * @param valueConverter [ValueConverter]
     * @param rmObject [DvTime]
     * @param timeString Local time [String]
     * @param pattern Pattern
     */
    private fun handleLocalTime(valueConverter: ValueConverter, rmObject: DvTime, timeString: String, pattern: String?) {
        val timeStart = timeString.indexOf("T")
        val time = if (timeStart == -1) timeString else timeString.substring(timeStart + 1)

        try {
            val localTime = valueConverter.parseTime(time)
            rmObject.value = DateTimeFormatter.ISO_LOCAL_TIME.format(localTime)
        } catch (ignored: DateTimeException) {
            handlePartialTime(valueConverter, rmObject, time, pattern)
        } catch (ignored: ConversionException) {
            handlePartialTime(valueConverter, rmObject, time, pattern)
        }
    }

    /**
     * Sets offset time [String] to [DvTime].
     *
     * @param valueConverter [ValueConverter]
     * @param rmObject [DvTime]
     * @param timeString Offset time [String]
     * @param pattern Pattern
     */
    private fun handleOffsetTime(valueConverter: ValueConverter, rmObject: DvTime, timeString: String, pattern: String?) {
        val timeStart = timeString.indexOf("T")
        val time = if (timeStart == -1) timeString else timeString.substring(timeStart + 1)

        try {
            val offsetTime = valueConverter.parseOffsetTime(time)
            rmObject.value = DateTimeFormatter.ISO_OFFSET_TIME.format(offsetTime)
        } catch (ignored: DateTimeException) {
            handlePartialTime(valueConverter, rmObject, time, pattern)
        } catch (ignored: ConversionException) {
            handlePartialTime(valueConverter, rmObject, time, pattern)
        }
    }

    /**
     * Checks if time [String] is in offset time format.
     *
     * @param timeString Time [String]
     * @return [Boolean] indicating if time [String] is in offset time format or not
     */
    private fun isOffsetTime(timeString: String): Boolean =
        try {
            DateTimeConversionUtils.toOffsetTime(timeString, true)
            true
        } catch (ex: DateTimeException) {
            false
        }

    private fun handlePartialTime(valueConverter: ValueConverter, rmObject: DvTime, timeString: String, pattern: String?) {
        if (pattern.isNullOrBlank()) {
            rmObject.value = valueConverter.parsePartialTime(timeString).format()
        } else {
            rmObject.value = valueConverter.parsePartialTime(timeString, pattern).format(pattern)
        }
    }
}
