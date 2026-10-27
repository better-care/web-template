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

package care.better.platform.web.template.converter.raw.factory.node

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.factory.leaf.RmObjectLeafNodeFactory
import org.openehr.rm.common.Participation
import org.openehr.rm.composition.*
import org.openehr.rm.datastructures.*
import org.openehr.rm.datatypes.DvInterval
import org.openehr.rm.integration.GenericEntry

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton that delegates to the [RmObjectNodeFactory] instance based on [RmObject] RM name.
 * Note that [RmObjectNodeFactory] are defined for all nodes except leaf nodes.
 * Factories for leaf nodes are defined with a [RmObjectLeafNodeFactory] interface.
 */
object RmObjectNodeFactoryDelegator {

    private val rmObjectNodeFactories: Map<String, RmObjectNodeFactory<out RmObject>> = mapOf(
        Pair(RmUtils.getRmTypeName(Action::class.java), ActionFactory),
        Pair(RmUtils.getRmTypeName(Activity::class.java), ActivityFactory),
        Pair(RmUtils.getRmTypeName(AdminEntry::class.java), EntryInstanceFactory { AdminEntry() }),
        Pair(RmUtils.getRmTypeName(Cluster::class.java), LocatableInstanceFactory { Cluster() }),
        Pair(RmUtils.getRmTypeName(Composition::class.java), CompositionFactory),
        Pair(RmUtils.getRmTypeName(DvInterval::class.java), RmObjectInstanceFactory { DvInterval() }),
        Pair(RmUtils.getRmTypeName(Element::class.java), ElementFactory),
        Pair(RmUtils.getRmTypeName(Evaluation::class.java), EntryInstanceFactory { Evaluation() }),
        Pair(RmUtils.getRmTypeName(Event::class.java), EventInstanceFactory { PointEvent() }),
        Pair(RmUtils.getRmTypeName(EventContext::class.java), RmObjectInstanceFactory { EventContext() }),
        Pair(RmUtils.getRmTypeName(History::class.java), LocatableInstanceFactory { History() }),
        Pair(RmUtils.getRmTypeName(Instruction::class.java), InstructionFactory),
        Pair(RmUtils.getRmTypeName(IntervalEvent::class.java), IntervalEventFactory),
        Pair(RmUtils.getRmTypeName(IsmTransition::class.java), RmObjectInstanceFactory { IsmTransition() }),
        Pair(RmUtils.getRmTypeName(ItemList::class.java), LocatableInstanceFactory { ItemList() }),
        Pair(RmUtils.getRmTypeName(ItemSingle::class.java), LocatableInstanceFactory { ItemSingle() }),
        Pair(RmUtils.getRmTypeName(ItemTable::class.java), LocatableInstanceFactory { ItemTable() }),
        Pair(RmUtils.getRmTypeName(ItemTree::class.java), LocatableInstanceFactory { ItemTree() }),
        Pair(RmUtils.getRmTypeName(Observation::class.java), EntryInstanceFactory { Observation() }),
        Pair(RmUtils.getRmTypeName(Participation::class.java), RmObjectInstanceFactory { Participation() }),
        Pair(RmUtils.getRmTypeName(PointEvent::class.java), EventInstanceFactory { PointEvent() }),
        Pair(RmUtils.getRmTypeName(Section::class.java), LocatableInstanceFactory { Section() }),
        Pair(RmUtils.getRmTypeName(GenericEntry::class.java), LocatableInstanceFactory { GenericEntry() }))


    /**
     * Delegates [RmObject] initialization to the [RmObjectNodeFactory] based on the RM type and returns newly created instance.
     *
     * @param rmType [RmObject] RM type
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param webTemplatePath [WebTemplatePath]
     * @return New instance of the [RmObject]
     * @throws [ConversionException] if [RmObjectNodeFactory] is not found
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegateOrThrow(rmType: String, conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T {
        val factory = rmObjectNodeFactories[RmUtils.getNonGenericRmNamePart(rmType)]
            ?: throw ConversionException("RM object node factory for $rmType not found.")

        return (factory as RmObjectNodeFactory<T>).create(conversionContext, amNode, webTemplatePath)
    }

    /**
     * Delegates [RmObject] initialization to the [RmObjectNodeFactory] based on the RM type and returns newly created instance.
     *
     * @param rmType [RmObject] RM type
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param webTemplatePath [WebTemplatePath]
     * @return New instance of the [RmObject] if [RmObjectNodeFactory] is found, otherwise, null
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegateOrNull(rmType: String, conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T? {
        val factory = rmObjectNodeFactories[RmUtils.getNonGenericRmNamePart(rmType)] ?: return null

        return (factory as RmObjectNodeFactory<T>).create(conversionContext, amNode, webTemplatePath)
    }
}
