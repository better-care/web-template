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
package care.better.platform.web.template.builder

import care.better.openehr.rm.RmObject
import care.better.openehr.terminology.OpenEhrTerminology
import care.better.platform.template.AmAttribute
import care.better.platform.template.AmNode
import care.better.platform.template.AmTreeBuilder
import care.better.platform.template.AmUtils
import care.better.platform.template.AmUtils.createInterval
import care.better.platform.template.AmUtils.findDescription
import care.better.platform.template.AmUtils.findTerm
import care.better.platform.template.AmUtils.findTermBindings
import care.better.platform.template.AmUtils.findTermText
import care.better.platform.template.AmUtils.findText
import care.better.platform.template.AmUtils.getCObjectItem
import care.better.platform.template.AmUtils.getNameCodePhrase
import care.better.platform.template.AmUtils.isNameConstrained
import care.better.platform.template.AmUtils.resolvePath
import care.better.platform.template.type.CollectionInfo
import care.better.platform.template.type.CollectionType
import care.better.platform.template.type.TypeInfo
import care.better.platform.utils.RmUtils
import care.better.platform.utils.TemplateUtils
import care.better.platform.utils.exception.RmClassCastException
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.builder.compactor.MediumWebTemplateCompactor
import care.better.platform.web.template.builder.compactor.WebTemplateCompactor
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.exception.BuilderException
import care.better.platform.web.template.builder.id.WebTemplateIdBuilder
import care.better.platform.web.template.builder.input.WebTemplateInputBuilderDelegator
import care.better.platform.web.template.builder.model.*
import care.better.platform.web.template.builder.model.input.CareflowStepWebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.range.WebTemplateIntegerRange
import care.better.platform.web.template.builder.postprocess.WebTemplateNodeChildrenPostProcessorDelegator
import care.better.platform.web.template.builder.utils.CodePhraseUtils
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ISM_TRANSITION_GROUP_NAME
import care.better.platform.web.template.converter.raw.extensions.isForElement
import care.better.platform.web.template.converter.raw.extensions.isNotNullOrBlank
import care.better.platform.web.template.converter.raw.extensions.isNotNullOrEmpty
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Lists
import com.google.common.collect.Multimap
import org.apache.commons.lang3.StringUtils
import org.openehr.am.aom.*
import org.openehr.am.aom.Annotation
import org.openehr.base.foundationtypes.IntervalOfInteger
import org.openehr.rm.common.Locatable
import org.openehr.rm.composition.Action
import org.openehr.rm.composition.Activity
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.IsmTransition
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.History
import org.openehr.rm.datatypes.*
import java.util.*

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Builder for building [WebTemplate] from the [Template].
 *
 * @constructor Creates a new instance of [WebTemplateBuilder].
 * @param template [Template]
 * @param webTemplateBuilderContext [WebTemplateBuilderContext]
 */
