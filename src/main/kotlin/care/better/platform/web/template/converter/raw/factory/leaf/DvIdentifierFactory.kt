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

package care.better.platform.web.template.converter.raw.factory.leaf

import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvIdentifier

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [DvIdentifier].
 */
internal object DvIdentifierFactory : RmObjectLeafNodeFactory<DvIdentifier>() {

    override fun createInstance(attributes: Set<AttributeDto>): DvIdentifier = DvIdentifier()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvIdentifier,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        when {
            attribute.attribute.isBlank() || attribute.attribute == "id" -> {
                rmObject.id = jsonNode.asText()
                true
            }
            attribute.attribute == "issuer" -> {
                rmObject.issuer = jsonNode.asText()
                true
            }
            attribute.attribute == "assigner" -> {
                rmObject.assigner = jsonNode.asText()
                true
            }
            attribute.attribute == "type" -> {
                rmObject.type = jsonNode.asText()
                true
            }
            else -> false
        }
}
