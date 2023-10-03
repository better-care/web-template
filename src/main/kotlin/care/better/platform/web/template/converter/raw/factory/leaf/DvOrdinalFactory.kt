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
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.am.aom.CDvOrdinal
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvOrdinal

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvOrderedFactory] that creates a new instance of [DvOrdinal].
 */
internal object DvOrdinalFactory : DvOrderedFactory<DvOrdinal>() {

    override fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvOrdinal, webTemplateInput: WebTemplateInput) {
        val cDvOrdinal = if (amNode.cObject is CDvOrdinal) amNode.cObject as CDvOrdinal else null
        cDvOrdinal?.also {
            it.list.firstOrNull { ordinal -> webTemplateInput.list.isNotEmpty() && webTemplateInput.list[0].value == ordinal.symbol?.definingCode?.codeString }
                ?.also { ordinal ->
                    copyFromDvOrdinal(conversionContext, amNode, rmObject, ordinal)
                }
        }
    }

    override fun createInstance(attributes: Set<AttributeDto>): DvOrdinal = DvOrdinal()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvOrdinal,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) || run {
            val cDvOrdinal = if (amNode.cObject is CDvOrdinal) amNode.cObject as CDvOrdinal else null
            if (cDvOrdinal != null) {
                if (attribute.attribute.isBlank() || attribute.attribute == "value") {
                    handleValueAttribute(conversionContext, amNode, jsonNode, rmObject, cDvOrdinal)
                    true
                } else if (attribute.attribute == "code") {
                    handleCodeAttribute(conversionContext, amNode, jsonNode, rmObject, cDvOrdinal)
                    true
                } else {
                    false
                }
            } else {
                if (attribute.attribute == "ordinal") {
                    handleOrdinalAttributeIfNoConstraint(jsonNode, rmObject)
                    true
                } else if (attribute.attribute == "code") {
                    handleCodeAttributeIfNoConstraint(jsonNode, rmObject)
                    true
                } else if (attribute.attribute.isBlank() || attribute.attribute == "value") {
                    getInitializedSymbol(rmObject).value = jsonNode.asText()
                    true
                } else if (attribute.attribute == "preferred_term") {
                    handlePreferredTermAttributeIfNoConstraint(jsonNode, rmObject)
                    true
                } else if (attribute.attribute == "terminology") {
                    handleTerminologyIdAttributeIfNoConstraint(jsonNode, rmObject)
                    true
                } else {
                    false
                }
            }
        }

    override fun handleAfterParent(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            jsonNode: JsonNode,
            rmObject: DvOrdinal,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>): Boolean {
        val cDvOrdinal = if (amNode.cObject is CDvOrdinal) amNode.cObject as CDvOrdinal else null
        if (cDvOrdinal != null) {
            val dvOrdinal = cDvOrdinal.list.firstOrNull { attribute.attribute == it.symbol?.definingCode?.codeString }
            if (dvOrdinal != null) {
                copyFromDvOrdinal(conversionContext, amNode, rmObject, dvOrdinal)
                return true
            }
        }
        return false
    }

    override fun handleOnParent(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            jsonNode: JsonNode,
            rmObject: DvOrdinal,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>,
            strictSearching: Boolean): Boolean =
        super.handleOnParent(conversionContext, amNode, attribute, jsonNode, rmObject, webTemplatePath, parents, false)

    private fun getInitializedSymbol(rmObject: DvOrdinal): DvCodedText {
        rmObject.symbol = (rmObject.symbol ?: DvCodedText()).apply {
            definingCode = (definingCode ?: CodePhrase()).apply {
                terminologyId = (terminologyId ?: TerminologyId()).apply {
                    value = "external"
                }
            }
        }
        return rmObject.symbol!!
    }

    private fun handlePreferredTermAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvOrdinal) {
        getInitializedSymbol(rmObject).definingCode = CodePhrase().apply {
            this.preferredTerm = jsonNode.asText()
        }
    }

    private fun handleTerminologyIdAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvOrdinal) {
        getInitializedSymbol(rmObject).definingCode?.terminologyId = TerminologyId().apply {
            this.value = jsonNode.asText()
        }
    }

    private fun handleCodeAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvOrdinal) {
        getInitializedSymbol(rmObject).definingCode = CodePhrase().apply {
            this.codeString = jsonNode.asText()
        }
    }

    private fun handleOrdinalAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvOrdinal) {
        if (jsonNode.isNumber) {
            rmObject.value = jsonNode.numberValue().toInt()
        } else {
            val textValue = jsonNode.asText()

            try {
                rmObject.value = textValue.toInt()
            } catch (ex: NumberFormatException) {
                throw ConversionException("Invalid value for attribute 'ordinal' on DV_ORDINAL: ${jsonNode.asText()}", ex)
            }
        }
    }

    /**
     * Sets values to [DvOrdinal] from [JsonNode] "|value" entry value.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvOrdinal]
     * @param cDvOrdinal [CDvOrdinal]
     */
    private fun handleValueAttribute(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvOrdinal, cDvOrdinal: CDvOrdinal) {
        if (jsonNode.isNumber) {
            cDvOrdinal.list.firstOrNull { jsonNode.numberValue().toInt() == it.value }?.also {
                copyFromDvOrdinal(conversionContext, amNode, rmObject, it)
            }
        } else {
            val textValue = jsonNode.asText()
            try {
                cDvOrdinal.list.firstOrNull { textValue.toInt() == it.value }?.also {
                    copyFromDvOrdinal(conversionContext, amNode, rmObject, it)
                }
            } catch (ex: NumberFormatException) {
                if (textValue.contains("::")) {
                    cDvOrdinal.list.firstOrNull { textValue.substring(0, textValue.indexOf("::")) == it.symbol?.definingCode?.codeString }?.also {
                        copyFromDvOrdinal(conversionContext, amNode, rmObject, it)
                    }
                } else {
                    cDvOrdinal.list.firstOrNull { textValue == it.symbol?.definingCode?.codeString }?.also {
                        copyFromDvOrdinal(conversionContext, amNode, rmObject, it)
                    }
                }
            }
        }
    }

    /**
     * Sets values to [DvOrdinal] from [JsonNode] "|code" entry value.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvOrdinal]
     * @param cDvOrdinal [CDvOrdinal]
     */
    private fun handleCodeAttribute(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvOrdinal, cDvOrdinal: CDvOrdinal) {
        cDvOrdinal.list.firstOrNull { jsonNode.asText() == it.symbol?.definingCode?.codeString }?.also {
            copyFromDvOrdinal(conversionContext, amNode, rmObject, it)
        }
    }

    /**
     * Copy values from [CDvOrdinal] [DvOrdinal] to [DvOrdinal].
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param rmObject [DvOrdinal]
     * @param dvOrdinal [CDvOrdinal] [DvOrdinal]
     */
    private fun copyFromDvOrdinal(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvOrdinal, dvOrdinal: DvOrdinal) {
        rmObject.value = dvOrdinal.value
        rmObject.symbol = dvOrdinal.symbol?.let {
            DvCodedText().apply {

                if (it.definingCode?.codeString != null && it.value.isNullOrBlank()) {
                    this.value =
                        WebTemplateConversionUtils.getTermText(
                                amNode,
                                it.definingCode?.terminologyId?.value,
                                it.definingCode?.codeString,
                                conversionContext.language)
                } else {
                    this.value = it.value
                }

                this.definingCode = it.definingCode?.let { codePhrase ->
                    CodePhrase().apply {
                        this.terminologyId = codePhrase.terminologyId
                        this.codeString = codePhrase.codeString
                    }
                }
            }
        }
    }
}
