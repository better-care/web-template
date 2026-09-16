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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ACCURACY
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ACCURACY_IS_PERCENT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.DENOMINATOR
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NUMERATOR
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PRECISION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TYPE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VALUE
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
            this.putIfNotNull("|$NUMERATOR", rmObject.numerator)
            this.putIfNotNull("|$DENOMINATOR", rmObject.denominator)
            this.putIfNotNull("|$TYPE", rmObject.type)
            this.putIfNotNull("|$VALUE", rmObject.numerator / rmObject.denominator)
            this.putIfNotNull("|$PRECISION", rmObject.precision)
            this.putIfNotNull("|$ACCURACY", rmObject.accuracy)
            this.putIfNotNull("|$ACCURACY_IS_PERCENT", rmObject.accuracyIsPercent)
            map(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: DvProportion): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            if (2 == rmObject.type) {
                this.putIfNotNull("|$VALUE", "${valueConverter.formatFloat(rmObject.numerator)}%")
            } else {
                this.putIfNotNull(
                    "|$VALUE",
                    "${valueConverter.formatFloat(rmObject.numerator)}/${valueConverter.formatFloat(rmObject.denominator)}")
            }
            this.putIfNotNull("|$NUMERATOR", valueConverter.formatFloat(rmObject.numerator))
            this.putIfNotNull("|$DENOMINATOR", valueConverter.formatFloat(rmObject.denominator))
            this.putIfNotNull("|$TYPE", rmObject.type.toString())
            this.putIfNotNull("|$PRECISION", rmObject.precision.toString())
            this.putIfNotNull("|$ACCURACY", rmObject.accuracy?.let { valueConverter.formatFloat(it) })
            this.putIfNotNull("|$ACCURACY_IS_PERCENT", rmObject.accuracyIsPercent.toString())
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            this.resolve()
        }

}
