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
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.am.aom.CDate
import org.openehr.rm.datatypes.DvDate
import java.time.DateTimeException
import java.time.format.DateTimeFormatter

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvDate].
 */
internal object DvDateFactory : DvQuantifiedFactory<DvDate>() {
    private const val FULL_PATTERN: String = "yyyy-mm-dd"
    private const val PARTIAL_PATTERN: String = "yyyy-??-??"

    override fun createInstance(attributes: Set<AttributeDto>): DvDate = DvDate()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvDate,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                if (attribute.attribute.isBlank() || attribute.attribute == "value") {
                    handleDate(
                        conversionContext,
                        amNode,
                        rmObject,
                        jsonNode.asText())
                    true
                } else {
                    false
                }

    /**
     * Sets date [String] to [DvDate].
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param rmObject [DvDate]
     * @param dateString Date [String]
     */
    private fun handleDate(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvDate, dateString: String) {
        val pattern = AmUtils.getPrimitiveItem(amNode, CDate::class.java, "value")?.pattern ?: ""

        if (pattern == FULL_PATTERN) {
            handleLocalDate(conversionContext, rmObject, dateString, true)
        } else {
            if (pattern.isBlank()) {
                try {
                    handleLocalDate(conversionContext, rmObject, dateString, false)
                } catch (ignored: DateTimeException) {
                    handlePartialDate(conversionContext, rmObject, dateString, PARTIAL_PATTERN)
                } catch (ignored: ConversionException) {
                    handlePartialDate(conversionContext, rmObject, dateString, PARTIAL_PATTERN)
                }
            } else {
                handlePartialDate(conversionContext, rmObject, dateString, pattern)
            }
        }
    }

    /**
     * Sets local date [String] to [DvDate].
     *
     * @param conversionContext [ConversionContext]
     * @param rmObject [DvDate]
     * @param dateString Local date [String]
     */
    private fun handleLocalDate(conversionContext: ConversionContext, rmObject: DvDate, dateString: String, strict: Boolean) {
        if (strict) {
            rmObject.value = DateTimeFormatter.ISO_LOCAL_DATE.format(conversionContext.valueConverter.parseDate(dateString, true))
        } else {
            try {
                rmObject.value = DateTimeFormatter.ISO_LOCAL_DATE.format(conversionContext.valueConverter.parseDate(dateString, true))
            } catch (ignored: ConversionException) {
                try {
                    rmObject.value = conversionContext.valueConverter.parsePartialDate(dateString).format()
                } catch (e: IllegalArgumentException) {
                    throw ConversionException("Unable to convert value to LocalDate: $dateString", e)
                }
            }
        }
    }

    /**
     * Sets partial date [String] to [DvDate].
     *
     * @param conversionContext [ConversionContext]
     * @param rmObject [DvDate]
     * @param dateString Partial date [String]
     */
    private fun handlePartialDate(conversionContext: ConversionContext, rmObject: DvDate, dateString: String, pattern: String) {
        rmObject.value = conversionContext.valueConverter.parsePartialDate(dateString, pattern).format(pattern)
    }
}
