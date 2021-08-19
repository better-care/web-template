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

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmAttribute
import care.better.platform.template.AmNode
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.getFieldNames
import care.better.platform.web.template.converter.mapper.isEmptyInDepth
import care.better.platform.web.template.converter.mapper.toSingletonReversed
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.isEmpty
import care.better.platform.web.template.converter.raw.postprocessor.PostProcessDelegator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.rm.datatypes.DataValue

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Base class for creating new RM object leaf node instances.
 */
internal abstract class RmObjectLeafNodeFactory<T : RmObject> {

    /**
     * Creates a RM object in RAW format from RM object in STRUCTURED format ([JsonNode]).
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param node RM object in STRUCTURED format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @param parents Parent created in [AmNode] chain before leaf node
     * @return RM object in RAW format
     */
    fun create(
            conversionContext: ConversionContext,
            amNode: AmNode,
            node: JsonNode,
            webTemplatePath: WebTemplatePath,
            parents: List<Any> = listOf()): T? =
        when {
            node.isArray -> throw ConversionException("RM object leaf node factory can not handle ArrayNode", webTemplatePath.toString())
            node.isTextual && (node as TextNode).asText().isNullOrBlank() -> null
            node.isNull || node.isMissingNode -> null
            node.isObject -> createForObjectNode(conversionContext, amNode, node as ObjectNode, webTemplatePath, parents)
            else -> createForValueNode(conversionContext, amNode, node as ValueNode, webTemplatePath)
        }?.let {
            PostProcessDelegator.delegate(conversionContext, amNode, it, webTemplatePath)
            if ((it as RmObject).isEmpty(conversionContext.strictMode)) null else it
        }

    /**
     * Creates a new instance of RM object in RAW format.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @return RM object in RAW format
     */
    fun create(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?) = createInstance()

    /**
     * Handles [WebTemplateInput] on RM object in RAW format.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param rmObject RM object in RAW format
     * @param webTemplateInput [WebTemplateInput]
     */
    open fun handleWebTemplateInput(conversionContext: ConversionContext, amNode: AmNode, rmObject: T, webTemplateInput: WebTemplateInput) {

    }

