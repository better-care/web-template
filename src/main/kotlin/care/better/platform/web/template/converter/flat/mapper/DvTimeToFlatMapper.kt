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

package care.better.platform.web.template.converter.flat.mapper

import care.better.platform.template.AmUtils
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.am.aom.CTime
import org.openehr.rm.datatypes.DvTime
import java.time.DateTimeException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvQuantifiedToFlatMapper] that maps [DvTime] to FLAT format.
 */
internal object DvTimeToFlatMapper : DvQuantifiedToFlatMapper<DvTime>() {

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvTime,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        val value = requireNotNull(rmObject.value) { "DV_TIME value must not be null!" }
        try {
            val pattern = AmUtils.getPrimitiveItem(webTemplateNode.amNode, CTime::class.java, "value")?.pattern ?: ""
            flatConversionContext[webTemplatePath] = valueConverter.parseOpenEhrTime(value, pattern, false)
        } catch (ignored: ConversionException) {
            flatConversionContext[webTemplatePath] = value
        } catch (ignored: DateTimeException) {
            flatConversionContext[webTemplatePath] = value
        }
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvTime,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        val value = requireNotNull(rmObject.value) { "DV_TIME value must not be null!" }
        try {
            val pattern = AmUtils.getPrimitiveItem(webTemplateNode.amNode, CTime::class.java, "value")?.pattern ?: ""
            formattedFlatConversionContext[webTemplatePath] =
                valueConverter.formatOpenEhrTemporal(valueConverter.parseOpenEhrTime(value, pattern, false), pattern, false)
        } catch (ignored: ConversionException) {
            formattedFlatConversionContext[webTemplatePath] = value
        } catch (ignored: DateTimeException) {
            formattedFlatConversionContext[webTemplatePath] = value
        }
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
    }
}
