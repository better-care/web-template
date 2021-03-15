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
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.am.aom.CDvQuantity
import org.openehr.am.aom.CQuantityItem
import org.openehr.rm.datatypes.DvQuantity

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedFactory] that creates a new instance of [DvQuantity].
 */
internal object DvQuantityFactory : DvQuantifiedFactory<DvQuantity>() {

    override fun createInstance(attributes: Set<AttributeDto>): DvQuantity = DvQuantity()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: DvQuantity,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        super.handleField(conversionContext, amNode, attribute, rmObject, jsonNode, webTemplatePath) ||
                if (attribute.attribute.isBlank() || attribute.attribute == "magnitude" || attribute.attribute == "value") {
                    if (jsonNode.isNumber) {
                        rmObject.magnitude = jsonNode.numberValue().toDouble()
                    } else {
                        rmObject.magnitude = conversionContext.valueConverter.parseDouble(jsonNode.asText())
                    }
                    true
                } else if (attribute.attribute == "precision") {
                    try {
                        rmObject.precision = jsonNode.asText().toInt()
                        true
                    } catch (e: NumberFormatException) {
                        throw ConversionException("Invalid value for attribute 'precision' on DV_QUANTITY: ${jsonNode.asText()}", e, webTemplatePath.toString())
                    }
                } else if (attribute.attribute == "unit") {
                    rmObject.units = jsonNode.asText()
                    true
                } else if (attribute.attribute == "unit_system") {
                    rmObject.unitsSystem = jsonNode.asText()
                    true
                } else if (attribute.attribute == "unit_display_name") {
                    rmObject.unitsDisplayName = jsonNode.asText()
                    true
                } else {
                    false
                }


    override fun afterPropertiesSet(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvQuantity) {
        if (rmObject.units == null) {
            if (amNode.cObject is CDvQuantity) {
                val cDvQuantity = amNode.cObject as CDvQuantity
                if (cDvQuantity.list.size == 1) {
                    val item = cDvQuantity.list[0]
                    rmObject.units = item.units
                    updatePrecision(rmObject, item)
                }
            }
        }
        if (rmObject.precision == null) {
            if (amNode.cObject is CDvQuantity) {
                val cDvQuantity = amNode.cObject as CDvQuantity
                cDvQuantity.list.firstOrNull { it.units == rmObject.units }?.also {
                    updatePrecision(rmObject, it)
                }
            }
        }
    }

    override fun removeDependentValues(map: MutableMap<AttributeDto, JsonNode>): Boolean {
        if (map[AttributeDto.forAttribute("unit")] != null && map[AttributeDto.forAttribute("value")] == null && map[AttributeDto.forAttribute("magnitude")] == null && map[AttributeDto.ofBlank()] == null) {
            map.remove(AttributeDto.forAttribute("unit"))
            map.remove(AttributeDto.ofBlank())
            return true
        }
        return false
    }

    /**
     * Updates [DvQuantity] precision if missing after all properties were set.
     *
     * @param rmObject [DvQuantity]
     * @param item [CQuantityItem]
     */
    private fun updatePrecision(rmObject: DvQuantity, item: CQuantityItem) {
        if (rmObject.precision == null) {
            item.precision?.also { interval -> AmUtils.getMax(interval)?.also { rmObject.precision = it } }
        }
    }

    override fun canRemoveDependantValues(): Boolean = true
}
