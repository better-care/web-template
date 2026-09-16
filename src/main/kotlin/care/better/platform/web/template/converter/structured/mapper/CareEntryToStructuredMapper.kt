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

package care.better.platform.web.template.converter.structured.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.GUIDELINE_ID
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openehr.rm.composition.CareEntry

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Instance of [EntryToStructuredMapper] that maps [CareEntry] to STRUCTURED format.
 *
 * @constructor Creates a new instance of [CareEntryToStructuredMapper]
 */
internal open class CareEntryToStructuredMapper<T : CareEntry> : EntryToStructuredMapper<T>() {
    companion object {
        private val INSTANCE: CareEntryToStructuredMapper<out CareEntry> = CareEntryToStructuredMapper()

        @JvmStatic
        fun getInstance(): CareEntryToStructuredMapper<out CareEntry> = INSTANCE
    }

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T, objectNode: ObjectNode) {
        super.map(webTemplateNode, valueConverter, rmObject, objectNode)
        rmObject.guidelineId?.also {
            objectNode.putSingletonAsArray("_$GUIDELINE_ID") { ObjectRefToStructuredMapper.map(webTemplateNode, valueConverter, it) }
        }
    }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T, objectNode: ObjectNode) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, objectNode)
        rmObject.guidelineId?.also {
            objectNode.putSingletonAsArray("_$GUIDELINE_ID") { ObjectRefToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
        }
    }
}