class WebTemplateBuilder private constructor(template: Template, webTemplateBuilderContext: WebTemplateBuilderContext) {
    companion object {
        private const val CURRENT_VERSION = "2.3"

        private val IGNORED_RM_PROPERTIES: Set<RmProperty> =
            setOf(
                    RmProperty(Composition::class.java, "category"), // always constrained in the template otherwise default is OK
                    RmProperty(History::class.java, "origin"), // can be calculated from events
                    RmProperty(DvCodedText::class.java, "value"), // from code
                    RmProperty(Locatable::class.java, "archetype_node_id"), // from template
                    RmProperty(Locatable::class.java, "name")) // from template

        private val OVERRIDE_OPTIONAL: Set<RmProperty> =
            setOf(RmProperty(Activity::class.java, "timing")) // no longer mandatory in RM 1.0.4 (added for backward compatibility)

        private val SKIP_PATHS: Set<String> = setOf("name")

        private val ANY_DATA_TYPES =
            arrayOf(
                    RmUtils.getRmTypeName(DvCodedText::class.java),
                    RmUtils.getRmTypeName(DvText::class.java),
                    RmUtils.getRmTypeName(DvMultimedia::class.java),
                    RmUtils.getRmTypeName(DvParsable::class.java),
                    RmUtils.getRmTypeName(DvState::class.java),
                    RmUtils.getRmTypeName(DvBoolean::class.java),
                    RmUtils.getRmTypeName(DvIdentifier::class.java),
                    RmUtils.getRmTypeName(DvUri::class.java),
                    RmUtils.getRmTypeName(DvEhrUri::class.java),
                    RmUtils.getRmTypeName(DvDuration::class.java),
                    RmUtils.getRmTypeName(DvQuantity::class.java),
                    RmUtils.getRmTypeName(DvCount::class.java),
                    RmUtils.getRmTypeName(DvProportion::class.java),
                    RmUtils.getRmTypeName(DvDateTime::class.java),
                    RmUtils.getRmTypeName(DvDate::class.java),
                    RmUtils.getRmTypeName(DvTime::class.java),
                    RmUtils.getRmTypeName(DvOrdinal::class.java),
                    RmUtils.getRmTypeName(DvScale::class.java))

        /**
         * Builds [WebTemplate] from the [Template].
         * An additional starting RM path can be specified in which case web template will be built only for the portion of the template under the path.
         *
         * @param template [Template]
         * @param context  [WebTemplateBuilderContext]
         * @param from RM path
         * @return [WebTemplate]
         */
        @JvmStatic
        @JvmOverloads
        fun build(template: Template, context: WebTemplateBuilderContext, from: String? = null): WebTemplate? =
            WebTemplateBuilder(template, context).build(
                    AmTreeBuilder(template).build(),
                    from,
                    requireNotNull(template.templateId.value) { "Template ID is mandatory." },
                    TemplateUtils.extractSemVerFromTemplateDescription(template))

        /**
         * Builds [WebTemplate] from the [Template].
         * An additional starting RM path can be specified in which case web template will be built only for the portion of the template under the path.
         *
         * @param template [Template]
         * @param context  [WebTemplateBuilderContext]
         * @param from RM path
         * @return [WebTemplate]
         */
        @JvmStatic
        @JvmOverloads
        fun buildNonNull(template: Template, context: WebTemplateBuilderContext, from: String? = null): WebTemplate =
            build(template, context, from) ?: throw BuilderException("Web template is null.")
    }

    private val templateLanguage: String = requireNotNull(template.language?.codeString) { "Template default language is mandatory." }
    private val defaultLanguage = webTemplateBuilderContext.defaultLanguage ?: templateLanguage
    private val context = webTemplateBuilderContext.copy(
            defaultLanguage = defaultLanguage,
            otherDetails =  WebTemplateBuilderUtils.extractOtherDetails(template.description?.otherDetails ?: listOf()))

    private val segments: Deque<WebTemplateNode> = ArrayDeque()
    private val compactor: WebTemplateCompactor = MediumWebTemplateCompactor()
    private val idBuilder: WebTemplateIdBuilder = WebTemplateIdBuilder()
    private val postProcess: Boolean = true

    private fun build(root: AmNode, from: String?, templateId: String, semVer: String?): WebTemplate? =
        with(if (from == null) root else resolvePath(root, from)) {
            val nodes: Multimap<AmNode, WebTemplateNode> = ArrayListMultimap.create()
            if (this != null) {
                WebTemplate(
                        buildNode(null, this).apply {
                            compactor.compact(this)
                            idBuilder.buildIds(this, nodes)
                        },
                        templateId,
                        semVer,
                        context.contextLanguage ?: defaultLanguage,
                        context.languages,
                        CURRENT_VERSION,
                        nodes,
                        context.otherDetails)
            } else {
                null
            }
        }

    private fun buildNode(attributeName: String?, amNode: AmNode): WebTemplateNode =
        createNode(attributeName, amNode).apply {
            buildNodeChildren(amNode, this)
        }

