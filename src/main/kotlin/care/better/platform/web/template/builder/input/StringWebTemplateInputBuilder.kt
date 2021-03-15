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
import care.better.platform.web.template.builder.model.input.WebTemplateValidation
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.am.aom.CString
import org.openehr.rm.datatypes.DvIdentifier
import org.openehr.rm.datatypes.DvText

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object StringWebTemplateInputBuilder : WebTemplateInputBuilder<CString> {

    @JvmStatic
    fun createTextInput(amNode: AmNode, cString: CString?, suffix: String?): WebTemplateInput {
        val input = WebTemplateInput(WebTemplateInputType.TEXT, suffix)
        if (cString != null) {
            if (cString.pattern != null) {
                input.validation = WebTemplateValidation().apply {
                    this.pattern = cString.pattern
                }
            }
            if (cString.list.isNotEmpty()) {
                input.list.addAll(cString.list.map(WebTemplateCodedValue.toCodedValue()))
            }
            input.listOpen = true == cString.listOpen
            input.fixed = cString.list.size == 1 && true != cString.listOpen
        }
        val dataValueClass = WebTemplateBuilderUtils.getDataValueClass(amNode)
        if (dataValueClass != null) {
            val defaultValue = WebTemplateBuilderUtils.getDefaultValue(amNode, dataValueClass)
            if (defaultValue != null) {
                if (defaultValue is DvIdentifier) {
                    input.defaultValue = "${defaultValue.issuer}::${defaultValue.assigner}::${defaultValue.id}::${defaultValue.type}"
                } else if (defaultValue is DvText) {
                    input.defaultValue = defaultValue.value.toString()
                }
            }
        }
        return input
    }

    override fun build(amNode: AmNode, validator: CString?, context: WebTemplateBuilderContext): WebTemplateInput = createTextInput(amNode, validator, null)

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val cString = getPrimitiveItem(node.amNode, CString::class.java, "value")
        node.inputs.add(build(node.amNode, cString, context))
    }
}
