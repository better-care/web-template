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

package care.better.platform.web.template.converter.raw.generics

import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.common.FeederAudit
import org.openehr.rm.common.FeederAuditDetails
import org.openehr.rm.datatypes.DvParsable

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton used to extract ctx generic fields from the RM object in STRUCTURED format.
 */
internal object GenericFieldExtractor : (JsonNode) -> FeederAudit? {

    /**
     * Extracts [FeederAudit] from ctx generic fields from the RM object in STRUCTURED format.
     *
     * @param node RM object in STRUCTURED format
     * @return [FeederAudit] if generic fields exist, otherwise, return null
     */
    override fun invoke(node: JsonNode): FeederAudit? {

        val structuredGenericFieldNode = node.path("ctx").path("generic_fields")
        val flatGenericFieldNode = node.path("ctx/generic_fields")

        return when {
            structuredGenericFieldNode.isMissingNode && flatGenericFieldNode.isMissingNode -> null
            !structuredGenericFieldNode.isMissingNode && !flatGenericFieldNode.isMissingNode -> {
                throw ConversionException("Generic fields were passed in both FLAT and STRUCTURED format.")
            }
            flatGenericFieldNode.isMissingNode -> createFeederAudit(structuredGenericFieldNode)
            else -> createFeederAudit(flatGenericFieldNode)
        }
    }

    private fun createFeederAudit(jsonNode: JsonNode): FeederAudit =
        FeederAudit().apply {
            this.originalContent = DvParsable().apply {
                this.formalism = "application/json"
                this.value = ConversionObjectMapper.writeValueAsString(jsonNode)
            }
            this.originatingSystemAudit = FeederAuditDetails().apply {
                this.systemId = "FormRenderer"
            }
        }
}
