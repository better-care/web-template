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
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.resolve
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.datatypes.DvProportion

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToStructuredMapper] that maps [DvProportion] to STRUCTURED format.
 */
internal object DvProportionToStructuredMapper : DvQuantifiedToStructuredMapper<DvProportion>() {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvProportion): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            this.putIfNotNull("|numerator", rmObject.numerator)
            this.putIfNotNull("|denominator", rmObject.denominator)
            this.putIfNotNull("|type", rmObject.type)
            this.putIfNotNull("|value", rmObject.numerator / rmObject.denominator)
            map(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvProportion): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            if (2 == rmObject.type) {
                this.putIfNotNull("|value", "${valueConverter.formatDouble(rmObject.numerator.toDouble())}%")
            } else {
                this.putIfNotNull(
                    "|value",
                    "${valueConverter.formatDouble(rmObject.numerator.toDouble())}/${valueConverter.formatDouble(rmObject.denominator.toDouble())}")
                this.putIfNotNull("|numerator", valueConverter.formatDouble(rmObject.numerator.toDouble()))
                this.putIfNotNull("|denominator", valueConverter.formatDouble(rmObject.denominator.toDouble()))
                this.putIfNotNull("|type", rmObject.type.toString())
            }
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

}
