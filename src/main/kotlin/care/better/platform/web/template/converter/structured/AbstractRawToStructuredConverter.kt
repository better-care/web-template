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

package care.better.platform.web.template.converter.structured

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.utils.RmUtils
import care.better.platform.utils.exception.RmClassCastException
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.FromRawConversion
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.structured.mapper.RmObjectToStructuredMapperDelegator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Sets
import org.openehr.rm.common.Locatable
import org.openehr.rm.composition.Composition
import org.openehr.rm.datatypes.*
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Base class that converts the RM object in RAW format to the RM object in STRUCTURED format.
 */
internal abstract class AbstractRawToStructuredConverter(private val objectMapper: ObjectMapper) {

    private val exported = Sets.newIdentityHashSet<Any>()

    /**
     * Converts the RM object in RAW format to the RM object in STRUCTURED format.
     *
     * @param webTemplate [WebTemplate]
     * @param fromRawConversion [FromRawConversion]
     * @param rmObject RM object in RAW format
     * @return RM object in STRUCTURED format
     */
    fun <R : RmObject> convert(webTemplate: WebTemplate, fromRawConversion: FromRawConversion, rmObject: R): JsonNode? =
        if (fromRawConversion.aqlPath.isNullOrBlank() && fromRawConversion.webTemplatePath.isNullOrBlank()) {
            if (rmObject is Composition) {
                convert(webTemplate, rmObject)
            } else {
                throw ConversionException("RM object must be ${webTemplate.tree.rmType} when web template or AQL path is not provided.")
            }
        } else {
            if (fromRawConversion.webTemplatePath.isNullOrBlank())
                convertForAqlPath(webTemplate, fromRawConversion.aqlPath!!, rmObject)
            else
                convertForWebTemplatePath(webTemplate, fromRawConversion.webTemplatePath, rmObject)
        }

    /**
     * Converts the RM composition object in RAW format to the RM composition object in STRUCTURED format.
     *
     * @param webTemplate [WebTemplate]
     * @param composition RM composition object
     * @return RM composition object in STRUCTURED format
     */
    fun convert(webTemplate: WebTemplate, composition: Composition): JsonNode? =
        map(webTemplate.tree, composition)?.apply { setGenericFields(webTemplate.tree, composition, this) }

    /**
     * Converts the RM object in RAW format to the RM object in STRUCTURED format for the AQL path.
     *
     * @param webTemplate [WebTemplate]
     * @param aqlPath AQL path to the RM object
     * @param rmObject RM object in RAW format
     * @return RM object in STRUCTURED format
     */
    fun <R : RmObject> convertForAqlPath(webTemplate: WebTemplate, aqlPath: String, rmObject: R): JsonNode? =
        map(webTemplate.findWebTemplateNodeByAqlPath(aqlPath), rmObject)

    /**
     * Converts the RM object in RAW format to the RM object in STRUCTURED format for the web template path.
     *
     * @param webTemplate [WebTemplate]
     * @param webTemplatePath Web template path to the RM object
     * @param rmObject RM object in RAW format
     * @return RM object in STRUCTURED format
     */
    fun <R : RmObject> convertForWebTemplatePath(webTemplate: WebTemplate, webTemplatePath: String, rmObject: R): JsonNode? =
        map(webTemplate.findWebTemplateNode(webTemplatePath), rmObject)

    /**
     * Maps the RM object in RAW format to the STRUCTURED format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObject RM object in RAW format
     */
    abstract fun <R : RmObject> mapRmObject(webTemplateNode: WebTemplateNode, rmObject: R): JsonNode?

    /**
     * Maps the RM object in RAW format to the STRUCTURED format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObject RM object in RAW format
     */
    fun <R : RmObject> mapRmObjectInternally(webTemplateNode: WebTemplateNode, rmObject: R): JsonNode? {
        if (!exported.contains(rmObject)) {
            return mapRmObject(webTemplateNode, rmObject)
        }
        return null
    }

    /**
     * Maps the RM object in RAW format to the RM object in STRUCTURED format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObject RM object in RAW format
     */
    protected open fun map(webTemplateNode: WebTemplateNode, rmObject: RmObject): ObjectNode? {
        val objectNode = objectMapper.createObjectNode()
        val jsonNode = mapRmObjectInternally(webTemplateNode, rmObject) ?: return null

        objectNode.set<ObjectNode>(webTemplateNode.jsonId, jsonNode)

        if (rmObject is DvInterval) {
            return objectNode
        }

        webTemplateNode.children.forEach { child ->
            val nodes = mapInChain(child, child.chain, rmObject)
            if (nodes.isNotEmpty()) {
                val arrayNode = objectMapper.createArrayNode()
                nodes.forEach { arrayNode.add(it) }
                (jsonNode as ObjectNode).set<ArrayNode>(child.jsonId, arrayNode)
            }
        }

        return objectNode
    }

