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

package care.better.platform.web.template.converter.raw.special

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.factory.leaf.RmObjectLeafNodeFactoryDelegator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.openehr.rm.datatypes.DvDateTime

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Interface used to handle special case attributes on the RM object in RAW format.
 */
interface SpecialCaseRmObjectHandler {
    companion object {
        private val dvDateTimeRmType: String = RmUtils.getRmTypeName(DvDateTime::class.java)
    }

    /**
     * Converts and handles a special case attribute on the RM object in RAW format.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param arrayNode [ArrayNode]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path.
     * @throws [ConversionException] if RM object has incompatible RM type
     */
    fun handle(conversionContext: ConversionContext, amNode: AmNode, arrayNode: ArrayNode, rmObject: RmObject, webTemplatePath: WebTemplatePath) {
        if (rmObject::class.java == getAcceptableClass())
            handleOnRmObject(conversionContext, amNode, arrayNode[0], rmObject, webTemplatePath)
        else
            throw ConversionException(
                "Special case attribute ${getAcceptableClass()} can only be used on an ${getAcceptableClass().name}",
                webTemplatePath.toString())
    }

    /**
     * Converts and handles a special case attribute on the RM object in RAW format.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param jsonNode [JsonNode]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path.
     */
    fun handleOnRmObject(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: RmObject, webTemplatePath: WebTemplatePath)

    /**
     * Returns the RM object [Class] that can be handled by this [SpecialCaseRmObjectHandler].
     *
     * @return RM object [Class] that can be handled by this [SpecialCaseRmObjectHandler]
     */
    fun getAcceptableClass(): Class<*>

    /**
     * Returns the attribute name that can be handled by this [SpecialCaseRmObjectHandler].
     *
     * @return Attribute name that can be handled by this [SpecialCaseRmObjectHandler]
     */
    fun getAttributeName(): String

    /**
     * Converts [JsonNode] to [DvDateTime] using [RmObjectLeafNodeFactoryDelegator].
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param jsonNode [JsonNode]
     * @param webTemplatePath Web template path.
     */
    fun createDvDateTime(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, webTemplatePath: WebTemplatePath): DvDateTime? =
        if (jsonNode.isArray) {
            jsonNode.mapNotNull {
                RmObjectLeafNodeFactoryDelegator.delegateOrThrow(dvDateTimeRmType, conversionContext, amNode, it, webTemplatePath) as DvDateTime?
            }.firstOrNull()
        } else {
            RmObjectLeafNodeFactoryDelegator.delegateOrThrow(dvDateTimeRmType, conversionContext, amNode, jsonNode, webTemplatePath) as DvDateTime?
        }
}
