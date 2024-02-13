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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PERCENTAGE_PROPORTION_TYPE
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.am.aom.CBoolean
import org.openehr.am.aom.CInteger
import org.openehr.am.aom.CReal
import org.openehr.rm.datatypes.DvProportion
import java.math.RoundingMode

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvProportion].
 */
internal object DvProportionFactory : DvQuantifiedFactory<DvProportion>() {

    private val sortMap: Map<AttributeDto, Int> = mapOf(
        Pair(AttributeDto.forAttribute("type"), 0),
        Pair(AttributeDto.forAttribute(""), 1),
        Pair(AttributeDto.forAttribute("value"), 2),
        Pair(AttributeDto.forAttribute("numerator"), 3),
        Pair(AttributeDto.forAttribute("denominator"), 4))

    override fun sortFieldNames(attributes: List<AttributeDto>): List<AttributeDto> =
        attributes.asSequence().map { Pair(it, sortMap[it] ?: Integer.MAX_VALUE) }.sortedBy { it.second }.map { it.first }.toList()

    override fun createInstance(attributes: Set<AttributeDto>): DvProportion = DvProportion()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvProportion,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                when {
                    attribute.attribute.isBlank() -> {
                        ensureType(amNode, rmObject)
                        setWithoutAttribute(conversionContext, amNode, rmObject, jsonNode, getMaxPrecision(amNode))
                        true
                    }
                    attribute.attribute == "type" -> {
                        setType(amNode, rmObject, jsonNode, webTemplatePath)
                        true
                    }
                    attribute.attribute == "numerator" -> {
                        ensureType(amNode, rmObject)
                        rmObject.numerator = convertValue(conversionContext, jsonNode, getMaxPrecision(amNode))

                        rmObject.type?.also {
                            if (it.toInt() == PERCENTAGE_PROPORTION_TYPE) {
                                rmObject.denominator = 100.0f
                            }
                        }
                        true
                    }
                    attribute.attribute == "denominator" -> {
                        ensureType(amNode, rmObject)
                        rmObject.denominator = convertValue(conversionContext, jsonNode, getMaxPrecision(amNode))
                        true
                    }
                    else -> false
                }

    override fun afterPropertiesSet(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvProportion) {
        if (rmObject.precision == null) {
            rmObject.precision = getMaxPrecision(amNode)
        }
    }

    override fun removeDependentValues(map: MutableMap<AttributeDto, JsonNode>): Boolean {
        if (map[AttributeDto.forAttribute("numerator")] == null) {
            val removedValues = listOf(
                    map.remove(AttributeDto.forAttribute("numerator")),
                    map.remove(AttributeDto.forAttribute("denominator")),
                    map.remove(AttributeDto.ofBlank()),
                    map.remove(AttributeDto.forAttribute("value")),
                    map.remove(AttributeDto.forAttribute("type")))
            return removedValues.any { it != null }
        }
        return false
    }

    /**
     * Retrieves the maximal precision from [AmNode].
     *
     * @param amNode [AmNode]
     * @return Maximal precision if found, otherwise, return null
     */
    private fun getMaxPrecision(amNode: AmNode): Int? =
        getMaxPrecision(
            AmUtils.getPrimitiveItem(amNode, CInteger::class.java, "precision"),
            AmUtils.getPrimitiveItem(amNode, CBoolean::class.java, "is_integral"))

    /**
     * Retrieves maximal precision from [CInteger] and [CBoolean].
     *
     * @param precision [CInteger]
     * @param integral [CBoolean]
     * @return Maximal precision if found, otherwise, return null
     */
    private fun getMaxPrecision(precision: CInteger?, integral: CBoolean?): Int? =
        when {
            integral != null && integral.trueValid && !integral.falseValid -> 0
            precision != null -> AmUtils.getMax(precision.range)
            else -> null
        }

