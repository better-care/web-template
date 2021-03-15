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
import care.better.platform.template.AmUtils.getMax
import care.better.platform.template.AmUtils.getPrimitiveItem
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.WebTemplateProportionType
import care.better.platform.web.template.builder.model.input.WebTemplateValidation
import care.better.platform.web.template.builder.model.input.range.WebTemplateDecimalRange
import care.better.platform.web.template.builder.model.input.range.WebTemplateValidationIntegerRange
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.am.aom.CBoolean
import org.openehr.am.aom.CInteger
import org.openehr.am.aom.CReal
import org.openehr.rm.datatypes.DvProportion
import kotlin.math.roundToInt

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object ProportionWebTemplateInputBuilder : WebTemplateInputBuilder<Any> {
    private val SINGLE_INPUT_TYPES = setOf(WebTemplateProportionType.PERCENT.ordinal, WebTemplateProportionType.UNITARY.ordinal)
    private val INTEGRAL_TYPES = setOf(WebTemplateProportionType.FRACTION.ordinal, WebTemplateProportionType.INTEGER_FRACTION.ordinal)

    override fun build(amNode: AmNode, validator: Any?, context: WebTemplateBuilderContext): WebTemplateInput {
        val type = getPrimitiveItem(amNode, CInteger::class.java, "type")
        val precision = getPrimitiveItem(amNode, CInteger::class.java, "precision")!!
        val isIntegral = getPrimitiveItem(amNode, CBoolean::class.java, "is_integral")!!
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(amNode, DvProportion::class.java)
        val input = WebTemplateInput(WebTemplateInputType.PROPORTION)
        if (type != null && isSingleInput(type)) {
            val numerator = createInput(
                amNode, "numerator", type, precision, isIntegral,
                defaultValue?.numerator)
            if (type.list.contains(WebTemplateProportionType.PERCENT.ordinal)) {
                val numeratorCodedValue = WebTemplateCodedValue("%", "%")
                numeratorCodedValue.validation = numerator.validation
                input.list.add(numeratorCodedValue)
            } else {
                val numeratorCodedValue = WebTemplateCodedValue("", "")
                numeratorCodedValue.validation = numerator.validation
                input.list.add(numeratorCodedValue)
            }
            if (defaultValue != null) {
                input.defaultValue = defaultValue.numerator.toString()
            }
        } else {
            val numerator = createInput(amNode, "numerator", type, precision, isIntegral, defaultValue?.numerator)
            val denominator = createInput(amNode, "denominator", type, precision, isIntegral, defaultValue?.denominator)
            val numeratorCodedValue = WebTemplateCodedValue("numerator", "")
            numeratorCodedValue.validation = numerator.validation
            input.list.add(numeratorCodedValue)
            val denominatorCodedValue = WebTemplateCodedValue("denominator", "")
            denominatorCodedValue.validation = denominator.validation
            input.list.add(denominatorCodedValue)
            if (defaultValue != null) {
                input.defaultValue = defaultValue.numerator.toString() + '/' + defaultValue.denominator.toString()
            }
        }
        return input
    }

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val type = getPrimitiveItem(node.amNode, CInteger::class.java, "type")
        val precision = getPrimitiveItem(node.amNode, CInteger::class.java, "precision")
        val isIntegral = getPrimitiveItem(node.amNode, CBoolean::class.java, "is_integral")
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(node.amNode, DvProportion::class.java)
        if (type != null && isSingleInput(type)) {
            node.inputs.add(
                createInput(
                    node.amNode,
                    "numerator",
                    type,
                    precision,
                    isIntegral,
                    defaultValue?.numerator))
            node.inputs.add(createFixedDenominator(type, precision, isIntegral))
        } else {
            node.inputs.add(
                createInput(
                    node.amNode,
                    "numerator",
                    type,
                    precision,
                    isIntegral,
                    defaultValue?.numerator))
            node.inputs.add(
                createInput(
                    node.amNode,
                    "denominator",
                    type,
                    precision,
                    isIntegral,
                    defaultValue?.denominator))
        }
        val proportionTypes =
            if (type == null)
                WebTemplateProportionType.values().asSequence().map { it.name }.map { it.toLowerCase() }.toList()
            else
                WebTemplateProportionType.values().asSequence().filter { type.list.contains(it.ordinal) }.map { it.name }.map { it.toLowerCase() }.toList()

        node.proportionTypes.addAll(proportionTypes)
    }

    private fun createFixedDenominator(type: CInteger, precision: CInteger?, integral: CBoolean?): WebTemplateInput {
        val isIntegral = isIntegral(type, precision, integral)
        val input = WebTemplateInput(if (isIntegral) WebTemplateInputType.INTEGER else WebTemplateInputType.DECIMAL, "denominator")
        if (type.list.size == 2) { // percent and unitary
            val unitary = WebTemplateCodedValue("1", "1")
            unitary.validation = WebTemplateValidation().apply {
                if (isIntegral)
                    this.range = WebTemplateValidationIntegerRange(1, 1)
                else
                    this.range = WebTemplateDecimalRange(1.0f, ">=", 1.0f, "<=")

            }

            input.list.add(unitary)
            val percent = WebTemplateCodedValue("100", "100")
            percent.validation = WebTemplateValidation().apply {
                if (isIntegral)
                    this.range = WebTemplateValidationIntegerRange(100, 100)
                else
                    this.range = WebTemplateDecimalRange(100.0f, ">=", 100.0f, "<=")
            }
            input.list.add(percent)
        } else if (type.list.isNotEmpty() && type.list[0] == WebTemplateProportionType.UNITARY.ordinal) {
            input.validation = WebTemplateValidation().apply {
                if (isIntegral)
                    this.range = WebTemplateValidationIntegerRange(1, 1)
                else
                    this.range = WebTemplateDecimalRange(1.0f, ">=", 1.0f, "<=")
            }
        } else {
            input.validation = WebTemplateValidation().apply {
                if (isIntegral)
                    this.range = WebTemplateValidationIntegerRange(100, 100)
                else
                    this.range = WebTemplateDecimalRange(100.0f, ">=", 100.0f, "<=")
            }
        }
        return input
    }

    private fun createInput(
            amNode: AmNode,
            suffix: String,
            type: CInteger?,
            precision: CInteger?,
            integral: CBoolean?,
            defaultValue: Float?): WebTemplateInput =
        with(getPrimitiveItem(amNode, CReal::class.java, suffix)) {
            if (isIntegral(type, precision, integral))
                createIntegerInput(suffix, this, if (defaultValue == null) null else Math.round(defaultValue))
            else
                createDecimalInput(suffix, this, defaultValue)
        }

    private fun createDecimalInput(suffix: String, cReal: CReal?, defaultValue: Float?): WebTemplateInput =
        with(WebTemplateInput(WebTemplateInputType.DECIMAL, suffix)) {
            if (cReal != null) {
                if (cReal.range != null) {
                    val range = WebTemplateDecimalRange(cReal.range)
                    if (!range.isEmpty()) {
                        this.validation = WebTemplateValidation().apply { this.range = range }
                    }
                    this.fixed = range.isFixed() || cReal.list.size == 1
                }
                if (cReal.list.isNotEmpty()) {
                    this.list.addAll(cReal.list.map(WebTemplateCodedValue.toCodedValue()))
                }
                if (cReal.assumedValue != null) {
                    this.defaultValue = cReal.assumedValue
                }
            }
            if (defaultValue != null) {
                this.defaultValue = defaultValue
            }
            this
        }

    private fun createIntegerInput(suffix: String, cReal: CReal?, defaultValue: Int?): WebTemplateInput =
        with(WebTemplateInput(WebTemplateInputType.INTEGER, suffix)) {
            if (cReal != null) {
                if (cReal.range != null) {
                    val min = cReal.range?.lower?.roundToInt()
                    val max = cReal.range?.upper?.roundToInt()

                    val range = WebTemplateValidationIntegerRange(min, max)
                    if (!range.isEmpty()) {
                        this.validation = WebTemplateValidation().apply { this.range = range }
                    }
                    this.fixed = range.isFixed() || cReal.list.size == 1
                }
                if (cReal.list.isNotEmpty()) {
                    this.list.addAll(cReal.list.map {
                        val inputValue = (it).roundToInt().toString()
                        WebTemplateCodedValue(inputValue, inputValue)
                    })
                }
                val assumedValue = cReal.assumedValue
                if (assumedValue != null) {
                    this.defaultValue = assumedValue.roundToInt()
                }
            }
            if (defaultValue != null) {
                this.defaultValue = defaultValue
            }
            this
        }

    private fun isSingleInput(type: CInteger): Boolean = type.list.all { SINGLE_INPUT_TYPES.contains(it) }

    private fun isIntegral(type: CInteger?, precision: CInteger?, cIntegral: CBoolean?): Boolean =
        when {
            cIntegral != null && cIntegral.trueValid && !cIntegral.falseValid -> true
            precision != null && Integer.valueOf(0) == getMax(precision.range) -> true
            else -> type?.list?.all { INTEGRAL_TYPES.contains(it) } ?: false

        }
}
