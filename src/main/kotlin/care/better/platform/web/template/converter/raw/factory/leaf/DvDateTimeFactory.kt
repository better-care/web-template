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

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvDateTime].
 */
internal object DvDateTimeFactory : DvQuantifiedFactory<DvDateTime>() {

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

        try {
            val datetime = conversionContext.valueConverter.parseOpenEhrDateTime(dateTimeString, pattern, conversionContext.strictMode)
            rmObject.value = conversionContext.valueConverter.formatOpenEhrTemporal(datetime, pattern, conversionContext.strictMode)
        } catch (e: RuntimeException) {
            throw ConversionException("Error processing value \"$dateTimeString\" for pattern \"$pattern\"", webTemplatePath.toString())
        }
    }
}
