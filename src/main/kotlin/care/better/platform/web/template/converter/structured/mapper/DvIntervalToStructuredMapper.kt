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
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.datatypes.DvInterval

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [DvInterval] to STRUCTURED format.
 */
internal object DvIntervalToStructuredMapper : RmObjectToStructuredMapper<DvInterval> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvInterval): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.lower?.also {
                this.putSingletonAsArray("lower") {
                    RmObjectToStructuredMapperDelegator.delegate(webTemplateNode.children.first { child -> child.jsonId == "lower" }, valueConverter, it)
                }
            }
            rmObject.upper?.also {
                this.putSingletonAsArray("upper") {
                    RmObjectToStructuredMapperDelegator.delegate(webTemplateNode.children.first { child -> child.jsonId == "upper" }, valueConverter, it)
                }
            }
            this.putIfNotNull("|lower_included", rmObject.lowerIncluded)
            this.putIfNotNull("|upper_included", rmObject.upperIncluded)

            this.putIfNotNull("|lower_unbounded", rmObject.lowerUnbounded)
            this.putIfNotNull("|upper_unbounded", rmObject.upperUnbounded)
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvInterval): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.lower?.also {
                this.putSingletonAsArray("lower") {
                    RmObjectToStructuredMapperDelegator.delegateFormatted(
                        webTemplateNode.children.first { child -> child.jsonId == "lower" },
                        valueConverter,
                        it)
                }
            }
            rmObject.upper?.also {
                this.putSingletonAsArray("upper") {
                    RmObjectToStructuredMapperDelegator.delegateFormatted(
                        webTemplateNode.children.first { child -> child.jsonId == "upper" },
                        valueConverter,
                        it)
                }
            }
            this.put("|lower_included", rmObject.lowerIncluded?.toString())
            this.put("|upper_included", rmObject.upperIncluded?.toString())

            this.put("|lower_unbounded", rmObject.lowerUnbounded.toString())
            this.put("|upper_unbounded", rmObject.upperUnbounded.toString())
            this
        }

}
