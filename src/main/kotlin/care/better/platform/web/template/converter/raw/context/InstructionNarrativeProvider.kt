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

import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @author Bostjan Lah
 * @since 3.1.0
 *
 * Interface that provides INSTRUCTION.narrative during the RM object conversion from STRUCTURED to RAW format.
 * Narrative is a human readable content of the instruction, for example: "Take 2 pills every 6 hours".
 */
fun interface InstructionNarrativeProvider {
    /**
     * Gets INSTRUCTION.narrative at specified web template path.
     *
     * @param webTemplatePath Web template path
     * @return Instruction narrative as [DvText]
     */
    fun getNarrative(webTemplatePath: String): DvText?
}