    private fun buildNodeChildren(amNode: AmNode, webTemplateNode: WebTemplateNode) {
        segments.push(webTemplateNode)
        var children = createChildren(amNode, webTemplateNode)
        if (children.isEmpty() && amNode.isForElement()) {
            children = addAllDataValueChildren(amNode)
        }
        if (children.isEmpty()) {
            addInputs(webTemplateNode)
        }
        if (!webTemplateNode.hasInput()) {
            addRequiredRmAttributes(amNode, children)
        }
        segments.pop()

        processChildren(webTemplateNode, children)
        val cardinalities = getCardinalities(amNode, webTemplateNode)
        if (cardinalities.isNotEmpty()) {
            webTemplateNode.cardinalities = cardinalities
        }
    }

    private fun addAllDataValueChildren(amNode: AmNode): MutableList<WebTemplateNode> {
        val children: MutableList<WebTemplateNode> = mutableListOf()
        val amChildren: MutableList<AmNode> = mutableListOf()

        ANY_DATA_TYPES.forEach {
            val childNode = createAmNode(amNode, it, "value", Element::class.java)
            amChildren.add(childNode)
            children.add(createCustomNode(childNode, amNode.name, WebTemplateIntegerRange(1, 1)).apply {
                this.jsonId = WebTemplateBuilderUtils.getChoiceWebTemplateId(it.substring(3).lowercase())
            })
        }

        amNode.attributes["value"] = AmAttribute(createInterval(1, 1), amChildren).apply { this.rmOnly = true }
        return children
    }

    private fun createChildren(amNode: AmNode, node: WebTemplateNode): MutableList<WebTemplateNode> {
        val children: MutableList<WebTemplateNode> = ArrayList()
        if (addAttributes(getRmType(node))) {
            amNode.attributes.entries.asSequence().filter { !it.value.rmOnly }.forEach {
                if ("ACTION" == amNode.rmType && "ism_transition" == it.key)
                    addIsmTransition(children, amNode)
                else
                    addChildren(children, it.key, it.value)
            }
            addSpecialAttributes(amNode, children)
        }
        return children
    }

    private fun getRmType(node: WebTemplateNode): Class<*>? =
        try {
            RmUtils.getRmClass(node.rmType)
        } catch (ignored: RmClassCastException) {
            null
        }

    private fun addSpecialAttributes(amNode: AmNode, children: MutableList<WebTemplateNode>) {
        if ("INSTRUCTION" == amNode.rmType) {
            addChildren(children, "expiry_time", amNode.attributes["expiry_time"])
        }
    }

    private fun addAttributes(rmType: Class<*>?): Boolean =
        rmType == null || DvInterval::class.java.isAssignableFrom(rmType) || !DataValue::class.java.isAssignableFrom(rmType)

    private fun createNode(attributeName: String?, amNode: AmNode): WebTemplateNode =
        WebTemplateNode(amNode, amNode.rmType, getPath(attributeName, amNode)).apply {
            this.name = amNode.name

            val amNodeParent = amNode.parent
            if (StringUtils.isNotBlank(amNode.nodeId))
                setLocalizedNames(amNode, this)
            else if (amNodeParent != null && "ELEMENT" == amNodeParent.rmType)
                setLocalizedNames(amNodeParent, this)

            this.nodeId = amNode.archetypeNodeId
            this.occurences = WebTemplateIntegerRange(amNode.occurrences)

            amNode.annotations?.forEach { setNodeAnnotations(this, it) }
            setArchetypeAnnotations(amNode, this)

            amNode.viewConstraints?.also { setNodeViewAnnotations(this, it) }

            setTermBindings(this)
        }

    private fun setNodeAnnotations(node: WebTemplateNode, annotation: Annotation) {
        annotation.items.forEach { node.annotations[it.id!!] = it.value!! }
    }

