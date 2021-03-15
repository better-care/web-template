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

import care.better.openehr.terminology.OpenEhrTerminology
import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils.findTermBindings
import care.better.platform.template.AmUtils.getCObjectItems
import care.better.platform.template.AmUtils.getPrimitiveItem
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.RmProperty
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.utils.CodePhraseUtils
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.apache.commons.lang3.StringUtils
import org.openehr.am.aom.CCodePhrase
import org.openehr.am.aom.CCodeReference
import org.openehr.am.aom.CString
import org.openehr.am.aom.TermBindingItem
import org.openehr.rm.datastructures.IntervalEvent
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText
import java.util.*
import java.util.regex.Pattern

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@Suppress("SpellCheckingInspection")
internal object CodePhraseWebTemplateInputBuilder : WebTemplateInputBuilder<CCodePhrase> {
    private val OPENEHR_CONCEPT_GROUPS = Collections.singletonMap(RmProperty(IntervalEvent::class.java, "math_function"), "event math function")
    private val WILDCARD_VALUE = Pattern.compile("[^:]+:\\*")

    override fun build(amNode: AmNode, validator: CCodePhrase?, context: WebTemplateBuilderContext): WebTemplateInput =
        with(WebTemplateInput(WebTemplateInputType.CODED_TEXT)) {
            if (validator == null) {
                try {
                    buildOpenEhrList(amNode, context, this)
                } catch (_: ClassNotFoundException) {
                }
            } else {
                val terminology = validator.terminologyId?.value!!
                val codes = getCodes(amNode, validator, context, terminology)
                if (codes.isEmpty()) {
                    this.terminology = terminology
                } else {
                    this.list.addAll(codes)
                    this.fixed = isFixed(this.list)
                }
                addDefaultValue(amNode, validator, this)
            }
            return this
        }


    override fun build(node: WebTemplateNode, context: WebTemplateBuilderContext) {
        val defaultValue = WebTemplateBuilderUtils.getDefaultValue(node.amNode, DvCodedText::class.java)

        val cCodePhrases: List<CCodePhrase> =
            if (node.amNode.cObject is CCodePhrase)
                listOf(node.amNode.cObject as CCodePhrase)
            else
                getCObjectItems(node.amNode, CCodePhrase::class.java, "defining_code")

        if (cCodePhrases.isNotEmpty()) {
            val cCodePhrase = cCodePhrases.iterator().next()
            if (cCodePhrase is CCodeReference) {
                buildCodeReference(node, cCodePhrase, defaultValue)
            } else {
                val terminology = cCodePhrase.terminologyId?.value!!
                val codes = getCodes(node.amNode, cCodePhrase, context, terminology)
                val assumedValue = cCodePhrase.assumedValue
                if (codes.isEmpty()) {
                    addExternalTerminologyInputs(node, terminology, defaultValue, assumedValue)
                } else {
                    val input = WebTemplateInput(WebTemplateInputType.CODED_TEXT, "code")
                    input.list.addAll(codes)
                    input.fixed = isFixed(input.list)
                    input.defaultValue = assumedValue?.codeString
                    if (defaultValue != null) {
                        input.defaultValue = defaultValue.definingCode?.codeString
                    }
                    if (StringUtils.isNotBlank(terminology) && "local" != terminology) {
                        input.terminology = terminology
                    }
                    node.inputs.add(input)
                }
            }
            if (cCodePhrases.size > 1) {
                addOtherExternalTerminologies(cCodePhrases.subList(1, cCodePhrases.size).toMutableList(), node.inputs)
            }
        } else {
            addExternalTerminologyInputs(node, null, defaultValue, null)
        }
        val cString = getPrimitiveItem(node.amNode, CString::class.java, "value")
        if (cString != null && true == cString.listOpen) {
            node.inputs.forEach {
                it.listOpen = true
            }
        }
    }

    private fun addOtherExternalTerminologies(cCodePhrases: MutableList<CCodePhrase>, inputs: MutableList<WebTemplateInput>) {
        cCodePhrases.forEach {
            if (it is CCodeReference) {
                getReferenceSetUri(it)?.let { uri ->
                    addOtherTerminologies(inputs, uri)
                }
            }
        }
    }

