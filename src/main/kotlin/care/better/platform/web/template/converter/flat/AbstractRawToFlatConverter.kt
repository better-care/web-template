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

package care.better.platform.web.template.converter.flat

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.utils.RmUtils
import care.better.platform.utils.exception.RmClassCastException
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.converter.FromRawConversion
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.builder.model.WebTemplateNode
import com.google.common.collect.Sets
import org.openehr.rm.common.Locatable
import org.openehr.rm.composition.Composition
import org.openehr.rm.datatypes.DataValue
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvInterval
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Base class that converts the RM object in RAW format to the RM object in FLAT format.
 */
internal abstract class AbstractRawToFlatConverter<T> {

    private val exported = Sets.newIdentityHashSet<Any>()

    /**
     * Converts the RM object in RAW format to the RM object in FLAT format.
     *
     * @param webTemplate [WebTemplate]
     * @param fromRawConversion [FromRawConversion]
     * @param rmObject RM object in RAW format
     * @return RM object in FLAT format
     */
    fun <R : RmObject> convert(webTemplate: WebTemplate, fromRawConversion: FromRawConversion, rmObject: R): Map<String, T> =
        if (fromRawConversion.aqlPath.isNullOrBlank() && fromRawConversion.webTemplatePath.isNullOrBlank()) {
            if (rmObject is Composition) {
                convert(webTemplate, rmObject as Composition)
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
     * Converts the RM composition object in RAW format to the RM object in FLAT format.
     *
     * @param webTemplate [WebTemplate]
     * @param composition RM composition object
     * @return RM object in FLAT format
     */
    abstract fun convert(webTemplate: WebTemplate, composition: Composition): Map<String, T>

    /**
     * Converts the RM object in RAW format to the RM object in FLAT format for the AQL path.
     *
     * @param webTemplate [WebTemplate]
     * @param aqlPath AQL path to the RM object
     * @param rmObject RM object in RAW format
     * @return RM object in FLAT format
     */
    abstract fun <R : RmObject> convertForAqlPath(webTemplate: WebTemplate, aqlPath: String, rmObject: R): Map<String, T>

    /**
     * Converts the RM object in RAW format to the RM object in FLAT format for the web template path.
     *
     * @param webTemplate [WebTemplate]
     * @param webTemplatePath Web template path to the RM object
     * @param rmObject RM object in RAW format
     * @return RM object in FLAT format
     */
    abstract fun <R : RmObject> convertForWebTemplatePath(webTemplate: WebTemplate, webTemplatePath: String, rmObject: R): Map<String, T>

    /**
     * Maps the RM object in RAW format to the FLAT format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path
     */
    abstract fun <R : RmObject> mapRmObject(webTemplateNode: WebTemplateNode, rmObject: R, webTemplatePath: String)


    /**
     * Maps the RM object in RAW format to the FLAT format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path
     */
    fun <R : RmObject> mapRmObjectInternally(webTemplateNode: WebTemplateNode, rmObject: R, webTemplatePath: String) {
        if (!exported.contains(rmObject)) {
            mapRmObject(webTemplateNode, rmObject, webTemplatePath)
        }
    }

    /**
     * Recursively maps the RM object in RAW format to the RM object in FLAT format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path to the RM object
     */
    protected open fun map(webTemplateNode: WebTemplateNode, rmObject: RmObject, webTemplatePath: String) {
        mapRmObjectInternally(webTemplateNode, rmObject, webTemplatePath)

        if (rmObject is DvInterval) {
            return
        }

        webTemplateNode.children.forEach { child ->
            mapInChain(child, child.chain, listOf(rmObject), "$webTemplatePath/${child.jsonId}")
        }
    }

    /**
     * Maps [Collection] of RM objects in RAW format to the RM object in FLAT format.
     *
     * Note that some nodes are not presented in [WebTemplateNode] (ITEM_STRUCTURE for example). This function maps those elements as well.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param chain [List] of [AmNode] from parent [WebTemplateNode] [AmNode] to child [WebTemplateNode] [AmNode] (parent is excluded)
     * @param rmObjects [List] of RM objects in RAW format
     * @param webTemplatePath Web template path to the RM objects
     */
    @Suppress("UNCHECKED_CAST")
    private fun mapInChain(webTemplateNode: WebTemplateNode, chain: MutableList<AmNode>, rmObjects: List<RmObject>, webTemplatePath: String) {
        val currentAmNode = chain.first()

        if (chain.size == 1) {
            mapRmObjects(webTemplateNode, getMatchingRmObjects(webTemplateNode, rmObjects, currentAmNode), webTemplatePath)
        } else {
            val children = getMatchingRmObjects(
                if ("ELEMENT" == currentAmNode.rmType || webTemplateNode.rmType == currentAmNode.rmType) webTemplateNode else null,
                rmObjects,
                currentAmNode)
            mapOmittedRmObjects(webTemplateNode, children, webTemplatePath)
            mapInChain(webTemplateNode, chain.drop(1).toMutableList(), children, webTemplatePath)
        }
    }

    /**
     * Maps the RM objects in RAW format that are omitted from [WebTemplateNode] structure to the RM object in FLAT format.
     * Note that this can only be done for element RM objects since element RM attributes can be sent as data value attributes in FLAT or STRUCTURED format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObjects [List] of RM objects in RAW format
     * @param webTemplatePath Web template path to the RM objects
     */
    private fun mapOmittedRmObjects(webTemplateNode: WebTemplateNode, rmObjects: List<RmObject>, webTemplatePath: String) {
        rmObjects.forEachIndexed { index, rmObject ->
            mapRmObjectInternally(webTemplateNode, rmObject, "$webTemplatePath${if (webTemplateNode.isRepeating()) ":$index" else ""}")
        }
    }

    /**
     * Recursively maps the RM objects in RAW format to the RM object in FLAT format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param rmObjects [List] of RM objects in RAW format
     * @param webTemplatePath Web template path to the RM object
     */
    private fun mapRmObjects(webTemplateNode: WebTemplateNode, rmObjects: List<RmObject>, webTemplatePath: String) {
        rmObjects.forEachIndexed { index, rmObject ->
            map(webTemplateNode, rmObject, "$webTemplatePath${if (webTemplateNode.isRepeating()) ":$index" else ""}")
        }
    }

    /**
     * Returns [List] of RM object children that match [WebTemplateNode] and [AmNode].
     *
     * For example, content item has multiple observations, instructions and sections on different web template paths,
     * but only the matching observations will be returned.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param parents [List] of RM objects in RAW format
     * @param amNode [AmNode]
     * @return [List] of RM object children in RAW format
     */
    @Suppress("UNCHECKED_CAST")
    private fun getMatchingRmObjects(webTemplateNode: WebTemplateNode?, parents: List<RmObject>, amNode: AmNode): List<RmObject> {
        return parents.asSequence().flatMap { parent ->
            if (parent is DataValue) {
                sequenceOf(parent)
            } else {
                val child = amNode.getOnParent(parent)
                when {
                    child is Collection<*> -> getMatchingRmObjects(amNode, child as Collection<RmObject>, webTemplateNode)
                    child !is RmObject -> emptySequence()
                    rmObjectMatches(amNode, child, webTemplateNode) -> sequenceOf(child)
                    else -> emptySequence()
                }
            }
        }.toList()
    }

    /**
     * Checks if the RM object in RAW format has the same RM type as [AmNode]
     * or if the RM object in RAW format was created for [DvCodedText] with "|other" attribute.
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
     * Checks if the RM object in RAW format was created for [DvCodedText] with "|other" attribute.
     *
     * @param rmObject RM object in RAW format
     * @param rmClass [AmNode] RM type class
     * @param webTemplateNode [WebTemplateNode]
     * @return [Boolean] indicating if RM object in RAW format was created for [DvCodedText] with "|other" attribute
     */
    private fun isCodedTextForOtherAttribute(rmObject: RmObject, rmClass: Class<out RmObject>, webTemplateNode: WebTemplateNode?): Boolean =
        rmClass == DvCodedText::class.java && rmObject is DvText && true == webTemplateNode?.hasInput() && true == webTemplateNode.getInput()?.listOpen

    /**
     * Returns [Sequence] of RM objects in RAW format that match with [AmNode].
     *
     * @param amNode [AmNode]
     * @param rmObjects [Collection] of RM objects in RAW format
     * @param webTemplateNode [WebTemplateNode]
     * @return [Sequence] of RM objects in RAW format.
     */
    private fun getMatchingRmObjects(amNode: AmNode, rmObjects: Collection<RmObject>, webTemplateNode: WebTemplateNode?): Sequence<RmObject> =
        rmObjects.asSequence().mapNotNull {
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
     * @return [Boolean] indicating whether the RM locatable object matches with [AmNode] or not
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
}
