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

package care.better.platform.web.template.converter.structured.mapper

import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.marand.thinkehr.web.build.WebTemplateNode
import org.openehr.rm.composition.Instruction

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [EntryToStructuredMapper] that maps [Instruction] to STRUCTURED format.
 */
internal object InstructionToStructuredMapper : EntryToStructuredMapper<Instruction>() {

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Instruction): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)
            rmObject.wfDefinition?.also {
                this.putSingletonAsArray("_wf_definition") { DvParsableToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Instruction): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            rmObject.wfDefinition?.also {
                this.putSingletonAsArray("_wf_definition") { DvParsableToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }

            }
            this
        }
}
