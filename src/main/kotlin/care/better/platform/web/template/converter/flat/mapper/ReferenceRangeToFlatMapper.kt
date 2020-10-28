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

import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import com.marand.thinkehr.web.build.WebTemplateNode
import org.openehr.rm.datatypes.ReferenceRange

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [ReferenceRange] to FLAT format.
 */
internal object ReferenceRangeToFlatMapper : RmObjectToFlatMapper<ReferenceRange> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: ReferenceRange,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {

        rmObject.meaning?.also {
            RmObjectToFlatMapperDelegator.delegate(webTemplateNode, valueConverter, it, "$webTemplatePath/meaning", flatConversionContext)
        }

        rmObject.range?.also { NormalRangeToFlatMapper.map(webTemplateNode, valueConverter, it, webTemplatePath, flatConversionContext) }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: ReferenceRange,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {

        rmObject.meaning?.also {
            RmObjectToFlatMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/meaning", formattedFlatConversionContext)
        }

        rmObject.range?.also { NormalRangeToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, webTemplatePath, formattedFlatConversionContext) }
    }
}