    /**
     * Creates a RM object in RAW format from RM object in STRUCTURED format ([ObjectNode]).
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param objectNode RM object in STRUCTURED format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @param parents Parent created in [AmNode] chain before leaf node
     * @return RM object in RAW format
     */
    private fun createForObjectNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            objectNode: ObjectNode,
            webTemplatePath: WebTemplatePath,
            parents: List<Any> = listOf()): T? {
        val objectNodeMap = getFilteredObjectNodeMap(objectNode, webTemplatePath, conversionContext.strictMode)
        return when {
            objectNodeMap.isEmpty() && conversionContext.strictMode -> createInstance(emptySet())
            objectNodeMap.isNotEmpty() -> {
                val instance: T = createInstance(objectNodeMap.keys)
                /*
                  In rare cases, data value leaf node contains only properties for the parent.
                  In that case it is faster to create node and then return null than to always calculate if object node contains only properties for the parent.
                 */
                var handledOnInstance = false

                objectNodeMap.forEach { (attribute, jsonNode) ->
                    val handled = try {
                        val fieldHandled = handleField(conversionContext, amNode, attribute, instance, jsonNode, webTemplatePath)
                        if (fieldHandled) {
                            true
                        } else {
                            handleRmAttribute(conversionContext, amNode, attribute, jsonNode, instance, webTemplatePath)
                        }
                    } catch (ex: Exception) {
                        throw getException(ex, jsonNode, webTemplatePath)
                    }

                    handledOnInstance = handledOnInstance || handled

                    if (!handled) {
                        if (instance is DataValue) {
                            val handledOnParent = handleOnParent(conversionContext, amNode, attribute, jsonNode, instance, webTemplatePath, parents)

                            if (!handledOnParent) { //Currently only for DvCodedText and DvOrdinal!
                                val handledAfterParent = handleAfterParent(conversionContext, amNode, attribute, jsonNode, instance, webTemplatePath, parents)
                                handledOnInstance = handledOnInstance || handledAfterParent
                            }
                        } else {
                            throw ConversionException("${amNode.rmType} has no attribute ${attribute.originalAttribute}", webTemplatePath.toString())
                        }
                    }
                }

                if (handledOnInstance) {
                    afterPropertiesSet(conversionContext, amNode, objectNode, instance)
                    instance
                } else {
                    null
                }
            }
            else -> null
        }
    }

    /**
     * Filters [ObjectNode] entries.
     *
     * @param objectNode [ObjectNode]
     * @param strict [Boolean] indicating if strict mode is enabled
     * @return [Map] of filtered [ObjectNode] entries
     */
    private fun getFilteredObjectNodeMap(
            objectNode: ObjectNode,
            webTemplatePath: WebTemplatePath = WebTemplatePath.forBlankPath(),
            strict: Boolean = false): Map<AttributeDto, JsonNode> =
        if (objectNode.isEmpty) {
            emptyMap()
        } else {
            val map = mutableMapOf<AttributeDto, JsonNode>()

            if (objectNode.has("") && objectNode.getFieldNames().size != 1) {
                if (strict) {
                    throw ConversionException("Object node with blank attribute name and multiple fields", webTemplatePath.toString())
                }
                map.remove(AttributeDto.ofBlank())
            }

            objectNode.fields().forEach {
                if (!it.key.startsWith("transient_")) {
                    if (strict && !it.value.isNull) {
                        map[AttributeDto(it.key, it.key.replace("|", ""))] = it.value
                    } else if (!it.value.isNull && !it.value.isMissingNode && !(it.value.isTextual && it.value.asText().isNullOrBlank())) {
                        map[AttributeDto(it.key, it.key.replace("|", ""))] = it.value
                    }
                }
            }

            if (!strict) {
                removeDependentValues(map)
            }

            map
        }


    /**
     * Sets RM attribute in STRUCTURED format to the RM object.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param attribute [JsonNode] entry key
     * @param jsonNode [JsonNode] entry value
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     */
    private fun handleRmAttribute(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            jsonNode: JsonNode,
            rmObject: T,
            webTemplatePath: WebTemplatePath): Boolean {
        val amAttribute = getAmAttribute(amNode, attribute.attribute)
        if (amAttribute == null || amAttribute.children.size > 1) {
            return false
        }
        val attributeAmNode = amAttribute.children[0]
        setRmAttribute(conversionContext, attributeAmNode, jsonNode, rmObject, webTemplatePath + attribute.originalAttribute)
        return true
    }


    /**
     * Sets RM attribute in STRUCTURED format to data value RM object parent.
     * Note that this method only sets RM attributes on direct parent.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param attribute [JsonNode] entry key
     * @param jsonNode [JsonNode] entry value
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @param parents Parent created in [AmNode] chain before leaf node
     * @param strictSearching [Boolean] indicating whether an error will be thrown if the attribute is not found on the parent [AmNode]
     */
    protected open fun handleOnParent(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            jsonNode: JsonNode,
            rmObject: T,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>,
            strictSearching: Boolean = true): Boolean {
        if (parents.isNotEmpty()) {
            val parent = parents.last()
            val amAttribute = getAmAttribute(amNode.parent, attribute.attribute)
            if (amAttribute == null || amAttribute.children.size > 1) {
                return when {
                    !strictSearching -> false
                    conversionContext.isStrictModeNotEnabled() && jsonNode.isEmptyInDepth() -> false
                    else -> throw ConversionException(
                        "${amNode.rmType} has no attribute ${attribute.originalAttribute}",
                        (webTemplatePath + attribute.originalAttribute).toString())
                }
            }

            val attributeAmNode = amAttribute.children[0]
            setRmAttribute(conversionContext, attributeAmNode, jsonNode, parent, webTemplatePath + attribute.originalAttribute)
        }
        return true
    }

    protected open fun handleAfterParent(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            jsonNode: JsonNode,
            rmObject: T,
            webTemplatePath: WebTemplatePath,
            parents: List<Any>): Boolean = false

    /**
     * Sets RM attribute in STRUCTURED format to the RM object.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param jsonNode [JsonNode] entry value
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     */
    @Suppress("UNCHECKED_CAST")
    private fun setRmAttribute(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: Any, webTemplatePath: WebTemplatePath) {
        if (amNode.isCollectionOnParent()) { //RM are always passed as Array. It is ok to set newly created collection to the parent.
            amNode.setOnParent(rmObject, convert(conversionContext, amNode, jsonNode, webTemplatePath) as MutableCollection<Any>)
        } else {
            val node = if (jsonNode.isArray) (jsonNode as ArrayNode).toSingletonReversed(conversionContext, webTemplatePath) else jsonNode
            amNode.setOnParent(rmObject, convert(conversionContext, amNode, node, webTemplatePath))
        }
    }

    /**
     * Converts the RM object in STRUCTURED format to RM object or to [Collection] of RM objects in RAW format for the RM attribute.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode}
     * @param jsonNode [JsonNode]
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @return RM object or [Collection] of RM objects in RAW format
     */
    private fun convert(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, webTemplatePath: WebTemplatePath): Any? =
        when {
            jsonNode.isArray -> {
                val collection = mutableListOf<Any>()
                jsonNode.forEachIndexed { index, node ->
                    convert(conversionContext, amNode, node, webTemplatePath.copy(amNode, index))?.also { collection.add(it) }
                }
                PostProcessDelegator.delegate(conversionContext, amNode, collection, webTemplatePath.copy(amNode))
                collection
            }
            jsonNode.isObject && jsonNode.has("|raw") -> ConversionObjectMapper.convertRawJsonNode(
                conversionContext,
                amNode,
                jsonNode,
                webTemplatePath.copy(amNode))
            RmUtils.isRmClass(amNode.getTypeOnParent().type) -> {
                RmObjectLeafNodeFactoryDelegator.delegateOrThrow(amNode.rmType, conversionContext, amNode, jsonNode, webTemplatePath.copy(amNode))
            }
            else -> ConversionObjectMapper.convertValue(jsonNode, amNode.getTypeOnParent().type)
        }


    /**
     * Retrieves [AmAttribute] from [AmNode].
     *
     * @param amNode [AmNode] from which [AmAttribute] are retrieved
     * @param key JSON entry key
     * @return [AmAttribute] if found, otherwise, return null
     */
    private fun getAmAttribute(amNode: AmNode?, key: String): AmAttribute? =
        if (amNode == null) {
            null
        } else {
            amNode.attributes[key]
                ?: if (key.startsWith("_")) with(key.substring(1)) { amNode.attributes[this] ?: amNode.attributes["${this}s"] } else null
        }


    /**
     * Creates a new RM object in RAW format from RM object in STRUCTURED format ([ValueNode]).
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param valueNode RM object in STRUCTURED format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @return RM object in RAW format
     */
    protected open fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): T =
        createInstance().apply {
            try {
                handleField(conversionContext, amNode, AttributeDto.ofBlank(), this, valueNode, webTemplatePath)
                afterPropertiesSet(conversionContext, amNode, valueNode, this)
            } catch (ex: Exception) {
                throw getException(ex, valueNode, webTemplatePath)
            }
        }

    /**
     * Returns [Exception] to throw.
     *
     * @param ex [Exception]
     * @param jsonNode [JsonNode]
     * @param webTemplatePath [WebTemplatePath]
     * @return [ConversionException]
     */
    private fun getException(ex: Exception, jsonNode: JsonNode, webTemplatePath: WebTemplatePath) =
        if (ex is ConversionException) {
            if (ex.getPath() == null) {
                ex.copyWithPath(webTemplatePath.toString())
            } else {
                ex
            }
        } else {
            ConversionException(
                "Error processing value: ${ConversionObjectMapper.writeValueAsString(jsonNode)}",
                ex,
                webTemplatePath.toString())
        }

    /**
     * Removes dependant values from map and returns true if some fields were removed.
     *
     * @param map RM object in STRUCTURED format converted to map
     */
    open fun removeDependentValues(map: MutableMap<AttributeDto, JsonNode>): Boolean {
        return false
    }

    /**
     * Creates a new instance of a RM object in RAW format.
     *
     * @param attributes Names of all fields of [ObjectNode]
     * @return RM object in RAW format
     */
    protected abstract fun createInstance(attributes: Set<AttributeDto> = setOf()): T

    /**
     * Used to set missing default data after all [ObjectNode] entries have been set to the RM object.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param jsonNode [ObjectNode] in STRUCTURED format
     * @param rmObject RM object in RAW format
     */
    protected open fun afterPropertiesSet(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: T) {

    }

    /**
     * Handles [ObjectNode] entry on the RM object in RAW format.
     *
     * @param amNode [AmNode]
     * @param conversionContext [ConversionContext]
     * @param attribute [ObjectNode] entry key as [AttributeDto]
     * @param rmObject RM object in RAW format
     * @param jsonNode [ObjectNode] entry value
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     */
    abstract fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: T,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean

    /**
     * Sorts field names.
     *
     * @param attributes [List] of [ObjectNode] field names
     * @return [List] of sorted [ObjectNode] field names
     */
    protected open fun sortFieldNames(attributes: List<AttributeDto>): List<AttributeDto> = attributes

    open fun canRemoveDependantValues(): Boolean = false

    /**
     * Checks if [ObjectNode] in STRUCTURED format is empty or not
     *
     * @param objectNode [ObjectNode] in STRUCTURED format
     *  @return Boolean indicating if [ObjectNode] is empty or not.
     */
    fun isStructuredRmObjectEmpty(objectNode: ObjectNode) = objectNode.isEmpty || getFilteredObjectNodeMap(objectNode).isEmpty()
}
