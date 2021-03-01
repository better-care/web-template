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
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.datatypes.DvMultimedia

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [DvMultimedia] to FLAT format.
 */
internal object DvMultimediaToFlatMapper : RmObjectToFlatMapper<DvMultimedia> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvMultimedia,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        flatConversionContext[webTemplatePath] = rmObject.uri?.value
        flatConversionContext["$webTemplatePath|mediatype"] = rmObject.mediaType?.codeString
        flatConversionContext["$webTemplatePath|alternatetext"] = rmObject.alternateText
        if (rmObject.size > 0) {
            flatConversionContext["$webTemplatePath|size"] = rmObject.size
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvMultimedia,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        formattedFlatConversionContext[webTemplatePath] = rmObject.uri?.value
        formattedFlatConversionContext["$webTemplatePath|mediatype"] = rmObject.mediaType?.codeString
        formattedFlatConversionContext["$webTemplatePath|alternatetext"] = rmObject.alternateText
        if (rmObject.size > 0) {
            formattedFlatConversionContext["$webTemplatePath|size"] = rmObject.size.toString()
        }
    }
}