    /**
     * Recursively maps the RM object in RAW format to the RM object in STRUCTURED format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObject RM object in RAW format
     */
    private fun mapRecursive(webTemplateNode: WebTemplateNode, rmObject: RmObject): JsonNode? {
        val jsonNode = mapRmObjectInternally(webTemplateNode, rmObject) ?: objectMapper.createObjectNode()

        if (rmObject is DvInterval) {
            return jsonNode
        }
        webTemplateNode.children.forEach { child ->
            val nodes = mapInChain(child, child.chain, rmObject)
            if (nodes.isNotEmpty()) {
                val arrayNode = objectMapper.createArrayNode()
                arrayNode.addAll(nodes)
                (jsonNode as ObjectNode).set<ArrayNode>(child.jsonId, arrayNode)
            }
        }
        return jsonNode
    }

    /**
     * Maps the RM object in RAW format to the RM object in STRUCTURED format.
     *
     * Note that some nodes are not presented in [WebTemplateNode] (ITEM_STRUCTURE for example). This function maps those elements as well.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param chain [List] of [AmNode] from parent [WebTemplateNode] [AmNode] to child [WebTemplateNode] [AmNode] (parent is excluded)
     * @param rmObject RM objects in RAW format
     */
    @Suppress("UNCHECKED_CAST")
    private fun mapInChain(webTemplateNode: WebTemplateNode, chain: List<AmNode>, rmObject: RmObject): List<JsonNode> {
        val currentAmNode = chain.first()

        if (chain.size == 1) {
            return getMatchingRmObjects(webTemplateNode, rmObject, currentAmNode).asSequence()
                .mapNotNull { mapRecursive(webTemplateNode, it) }
                .filter { (it.isObject && !it.isEmpty) || !it.isObject }
                .toList()
        }

        val newChain = chain.drop(1)
        return getMatchingRmObjects(
            if ("ELEMENT" == currentAmNode.rmType || webTemplateNode.rmType == currentAmNode.rmType) webTemplateNode else null,
            rmObject,
            currentAmNode).asSequence().flatMap {

            val omittedNode: JsonNode? = mapRmObjectInternally(webTemplateNode, it)
            val convertedJsonNodes = mapInChain(webTemplateNode, newChain, it)

            if (convertedJsonNodes.isEmpty()) {
                if (omittedNode == null || (omittedNode.isObject && omittedNode.isEmpty)) emptySequence() else sequenceOf(omittedNode)
            } else {
                convertedJsonNodes.map { node ->
                    if (omittedNode == null || (omittedNode.isObject && omittedNode.isEmpty)) node else merge(
                        node,
                        { attribute ->
                            RmObjectToStructuredMapperDelegator.delegateResolveDefaultValueNodeAttribute(RmUtils.getRmClass(chain.last().rmType), attribute)
                        },
                        omittedNode as ObjectNode)
                }.asSequence()
            }
        }.toList()
    }

    /**
     * Merge two [JsonNode].
     *
     * @param node [JsonNode]
     * @param attributeNameResolver Attribute name resolver
     * @param omittedNode [JsonNode] for [RmObject] in RAW format that was omitted in [RmObject] in STRUCTURED format (Element, History, ItemTree...)
     * @return Merged [JsonNode]
     */
    private fun merge(node: JsonNode, attributeNameResolver: (String) -> String, omittedNode: ObjectNode): JsonNode {
        val iterator = omittedNode.fieldNames()
        val names: MutableList<String> = mutableListOf()
        while (iterator.hasNext()) {
            names.add(iterator.next())
        }

        return if (names.isNotEmpty()) {
            val objectNode =
                if (node.isObject)
                    node as ObjectNode
                else objectMapper.createObjectNode().apply { this.replace(attributeNameResolver.invoke(""), node) }
            names.forEach {
                objectNode.replace(it, omittedNode.get(it))
            }
            objectNode
        } else {
            node
        }
    }

    /**
     * Returns [List] of RM object children that match [WebTemplateNode] and [AmNode].
     *
     * For example, content item has multiple observations, instructions and sections on different web template paths,
     * but only matching observations will be returned.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param parent RM object in RAW format
     * @param amNode [AmNode]
     * @return [List] of RM object children in RAW format
     */
    @Suppress("UNCHECKED_CAST")
    private fun getMatchingRmObjects(webTemplateNode: WebTemplateNode?, parent: RmObject, amNode: AmNode): List<RmObject> =
        if (parent is DataValue) {
            listOf(parent)
        } else {
            val child = amNode.getOnParent(parent)
            when {
                child is Collection<*> -> getMatchingRmObjects(amNode, child as Collection<RmObject>, webTemplateNode)
                child !is RmObject -> emptyList()
                rmObjectMatches(amNode, child, webTemplateNode) -> listOf(child)
                else -> emptyList()
            }
        }

