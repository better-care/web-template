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
import org.openehr.base.basetypes.HierObjectId

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [HierObjectId].
 */
internal object HierObjectIdFactory : RmObjectLeafNodeFactory<HierObjectId>() {

    override fun createInstance(attributes: Set<AttributeDto>): HierObjectId = HierObjectId()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: HierObjectId,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        if (attribute.attribute.isBlank() || attribute.attribute == "value") {
            rmObject.value = jsonNode.asText()
            true
        } else {
            false
        }
}
