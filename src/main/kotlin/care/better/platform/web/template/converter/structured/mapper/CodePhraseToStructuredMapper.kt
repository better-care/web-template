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
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.datatypes.CodePhrase

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [CodePhrase] to STRUCTURED format.
 */
internal object CodePhraseToStructuredMapper : RmObjectToStructuredMapper<CodePhrase> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: CodePhrase): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapCodePhrase(rmObject, this)
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: CodePhrase): JsonNode =
        map(webTemplateNode, valueConverter, rmObject)

    private fun mapCodePhrase(codePhrase: CodePhrase, objectNode: ObjectNode) {
        objectNode.putIfNotNull("|code", codePhrase.codeString)
        objectNode.putIfNotNull("|terminology", codePhrase.terminologyId?.value)
        objectNode.putIfNotNull("|preferred_term", codePhrase.preferredTerm)
    }
}