    private fun addOtherTerminologies(inputs: MutableList<WebTemplateInput>, referenceSetUri: String) {
        inputs.forEach { it.otherTerminologies.add(referenceSetUri) }
    }

    private fun buildCodeReference(node: WebTemplateNode, cCodeReference: CCodeReference, defaultValue: DvCodedText?) {
        getReferenceSetUri(cCodeReference)?.also {
            addExternalTerminologyInputs(node, it, defaultValue, cCodeReference.assumedValue)
        }
    }

    private fun getReferenceSetUri(cCodeReference: CCodeReference): String? =
        with(cCodeReference.referenceSetUri) {
            if (this != null && this.startsWith("terminology:")) this.substring(12) else this
        }

    private fun addExternalTerminologyInputs(node: WebTemplateNode, terminology: String?, defaultValue: DvCodedText?, assumedValue: CodePhrase?) {
        val codeInput = WebTemplateInput(WebTemplateInputType.TEXT, "code")
        codeInput.terminology = terminology
        if (defaultValue != null) {
            codeInput.defaultValue = defaultValue.definingCode!!.codeString
        } else if (assumedValue != null) {
            codeInput.defaultValue = assumedValue.codeString
        }
        node.inputs.add(codeInput)
        val valueInput = WebTemplateInput(WebTemplateInputType.TEXT, "value")
        valueInput.terminology = terminology
        if (defaultValue != null) {
            valueInput.defaultValue = defaultValue.value
        }
        node.inputs.add(valueInput)
    }

    private fun isFixed(values: List<WebTemplateCodedValue>): Boolean {
        if (values.size == 1) {
            val codedValue = values[0]
            if (!WILDCARD_VALUE.matcher(codedValue.value).matches()) {
                return true
            }
        }
        return false
    }

    private fun addDefaultValue(amNode: AmNode, codePhrase: CCodePhrase?, input: WebTemplateInput) {
        var defaultValue: WebTemplateCodedValue? = null
        if (codePhrase?.assumedValue != null) {
            for (codedValue in input.list) {
                if (codedValue.value == codePhrase.assumedValue!!.codeString) {
                    defaultValue = codedValue
                    break
                }
            }
            if (defaultValue == null) {
                defaultValue = WebTemplateCodedValue(codePhrase.assumedValue?.codeString!!, null)
            }
        }
        val codedValue = WebTemplateBuilderUtils.getDefaultValue(amNode, DvCodedText::class.java)
        if (codedValue != null) {
            defaultValue = WebTemplateCodedValue(codedValue.definingCode?.codeString!!, codedValue.value)
        }
        input.defaultValue = defaultValue
    }

    private fun getCodes(amNode: AmNode, codePhrase: CCodePhrase, context: WebTemplateBuilderContext, terminology: String): List<WebTemplateCodedValue> =
        with(ConvertToWebTemplateCodedValueFunction(terminology, amNode, context)) {
            codePhrase.codeList.asSequence().filterNotNull().map(this).toList()
        }

    private fun buildOpenEhrList(amNode: AmNode, context: WebTemplateBuilderContext, input: WebTemplateInput) {
        val rmClass = RmUtils.getRmClass(amNode.parent!!.rmType)
        val groupName = OPENEHR_CONCEPT_GROUPS[RmProperty(rmClass, amNode.name)]
        if (groupName != null) {
            OpenEhrTerminology.getInstance().getGroupChildren(groupName).forEach {
                input.list.add(WebTemplateCodedValue(it, CodePhraseUtils.getOpenEhrTerminologyText(it, context.defaultLanguage)))
            }
            input.fixed = input.list.size == 1
        }
    }

    class ConvertToWebTemplateCodedValueFunction constructor(
            private val terminology: String,
            private val amNode: AmNode,
            private val context: WebTemplateBuilderContext) : (String) -> WebTemplateCodedValue {
        override fun invoke(code: String): WebTemplateCodedValue {
            val codedValue = CodePhraseUtils.getCodedValue(terminology, code, amNode, context)

            val amTermBindings: Map<String, TermBindingItem> = findTermBindings(amNode, code)
            for ((key, value) in amTermBindings) {
                val bindingCodedValue = CodePhraseUtils.getBindingCodedValue(value)
                if (bindingCodedValue != null) {
                    codedValue.termBindings[key] = bindingCodedValue
                }
            }
            return codedValue
        }
    }
}
