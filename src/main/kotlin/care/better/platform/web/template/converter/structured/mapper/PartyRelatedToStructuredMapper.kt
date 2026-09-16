/* Copyright 2026 Better Ltd (www.better.care)
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

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.RELATIONSHIP
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openehr.rm.common.PartyRelated

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [PartyRelated] to STRUCTURED format.
 */
internal object PartyRelatedToStructuredMapper : RmObjectToStructuredMapper<PartyRelated> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: PartyRelated): JsonNode {
        val node = PartyIdentifiedToStructuredMapper.map(webTemplateNode, valueConverter, rmObject) as ObjectNode
        rmObject.relationship?.also {
            node.putSingletonAsArray(RELATIONSHIP) { DvCodedTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
        }
        return node
    }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: PartyRelated): JsonNode {
        val node = PartyIdentifiedToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, rmObject) as ObjectNode
        rmObject.relationship?.also {
            node.putSingletonAsArray(RELATIONSHIP) { DvCodedTextToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
        }
        return node
    }
}
