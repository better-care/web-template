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
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.am.aom.CBoolean
import org.openehr.rm.datatypes.DvBoolean

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object BooleanWebTemplateInputBuilder : WebTemplateInputBuilder<CBoolean> {
    override fun build(amNode: AmNode, validator: CBoolean?, context: WebTemplateBuilderContext): WebTemplateInput =
        with(WebTemplateInput(WebTemplateInputType.BOOLEAN)) {
            if (validator != null) {
                if (validator.falseValid && !validator.trueValid) {
                    this.list.add(WebTemplateCodedValue.toCodedValue().invoke(false))
                    this.fixed = true
                } else if (!validator.falseValid && validator.trueValid) {
                    this.list.add(WebTemplateCodedValue.toCodedValue().invoke(true))
                    this.fixed = true
                }
                if (validator.assumedValue != null) {
                    this.defaultValue = validator.assumedValue.toString()
                }
            }
            WebTemplateBuilderUtils.getDefaultValue(amNode, DvBoolean::class.java)?.also { this.defaultValue = it.value.toString() }
            this
        }


    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val cBoolean = getPrimitiveItem(node.amNode, CBoolean::class.java, "value")
        val input = build(node.amNode, cBoolean, context)
        if (cBoolean?.assumedValue != null) {
            input.defaultValue = cBoolean.assumedValue
        }
        WebTemplateBuilderUtils.getDefaultValue(node.amNode, DvBoolean::class.java)?.also { input.defaultValue = it.value }
        node.inputs.add(input)
    }
}
