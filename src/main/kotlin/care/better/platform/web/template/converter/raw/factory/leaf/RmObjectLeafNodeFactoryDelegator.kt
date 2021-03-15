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
import care.better.platform.template.AmNode
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.factory.node.RmObjectNodeFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openehr.base.basetypes.*
import org.openehr.rm.common.*
import org.openehr.rm.composition.InstructionDetails
import org.openehr.rm.datatypes.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton that delegates to the [RmObjectLeafNodeFactory] instance based on [RmObject] RM type.
 * Note that [RmObjectLeafNodeFactory] are defined for all leaf nodes.
 * Factories for non-leaf nodes are defined with a [RmObjectNodeFactory] interface.
 */
object RmObjectLeafNodeFactoryDelegator {

    private val rmObjectLeafNodeFactories: Map<String, RmObjectLeafNodeFactory<out RmObject>> =
        mapOf(
            Pair(RmUtils.getRmTypeName(CodePhrase::class.java), CodePhraseFactory),
            Pair(RmUtils.getRmTypeName(DvBoolean::class.java), DvBooleanFactory),
            Pair(RmUtils.getRmTypeName(DvCodedText::class.java), DvCodedTextFactory.getInstance()),
            Pair(RmUtils.getRmTypeName(DvCount::class.java), DvCountFactory),
            Pair(RmUtils.getRmTypeName(DvDate::class.java), DvDateFactory),
            Pair(RmUtils.getRmTypeName(DvDateTime::class.java), DvDateTimeFactory),
            Pair(RmUtils.getRmTypeName(DvDuration::class.java), DvDurationFactory),
            Pair(RmUtils.getRmTypeName(DvEhrUri::class.java), DvEhrUriFactory),
            Pair(RmUtils.getRmTypeName(DvIdentifier::class.java), DvIdentifierFactory),
            Pair(RmUtils.getRmTypeName(DvMultimedia::class.java), DvMultimediaFactory),
            Pair(RmUtils.getRmTypeName(DvOrdinal::class.java), DvOrdinalFactory),
            Pair(RmUtils.getRmTypeName(DvParsable::class.java), DvParsableFactory),
            Pair(RmUtils.getRmTypeName(DvProportion::class.java), DvProportionFactory),
            Pair(RmUtils.getRmTypeName(DvQuantity::class.java), DvQuantityFactory),
            Pair(RmUtils.getRmTypeName(DvScale::class.java), DvScaleFactory),
            Pair(RmUtils.getRmTypeName(DvText::class.java), DvTextFactory),
            Pair(RmUtils.getRmTypeName(DvTime::class.java), DvTimeFactory),
            Pair(RmUtils.getRmTypeName(DvUri::class.java), DvUriFactory),
            Pair(RmUtils.getRmTypeName(FeederAudit::class.java), FeederAuditFactory),
            Pair(RmUtils.getRmTypeName(HierObjectId::class.java), HierObjectIdFactory),
            Pair(RmUtils.getRmTypeName(UidBasedId::class.java), HierObjectIdFactory),
            Pair(RmUtils.getRmTypeName(GenericId::class.java), GenericIdFactory),
            Pair(RmUtils.getRmTypeName(ObjectId::class.java), GenericIdFactory),
            Pair(RmUtils.getRmTypeName(ObjectVersionId::class.java), HierObjectIdFactory),
            Pair(RmUtils.getRmTypeName(InstructionDetails::class.java), InstructionDetailsFactory),
            Pair(RmUtils.getRmTypeName(Link::class.java), LinkFactory),
            Pair(RmUtils.getRmTypeName(Participation::class.java), ParticipationFactory),
            Pair(RmUtils.getRmTypeName(ObjectRef::class.java), ObjectRefFactory),
            Pair(RmUtils.getRmTypeName(PartyRef::class.java), PartyRefFactory),
            Pair(RmUtils.getRmTypeName(LocatableRef::class.java), ObjectRefFactory),
            Pair(RmUtils.getRmTypeName(AccessGroupRef::class.java), ObjectRefFactory),
            Pair(RmUtils.getRmTypeName(PartyIdentified::class.java), PartyIdentifiedFactory),
            Pair(RmUtils.getRmTypeName(PartyProxy::class.java), PartyIdentifiedFactory),
            Pair(RmUtils.getRmTypeName(PartyRelated::class.java), PartyRelatedFactory))

