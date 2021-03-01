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
import com.fasterxml.jackson.databind.node.ObjectNode
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.datatypes.DvInterval

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [DvInterval] for a normal range to STRUCTURED format.
 */
internal object NormalRangeToStructuredMapper : RmObjectToStructuredMapper<DvInterval> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvInterval): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)
            this
        }

    fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvInterval, objectNode: ObjectNode) {
        rmObject.lower?.also {
            objectNode.putSingletonAsArray("lower") { RmObjectToStructuredMapperDelegator.delegate(webTemplateNode, valueConverter, it) }
        }
        rmObject.upper?.also {
            objectNode.putSingletonAsArray("upper") { RmObjectToStructuredMapperDelegator.delegate(webTemplateNode, valueConverter, it) }
        }
    }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvInterval): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            this
        }

    fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvInterval, objectNode: ObjectNode) {
        rmObject.lower?.also {
            objectNode.putSingletonAsArray("lower") { RmObjectToStructuredMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it) }
        }
        rmObject.upper?.also {
            objectNode.putSingletonAsArray("upper") { RmObjectToStructuredMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it) }
        }
    }
}