    private fun setLocalizedNames(amNode: AmNode, node: WebTemplateNode) {
        val name = node.name ?: amNode.name
        if (defaultLanguage.isNotBlank()) {
            if (isNameConstrained(amNode)) {
                if (isConstrainedNameTranslated(amNode, amNode.nodeId))
                    node.localizedName = findText(amNode, defaultLanguage, amNode.nodeId)
                else
                    node.localizedName = name
            } else {
                node.localizedName = findText(amNode, defaultLanguage, amNode.nodeId) ?: name
            }
        }
        addLocalizedNames(amNode, amNode.nodeId, node, false)
    }

    private fun setTermBindings(node: WebTemplateNode) {
        val amNode = node.amNode
        val nodeId = amNode.nodeId

        if (nodeId.isNotNullOrEmpty()) {
            findTermBindings(amNode, nodeId).forEach { (key, value) -> CodePhraseUtils.getBindingCodedValue(value)?.also { node.termBindings[key] = it } }
        }
    }

    private fun addInputs(node: WebTemplateNode) {
        try {
            val rmClass = RmUtils.getRmClass(node.rmType)
            if (WebTemplateInputBuilderDelegator.isRmTypeWithInputs(rmClass)) {
                WebTemplateInputBuilderDelegator.delegate(node, context)
            }
        } catch (ignored: RmClassCastException) {
            WebTemplateInputBuilderDelegator.delegate(node, context)
        }
    }

    private fun addRequiredRmAttributes(parent: AmNode, children: MutableList<WebTemplateNode>) {
        try {
            val rmClass = RmUtils.getRmClass(parent.rmType)
            parent.attributes.entries.asSequence()
                .filter { isRequiredAttribute(rmClass, it) }
                .forEach {
                    val attributeName = it.key
                    val amNode = it.value.children[0]
                    if (children.none { child -> child.amNode == amNode }) {
                        children.add(createCustomNode(amNode, attributeName, WebTemplateIntegerRange(1, 1)))
                    }
                }
        } catch (ignored: RmClassCastException) {
        }
    }

    private fun isRequiredAttribute(rmClass: Class<out RmObject?>, entry: Map.Entry<String, AmAttribute>): Boolean =
        OVERRIDE_OPTIONAL.contains(RmProperty(rmClass, entry.key)) || entry.value.rmOnly && 1 == entry.value.existence?.lower && !isIgnored(entry, rmClass)

    private fun createCustomNode(amNode: AmNode, attributeName: String?, existence: WebTemplateIntegerRange): WebTemplateNode =
        buildNode(attributeName, amNode).apply {
            this.name = "${attributeName?.substring(0, 1)?.uppercase()}${attributeName?.substring(1)}"
            this.inContext = true
            this.occurences = existence
            setTermBindings(this)
        }

    private fun isIgnored(entry: Map.Entry<String, AmAttribute>, rmClass: Class<out RmObject?>): Boolean {
        var clazz: Class<*>? = rmClass
        while (clazz != null && RmObject::class.java.isAssignableFrom(clazz)) {
            if (IGNORED_RM_PROPERTIES.contains(RmProperty(clazz, entry.key))) {
                return true
            }
            clazz = clazz.superclass
        }
        return false
    }

