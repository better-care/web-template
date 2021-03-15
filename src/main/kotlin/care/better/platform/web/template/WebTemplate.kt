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

package care.better.platform.web.template

import care.better.openehr.rm.RmObject
import care.better.platform.path.PathSegment
import care.better.platform.path.PathUtils
import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.web.template.builder.archetype.StandardArchetypePredicateProvider
import care.better.platform.web.template.builder.exception.UnknownPathBuilderException
import care.better.platform.web.template.builder.mapper.WebTemplateObjectMapper
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.CodedValue
import care.better.platform.web.template.builder.model.input.CodedValueWithDescription
import care.better.platform.web.template.builder.predicate.MatchingChildAmNodePredicate
import care.better.platform.web.template.builder.predicate.MatchingConstrainedChildAmNodePredicate
import care.better.platform.web.template.converter.FromRawConversion
import care.better.platform.web.template.converter.ReversedWebTemplatePath
import care.better.platform.web.template.converter.flat.FormattedRawToFlatConverter
import care.better.platform.web.template.converter.flat.RawToFlatConverter
import care.better.platform.web.template.converter.raw.StructuredToRawConverter
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.structured.FlatToStructuredConverter
import care.better.platform.web.template.converter.structured.FormattedRawToStructuredConverter
import care.better.platform.web.template.converter.structured.RawToStructuredConverter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.Multimap
import org.openehr.am.aom.Template
import org.openehr.rm.common.Link
import org.openehr.rm.datatypes.DvEhrUri
import java.io.IOException
import java.io.OutputStream


/**
 * @author Primoz Delopst
 * @author Bostjan Lah
 * @since 3.1.0
 *
 * Class used to represent a single [Template] in the web format.
 *
 * @constructor Creates a new instance of [WebTemplate]
 * @param tree [WebTemplateNode]
 * @param templateId Template ID
 * @param defaultLanguage Default language
 * @param languages [Collection] of supported languages
 * @param version Version of [WebTemplate] model
 * @param nodes [Multimap] of [AmNode] and [WebTemplateNode]
 */
