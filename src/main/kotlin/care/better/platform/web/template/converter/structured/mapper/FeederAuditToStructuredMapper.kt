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

import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import care.better.platform.web.template.builder.model.WebTemplateNode
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
                this.putSingletonAsArray("feeder_system_audit") { FeederAuditDetailsToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.originatingSystemAudit?.also {
                this.putSingletonAsArray("originating_system_audit") { FeederAuditDetailsToStructuredMapper.map(webTemplateNode, valueConverter, it) }
            }
            rmObject.originalContent?.also {
                if (it is DvMultimedia)
                    this.putSingletonAsArray("original_content_multimedia") { DvMultimediaToStructuredMapper.map(webTemplateNode, valueConverter, it) }
                else
                    this.putSingletonAsArray("original_content") { DvParsableToStructuredMapper.map(webTemplateNode, valueConverter, it as DvParsable) }
            }
            this.putCollectionAsArray("feeder_system_item_id", rmObject.feederSystemItemIds) {
                DvIdentifierToStructuredMapper.map(webTemplateNode, valueConverter, it)
            }
            this.putCollectionAsArray("originating_system_item_id", rmObject.originatingSystemItemIds) {
                DvIdentifierToStructuredMapper.map(webTemplateNode, valueConverter, it)
            }
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: FeederAudit): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            rmObject.feederSystemAudit?.also {
                this.putSingletonAsArray("feeder_system_audit") { FeederAuditDetailsToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
            }
            rmObject.originatingSystemAudit?.also {
                this.putSingletonAsArray("originating_system_audit") {
                    FeederAuditDetailsToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
                }
            }
            rmObject.originalContent?.also {
                if (it is DvMultimedia)
                    this.putSingletonAsArray("original_content_multimedia") {
                        DvMultimediaToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
                    }
                else
                    this.putSingletonAsArray("original_content") {
                        DvParsableToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it as DvParsable)
                    }
            }
            this.putCollectionAsArray("feeder_system_item_id", rmObject.feederSystemItemIds) {
                DvIdentifierToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
            }
            this.putCollectionAsArray("originating_system_item_id", rmObject.originatingSystemItemIds) {
                DvIdentifierToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
            }
            this
        }
}
