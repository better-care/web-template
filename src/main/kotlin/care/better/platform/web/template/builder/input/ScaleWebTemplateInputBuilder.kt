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
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.WebTemplateScaleCodedValue
import care.better.platform.web.template.builder.utils.CodePhraseUtils
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.am.aom.CDvScale
import org.openehr.rm.datatypes.DvOrdinal

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object ScaleWebTemplateInputBuilder : WebTemplateInputBuilder<CDvScale> {
    override fun build(amNode: AmNode, validator: CDvScale?, context: WebTemplateBuilderContext): WebTemplateInput {
        val input = WebTemplateInput(WebTemplateInputType.CODED_TEXT)
        if (validator != null && validator.list.isNotEmpty()) {
            for (scale in validator.list) {
                val definingCode = scale.symbol?.definingCode
                val codedValue = WebTemplateScaleCodedValue(
                    CodePhraseUtils.getCodedValue(
                        definingCode?.terminologyId?.value!!,
                        definingCode.codeString!!,
                        amNode,
                        context))
                codedValue.scale = scale.value
                input.list.add(codedValue)
            }
            input.fixed = input.list.size == 1
            if (validator.assumedValue != null) {
                input.defaultValue = validator.assumedValue?.symbol?.definingCode?.codeString
            }
            val defaultValue = WebTemplateBuilderUtils.getDefaultValue(amNode, DvOrdinal::class.java)
            if (defaultValue != null) {
                input.defaultValue = defaultValue.symbol?.definingCode?.codeString
            }
        }
        return input
    }

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val cObject = node.amNode.cObject
        if (cObject is CDvScale) {
            node.inputs.add(build(node.amNode, cObject as CDvScale?, context))
        } else {
            node.inputs.add(build(node.amNode, null, context))
        }
    }
}
