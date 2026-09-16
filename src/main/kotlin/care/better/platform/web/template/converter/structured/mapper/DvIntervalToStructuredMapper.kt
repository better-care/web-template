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

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOWER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOWER_INCLUDED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOWER_UNBOUNDED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UPPER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UPPER_INCLUDED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UPPER_UNBOUNDED
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
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
                this.putSingletonAsArray(LOWER) {
                    RmObjectToStructuredMapperDelegator.delegate(webTemplateNode.children.first { child -> child.jsonId == LOWER }, valueConverter, it)
                }
            }
            rmObject.upper?.also {
                this.putSingletonAsArray(UPPER) {
                    RmObjectToStructuredMapperDelegator.delegate(webTemplateNode.children.first { child -> child.jsonId == UPPER }, valueConverter, it)
                }
            }
            if (rmObject.lowerIncluded == false) this.putIfNotNull("|$LOWER_INCLUDED", rmObject.lowerIncluded)
            if (rmObject.upperIncluded == false) this.putIfNotNull("|$UPPER_INCLUDED", rmObject.upperIncluded)
            if (rmObject.lowerUnbounded) this.putIfNotNull("|$LOWER_UNBOUNDED", rmObject.lowerUnbounded)
            if (rmObject.upperUnbounded) this.putIfNotNull("|$UPPER_UNBOUNDED", rmObject.upperUnbounded)
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvInterval): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.lower?.also {
                this.putSingletonAsArray(LOWER) {
                    RmObjectToStructuredMapperDelegator.delegateFormatted(
                        webTemplateNode.children.first { child -> child.jsonId == LOWER },
                        valueConverter,
                        it
                    )
                }
            }
            rmObject.upper?.also {
                this.putSingletonAsArray(UPPER) {
                    RmObjectToStructuredMapperDelegator.delegateFormatted(
                        webTemplateNode.children.first { child -> child.jsonId == UPPER },
                        valueConverter,
                        it
                    )
                }
            }
            if (rmObject.lowerIncluded == false) this.put("|$LOWER_INCLUDED", "false")
            if (rmObject.upperIncluded == false) this.put("|$UPPER_INCLUDED", "false")
            if (rmObject.lowerUnbounded) this.put("|$LOWER_UNBOUNDED", "true")
            if (rmObject.upperUnbounded) this.put("|$UPPER_UNBOUNDED", "true")
            this
        }

}
