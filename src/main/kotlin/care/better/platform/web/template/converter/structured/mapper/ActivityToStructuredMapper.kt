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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ACTION_ARCHETYPE_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TIMING
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.raw.factory.node.DEFAULT_ACTION_ARCHETYPE_ID
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.composition.Activity

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [LocatableToStructuredMapper] that maps [Activity] to STRUCTURED format.
 */
internal object ActivityToStructuredMapper : LocatableToStructuredMapper<Activity>() {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Activity): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)
            rmObject.timing?.also {
                this.putSingletonAsArray(TIMING) { DvParsableToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            if (rmObject.actionArchetypeId != null && rmObject.actionArchetypeId != DEFAULT_ACTION_ARCHETYPE_ID) {
                this.putIfNotNull(ACTION_ARCHETYPE_ID, rmObject.actionArchetypeId)
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Activity): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            rmObject.timing?.also {
                this.putSingletonAsArray(TIMING) { DvParsableToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            if (rmObject.actionArchetypeId != null && rmObject.actionArchetypeId != DEFAULT_ACTION_ARCHETYPE_ID) {
                this.putIfNotNull(ACTION_ARCHETYPE_ID, rmObject.actionArchetypeId)
            }
            this
        }
}