    private fun addChildren(children: MutableList<WebTemplateNode>, attributeName: String, attribute: AmAttribute?) {
        attribute?.children?.forEach {
            if (it.cObject !is ArchetypeSlot && !SKIP_PATHS.contains(attributeName) && it.isPropertyOnParent()) {
                val nameCodes = getMultipleNames(it)
                if (nameCodes.isEmpty()) {
                    val webTemplateNode = buildNode(attributeName, it)
                    children.add(webTemplateNode)
                    try {
                        if (IGNORED_RM_PROPERTIES.contains(RmProperty(RmUtils.getRmClass(it.parent!!.rmType), attributeName))) {
                            webTemplateNode.inContext = true
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    addChildrenByNames(children, attributeName, it, nameCodes)
                }
            }
        }
    }

    private fun addChildrenByNames(children: MutableList<WebTemplateNode>, attributeName: String, child: AmNode, nameCodes: List<String>) {
        nameCodes.forEach { addChildByName(children, attributeName, child, it) }
    }

    private fun addChildByName(children: MutableList<WebTemplateNode>, attributeName: String, child: AmNode, nameCode: String) {
        val webTemplateNode = createNode(attributeName, child).apply {
            this.name = findTermText(child, nameCode)
            if (StringUtils.isNotBlank(context.defaultLanguage)) {
                this.localizedName = findText(child, defaultLanguage, nameCode)
            }
            this.nameCodeString = nameCode
            this.path = getPath(attributeName, child, this.name)
            addLocalizedNames(child, nameCode, this, true)
        }
        children.add(webTemplateNode)
        buildNodeChildren(child, webTemplateNode)
    }

    private fun addLocalizedNames(amNode: AmNode, nameCode: String?, webTemplateNode: WebTemplateNode, useConstrainedCode: Boolean) {
        if (isNameConstrained(amNode) && !useConstrainedCode) {
            if (isConstrainedNameTranslated(amNode, nameCode)) {
                context.languages.asSequence() // translations in ADL designer
                    .map { Pair(it, findText(amNode, it, nameCode)) }
                    .filter { it.second.isNotNullOrBlank() }
                    .forEach { webTemplateNode.localizedNames[it.first] = it.second }
            } else if (defaultLanguage.isNotNullOrBlank()) {
                webTemplateNode.localizedNames[defaultLanguage] = webTemplateNode.name
            }
            context.languages.forEach { addLocalizedDescription(amNode, nameCode, webTemplateNode, it) }
        } else {
            addLocalizedNamesByCode(amNode, nameCode, webTemplateNode)
        }
        addLocalizedNamesFromAnnotations(webTemplateNode, WebTemplateBuilderUtils.getTranslationsFromAnnotations(amNode))
    }

    private fun isConstrainedNameTranslated(amNode: AmNode, nameCode: String?): Boolean =
        amNode.name == findText(amNode, templateLanguage, nameCode)

    private fun addLocalizedNamesByCode(amNode: AmNode, nameCode: String?, wtNode: WebTemplateNode) {
        context.languages.forEach {
            val localizedName = findText(amNode, it, nameCode)
            wtNode.localizedNames[it] = StringUtils.defaultString(localizedName)
            addLocalizedDescription(amNode, nameCode, wtNode, it)
        }
    }

    private fun addLocalizedNamesFromAnnotations(webTemplateNode: WebTemplateNode, localizedNamesFromAnnotations: Map<String, String?>) {
        localizedNamesFromAnnotations.entries.asSequence()
            .filter { StringUtils.isNotBlank(it.value) }
            .filter { context.languages.contains(it.key) }
            .forEach { webTemplateNode.localizedNames[it.key] = it.value }
    }

    private fun addLocalizedDescription(amNode: AmNode, nameCode: String?, wtNode: WebTemplateNode, language: String) {
        if (context.isAddDescriptions) {
            val description = findDescription(amNode, language, nameCode)
            if (description != null) {
                wtNode.localizedDescriptions[language] = description
            }
        }
    }

    private fun addIsmTransition(children: MutableList<WebTemplateNode>, amNode: AmNode) {
        val ismTransition = AmNode(amNode, "ISM_TRANSITION").apply {
            this.name = "ism_transition"
            val getter = RmUtils.getGetterForAttribute("ism_transition", Action::class.java)
            this.getter = getter
            this.setter = RmUtils.getSetterForAttribute("ism_transition", Action::class.java)
            this.setType(TypeInfo(getter!!.returnType, null))
        }

        val currentState = createIsmTransitionAttribute(ismTransition, "current_state", createInterval(1, 1))
        val transition = createIsmTransitionAttribute(ismTransition, "transition", createInterval(0, 1))
        val careflowStep = createIsmTransitionAttribute(ismTransition, "careflow_step", createInterval(0, 1))
        val webTemplateNode = createCustomNode(ismTransition, "ism_transition", WebTemplateIntegerRange(1, 1))

        segments.push(webTemplateNode)
        webTemplateNode.children.clear()

        val allowedCurrentStates: MutableSet<String> = hashSetOf()
        val careflowStepWtNode = createCareFlowStepNode(amNode, careflowStep, allowedCurrentStates)

        webTemplateNode.children.add(
                createCurrentStateInput(
                        currentState,
                        if (allowedCurrentStates.isEmpty()) OpenEhrTerminology.getInstance()
                            .getGroupChildren(ISM_TRANSITION_GROUP_NAME) else allowedCurrentStates))

        webTemplateNode.children.add(createCustomNode(transition, "transition", WebTemplateIntegerRange(0, 1)))
        webTemplateNode.children.add(careflowStepWtNode)
        children.add(webTemplateNode)

        segments.pop()
    }

    private fun createCareFlowStepNode(amNode: AmNode, careflowStep: AmNode, allowedCurrentStates: MutableSet<String>): WebTemplateNode =
        createCustomNode(careflowStep, "careflow_step", WebTemplateIntegerRange(0, 1)).apply {
            val ismTransitionAttribute = amNode.attributes["ism_transition"]
            if (ismTransitionAttribute != null) {
                val transitionInput = WebTemplateInput(WebTemplateInputType.CODED_TEXT, "code")
                val options = ismTransitionAttribute.children.asSequence()
                    .filter { !it.nodeId.isNullOrBlank() }
                    .map {
                        val codedValue = CodePhraseUtils.getCodedValue("local", it.nodeId!!, amNode, context)
                        val currentStateCCodePhrase = getCObjectItem(it, CCodePhrase::class.java, "current_state", "defining_code")
                        CareflowStepWebTemplateCodedValue(
                                codedValue,
                                if (currentStateCCodePhrase?.codeList != null) currentStateCCodePhrase.codeList else emptyList()).apply {
                            allowedCurrentStates.addAll(this.currentStates)
                        }
                    }.toList()
                transitionInput.list.addAll(options)
                this.inputs.clear()
                this.inputs.add(transitionInput)
            }
        }

    private fun createCurrentStateInput(currentState: AmNode, currentStates: Collection<String>): WebTemplateNode =
        createCustomNode(currentState, "current_state", WebTemplateIntegerRange(1, 1)).apply {
            val currentStateInput = WebTemplateInput(WebTemplateInputType.CODED_TEXT, "code")
            val currentStateOptions = currentStates.map { WebTemplateCodedValue(it, CodePhraseUtils.getOpenEhrTerminologyText(it, "en")) }

            currentStateInput.list.addAll(currentStateOptions)
            this.inputs.clear()
            this.inputs.add(currentStateInput)
        }

    private fun getPath(attributeName: String?, amNode: AmNode): String =
        if (attributeName == null)
            ""
        else
            "${if (segments.isEmpty()) "" else segments.peek()?.path}/${attributeName}${
                getArchetypePredicate(
                        amNode,
                        if (isNameConstrained(amNode)) amNode.name else null)
            }"

    private fun getPath(attributeName: String, amNode: AmNode, customName: String?): String =
        "${if (segments.isEmpty()) "" else segments.peek()?.path}/${attributeName}${getArchetypePredicate(amNode, customName)}"

    private fun getArchetypePredicate(amNode: AmNode, nameConstraint: String?): String =
        if (amNode.archetypeNodeId.isNullOrBlank())
            ""
        else
            "[${amNode.archetypeNodeId}${nameConstraint?.let { ",'$nameConstraint']" } ?: "]"}"

    private fun createIsmTransitionAttribute(parent: AmNode, attributeName: String, existence: IntervalOfInteger): AmNode =
        createAmNode(parent, "DV_CODED_TEXT", attributeName, IsmTransition::class.java).apply {
            parent.attributes[attributeName] = AmAttribute(existence, Lists.newArrayList(this)).apply { this.rmOnly = true }
        }

    @Suppress("UNCHECKED_CAST")
    private fun createAmNode(parent: AmNode, rmType: String, attributeName: String, parentClass: Class<*>): AmNode =
        AmNode(parent, rmType).apply {
            this.name = attributeName
            this.setter = RmUtils.getSetterForAttribute(attributeName, (parentClass as Class<out RmObject?>))
            val getter = RmUtils.getGetterForAttribute(attributeName, parentClass)
            this.getter = getter
            if (getter != null) {
                val returnType = getter.returnType
                if (MutableCollection::class.java.isAssignableFrom(returnType)) {
                    val fieldType = RmUtils.getFieldType(parentClass, RmUtils.getFieldForAttribute(attributeName))
                    if (MutableList::class.java.isAssignableFrom(returnType)) {
                        this.setType(TypeInfo(fieldType, CollectionInfo(CollectionType.LIST)))
                    } else {
                        this.setType(TypeInfo(fieldType, CollectionInfo(CollectionType.SET)))
                    }
                } else {
                    this.setType(TypeInfo(returnType, null))
                }
            }
        }

    private fun requiresCardinality(amAttribute: AmAttribute): Boolean =
        with(WebTemplateIntegerRange(amAttribute.cardinality?.interval)) {
            if (this.isEmpty()) {
                false
            } else {
                val min = this.min
                val max = this.max
                if (min == null || 0 == min) { // no lower limit
                    false
                } else {
                    val childrenCount = amAttribute.children.size
                    if (bothOne(min, max) && childrenCount == 1)  // only one child - required
                        false
                    else
                        min > 1 || max != null && max < childrenCount
                }
            }
        }

    private fun getCardinalities(amNode: AmNode, node: WebTemplateNode?): MutableList<WebTemplateCardinality> =
        amNode.attributes.entries.asSequence()
            .filter { it.value.cardinality != null && requiresCardinality(it.value) }
            .map { WebTemplateCardinality(WebTemplateIntegerRange(it.value.cardinality?.interval), "${node?.path}/${it.key}") }
            .toMutableList()

    private fun setArchetypeAnnotations(amNode: AmNode, node: WebTemplateNode) {
        val archetypeTerm = findTerm(amNode.terms, amNode.nodeId) // special annotation from Archetype (extra value on term)
        archetypeTerm?.items?.forEach {
            if (AmUtils.TEXT_ID != it.id && "description" != it.id) {
                node.annotations.merge(it.id!!, it.value!!) { first: String, second: String -> "$first;$second" }
            }
        }
    }

    private fun setNodeViewAnnotations(node: WebTemplateNode, viewAnnotations: List<TView.Constraints.Items>) {
        viewAnnotations.forEach {
            val value: Any = it.value
            if (value is org.w3c.dom.Element)
                node.annotations["view:${it.id}"] = value.textContent
            else
                node.annotations["view:${it.id}"] = value.toString()
        }
    }

    private fun processChildren(webTemplateNode: WebTemplateNode, children: List<WebTemplateNode>) {
        if (children.isNotEmpty()) {
            webTemplateNode.children.clear()
            webTemplateNode.children.addAll(children)
            if (postProcess) {
                WebTemplateNodeChildrenPostProcessorDelegator.delegate(webTemplateNode.rmType, webTemplateNode)
            }
        }
    }

    private fun bothOne(min: Int, max: Int?): Boolean = max != null && min == 1 && max == 1

    private fun getMultipleNames(child: AmNode): List<String> = getNameCodePhrase(child)?.codeList ?: emptyList()
}
