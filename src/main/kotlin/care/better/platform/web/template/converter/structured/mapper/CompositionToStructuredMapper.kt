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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CATEGORY
import care.better.platform.web.template.converter.constant.WebTemplateConstants.COMPOSER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TERRITORY
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.composition.Composition

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [LocatableToStructuredMapper] that maps [Composition] to STRUCTURED format.
 */
internal object CompositionToStructuredMapper : LocatableToStructuredMapper<Composition>() {

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Composition): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)

            rmObject.language?.also {
                this.putSingletonAsArray(LANGUAGE) { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }

            rmObject.territory?.also {
                this.putSingletonAsArray(TERRITORY) { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }

            rmObject.category?.also {
                this.putSingletonAsArray(CATEGORY) { DvCodedTextToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }

            rmObject.composer?.also {
                this.putSingletonAsArray(COMPOSER) { RmObjectToStructuredMapperDelegator.delegate(webTemplateNode, valueConverter, it) }
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: Composition): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)

            rmObject.language?.also {
                this.putSingletonAsArray(LANGUAGE) { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }

            rmObject.territory?.also {
                this.putSingletonAsArray(TERRITORY) { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }

            rmObject.category?.also {
                this.putSingletonAsArray(CATEGORY) { DvCodedTextToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }

            rmObject.composer?.also {
                this.putSingletonAsArray(COMPOSER) { RmObjectToStructuredMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it) }
            }

            this
        }
}
