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
import org.openehr.am.aom.CString

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
abstract class PartyProxyWebTemplateInputBuilder : WebTemplateInputBuilder<Any> {
    override fun build(amNode: AmNode, validator: Any?, context: WebTemplateBuilderContext): WebTemplateInput = WebTemplateInput(WebTemplateInputType.TEXT)

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        node.inputs.add(createInput(node, "id"))
        node.inputs.add(createInput(node, "id_scheme"))
        node.inputs.add(createInput(node, "id_namespace"))
    }

    protected open fun createInput(node: WebTemplateNode, suffix: String): WebTemplateInput {
        val cString = getPrimitiveItem(node.amNode, CString::class.java, suffix)
        return StringWebTemplateInputBuilder.createTextInput(node.amNode, cString, suffix)
    }
}
