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

package care.better.platform.web.template.converter.raw.context

import care.better.platform.path.NameAndNodeMatchingPathValueExtractor
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SELF_REFERENCE_COMPOSITION
import org.openehr.rm.common.Link
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Instruction
import org.openehr.rm.composition.InstructionDetails

/**
 * @author Primoz Delopst
 * @author Bostjan Lah
 * @since 3.1.0
 *
 * Interface used to handle [InstructionDetails] during the RM object conversion from STRUCTURED to RAW format.
 */
interface ActionToInstructionHandler {
    /**
     * Handles [InstructionDetails].
     *
     * @param composition [Composition] created during RM object conversion.
     * @param instructionDetails [InstructionDetails]
     * @param instructionDetailsData [InstructionDetailsData]
     * @param conversionContext [ConversionContext]
     */
    fun handle(
            composition: Composition,
            instructionDetails: InstructionDetails,
            instructionDetailsData: InstructionDetailsData,
            conversionContext: ConversionContext)

    /**
     * Resolves path from the [InstructionDetails].
     *
     * @return Resolved path
     */
    fun resolvePath(
            composition: Composition,
            instructionDetails: InstructionDetails,
            instructionDetailsData: InstructionDetailsData,
            conversionContext: ConversionContext): String?

}

/**
 * Implementation of [ActionToInstructionHandler] that handles [InstructionDetails]
 * for the [Composition] created during the RM object conversion from STRUCTURED to RAW format.
 *
 * @constructor Creates a new instance of [InCompositionActionToInstructionHandler]
 */
open class InCompositionActionToInstructionHandler : ActionToInstructionHandler {
    override fun handle(
            composition: Composition,
            instructionDetails: InstructionDetails,
            instructionDetailsData: InstructionDetailsData,
            conversionContext: ConversionContext) {
        instructionDetails.instructionId?.id?.value?.also {
            val resolvedComposition = resolve(composition, instructionDetails, instructionDetailsData, conversionContext)

            if (resolvedComposition != null) {
                val pathValueExtractor = NameAndNodeMatchingPathValueExtractor(instructionDetails.instructionId?.path!!)
                val instructions = pathValueExtractor.getValue(resolvedComposition, true)
                if (instructions.isNotEmpty() && instructions[0] is Instruction) {
                    val instruction = instructions[0] as Instruction
                    if (instruction.activities.isNotEmpty()) {
                        if (instruction.activities.size > 1) {
                            if (instructionDetailsData.activityIndex != null) {
                                val index: Int = instructionDetailsData.activityIndex
                                if (instruction.activities.size > index) {
                                    val activity = instruction.activities[index]
                                    val name = activity.name?.value!!
                                    if (name.contains("#")) {
                                        instructionDetails.activityId = "activities[" + activity.archetypeNodeId + ",'" + Link.quote(name) + "']"
                                    } else {
                                        instructionDetails.activityId = "activities[" + activity.archetypeNodeId + ',' + Link.getNameSuffix(name, index) + ']'
                                    }
                                }
                            }
                        } else {
                            val activity = instruction.activities[0]
                            instructionDetails.activityId = "activities[" + activity.archetypeNodeId + ']'
                        }
                    }
                }
            }

        }
    }

    override fun resolvePath(
            composition: Composition,
            instructionDetails: InstructionDetails,
            instructionDetailsData: InstructionDetailsData,
            conversionContext: ConversionContext): String? = null


    open fun resolve(
            composition: Composition,
            instructionDetails: InstructionDetails,
            instructionDetailsData: InstructionDetailsData,
            conversionContext: ConversionContext): Composition? =
        if (SELF_REFERENCE_COMPOSITION == instructionDetails.instructionId?.id?.value)
            composition
        else
            null
}
