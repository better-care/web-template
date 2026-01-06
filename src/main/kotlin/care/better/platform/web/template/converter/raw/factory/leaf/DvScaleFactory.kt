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
import org.openehr.am.aom.CDvScale
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvScale

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvOrderedFactory] that creates a new instance of [DvScale].
 */
internal object DvScaleFactory : DvOrderedFactory<DvScale>() {
    override fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvScale, webTemplateInput: WebTemplateInput) {
        val cDvScale = if (amNode.cObject is CDvScale) amNode.cObject as CDvScale else null
        cDvScale?.also {
            it.list.firstOrNull { scale -> webTemplateInput.list.isNotEmpty() && webTemplateInput.list[0].value == scale.symbol?.definingCode?.codeString }
                ?.also { scale ->
                    copyFromDvScale(conversionContext, amNode, rmObject, scale)
                }
        }
    }

    override fun createInstance(attributes: Set<AttributeDto>): DvScale = DvScale()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvScale,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) || run {
            val cDvScale = if (amNode.cObject is CDvScale) amNode.cObject as CDvScale else null
            if (cDvScale != null) {
                if (attribute.attribute.isBlank() || attribute.attribute == "value") {
                    handleValueAttribute(conversionContext, amNode, jsonNode, rmObject, cDvScale)
                    true
                } else if (attribute.attribute == "code") {
                    handleCodeAttribute(conversionContext, amNode, jsonNode, rmObject, cDvScale)
                    true
                } else {
                    false
                }
            } else {
                if (attribute.attribute == "scale") {
                    handleScaleAttributeIfNoConstraint(jsonNode, rmObject)
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
            rmObject: DvScale,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>): Boolean {
        val cDvScale = if (amNode.cObject is CDvScale) amNode.cObject as CDvScale else null
        if (cDvScale != null) {
            val dvScale = cDvScale.list.firstOrNull { attribute.attribute == it.symbol?.definingCode?.codeString }
            if (dvScale != null) {
                copyFromDvScale(conversionContext, amNode, rmObject, dvScale)
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
            rmObject: DvScale,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>,
            strictSearching: Boolean): Boolean =
        super.handleOnParent(conversionContext, amNode, attribute, jsonNode, rmObject, webTemplatePath, parents, false)

    private fun getInitializedSymbol(rmObject: DvScale): DvCodedText {
        rmObject.symbol = (rmObject.symbol ?: DvCodedText()).apply {
            definingCode = (definingCode ?: CodePhrase()).apply {
                terminologyId = (terminologyId ?: TerminologyId()).apply {
                    value = "external"
                }
            }
        }
        return rmObject.symbol!!
    }

    private fun handlePreferredTermAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvScale) {
        val symbol = getInitializedSymbol(rmObject)
        symbol.definingCode?.preferredTerm = jsonNode.asText()
    }

    private fun handleTerminologyIdAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvScale) {
        getInitializedSymbol(rmObject).definingCode?.terminologyId = TerminologyId().apply {
            this.value = jsonNode.asText()
        }
    }

    private fun handleCodeAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvScale) {
        val symbol = getInitializedSymbol(rmObject)
        symbol.definingCode?.codeString = jsonNode.asText()

    }

    private fun handleScaleAttributeIfNoConstraint(jsonNode: JsonNode, rmObject: DvScale) {
        if (jsonNode.isNumber) {
            rmObject.value = jsonNode.numberValue().toDouble()
        } else {
            val textValue = jsonNode.asText()

            try {
                rmObject.value = textValue.toDouble()
            } catch (ex: NumberFormatException) {
                throw ConversionException("Invalid value for attribute 'scale' on DV_SCALE: ${jsonNode.asText()}", ex)
            }
        }
    }

    /**
     * Sets values to [DvScale] from [JsonNode] "|value" entry value.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvScale]
     * @param cDvScale [CDvScale]
     */
    private fun handleValueAttribute(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvScale, cDvScale: CDvScale) {
        if (jsonNode.isNumber) {
            cDvScale.list.firstOrNull { jsonNode.numberValue().toDouble() == it.value }?.also {
                copyFromDvScale(conversionContext, amNode, rmObject, it)
            }
        } else {
            val textValue = jsonNode.asText()
            try {
                cDvScale.list.firstOrNull { textValue.toDouble() == it.value }?.also {
                    copyFromDvScale(conversionContext, amNode, rmObject, it)
                }
            } catch (ex: NumberFormatException) {
                if (textValue.contains("::")) {
                    cDvScale.list.firstOrNull { textValue.substring(0, textValue.indexOf("::")) == it.symbol?.definingCode?.codeString }?.also {
                        copyFromDvScale(conversionContext, amNode, rmObject, it)
                    }
                } else {
                    cDvScale.list.firstOrNull { textValue == it.symbol?.definingCode?.codeString }?.also {
                        copyFromDvScale(conversionContext, amNode, rmObject, it)
                    }
                }
            }
        }
    }

    /**
     * Sets values to [DvScale] from [JsonNode] "|code" entry value.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvScale]
     * @param cDvScale [CDvScale]
     */
    private fun handleCodeAttribute(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvScale, cDvScale: CDvScale) {
        cDvScale.list.firstOrNull { jsonNode.asText() == it.symbol?.definingCode?.codeString }?.also {
            copyFromDvScale(conversionContext, amNode, rmObject, it)
        }
    }

    /**
     * Copy values from [CDvScale] [DvScale] to [DvScale].
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param rmObject [DvScale]
     * @param dvScale [CDvScale] [DvScale]
     */
    private fun copyFromDvScale(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvScale, dvScale: DvScale) {
        rmObject.value = dvScale.value
        rmObject.symbol = dvScale.symbol?.let {
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