    /**
     * Checks if the RM object in RAW format has the same RM type as [AmNode]
     * or if the RM object in RAW format was created for [DvCodedText] with the "|other" attribute.
     *
     * @param amNode [AmNode]
     * @param rmObject RM object in RAW format
     * @param webTemplateNode [WebTemplateNode]
     */
    private fun rmObjectMatches(amNode: AmNode, rmObject: RmObject, webTemplateNode: WebTemplateNode?): Boolean =
        try {
            val rmClass = RmUtils.getRmClass(amNode.rmType)
            rmClass.isInstance(rmObject) || isCodedTextForOtherAttribute(rmObject, rmClass, webTemplateNode)
        } catch (ignored: RmClassCastException) {
            false
        }

    /**
     * Checks if the RM object in RAW format was created for [DvCodedText] with the "|other" attribute.
     *
     * @param rmObject RM object in RAW format
     * @param rmClass [AmNode] RM type class
     * @param webTemplateNode [WebTemplateNode]
     * @return [Boolean] indicating if RM object in RAW format was created for [DvCodedText] with |other attribute
     */
    private fun isCodedTextForOtherAttribute(rmObject: RmObject, rmClass: Class<out RmObject>, webTemplateNode: WebTemplateNode?): Boolean =
        rmClass == DvCodedText::class.java && rmObject is DvText && true == webTemplateNode?.hasInput() && true == webTemplateNode.getInput()?.listOpen

    /**
     * Returns [List] of RM objects in RAW format that match with [AmNode].
     *
     * @param amNode [AmNode]
     * @param rmObjects [Collection] of RM objects in RAW format
     * @param webTemplateNode [WebTemplateNode]
     * @return [List] of RM objects in RAW format.
     */
    private fun getMatchingRmObjects(amNode: AmNode, rmObjects: Collection<RmObject>, webTemplateNode: WebTemplateNode?): List<RmObject> =
        rmObjects.mapNotNull {
            if (it is Locatable) {
                if (locatableMatches(amNode, it, webTemplateNode)) it else null
            } else {
                it
            }
        }

    /**
     * Checks if the RM locatable object name matches with [AmNode].
     *
     * @param amNode [AmNode]
     * @param locatable RM locatable object in RAW format
     * @param webTemplateNode [WebTemplateNode]
     * @return [Boolean] indicating if RM locatable object matches with [AmNode] or not
     */
    private fun locatableMatches(amNode: AmNode, locatable: Locatable, webTemplateNode: WebTemplateNode?): Boolean =
        if (AmUtils.matches(amNode, locatable)) {
            val name = locatable.name
            val codeNameString = webTemplateNode?.nameCodeString
            if (codeNameString != null && name is DvCodedText) {
                codeNameString == name.definingCode?.codeString
            } else {
                AmUtils.isNameConstrained(amNode) || findSiblingsWithConstrainedName(amNode).none { AmUtils.matches(it, locatable) }
            }
        } else {
            false
        }

    /**
     * Finds [AmNode] siblings with the same node ID and name constraint.
     *
     * @param amNode [AmNode]
     * @return [List] of [AmNode] with the same ID and name
     */
    private fun findSiblingsWithConstrainedName(amNode: AmNode): List<AmNode> =
        with(amNode.parent) {
            return this?.attributes?.get(AmUtils.attributeNameOf(this, amNode))?.children
                ?.filter { it != amNode && amNode.archetypeNodeId == it.archetypeNodeId && AmUtils.isNameConstrained(it) } ?: emptyList()
        }

    /**
     * Sets ctx generic fields to the RM composition object in STRUCTURED format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param composition [Composition]
     * @param structuredComposition Composition in STRUCTURED format
     */
    private fun setGenericFields(webTemplateNode: WebTemplateNode, composition: Composition, structuredComposition: ObjectNode) {
        composition.feederAudit?.originatingSystemAudit?.also {
            if ("FormRenderer" == it.systemId) {
                val originalContent = composition.feederAudit?.originalContent
                if (originalContent is DvParsable) {
                    try {
                        val jsonNode = objectMapper.readTree(originalContent.value)
                        if (structuredComposition.has(webTemplateNode.jsonId)) {
                            (structuredComposition.get(webTemplateNode.jsonId) as ObjectNode).remove("_feeder_audit")
                        }

                        structuredComposition.putObject("ctx").set("generic_fields", jsonNode)
                    } catch (ignore: IOException) {
                    }
                }

            }
        }
    }
}