    /**
     * Delegates [RmObject] initialization to the [RmObjectLeafNodeFactory] based on the RM type and returns newly created instance.
     *
     * @param rmType [RmObject] RM type
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param node RM object in STRUCTURED format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @param parents Parent created in [AmNode] chain before leaf node
     * @return New instance of the [RmObject]
     * @throws [ConversionException] if [RmObjectLeafNodeFactory] is not found
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    internal fun <T : RmObject> delegateOrThrow(
            rmType: String,
            conversionContext: ConversionContext,
            amNode: AmNode,
            node: JsonNode,
            webTemplatePath: WebTemplatePath,
            parents: List<Any> = listOf()): T? {
        val factory = rmObjectLeafNodeFactories[RmUtils.getNonGenericRmNamePart(rmType)]
            ?: throw  ConversionException("RM object leaf node factory for $rmType not found.")

        return (factory as RmObjectLeafNodeFactory<T>).create(conversionContext, amNode, node, webTemplatePath, parents)
    }

    /**
     * Delegates [RmObject] initialization to the [RmObjectLeafNodeFactory] based on the RM type and returns newly created instance.
     *
     * @param rmType [RmObject] RM type
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param node RM object in STRUCTURED format
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @param parents Parent created in [AmNode] chain before leaf node
     * @return New instance of the [RmObject] if [RmObjectNodeFactory] is found, otherwise, null
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    internal fun <T : RmObject> delegateOrNull(
            rmType: String,
            conversionContext: ConversionContext,
            amNode: AmNode,
            node: JsonNode,
            webTemplatePath: WebTemplatePath,
            parents: List<Any> = listOf()): T? {
        val factory = rmObjectLeafNodeFactories[RmUtils.getNonGenericRmNamePart(rmType)] ?: return null

        return (factory as RmObjectLeafNodeFactory<T>).create(conversionContext, amNode, node, webTemplatePath, parents)
    }


    /**
     * Delegates [RmObject] initialization to the [RmObjectLeafNodeFactory] based on the RM type and returns newly created instance.
     *
     * @param rmType [RmObject] RM type
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @return New instance of the [RmObject]
     * @throws [ConversionException] if [RmObjectLeafNodeFactory] is not found
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegateOrThrow(
            rmType: String,
            conversionContext: ConversionContext,
            amNode: AmNode?,
            webTemplatePath: WebTemplatePath?): T {
        val factory = rmObjectLeafNodeFactories[RmUtils.getNonGenericRmNamePart(rmType)]
            ?: throw  ConversionException("RM object leaf node factory for $rmType not found.")

        return (factory as RmObjectLeafNodeFactory<T>).create(conversionContext, amNode, webTemplatePath)
    }

    /**
     * Delegates [RmObject] initialization to the [RmObjectLeafNodeFactory] based on the RM type and returns newly created instance.
     *
     * @param rmType [RmObject] RM type
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param webTemplatePath Web template path from root to current node [WebTemplatePath]
     * @return New instance of the [RmObject] if [RmObjectNodeFactory] is found, otherwise, null
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegateOrNull(
            rmType: String,
            conversionContext: ConversionContext,
            amNode: AmNode?,
            webTemplatePath: WebTemplatePath?): T? {
        val factory = rmObjectLeafNodeFactories[RmUtils.getNonGenericRmNamePart(rmType)] ?: return null

        return (factory as RmObjectLeafNodeFactory<T>).create(conversionContext, amNode, webTemplatePath)
    }

    /**
     * Delegates [RmObject] [WebTemplateInput] handling to the [RmObjectLeafNodeFactory] based on the RM type.
     *
     * @param rmType [RmObject] RM type
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param rmObject RM object in RAW format
     * @param webTemplateInput [WebTemplateInput]
     * @return New instance of the [RmObject] if [RmObjectNodeFactory] is found, otherwise, null
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    internal fun <T : RmObject> delegateWebTemplateInputHandling(
            rmType: String,
            conversionContext: ConversionContext,
            amNode: AmNode,
            rmObject: T,
            webTemplateInput: WebTemplateInput) {
        val factory = rmObjectLeafNodeFactories[RmUtils.getNonGenericRmNamePart(rmType)]
            ?: throw  ConversionException("RM object leaf node factory for $rmType not found.")

        (factory as RmObjectLeafNodeFactory<T>).handleWebTemplateInput(conversionContext, amNode, rmObject, webTemplateInput)
    }

    /**
     * Delegates checking if [ObjectNode] is empty to the [RmObjectLeafNodeFactory] based on the RM type..
     *
     * @param node RM object in STRUCTURED format
     * @return Boolean indicating if [ObjectNode] is empty or not.
     */
    @Suppress("UNCHECKED_CAST")
    internal fun delegateIsEmpty(node: ObjectNode): Boolean =
        rmObjectLeafNodeFactories.values.asSequence()
            .filter { it.canRemoveDependantValues() }
            .any { it.isStructuredRmObjectEmpty(node)  }
}
