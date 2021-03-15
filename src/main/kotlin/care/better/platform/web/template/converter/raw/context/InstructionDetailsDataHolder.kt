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

import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SELF_REFERENCE_COMPOSITION
import org.openehr.rm.composition.InstructionDetails

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Holds [InstructionDetailsData] for [InstructionDetails].
 *
 * @constructor Creates a new instance of [InstructionDetailsDataHolder]
 */
internal class InstructionDetailsDataHolder {
    private val instructionDetailsData: MutableMap<InstructionDetails, InstructionDetailsData.Builder> = mutableMapOf()

    /**
     * Computes [InstructionDetailsData.Builder] for the [InstructionDetails] if absent.
     *
     * @param instructionDetails [InstructionDetails]
     * @return [InstructionDetailsData.Builder] for the [InstructionDetails]
     */
    fun computeIfAbsent(instructionDetails: InstructionDetails): InstructionDetailsData.Builder =
        instructionDetailsData.computeIfAbsent(instructionDetails) { InstructionDetailsData.create() }

    /**
     * Returns the [Map] of [InstructionDetails] and [InstructionDetailsData].
     *
     * @return [Map] of [InstructionDetails] and [InstructionDetailsData]
     */
    fun getAll(): Map<InstructionDetails, InstructionDetailsData> =
        instructionDetailsData.entries.associateBy({ it.key }, { it.value.build() })
}

/**
 * Holds details for the [InstructionDetails].
 *
 * @constructor Creates a new instance of [InstructionDetailsData]
 * @param instructionIndex Index of the instruction
 * @param activityIndex Index of the activity
 * @param instructionNode UUID of the instruction
 * @param webTemplatePath Web template path
 * @param instructionNode Instruction [AmNode]
 * @param referenceSelfComposition [Boolean] if the [InstructionDetails] is referencing self composition
 */
class InstructionDetailsData private constructor(
        val instructionIndex: Int? = null,
        val activityIndex: Int? = null,
        val instructionUid: String? = null,
        val webTemplatePath: String? = null,
        val instructionNode: AmNode? = null,
        val referenceSelfComposition: Boolean = false) {

    companion object {
        /**
         * Creates a new instance of [Builder].
         *
         * @return [Builder]
         */
        @JvmStatic
        fun create(): Builder = Builder()
    }

    /**
     * Builder used to create [InstructionDetailsData].
     *
     * @constructor Creates a new instance of [Builder]
     * @param instructionIndex Index of the instruction
     * @param activityIndex Index of the activity
     * @param instructionNode UUID of the instruction
     * @param webTemplatePath Web template path
     * @param instructionNode Instruction [AmNode]
     * @param referenceSelfComposition [Boolean] if [InstructionDetails] is referencing self composition
     */
    class Builder(
            private var instructionIndex: Int? = null,
            private var activityIndex: Int? = null,
            private var instructionUid: String? = null,
            private var webTemplatePath: String? = null,
            private var instructionNode: AmNode? = null,
            private var referenceSelfComposition: Boolean = false) {


        fun withInstructionIndex(instructionIndex: Int) = apply { this.instructionIndex = instructionIndex }
        fun withActivityIndex(activityIndex: Int) = apply { this.activityIndex = activityIndex }
        fun withInstructionUid(instructionUid: String) = apply { this.instructionUid = instructionUid }
        fun withWebTemplatePath(webTemplatePath: String) = apply { this.webTemplatePath = webTemplatePath }
        fun withInstructionNode(instructionNode: AmNode) = apply { this.instructionNode = instructionNode }
        fun isReferenceSelfComposition(compositionUid: String) = apply { this.referenceSelfComposition = compositionUid == SELF_REFERENCE_COMPOSITION }

        /**
         * Creates a new instance of [InstructionDetailsData].
         *
         * @return [InstructionDetailsData]
         */
        fun build(): InstructionDetailsData = InstructionDetailsData(
            instructionIndex,
            activityIndex,
            instructionUid,
            webTemplatePath,
            instructionNode,
            referenceSelfComposition)
    }
}
