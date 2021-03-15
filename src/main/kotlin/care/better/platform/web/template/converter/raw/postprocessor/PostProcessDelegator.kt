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

package care.better.platform.web.template.converter.raw.postprocessor

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.context.RmVisitor
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.PartyRef
import org.openehr.rm.common.Locatable
import org.openehr.rm.common.Participation
import org.openehr.rm.composition.*
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.Event
import org.openehr.rm.datastructures.History
import org.openehr.rm.datastructures.IntervalEvent
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvIdentifier
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 *  Singleton used to delegate the RM object to the [PostProcessor] based on the RM object class.
 */
internal object PostProcessDelegator {
    private val postProcessorMap: Map<Class<*>, PostProcessor<*>> =
        mapOf(
            Pair(Action::class.java, ActionPostProcessor),
            Pair(Activity::class.java, ActivityPostProcessor),
            Pair(Composition::class.java, CompositionPostProcessor),
            Pair(DvText::class.java, DvTextPostProcessor),
            Pair(DvCodedText::class.java, DvCodedTextPostProcessor),
            Pair(Entry::class.java, EntryPostProcessor.getInstance()),
            Pair(EventContext::class.java, EventContextPostProcessor),
            Pair(Event::class.java, EventPostProcessor.getInstance()),
            Pair(GenericId::class.java, GenericIdPostProcessor),
            Pair(History::class.java, HistoryPostProcessor),
            Pair(DvIdentifier::class.java, DvIdentifierPostProcessor),
            Pair(Instruction::class.java, InstructionPostProcessor),
            Pair(IntervalEvent::class.java, IntervalEventPostProcessor),
            Pair(IsmTransition::class.java, IsmTransitionPostProcessor),
            Pair(MutableList::class.java, MutableListPostProcessor),
            Pair(Locatable::class.java, LocatablePostProcessor.getInstance()),
            Pair(Element::class.java, ElementPostProcessor),
            Pair(Observation::class.java, ObservationPostProcessor),
            Pair(Participation::class.java, ParticipationPostProcessor),
            Pair(PartyRef::class.java, PartyRefPostProcessor))

    private val postProcessors: List<PostProcessor<*>> = postProcessorMap.values.sortedBy { it.getOrder() }

    /**
     * Delegates the RM object to the [PostProcessor] and executes the post-processing.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param instance RM object or to the [List] of RM objects
     * @param webTemplatePath Web template path
     */
    @Suppress("UNCHECKED_CAST")
    fun delegate(conversionContext: ConversionContext, amNode: AmNode?, instance: Any?, webTemplatePath: WebTemplatePath?) {
        if (instance == null) {
            return
        }
        val instanceClass = instance::class.java

        if (webTemplatePath != null && instance is RmObject) {
            conversionContext.rmVisitors[instanceClass]?.also { (it as RmVisitor<RmObject>).visit(instance, webTemplatePath.toString()) }
        }

        val processor = postProcessorMap[instanceClass]
        if (processor == null) {
            for (postProcessor in postProcessors) {
                if (postProcessor.accept(instanceClass)) {
                    (postProcessor as PostProcessor<Any>).postProcess(conversionContext, amNode, instance, webTemplatePath)
                    return
                }
            }
        } else {
            (processor as PostProcessor<Any>).postProcess(conversionContext, amNode, instance, webTemplatePath)
        }
    }

    fun getPostProcessors(): Map<Class<*>, PostProcessor<*>> = postProcessorMap
}