    /**
     * Ensures that the default type is set to the [DvProportion] if "|type" attribute was not present.
     *
     * @param amNode [AmNode]
     * @param rmObject [DvProportion]
     */
    private fun ensureType(amNode: AmNode, rmObject: DvProportion) {
        if (rmObject.type == null) {
            val type = AmUtils.getPrimitiveItem(amNode, CInteger::class.java, "type")
            if (type == null) {
                rmObject.type = 0
            } else {
                if (type.list.contains(2)) {
                    rmObject.type = 2
                } else if (type.list.contains(0) || type.list.isEmpty()) {
                    rmObject.type = 0
                } else {
                    rmObject.type = type.list[0]
                }
            }
        }
    }

    /**
     * Sets type to [DvProportion] from [JsonNode] "|type" entry value.
     *
     * @param jsonNode [JsonNode]
     * @param rmObject [DvProportion]
     */
    private fun setType(amNode: AmNode, rmObject: DvProportion, jsonNode: JsonNode, webTemplatePath: WebTemplatePath) {
        val value = if (jsonNode.isNumber) jsonNode.numberValue().toInt() else jsonNode.asText()?.toInt()

        if (value == null || value > 4 || value < 0) {
            throw ConversionException("Invalid value '$value' for DV_PROPORTION.type, allowed values: 0..4", webTemplatePath.toString())
        }

        val typePrimitive = AmUtils.getPrimitiveItem(amNode, CInteger::class.java, "type")

        if (typePrimitive == null) {
            rmObject.type = value
        } else {
            if (typePrimitive.list.contains(value)) {
                rmObject.type = value
            } else if (typePrimitive.list.contains(2)) {
                rmObject.type = 2
            } else if (typePrimitive.list.isEmpty() || typePrimitive.list.contains(0)) {
                rmObject.type = 0
            } else {
                rmObject.type = typePrimitive.list[0]
            }
        }
    }

    /**
     * Sets values to [DvProportion] from [JsonNode] "" entry value.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvProportion]
     * @param precisionMax Maximal precision
     */
    private fun setWithoutAttribute(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvProportion, jsonNode: JsonNode, precisionMax: Int?) {
        val numerator = AmUtils.getPrimitiveItem(amNode, CReal::class.java, "numerator")?.also {
            val fixedNumerator = WebTemplateConversionUtils.getFixedValue(it.range)
            if (fixedNumerator != null) {
                rmObject.numerator = fixedNumerator
                rmObject.denominator = convertValue(conversionContext, jsonNode, precisionMax)
            }
        }

        val denominator = AmUtils.getPrimitiveItem(amNode, CReal::class.java, "denominator")?.also {
            val fixedDenominator = WebTemplateConversionUtils.getFixedValue(it.range)
            if (fixedDenominator != null) {
                rmObject.numerator = convertValue(conversionContext, jsonNode, precisionMax)
                rmObject.denominator = fixedDenominator
            }
        }

        if (numerator == null && denominator == null && rmObject.type == PERCENTAGE_PROPORTION_TYPE) {
            rmObject.numerator = convertValue(conversionContext, jsonNode, precisionMax)
            rmObject.denominator = 100.0f
        }
    }

    /**
     * Converts [JsonNode] to [Float].
     *
     * @param conversionContext [ConversionContext]
     * @param jsonNode [JsonNode]
     * @param precisionMax Maximal precision
     * @return [Float] value
     */
    private fun convertValue(conversionContext: ConversionContext, jsonNode: JsonNode, precisionMax: Int?): Float =
        if (jsonNode.isNumber)
            convertWithFixedPrecision(jsonNode.numberValue().toDouble(), precisionMax)
        else
            convertWithFixedPrecision(conversionContext.valueConverter.parseDouble(jsonNode.asText()), precisionMax)

    /**
     * Converts [Double] to [Float] with fixed precision.
     *
     * @param value [Double] value
     * @param precisionMax Maximal precision
     * @return [Float] value
     */
    private fun convertWithFixedPrecision(value: Double, precisionMax: Int?): Float =
        if (precisionMax == null)
            value.toFloat()
        else
            value.toBigDecimal().setScale(precisionMax, RoundingMode.HALF_UP).toFloat()

    override fun canRemoveDependantValues(): Boolean = true
}
