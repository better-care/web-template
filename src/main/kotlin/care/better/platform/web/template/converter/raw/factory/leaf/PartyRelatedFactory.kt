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
import care.better.platform.template.AmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromAmNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.rm.common.PartyRelated
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [PartyRelated].
 */
internal object PartyRelatedFactory : RmObjectLeafNodeFactory<PartyRelated>() {
    override fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): PartyRelated =
        throw ConversionException("${amNode.rmType} can not be created from simple value", webTemplatePath.toString())

    override fun createInstance(attributes: Set<AttributeDto>): PartyRelated = PartyRelated()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: PartyRelated,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        PartyIdentifiedFactory.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath)

    override fun afterPropertiesSet(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: PartyRelated) {
        AmUtils.getAmNode(amNode, "relationship")?.also { rmObject.relationship = DvCodedText.createFromAmNode(it) }
    }
}
