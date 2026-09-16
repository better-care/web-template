/* Copyright 2026 Better Ltd (www.better.care)
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

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.GUIDELINE_ID
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.composition.CareEntry

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Implementation of [EntryToFlatMapper] that maps [CareEntry] to FLAT format.
 *
 * @constructor Creates a new instance of [CareEntryToFlatMapper]
 */
internal open class CareEntryToFlatMapper<T : CareEntry> : EntryToFlatMapper<T>() {
    companion object {
        private val INSTANCE: CareEntryToFlatMapper<out CareEntry> = CareEntryToFlatMapper()

        @JvmStatic
        fun getInstance(): CareEntryToFlatMapper<out CareEntry> = INSTANCE
    }

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
        rmObject.guidelineId?.also {
            ObjectRefToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_$GUIDELINE_ID", flatConversionContext)
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
        rmObject.guidelineId?.also {
            ObjectRefToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_$GUIDELINE_ID", formattedFlatConversionContext)
        }
    }
}
