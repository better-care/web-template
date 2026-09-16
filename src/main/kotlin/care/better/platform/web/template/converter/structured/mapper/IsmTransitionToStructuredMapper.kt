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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CAREFLOW_STEP
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CURRENT_STATE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.REASON
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TRANSITION
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.composition.IsmTransition

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [IsmTransition] to STRUCTURED format.
 */
internal object IsmTransitionToStructuredMapper : RmObjectToStructuredMapper<IsmTransition> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: IsmTransition): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.currentState?.also {
                this.putSingletonAsArray(CURRENT_STATE) { DvCodedTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.transition?.also {
                this.putSingletonAsArray(TRANSITION) { DvCodedTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.careflowStep?.also {
                this.putSingletonAsArray(CAREFLOW_STEP) { DvCodedTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            this.putCollectionAsArray("_$REASON", rmObject.reason) { DvTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: IsmTransition): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.currentState?.also {
                this.putSingletonAsArray(CURRENT_STATE) { DvCodedTextToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.transition?.also {
                this.putSingletonAsArray(TRANSITION) { DvCodedTextToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.careflowStep?.also {
                this.putSingletonAsArray(CAREFLOW_STEP) { DvCodedTextToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            this.putCollectionAsArray("_$REASON", rmObject.reason) { DvTextToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            this
        }
}
