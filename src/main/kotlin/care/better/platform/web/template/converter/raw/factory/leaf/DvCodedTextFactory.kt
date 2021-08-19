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
import care.better.platform.template.AmUtils
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.isForElement
import care.better.platform.web.template.converter.raw.extensions.isNotEmpty
import care.better.platform.web.template.converter.raw.factory.node.RmObjectNodeFactoryDelegator
import care.better.platform.web.template.converter.raw.postprocessor.PostProcessDelegator
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.am.aom.CCodePhrase
import org.openehr.am.aom.CCodeReference
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemList
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText
import org.openehr.rm.datatypes.TermMapping
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [RmObjectLeafNodeFactory] that creates a new instance of [DvCodedText].
 *
 * @constructor Creates a new instance of [DvCountFactory]
 */
internal open class DvCodedTextFactory : RmObjectLeafNodeFactory<DvCodedText>() {

    companion object {
        private val TERMINOLOGY_PATTERN = Pattern.compile("^terminology:([^?/(]+).*$")

        private val DELIMITER_PATTERN = Pattern.compile("::", Pattern.LITERAL)

        private val INSTANCE: DvCodedTextFactory = DvCodedTextFactory()

        @JvmStatic
        fun getInstance(): DvCodedTextFactory = INSTANCE
    }

    private val sortMap: Map<AttributeDto, Int> =
        mapOf(
            Pair(AttributeDto.ofBlank(), 0),
            Pair(AttributeDto.forAttribute("value"), 1),
            Pair(AttributeDto.forAttribute("code"), 2),
            Pair(AttributeDto.forAttribute("terminology"), 3),
            Pair(AttributeDto.forAttribute("preferred_term"), 4),
            Pair(AttributeDto.forAttribute("_mapping"), 5),
            Pair(AttributeDto.forAttribute("other"), 6))

    override fun sortFieldNames(attributes: List<AttributeDto>): List<AttributeDto> =
        attributes.asSequence().map { Pair(it, sortMap[it] ?: Integer.MAX_VALUE) }.sortedBy { it.second }.map { it.first }.toList()

