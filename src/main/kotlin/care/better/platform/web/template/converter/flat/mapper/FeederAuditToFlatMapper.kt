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
import org.openehr.rm.common.FeederAudit
import org.openehr.rm.datatypes.DvMultimedia
import org.openehr.rm.datatypes.DvParsable

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [FeederAudit] to FLAT format.
 */
internal object FeederAuditToFlatMapper : RmObjectToFlatMapper<FeederAudit> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: FeederAudit,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.feederSystemAudit?.also {
            FeederAuditDetailsToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/feeder_system_audit", flatConversionContext)
        }

        rmObject.originatingSystemAudit?.also {
            FeederAuditDetailsToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/originating_system_audit", flatConversionContext)
        }

        rmObject.originalContent?.also {
            if (it is DvMultimedia)
                DvMultimediaToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/original_content_multimedia", flatConversionContext)
            else
                DvParsableToFlatMapper.map(webTemplateNode, valueConverter, it as DvParsable, "$webTemplatePath/original_content", flatConversionContext)
        }

        rmObject.feederSystemItemIds.forEachIndexed { index, dvIdentifier ->
            DvIdentifierToFlatMapper.map(webTemplateNode, valueConverter, dvIdentifier, "$webTemplatePath/feeder_system_item_id:$index", flatConversionContext)
        }

        rmObject.originatingSystemItemIds.forEachIndexed { index, dvIdentifier ->
            DvIdentifierToFlatMapper.map(
                webTemplateNode,
                valueConverter,
                dvIdentifier,
                "$webTemplatePath/originating_system_item_id:$index",
                flatConversionContext)
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: FeederAudit,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.feederSystemAudit?.also {
            FeederAuditDetailsToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/feeder_system_audit",
                formattedFlatConversionContext)
        }

        rmObject.originatingSystemAudit?.also {
            FeederAuditDetailsToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/originating_system_audit",
                formattedFlatConversionContext)
        }

        rmObject.originalContent?.also {
            if (it is DvMultimedia)
                DvMultimediaToFlatMapper.mapFormatted(
                    webTemplateNode,
                    valueConverter,
                    it,
                    "$webTemplatePath/original_content_multimedia",
                    formattedFlatConversionContext)
            else
                DvParsableToFlatMapper.mapFormatted(
                    webTemplateNode,
                    valueConverter,
                    it as DvParsable,
                    "$webTemplatePath/original_content",
                    formattedFlatConversionContext)
        }

        rmObject.feederSystemItemIds.forEachIndexed { index, dvIdentifier ->
            DvIdentifierToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                dvIdentifier,
                "$webTemplatePath/feeder_system_item_id:$index",
                formattedFlatConversionContext)
        }

        rmObject.originatingSystemItemIds.forEachIndexed { index, dvIdentifier ->
            DvIdentifierToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                dvIdentifier,
                "$webTemplatePath/originating_system_item_id:$index",
                formattedFlatConversionContext)
        }
    }
}
