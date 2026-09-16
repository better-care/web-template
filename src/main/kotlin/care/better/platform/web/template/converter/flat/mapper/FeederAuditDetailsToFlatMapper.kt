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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOCATION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PROVIDER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PROVIDER_RELATED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SUBJECT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SUBJECT_RELATED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SYSTEM_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TIME
import care.better.platform.web.template.converter.constant.WebTemplateConstants.VERSION_ID
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.common.FeederAuditDetails
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartyRelated

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [FeederAuditDetails] to FLAT format.
 */
internal object FeederAuditDetailsToFlatMapper : RmObjectToFlatMapper<FeederAuditDetails> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: FeederAuditDetails,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.time?.also { DvDateTimeToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath|$TIME", flatConversionContext) }

        rmObject.subject?.also {
            if (it is PartyIdentified) {
                val attributeName = if (it is PartyRelated) SUBJECT_RELATED else SUBJECT
                PartyIdentifiedToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$attributeName", flatConversionContext)
            }
        }

        rmObject.provider?.also {
            val attributeName = if (it is PartyRelated) PROVIDER_RELATED else PROVIDER
            PartyIdentifiedToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$attributeName", flatConversionContext)
        }

        rmObject.location?.also { PartyIdentifiedToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$LOCATION", flatConversionContext) }

        flatConversionContext["$webTemplatePath|$SYSTEM_ID"] = rmObject.systemId
        flatConversionContext["$webTemplatePath|$VERSION_ID"] = rmObject.versionId
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: FeederAuditDetails,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.time?.also {
            DvDateTimeToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath|$TIME",
                formattedFlatConversionContext)
        }

        rmObject.subject?.also {
            if (it is PartyIdentified) {
                val attributeName = if (it is PartyRelated) SUBJECT_RELATED else SUBJECT
                PartyIdentifiedToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$attributeName", formattedFlatConversionContext)
            }
        }

        rmObject.provider?.also {
            val attributeName = if (it is PartyRelated) PROVIDER_RELATED else PROVIDER
            PartyIdentifiedToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$attributeName", formattedFlatConversionContext)
        }

        rmObject.location?.also {
            PartyIdentifiedToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/$LOCATION",
                formattedFlatConversionContext)
        }

        formattedFlatConversionContext["$webTemplatePath|$SYSTEM_ID"] = rmObject.systemId
        formattedFlatConversionContext["$webTemplatePath|$VERSION_ID"] = rmObject.versionId
    }
}
