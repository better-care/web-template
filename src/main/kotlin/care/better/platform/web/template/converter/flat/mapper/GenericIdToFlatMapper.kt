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
import org.openehr.base.basetypes.GenericId

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [GenericId] to FLAT format.
 */
internal object GenericIdToFlatMapper : RmObjectToFlatMapper<GenericId> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: GenericId,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        ObjectIdToFlatMapper.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
        flatConversionContext["$webTemplatePath|id_scheme"] = rmObject.scheme
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: GenericId,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        ObjectIdToFlatMapper.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
        formattedFlatConversionContext["$webTemplatePath|id_scheme"] = rmObject.scheme
    }
}