@JsonPropertyOrder("templateId", "semVer", "version", "defaultLanguage", "languages", "tree")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class WebTemplate internal constructor(
        val tree: WebTemplateNode,
        val templateId: String,
        val semVer: String?,
        val defaultLanguage: String,
        val languages: Collection<String>,
        val version: String,
        @JsonIgnore private val nodes: Multimap<AmNode, WebTemplateNode>) {

    companion object {
        /**
         * Resolves the AQL path represented as [List] of [PathSegment] on [AmNode].
         *
         * @param amNode [AmNode]
         * @param pathSegments AQL path represented as [List] of [PathSegment]
         * @return [AmNode] find for AQL path
         */
        @JvmStatic
        fun resolvePath(amNode: AmNode, pathSegments: List<PathSegment>): AmNode? {
            var node = amNode
            for (pathSegment in pathSegments) {
                val childNode = findChildNode(node, pathSegment, null) ?: return null
                node = childNode
            }
            return node
        }

        /**
         * Finds [AmNode] child for [PathSegment] and RM type.
         *
         * @param amNode [AmNode]
         * @param pathSegment [PathSegment]
         * @param rmType RM type
         * @return Matching [AmNode] child if exist, otherwise, return null
         */
        @JvmStatic
        fun findChildNode(amNode: AmNode, pathSegment: PathSegment, rmType: String?): AmNode? =
            with(amNode.attributes[pathSegment.element]) {
                if (this == null) {
                    null
                } else {
                    val matchingNodes = this.children.filter { MatchingChildAmNodePredicate(pathSegment, rmType).invoke(it) }
                    if (matchingNodes.size == 1)
                        matchingNodes[0]
                    else
                        this.children.firstOrNull { MatchingConstrainedChildAmNodePredicate(pathSegment, rmType).invoke(it) }
                }
            }
    }

    /**
     * Converts the RM object in FLAT format to the RM object in RAW format.
     *
     * @param flatRmObject RM object in FLAT format ([Map] of web template path and value pairs)
     * @param conversionContext [ConversionContext]
     * @return RM object in RAW format
     */
    fun <T : RmObject> convertFromFlatToRaw(flatRmObject: Map<String, Any?>, conversionContext: ConversionContext): T? =
        convertFromStructuredToRaw(FlatToStructuredConverter.getInstance().invoke(flatRmObject) as ObjectNode, conversionContext)

    /**
     * Converts the RM object in STRUCTURED format to the RM object in RAW format.
     *
     * @param structuredRmObject RM object in STRUCTURED format ([ObjectNode])
     * @param conversionContext [ConversionContext]
     * @return RM object in RAW format
     */
    fun <T : RmObject> convertFromStructuredToRaw(structuredRmObject: ObjectNode, conversionContext: ConversionContext): T? {
        return StructuredToRawConverter(conversionContext.createBuilder(this).build(), structuredRmObject).convert()
    }

    /**
     * Converts the RM object in RAW format to the RM object in FLAT format.
     *
     * @param rmObject RM object in RAW format
     * @param fromRawConversion [FromRawConversion]
     * @return RM object in FLAT format ([Map] of web template path and value pairs)
     */
    @JvmOverloads
    fun <T : RmObject> convertFromRawToFlat(rmObject: T, fromRawConversion: FromRawConversion = FromRawConversion.create()): Map<String, Any> =
        RawToFlatConverter().convert(this, fromRawConversion, rmObject)

    /**
     * Converts the RM object in RAW format to the RM object in FLAT format with formatted values.
     *
     * @param rmObject RM object in RAW format
     * @param fromRawConversion [FromRawConversion]
     * @return RM object in FLAT format ([Map] of web template path and formatted value pairs)
     */
    @JvmOverloads
    fun <T : RmObject> convertFormattedFromRawToFlat(rmObject: T, fromRawConversion: FromRawConversion = FromRawConversion.create()): Map<String, String> =
        FormattedRawToFlatConverter(fromRawConversion.valueConverter).convert(this, fromRawConversion, rmObject)

    /**
     * Converts the RM object in RAW format to the RM object in STRUCTURED format.
     *
     * @param rmObject RM object in RAW format
     * @param fromRawConversion [FromRawConversion]
     * @return RM object in STRUCTURED format
     */
    @JvmOverloads
    fun <T : RmObject> convertFromRawToStructured(rmObject: T, fromRawConversion: FromRawConversion = FromRawConversion.create()): JsonNode? =
        RawToStructuredConverter(fromRawConversion.objectMapper).convert(this, fromRawConversion, rmObject)

    /**
     * Converts the RM object in RAW format to the RM object in STRUCTURED format with formatted values.
     *
     * @param rmObject RM object in RAW format
     * @param fromRawConversion [FromRawConversion]
     * @return RM object in STRUCTURED format with formatted values
     */
    @JvmOverloads
    fun <T : RmObject> convertFormattedFromRawToStructured(rmObject: T, fromRawConversion: FromRawConversion = FromRawConversion.create()): JsonNode? =
        FormattedRawToStructuredConverter(fromRawConversion.valueConverter, fromRawConversion.objectMapper).convert(this, fromRawConversion, rmObject)

    /**
     * Converts [WebTemplate] to JSON formatted [String].
     *
     * @param pretty [Boolean] indicating if pretty formatting will be used
     * @return JSON [String]
     * @throws [JsonProcessingException] thrown when conversion to JSON fails
     */
    @Throws(JsonProcessingException::class)
    @JvmOverloads
    fun asJson(pretty: Boolean = false): String = WebTemplateObjectMapper.getWriter(pretty).writeValueAsString(this)

    /**
     * Writes [WebTemplate] to the [OutputStream].
     *
     * @param outputStream [OutputStream]
     * @param pretty [Boolean] indicating if pretty formatting will be used
     * @throws [IOException] thrown when output could not be written out
     */
    @Throws(IOException::class)
    @JvmOverloads
    fun write(outputStream: OutputStream, pretty: Boolean = false) = WebTemplateObjectMapper.getWriter(pretty).writeValue(outputStream, this)

    /**
     * Finds [WebTemplateNode] for the web template path.
     *
     * @param webTemplatePath Web template path
     * @return [WebTemplateNode] [WebTemplateNode] if found, otherwise, return null
     * @throws [UnknownPathBuilderException] web template path is not valid for this web template
     */
    fun findWebTemplateNode(webTemplatePath: String): WebTemplateNode {
        val path: ReversedWebTemplatePath = ReversedWebTemplatePath.fromString(
            if (webTemplatePath.startsWith("/"))
                "${tree.jsonId}$webTemplatePath"
            else
                webTemplatePath)
        return when {
            path.key != tree.jsonId -> throw UnknownPathBuilderException(path.getId(), path.key)
            path.child == null -> tree
            else -> findWebTemplateNodeRecursive(path.child, path.getId(), tree)
        }
    }

    /**
     * Recursively finds [WebTemplateNode] for the web template path.
     *
     * @param webTemplatePath [ReversedWebTemplatePath]
     * @param id Web template path
     * @return [WebTemplateNode] if found, otherwise, return null
     * @throws [UnknownPathBuilderException] web template path is not valid for this web template
     */
    private fun findWebTemplateNodeRecursive(webTemplatePath: ReversedWebTemplatePath, id: String, webTemplateNode: WebTemplateNode): WebTemplateNode =
        with(webTemplateNode.children.firstOrNull { it.jsonIdMatches(webTemplatePath.key) } ?: throw UnknownPathBuilderException(id, webTemplatePath.key)) {
            if (webTemplatePath.child == null) {
                this
            } else {
                findWebTemplateNodeRecursive(webTemplatePath.child, id, this)
            }
        }

    /**
     * Finds [WebTemplateNode] for the AQL path.
     *
     * @param aqlPath AQL path
     * @return [WebTemplateNode]
     * @throws [UnknownPathBuilderException] if no node was found for the AQL path
     */
    fun findWebTemplateNodeByAqlPath(aqlPath: String): WebTemplateNode = findWebTemplateNodeByAqlPath(PathUtils.getPathSegments(aqlPath))

    /**
     * Finds [WebTemplateNode] for the AQL path.
     *
     * @param aqlPath AQL path
     * @return [WebTemplateNode] if found, otherwise, return null
     */
    fun findWebTemplateNodeByAqlPathOrNull(aqlPath: String): WebTemplateNode? =
        with(PathUtils.getPathSegments(aqlPath)) {
            val amNode = resolvePath(tree.amNode, this)
            getMatchingWebTemplateNode(this, amNode)
        }

    /**
     * Finds [WebTemplateNode] for the AQL path and archetype ID.
     *
     * @param archetypeId Archetype ID
     * @param aqlPath AQL path
     * @return [WebTemplateNode] if found, otherwise, return null
     * @throws [UnknownPathBuilderException] if no node was found for the AQL path
     */
    fun findWebTemplateNodesByAqlPath(archetypeId: String, aqlPath: String): List<WebTemplateNode> {
        val archetypeNodes = with(mutableListOf<WebTemplateNode>()) {
            findWebTemplateNodesByArchetypeId(tree, archetypeId, this)
            this.toList()
        }

        val pathSegments = PathUtils.getPathSegments(aqlPath)
        return archetypeNodes.mapNotNull { resolvePath(it.amNode, pathSegments) }.mapNotNull { getMatchingWebTemplateNode(pathSegments, it) }
    }

    /**
     * Finds [WebTemplateNode] for the AQL path and RM type.
     *
     * @param rmType RM type
     * @param aqlPath AQL path
     * @return [WebTemplateNode] if found, otherwise, return null
     * @throws [UnknownPathBuilderException] if no node was found for the AQL path
     */
    fun findWebTemplateNodeByAqlPath(rmType: String, aqlPath: String): WebTemplateNode =
        findWebTemplateNodeByAqlPath(rmType, PathUtils.getPathSegments(aqlPath))

    /**
     * Finds [WebTemplateNode] for the AQL path represented as [List] of [PathSegment] and RM type.
     *
     * @param rmType RM type
     * @param pathSegments AQL path represented as [List] of [PathSegment]
     * @return [WebTemplateNode] if found, otherwise, return null
     * @throws [UnknownPathBuilderException] if no node was found for the AQL path
     */
    fun findWebTemplateNodeByAqlPath(rmType: String, pathSegments: List<PathSegment>): WebTemplateNode {
        val amNode: AmNode? = if (pathSegments.size > 1) {
            val lastSegment = pathSegments[pathSegments.size - 1]
            val parentNode = resolvePath(tree.amNode, pathSegments.subList(0, pathSegments.size - 1))
            if (parentNode == null) {
                null
            } else {
                findChildNode(parentNode, lastSegment, rmType)
            }
        } else {
            resolvePath(tree.amNode, pathSegments)
        }
        return getMatchingWebTemplateNode(pathSegments, amNode) ?: throw UnknownPathBuilderException(PathUtils.buildPath(pathSegments))
    }

    /**
     * Finds [WebTemplateNode] for the AQL path represented as [List] of [PathSegment].
     *
     * @param pathSegments AQL path represented as [List] of [PathSegment]
     * @return [WebTemplateNode] if found, otherwise, return null
     * @throws [UnknownPathBuilderException] if no node was found for the AQL path
     */
    fun findWebTemplateNodeByAqlPath(pathSegments: List<PathSegment>): WebTemplateNode =
        with(resolvePath(tree.amNode, pathSegments)) {
            getMatchingWebTemplateNode(pathSegments, this) ?: throw UnknownPathBuilderException(PathUtils.buildPath(pathSegments))
        }

    /**
     * Finds multiple [WebTemplateNode] for the archetype ID.
     *
     * @param  webTemplateNode [WebTemplateNode]
     * @param archetypeId Archetype ID
     * @param archetypeWebTemplateNodes [MutableList] of [WebTemplateNode]
     */
    private fun findWebTemplateNodesByArchetypeId(
            webTemplateNode: WebTemplateNode,
            archetypeId: String,
            archetypeWebTemplateNodes: MutableList<WebTemplateNode>) {
        if (archetypeId == webTemplateNode.nodeId) {
            archetypeWebTemplateNodes.add(webTemplateNode)
        }
        webTemplateNode.children.forEach { findWebTemplateNodesByArchetypeId(it, archetypeId, archetypeWebTemplateNodes) }

    }

    /**
     * Finds [WebTemplateNode] for the AQL path represented as [List] of [PathSegment] and [AmNode].
     *
     * @param pathSegments AQL path represented as [List] of [PathSegment]
     * @param amNode [AmNode]
     * @return [WebTemplateNode] if found, otherwise, return null
     */
    private fun getMatchingWebTemplateNode(pathSegments: List<PathSegment>, amNode: AmNode?): WebTemplateNode? =
        with(nodes[amNode]) {
            if (this == null) {
                null
            } else {
                when {
                    this.size == 1 -> this.iterator().next()
                    this.isNotEmpty() -> {
                        val lastSegment = pathSegments.last()
                        if (lastSegment.name == null) this.iterator().next() else this.firstOrNull { lastSegment.name == it.name }
                    }
                    else -> null
                }
            }
        }

    /**
     * Returns [Collection] of [WebTemplateNode] for [AmNode].
     * @param amNode [AmNode]
     * @return [Collection] of [WebTemplateNode]
     */
    fun getWebTemplateNodes(amNode: AmNode?): Collection<WebTemplateNode> = amNode?.let { nodes.get(it)?.toList() } ?: emptyList()

    /**
     * Returns [List] of [CodedValue] for the web template path.
     * If language is passed, [List] of [CodedValue] will be returned for that language, otherwise,
     * [List] of [CodedValue] for [WebTemplate] default language will be returned.
     *
     * @param webTemplatePath Web template path
     * @return [List] of [CodedValue]
     * @throws [UnknownPathBuilderException] web template path is not valid for this web template
     */
    @JvmOverloads
    fun getCodes(webTemplatePath: String, language: String? = null): List<CodedValue> =
        with(findWebTemplateNode(webTemplatePath)) {
            if (this.hasInput())
                this.getInput()?.list?.map { CodedValue(it.value, if (language == null) it.label else it.localizedLabels[language]) } ?: emptyList()
            else
                emptyList()
        }

    /**
     * Returns a [List] of [CodedValue] including their descriptions for the web template path in the specified language.
     *
     * @param webTemplatePath Web template path
     * @param language Language
     * @return [List] of [CodedValue] including their descriptions for the web template path in the specified language
     * @throws [UnknownPathBuilderException] web template path is not valid for this web template
     */
    fun getCodesWithDescription(webTemplatePath: String, language: String): List<CodedValueWithDescription> =
        with(findWebTemplateNode(webTemplatePath)) {
            val termDefinitions = if (this.amNode.termDefinitions.containsKey(language))
                this.amNode.termDefinitions.let { it[language] } ?: emptyList()
            else
                this.amNode.terms

            if (this.hasInput())
                this.getInput()?.list?.map {
                    CodedValueWithDescription(it.value, it.localizedLabels[language]!!, AmUtils.findTerm(termDefinitions, it.value, "description")!!)
                } ?: emptyList()
            else
                emptyList()
        }

    /**
     * Returns a label (title) for the web template path.
     * If language is passed, label will be returned for that language, otherwise, label for [WebTemplate] default language will be returned.
     *
     * @param webTemplatePath web template path
     * @param language Language
     * @return Label [String]
     * @throws [UnknownPathBuilderException] if web template path is not valid for this web template
     */
    @JvmOverloads
    fun getLabel(webTemplatePath: String, language: String? = null): String? =
        with(findWebTemplateNode(webTemplatePath)) {
            if (language == null) this.localizedName else this.localizedNames[language]
        }

    /**
     * Returns a RM path suitable to be used for [DvEhrUri] or [Link] for the given web template path.
     * Returned path takes into account the segment indexes. Returned path does not include portion for the EHR uid and composition uid.
     *
     * @param webTemplatePath path
     * @return Path [String]
     */
    fun getLinkPath(webTemplatePath: String): String {
        val path: ReversedWebTemplatePath = ReversedWebTemplatePath.fromString(
            if (webTemplatePath.startsWith("/"))
                "${tree.jsonId}$webTemplatePath"
            else
                webTemplatePath)
        return when {
            path.key != tree.jsonId || path.child == null -> throw UnknownPathBuilderException(path.getId(), path.key)
            else -> getLinkPathRecursive(path.child, path.getId(), "", tree)
        }
    }

    /**
     * Returns [List] of [WebTemplateNode] for [AmNode]
     *
     * @param amNode [AmNode]
     * @return [List] of [WebTemplateNode]
     */
    fun getNodes(amNode: AmNode?): List<WebTemplateNode> =
        if (amNode == null)
            emptyList()
        else
            nodes[amNode]?.toList() ?: emptyList()


    /**
     * Recursively gets a RM path suitable to be used for [DvEhrUri] or [Link] for the given web template path.
     * Returned path takes into account segment indexes. Returned path does not include portion for the EHR uid and composition uid.
     *
     * @param webTemplatePath [ReversedWebTemplatePath]
     * @param id Web template path
     * @param linkPath Current generated path in the recursion
     * @param webTemplateNode [WebTemplateNode]
     * @return Path [String]
     */
    private fun getLinkPathRecursive(webTemplatePath: ReversedWebTemplatePath, id: String, linkPath: String, webTemplateNode: WebTemplateNode): String =
        with(webTemplateNode.children.firstOrNull { it.jsonIdMatches(webTemplatePath.key) } ?: throw UnknownPathBuilderException(id, webTemplatePath.key)) {
            if (webTemplatePath.child == null) {
                "$linkPath${this.getSubPath(webTemplatePath.index ?: 0, StandardArchetypePredicateProvider)}"
            } else {
                getLinkPathRecursive(
                    webTemplatePath.child,
                    id,
                    "$linkPath${this.getSubPath(webTemplatePath.index ?: 0, StandardArchetypePredicateProvider)}",
                    this)
            }
        }
}
