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
import org.openehr.rm.common.Participation
import org.openehr.rm.common.PartyIdentified

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [Participation] to FLAT format.
 */
internal object ParticipationToFlatMapper : RmObjectToFlatMapper<Participation> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Participation,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        mapInternal(rmObject, webTemplatePath, flatConversionContext)
    }

    @Suppress("UNCHECKED_CAST")
    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Participation,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        mapInternal(rmObject, webTemplatePath, formattedFlatConversionContext)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> mapInternal(
            rmObject: Participation,
            webTemplatePath: String,
            flatMappingContext: AbstractFlatMappingContext<T>) {
        flatMappingContext["$webTemplatePath|function"] = rmObject.function?.value as T?
        flatMappingContext["$webTemplatePath|mode"] = rmObject.mode?.value as T?

        val performer = rmObject.performer
        if (performer is PartyIdentified) {
            flatMappingContext["$webTemplatePath|name"] = performer.name as T?

            performer.externalRef?.also { mapPartyRef(it, webTemplatePath, flatMappingContext) }

            performer.identifiers.forEachIndexed { index, dvIdentifier ->
                flatMappingContext["$webTemplatePath|identifiers_assigner:$index"] = dvIdentifier.assigner as T?
                flatMappingContext["$webTemplatePath|identifiers_issuer:$index"] = dvIdentifier.issuer as T?
                flatMappingContext["$webTemplatePath|identifiers_type:$index"] = dvIdentifier.type as T?
                flatMappingContext["$webTemplatePath|identifiers_id:$index"] = dvIdentifier.id as T?
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> mapPartyRef(
            rmObject: ObjectRef,
            webTemplatePath: String,
            flatMappingContext: AbstractFlatMappingContext<T>) {
        flatMappingContext["$webTemplatePath|id"] = rmObject.id?.value as T?
        flatMappingContext["$webTemplatePath|id_namespace"] = rmObject.namespace as T?
        if (rmObject.id is GenericId) {
            flatMappingContext["$webTemplatePath|id_scheme"] = (rmObject.id as GenericId).scheme as T?
        }
    }
}
