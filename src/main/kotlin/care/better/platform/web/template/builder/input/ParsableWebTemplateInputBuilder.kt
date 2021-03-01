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
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.am.aom.CString
import org.openehr.rm.datatypes.DvParsable

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object ParsableWebTemplateInputBuilder : WebTemplateInputBuilder<Any> {

    override fun build(amNode: AmNode, validator: Any?, context: WebTemplateBuilderContext): WebTemplateInput = WebTemplateInput(WebTemplateInputType.TEXT)

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(node.amNode, DvParsable::class.java)
        node.inputs.add(createInput(node, "value", defaultValue?.value))
        node.inputs.add(createInput(node, "formalism", defaultValue?.formalism))
    }

    private fun createInput(node: WebTemplateNode, suffix: String, defaultValue: Any?): WebTemplateInput {
        val cString = getPrimitiveItem(node.amNode, CString::class.java, suffix)
        val input = StringWebTemplateInputBuilder.createTextInput(node.amNode, cString, suffix)
        if (defaultValue != null) {
            input.defaultValue = defaultValue
        }
        return input
    }
}
