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
import org.openehr.am.aom.CDateTime
import org.openehr.rm.datatypes.DvDate
import org.openehr.rm.datatypes.DvDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvDateTime].
 */
internal object DvDateTimeFactory : DvQuantifiedFactory<DvDateTime>() {

    private const val FULL_PATTERN = "yyyy-mm-ddTHH:MM:SS"
    private val PARTIAL_PATTERN = Pattern.compile("[0-9]{4}(-[0-9]{2}(-[0-9]{2}(T[0-9]{2})?)?)?")

    override fun createInstance(attributes: Set<AttributeDto>): DvDateTime = DvDateTime()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvDateTime,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                if (attribute.attribute.isBlank() || attribute.attribute == "value") {
                    val textValue = jsonNode.asText()
                    handleDateTime(conversionContext, amNode, rmObject, textValue, webTemplatePath)
                    true
                } else {
                    false
                }

    /**
     * Sets date time [String] to [DvDateTime].
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param rmObject [DvDate]
     * @param dateTimeString Date time [String]
     * @param webTemplatePath [WebTemplatePath]
     */
    private fun handleDateTime(
            conversionContext: ConversionContext,
            amNode: AmNode,
            rmObject: DvDateTime,
            dateTimeString: String,
            webTemplatePath: WebTemplatePath) {
        val pattern = AmUtils.getPrimitiveItem(amNode, CDateTime::class.java, "value")?.pattern ?: ""

        if (pattern.isBlank() && PARTIAL_PATTERN.matcher(dateTimeString).matches()) {
            rmObject.value = conversionContext.valueConverter.parsePartialDateTime(dateTimeString).format()
        } else if (pattern == FULL_PATTERN) {
            rmObject.value = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(conversionContext.valueConverter.parseDateTime(dateTimeString, true))
        } else {
            if (pattern.isNotBlank() && (pattern.contains("?") || pattern.contains("X"))) {
                try {
                    rmObject.value = conversionContext.valueConverter.parsePartialDateTime(dateTimeString, pattern).format(pattern)
                } catch (ex: IllegalArgumentException) {
                    if (conversionContext.strictMode){
                        throw ConversionException("Invalid partial datetime for pattern $pattern", webTemplatePath.toString())
                    }
                    rmObject.value = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(conversionContext.valueConverter.parseDateTime(dateTimeString, false))
                }
            } else {
                rmObject.value = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(conversionContext.valueConverter.parseDateTime(dateTimeString, false))
            }
        }
    }

}
