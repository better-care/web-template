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
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.WebTemplateValidation
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.am.aom.CCodeReference
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object CodeReferenceWebTemplateInputBuilder : WebTemplateInputBuilder<CCodeReference> {
    override fun build(amNode: AmNode, validator: CCodeReference?, context: WebTemplateBuilderContext): WebTemplateInput {
        val input = WebTemplateInput(WebTemplateInputType.CODED_TEXT)
        if (validator?.referenceSetUri != null) {
            val validation = WebTemplateValidation()
            validation.pattern = validator.referenceSetUri
            input.validation = validation
        }
        addDefaultValue(amNode, input)
        return input
    }

    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        throw UnsupportedOperationException("This type not allowed!")
    }

    private fun addDefaultValue(amNode: AmNode, input: WebTemplateInput) {
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(amNode, DvCodedText::class.java)
        if (defaultValue != null) {
            input.defaultValue = WebTemplateCodedValue(defaultValue.definingCode?.codeString!!, defaultValue.value)
        }
    }
}
