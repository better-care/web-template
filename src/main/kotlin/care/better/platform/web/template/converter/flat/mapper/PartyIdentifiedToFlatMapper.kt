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

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.IDENTIFIER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ID_NAMESPACE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ID_SCHEME
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NAME
import care.better.platform.web.template.converter.flat.context.AbstractFlatMappingContext
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.ObjectRef
import org.openehr.rm.common.PartyIdentified

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [PartyIdentified] to FLAT format.
 */
internal object PartyIdentifiedToFlatMapper : RmObjectToFlatMapper<PartyIdentified> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: PartyIdentified,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.externalRef?.also { mapPartyRef(it, webTemplatePath, flatConversionContext) }
        flatConversionContext["$webTemplatePath|$NAME"] = rmObject.name
        rmObject.identifiers.forEachIndexed { index, dvIdentifier ->
            DvIdentifierToFlatMapper.map(webTemplateNode, valueConverter, dvIdentifier, "$webTemplatePath/_$IDENTIFIER:$index", flatConversionContext)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: PartyIdentified,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.externalRef?.also { mapPartyRef(it, webTemplatePath, formattedFlatConversionContext) }
        rmObject.name?.also { formattedFlatConversionContext["$webTemplatePath|$NAME"] = it }
        rmObject.identifiers.forEachIndexed { index, dvIdentifier ->
            DvIdentifierToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                dvIdentifier,
                "$webTemplatePath/_$IDENTIFIER:$index",
                formattedFlatConversionContext)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> mapPartyRef(
            rmObject: ObjectRef,
            webTemplatePath: String,
            flatMappingContext: AbstractFlatMappingContext<T>) {
        rmObject.id?.also {
            flatMappingContext["$webTemplatePath|$ID"] = it.value as T?
            if (it is GenericId) {
                flatMappingContext["$webTemplatePath|$ID_SCHEME"] = it.scheme as T?
            }
        }
        flatMappingContext["$webTemplatePath|$ID_NAMESPACE"] = rmObject.namespace as T?
    }
}
