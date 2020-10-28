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

import care.better.platform.web.template.converter.flat.context.AbstractFlatMappingContext
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import com.marand.thinkehr.web.build.WebTemplateNode
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.ObjectRef

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [ObjectRef] to FLAT format.
 */
internal object ObjectRefToFlatMapper : RmObjectToFlatMapper<ObjectRef> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: ObjectRef,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        mapInternal(rmObject, webTemplatePath, flatConversionContext)
    }

    @Suppress("UNCHECKED_CAST")
    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: ObjectRef,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        mapInternal(rmObject, webTemplatePath, formattedFlatConversionContext)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> mapInternal(
            rmObject: ObjectRef,
            webTemplatePath: String,
            flatMappingContext: AbstractFlatMappingContext<T>) {
        rmObject.id?.also {
            flatMappingContext["$webTemplatePath|id"] = it.value as T?
            if (it is GenericId) {
                flatMappingContext["$webTemplatePath|id_scheme"] = it.scheme as T?
            }
        }
        flatMappingContext["$webTemplatePath|type"] = rmObject.type as T?
        flatMappingContext["$webTemplatePath|namespace"] = rmObject.namespace as T?
    }
}
