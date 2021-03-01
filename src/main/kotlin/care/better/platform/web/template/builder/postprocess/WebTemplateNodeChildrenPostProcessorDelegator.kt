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

package care.better.platform.web.template.builder.postprocess

import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.composition.*
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.Event
import org.openehr.rm.datastructures.IntervalEvent
import org.openehr.rm.datastructures.PointEvent
import org.openehr.rm.datatypes.DvParsable
import org.openehr.rm.datatypes.DvProportion

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton used to delegate the RM object to the [WebTemplateNodeChildrenPostProcessor] based on the RM object type.
 *
 * @constructor Creates a new instance of [WebTemplateNodeChildrenPostProcessorDelegator]
 */
internal object WebTemplateNodeChildrenPostProcessorDelegator {

    private val webTemplateNodeChildrenPostProcessors: Map<String, WebTemplateNodeChildrenPostProcessor> =
        mapOf(
            Pair(RmUtils.getRmTypeName(Element::class.java), ElementChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(Action::class.java), ActionChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(Evaluation::class.java), EvaluationChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(Instruction::class.java), InstructionChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(Observation::class.java), ObservationChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(DvProportion::class.java), ProportionChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(DvParsable::class.java), ParsableChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(Event::class.java), EventChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(PointEvent::class.java), EventChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(IntervalEvent::class.java), EventChildrenPostProcessor()),
            Pair(RmUtils.getRmTypeName(Composition::class.java), CompositionChildrenPostProcessor()))

    /**
     * Delegates the [WebTemplateNode] to the [WebTemplateNodeChildrenPostProcessor] and executes the post-processing.
     *
     * @param rmType RM type
     * @param webTemplateNode [WebTemplateNode]
     */
    fun delegate(rmType: String, webTemplateNode: WebTemplateNode) {
        webTemplateNodeChildrenPostProcessors[rmType]?.postProcess(webTemplateNode)
    }
}