    override fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvCodedText, webTemplateInput: WebTemplateInput) {
        if (webTemplateInput.list.isNotEmpty()) {
            handleDvCodedTextString(conversionContext, amNode, rmObject, webTemplateInput.list[0].value)
        }
    }

    override fun createInstance(attributes: Set<AttributeDto>): DvCodedText = DvCodedText()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvCodedText,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        getFactory(amNode).handleDvCodedTextField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath)


    protected open fun handleDvCodedTextField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvCodedText,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        when {
            attribute.attribute.isBlank() -> {
                handleBlankAttribute(conversionContext, amNode, jsonNode, rmObject)
                true
            }
            attribute.attribute == "value" -> {
                handleValueAttribute(amNode, jsonNode, rmObject)
                true
            }
            attribute.attribute == "terminology" -> {
                handleTerminologyAttribute(jsonNode, rmObject)
                true
            }
            attribute.attribute == "code" -> {
                handleDvCodedTextString(conversionContext, amNode, rmObject, jsonNode.asText())
                true
            }
            attribute.attribute == "preferred_term" -> {
                handlePreferredTermAttribute(jsonNode, rmObject)
                true
            }
            attribute.attribute == "_mapping" -> {
                rmObject.mappings = jsonNode.mapIndexedNotNull { index, node ->
                    TermMappingFactory.create(conversionContext, amNode, node, WebTemplatePath(attribute.originalAttribute, webTemplatePath, index))
                }.toMutableList()
                true
            }
            else -> false
        }

    /**
     * Returns [DvCodedTextFactory] for [AmNode].
     *
     * @param amNode [AmNode]
     * @return [DvCodedTextFactory]
     */
    private fun getFactory(amNode: AmNode): DvCodedTextFactory =
        when {
            isIsmTransitionNodeChild(amNode) -> IsmTransitionDvCodedTextFactory
            isNullFlavorNode(amNode) -> NullFlavorDvCodedTextFactory
            else -> this
        }

    override fun afterPropertiesSet(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvCodedText) {
        getFactory(amNode).afterPropertiesSet(amNode, rmObject)
    }

    protected open fun afterPropertiesSet(amNode: AmNode, rmObject: DvCodedText) {

    }


    override fun handleAfterParent(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            jsonNode: JsonNode,
            rmObject: DvCodedText,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>): Boolean {
        if (attribute.attribute == "other") {
            val dvText = DvText(jsonNode.asText())
            PostProcessDelegator.delegate(conversionContext, amNode, dvText, webTemplatePath + attribute.originalAttribute)
            if (dvText.isNotEmpty()) {
                addToParent(conversionContext, amNode, parents, DvText(jsonNode.asText()), webTemplatePath + attribute.originalAttribute)
            }
            return false
        } else {
            return if (rmObject.definingCode?.codeString != null && rmObject.definingCode?.codeString != attribute.attribute) {
                val dvCodedText = DvCodedText().apply { handleDvCodedTextString(conversionContext, amNode, this, attribute.attribute) }
                PostProcessDelegator.delegate(conversionContext, amNode, dvCodedText, webTemplatePath + attribute.originalAttribute)
                if (dvCodedText.isNotEmpty()) {
                    addToParent(conversionContext, amNode, parents, dvCodedText, webTemplatePath + attribute.originalAttribute)
                }
                false
            } else {
                handleDvCodedTextString(conversionContext, amNode, rmObject, attribute.attribute)
                true
            }
        }
    }

    override fun handleOnParent(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            jsonNode: JsonNode,
            rmObject: DvCodedText,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>,
            strictSearching: Boolean): Boolean =
        super.handleOnParent(conversionContext, amNode, attribute, jsonNode, rmObject, webTemplatePath, parents, false)

    override fun removeDependentValues(map: MutableMap<AttributeDto, JsonNode>): Boolean {
        val codeNode = map[AttributeDto.forAttribute("code")]
        if (codeNode != null) {
            val codeAttribute = AttributeDto.forAttribute(codeNode.asText())
            if (map[codeAttribute] != null) {
                map.remove(codeAttribute)
                return true
            }
        }
        return false
    }

    /**
     * Sets value to [DvCodedText] from [JsonNode] "" entry value.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvCodedText]
     */
    protected open fun handleBlankAttribute(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvCodedText) {
        handleDvCodedTextString(conversionContext, amNode, rmObject, jsonNode.asText())
    }

    /**
     * Sets value to [DvCodedText] from [JsonNode] "|value" entry value.
     *
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvCodedText]
     */
    protected open fun handleValueAttribute(amNode: AmNode, jsonNode: JsonNode, rmObject: DvCodedText) {
        rmObject.value = jsonNode.asText()
    }

    /**
     * Sets preferred_term to [DvCodedText] from [JsonNode] "|preferred_term" entry value.
     *
     * @param jsonNode [JsonNode]
     * @param rmObject [DvCodedText]
     */
    protected open fun handlePreferredTermAttribute(jsonNode: JsonNode, rmObject: DvCodedText) {
        if (rmObject.definingCode == null) {
            rmObject.definingCode = CodePhrase()
        }

        rmObject.definingCode.also {
            if (it == null) {
                rmObject.definingCode = CodePhrase().apply {
                    this.preferredTerm = jsonNode.asText()
                }
            } else {
                it.preferredTerm = jsonNode.asText()
            }
        }
    }

    /**
     * Sets terminology ID to [DvCodedText] from [JsonNode] "|terminology" entry value.
     *
     * @param jsonNode [JsonNode]
     * @param rmObject [DvCodedText]
     */
    private fun handleTerminologyAttribute(jsonNode: JsonNode, rmObject: DvCodedText) {
        rmObject.definingCode.also { codePhrase ->
            if (codePhrase == null) {
                rmObject.definingCode = CodePhrase().apply {
                    this.terminologyId = TerminologyId().apply { this.value = jsonNode.asText() }
                }
            } else {
                codePhrase.terminologyId.also { terminology ->
                    if (terminology == null) {
                        codePhrase.terminologyId = TerminologyId().apply { this.value = jsonNode.asText() }
                    } else {
                        terminology.value = jsonNode.asText()
                    }
                }
            }
        }
    }

    /**
     * Sets values from [DvCodedText] string (code::value::terminology).
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param rmObject [DvCodedText]
     * @param valueString [DvCodedText] string
     */
    protected open fun handleDvCodedTextString(conversionContext: ConversionContext, amNode: AmNode, rmObject: DvCodedText, valueString: String) {
        val (code, value, terminologyId) = getCodedTextValues(valueString)

        val codePhrase: CodePhrase =
            if (rmObject.definingCode == null)
                CodePhrase().also { rmObject.definingCode = it }
            else
                rmObject.definingCode as CodePhrase

        codePhrase.codeString = code
        handleDvCodedTextStringTerminology(amNode, codePhrase, terminologyId)

        if (rmObject.value == null) {
            rmObject.value = value
                ?: WebTemplateConversionUtils.getTermText(
                    amNode,
                    codePhrase.terminologyId?.value,
                    rmObject.definingCode?.codeString,
                    conversionContext.language)
        }

        if (conversionContext.termBindingTerminologies.isNotEmpty()) {
            amNode.getTermBindings(code)
                .filter { conversionContext.termBindingTerminologies.contains("*") || conversionContext.termBindingTerminologies.contains(it.key) }
                .forEach {
                    rmObject.mappings.add(createTermMapping(it.key, it.value))
                }
        }
    }

    /**
     * Sets terminology to [CodePhrase] [TerminologyId].
     *
     * @param amNode [AmNode]
     * @param codePhrase [CodePhrase]
     * @param terminologyId Terminology ID
     */
    protected open fun handleDvCodedTextStringTerminology(amNode: AmNode, codePhrase: CodePhrase, terminologyId: String?) {
        if (codePhrase.terminologyId == null) {
            if (terminologyId == null) {
                codePhrase.terminologyId = getTerminologyIdFromAnNode(amNode)
            } else {
                codePhrase.terminologyId = TerminologyId().apply { this.value = terminologyId }
            }
        }
    }

    /**
     * Retrieves [TerminologyId] from [AmNode] or returns [TerminologyId] with external value.
     *
     * @param amNode [AmNode]
     * @return New instance of [TerminologyId]
     */
    protected open fun getTerminologyIdFromAnNode(amNode: AmNode): TerminologyId {
        val cCodePhrase =
            if (amNode.cObject is CCodePhrase)
                amNode.cObject as CCodePhrase
            else
                AmUtils.getCObjectItem(amNode, CCodePhrase::class.java, "defining_code")

        return TerminologyId().apply {
            if (cCodePhrase?.terminologyId != null) {
                this.value = cCodePhrase.terminologyId?.value
            } else if (cCodePhrase is CCodeReference) {
                cCodePhrase.referenceSetUri?.also {
                    val matcher = TERMINOLOGY_PATTERN.matcher(it)
                    if (matcher.matches()) {
                        this.value = matcher.group(1)
                    }
                }
            }

            if (this.value == null) {
                this.value = getDefaultTerminology()
            }
        }
    }

    /**
     * Return [Triple] of [DvCodedText] code, value and terminology created from [DvCodedText] string (terminology::code::value, code::value, code).
     *
     * @param dvCodedTextString [DvCodedText] string (terminology::code::value, code::value, code)
     * @return [Triple] of [DvCodedText] code, value and terminology
     */
    private fun getCodedTextValues(dvCodedTextString: String): Triple<String, String?, String?> =
        with(DELIMITER_PATTERN.split(dvCodedTextString)) {
            when {
                this.size > 2 -> Triple(this[1], this[2], this[0])
                this.size > 1 -> Triple(this[0], this[1], null)
                else -> Triple(this[0], null, null)
            }
        }

    protected open fun getDefaultTerminology(): String = "external"

    /**
     * Creates a new instance of [TermMapping] for code and terminology ID.
     *
     * @param code Code
     * @param terminologyId Terminology ID
     */
    private fun createTermMapping(terminologyId: String, code: String): TermMapping =
        TermMapping().apply {
            this.match = "="
            this.target = CodePhrase().apply {
                this.terminologyId = TerminologyId().apply {
                    this.value = terminologyId
                }
                this.codeString = code
            }
        }

    /**
     * Checks if [AmNode] parent has an ISM_TRANSITION RM type.
     *
     * @param amNode [AmNode]
     * @return [Boolean] indicating if [AmNode] parent has an ISM_TRANSITION RM type or not
     */
    private fun isIsmTransitionNodeChild(amNode: AmNode): Boolean = amNode.parent != null && "ISM_TRANSITION" == amNode.parent?.rmType

    /**
     * Checks if [AmNode] is for null_flavor RM attribute.
     *
     * @param amNode [AmNode]
     * @return [Boolean] indicating if [AmNode] is for null_flavor RM attribute or not
     */
    private fun isNullFlavorNode(amNode: AmNode): Boolean = amNode.name == "null_flavour"

    /**
     * Add new [DvText] or [DvCodedText] instance to the parent with multiple [Element] instances.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param parents Parents created in [AmNode] chain before leaf node
     * @param dvText A new [DvText] or [DvCodedText] instance
     * @param webTemplatePath Web template path from the root to the current node [WebTemplatePath]
     */
    @Suppress("UNCHECKED_CAST", "DuplicatedCode")
    private fun addToParent(
            conversionContext: ConversionContext,
            amNode: AmNode,
            parents: List<Any>,
            dvText: DvText,
            webTemplatePath: WebTemplatePath): Boolean {
        if (parents.size >= 2) {
            val lastParentWithCollection = parents.lastOrNull { it is MutableCollection<*> || it is ItemTree || it is ItemList }
            val lastParent = parents.last()
            if (lastParent::class.java == Element::class.java) {
                when (lastParentWithCollection) {
                    is ItemTree -> {
                        val elementAmNode =
                            getElementAmNode(amNode) ?: throw ConversionException("AM node for the ELEMENT not found", webTemplatePath.toString())
                        addToCollection(
                            conversionContext,
                            elementAmNode,
                            lastParentWithCollection.items as MutableCollection<Any>,
                            {
                                RmObjectNodeFactoryDelegator.delegateOrThrow(
                                    RmUtils.getRmTypeName(Element::class.java),
                                    conversionContext,
                                    elementAmNode,
                                    webTemplatePath) as Element
                            },
                            dvText,
                            webTemplatePath)
                        return true
                    }
                    is ItemList -> {
                        val elementAmNode =
                            getElementAmNode(amNode) ?: throw ConversionException("AM node for the ELEMENT not found", webTemplatePath.toString())
                        addToCollection(
                            conversionContext,
                            elementAmNode,
                            lastParentWithCollection.items as MutableCollection<Any>,
                            {
                                RmObjectNodeFactoryDelegator.delegateOrThrow(
                                    RmUtils.getRmTypeName(Element::class.java),
                                    conversionContext,
                                    elementAmNode,
                                    webTemplatePath) as Element
                            },
                            dvText,
                            webTemplatePath)
                        return true
                    }
                    is MutableCollection<*> -> {
                        val elementAmNode =
                            getElementAmNode(amNode) ?: throw ConversionException("AM node for the ELEMENT not found", webTemplatePath.toString())
                        addToCollection(
                            conversionContext,
                            elementAmNode,
                            lastParentWithCollection as MutableCollection<Any>,
                            {
                                RmObjectNodeFactoryDelegator.delegateOrThrow(
                                    RmUtils.getRmTypeName(Element::class.java),
                                    conversionContext,
                                    elementAmNode,
                                    webTemplatePath) as Element
                            },
                            dvText,
                            webTemplatePath)
                        return true
                    }
                    else -> return false
                }
            }
        }
        return false
    }

    /**
     * Returns [Element]  [AmNode].
     *
     * @param amNode [DvCodedText] [AmNode]
     * @return [Element]  [AmNode
     */
    private fun getElementAmNode(amNode: AmNode?): AmNode? =
        when {
            amNode == null -> null
            amNode.isForElement() -> amNode
            else -> getElementAmNode(amNode.parent)
        }

    /**
     * Creates [Element], sets values to it, post-processes it and adds it to the [MutableCollection].
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param mutableCollection [MutableCollection]
     * @param elementFactory Factory that creates a new instance of [Element]
     * @param dvText A new [DvText] or [DvCodedText] instance
     * @param webTemplatePath Web template path from the root to the current node [WebTemplatePath]
     */
    private fun addToCollection(
            conversionContext: ConversionContext,
            amNode: AmNode,
            mutableCollection: MutableCollection<Any>,
            elementFactory: () -> Element,
            dvText: DvText,
            webTemplatePath: WebTemplatePath) {
        val element = elementFactory.invoke()
        element.value = dvText
        PostProcessDelegator.delegate(conversionContext, amNode, element, webTemplatePath)

        if (element.isNotEmpty()) {
            mutableCollection.add(element)
        }
    }
}
