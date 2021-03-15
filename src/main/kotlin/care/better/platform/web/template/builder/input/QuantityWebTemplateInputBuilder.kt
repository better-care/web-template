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
import care.better.platform.template.AmUtils.findText
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.WebTemplateValidation
import care.better.platform.web.template.builder.model.input.range.WebTemplateDecimalRange
import care.better.platform.web.template.builder.model.input.range.WebTemplateValidationIntegerRange
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.am.aom.CDvQuantity
import org.openehr.am.aom.CQuantityItem
import org.openehr.rm.datatypes.DvQuantity

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object QuantityWebTemplateInputBuilder : WebTemplateInputBuilder<CDvQuantity> {
    override fun build(amNode: AmNode, validator: CDvQuantity?, context: WebTemplateBuilderContext): WebTemplateInput {
        val input = WebTemplateInput(WebTemplateInputType.QUANTITY)
        if (validator != null) {
            if (validator.list.isNotEmpty()) {
                buildFromList(input, validator.list, amNode, context.languages)
            }
            if (validator.assumedValue != null) {
                input.defaultValue = "${validator.assumedValue?.magnitude.toString()} ${validator.assumedValue?.units}"
            }
        }
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(amNode, DvQuantity::class.java)
        if (defaultValue != null) {
            input.defaultValue = "${defaultValue.magnitude} ${defaultValue.units}"
        }
        return input
    }

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val magnitude = WebTemplateInput(WebTemplateInputType.DECIMAL, "magnitude")
        val units: WebTemplateInput
        if (node.amNode.cObject is CDvQuantity) {
            units = WebTemplateInput(WebTemplateInputType.CODED_TEXT, "unit")
            val dvQuantity = node.amNode.cObject as CDvQuantity
            if (dvQuantity.list.isNotEmpty()) {
                buildFromList(units, dvQuantity.list, node.amNode, context.languages)
                if (units.list.size == 1) {
                    magnitude.validation = units.list[0].validation
                }
            }
            if (dvQuantity.assumedValue != null) {
                magnitude.defaultValue = dvQuantity.assumedValue?.magnitude
                units.defaultValue = dvQuantity.assumedValue?.units
            }
            val defaultValue = WebTemplateBuilderUtils.getDefaultValue(node.amNode, DvQuantity::class.java)
            if (defaultValue != null) {
                magnitude.defaultValue = defaultValue.magnitude
                units.defaultValue = defaultValue.units
            }
        } else {
            units = WebTemplateInput(WebTemplateInputType.TEXT, "unit")
        }
        node.inputs.add(magnitude)
        node.inputs.add(units)
    }

    private fun buildFromList(input: WebTemplateInput, quantityItems: List<CQuantityItem>, amNode: AmNode, languages: Collection<String>) {
        for (item in quantityItems) {
            val precision = WebTemplateValidationIntegerRange(item.precision)
            val range = WebTemplateDecimalRange(item.magnitude)
            val value = WebTemplateCodedValue(item.units, item.units)
            if (!precision.isEmpty() || !range.isEmpty()) {
                value.validation = WebTemplateValidation().apply {
                    if (!precision.isEmpty()) {
                        this.precision = precision
                    }
                    if (!range.isEmpty()) {
                        this.range = range
                    }
                }
            }

            languages.asSequence()
                .map { Pair(it, findText(amNode, it, item.units)) }
                .filter { it.second != null }
                .forEach { value.localizedLabels[it.first] = it.second!! }

            input.list.add(value)
        }
    }
}
