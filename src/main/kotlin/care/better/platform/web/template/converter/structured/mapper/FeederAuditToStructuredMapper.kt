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

package care.better.platform.web.template.converter.structured.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FEEDER_SYSTEM_AUDIT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FEEDER_SYSTEM_ITEM_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINAL_CONTENT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINAL_CONTENT_MULTIMEDIA
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINATING_SYSTEM_AUDIT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ORIGINATING_SYSTEM_ITEM_ID
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.rm.common.FeederAudit
import org.openehr.rm.datatypes.DvMultimedia
import org.openehr.rm.datatypes.DvParsable

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToStructuredMapper] that maps [FeederAudit] to STRUCTURED format.
 */
internal object FeederAuditToStructuredMapper : RmObjectToStructuredMapper<FeederAudit> {
    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: FeederAudit): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.feederSystemAudit?.also {
                this.putSingletonAsArray(FEEDER_SYSTEM_AUDIT) { FeederAuditDetailsToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.originatingSystemAudit?.also {
                this.putSingletonAsArray(ORIGINATING_SYSTEM_AUDIT) { FeederAuditDetailsToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.originalContent?.also {
                if (it is DvMultimedia)
                    this.putSingletonAsArray(ORIGINAL_CONTENT_MULTIMEDIA) { DvMultimediaToStructuredMapper.map(webTemplateNode, valueConverter, it) }
                else
                    this.putSingletonAsArray(ORIGINAL_CONTENT) { DvParsableToStructuredMapper.map(webTemplateNode, valueConverter, it as DvParsable) }
            }
            this.putCollectionAsArray(FEEDER_SYSTEM_ITEM_ID, rmObject.feederSystemItemIds) {
                DvIdentifierToStructuredMapper.map(webTemplateNode, valueConverter, it)
            }
            this.putCollectionAsArray(ORIGINATING_SYSTEM_ITEM_ID, rmObject.originatingSystemItemIds) {
                DvIdentifierToStructuredMapper.map(webTemplateNode, valueConverter, it)
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: FeederAudit): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.feederSystemAudit?.also {
                this.putSingletonAsArray(FEEDER_SYSTEM_AUDIT) { FeederAuditDetailsToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.originatingSystemAudit?.also {
                this.putSingletonAsArray(ORIGINATING_SYSTEM_AUDIT) {
                    FeederAuditDetailsToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
                }
            }
            rmObject.originalContent?.also {
                if (it is DvMultimedia)
                    this.putSingletonAsArray(ORIGINAL_CONTENT_MULTIMEDIA) {
                        DvMultimediaToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
                    }
                else
                    this.putSingletonAsArray(ORIGINAL_CONTENT) {
                        DvParsableToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it as DvParsable)
                    }
            }
            this.putCollectionAsArray(FEEDER_SYSTEM_ITEM_ID, rmObject.feederSystemItemIds) {
                DvIdentifierToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
            }
            this.putCollectionAsArray(ORIGINATING_SYSTEM_ITEM_ID, rmObject.originatingSystemItemIds) {
                DvIdentifierToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
            }
            this
        }
}
