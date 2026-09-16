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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NORMAL_RANGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NORMAL_STATUS
import care.better.platform.web.template.converter.constant.WebTemplateConstants.OTHER_REFERENCE_RANGES
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openehr.rm.datatypes.DvOrdered

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [RmObjectToStructuredMapper] that maps [DvOrdered] to STRUCTURED format.
 *
 * @constructor Creates a new instance of [DvOrdinalToStructuredMapper]
 */
internal abstract class DvOrderedToStructuredMapper<T : DvOrdered> : RmObjectToStructuredMapper<T> {
    protected open fun map(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: T,
        objectNode: ObjectNode
    ) {
        rmObject.normalRange?.also {
            objectNode.putSingletonAsArray("_$NORMAL_RANGE") { NormalRangeToStructuredMapper.map(webTemplateNode, valueConverter, it) }
        }

        objectNode.putCollectionAsArray("_$OTHER_REFERENCE_RANGES", rmObject.otherReferenceRanges) {
            ReferenceRangeToStructuredMapper.map(webTemplateNode, valueConverter, it)
        }

        objectNode.putIfNotNull("|$NORMAL_STATUS", rmObject.normalStatus?.codeString)
    }

    protected open fun mapFormatted(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: T,
        objectNode: ObjectNode
    ) {
        rmObject.normalRange?.also {
            objectNode.putSingletonAsArray("_$NORMAL_RANGE") { NormalRangeToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
        }

        objectNode.putCollectionAsArray("_$OTHER_REFERENCE_RANGES", rmObject.otherReferenceRanges) {
            ReferenceRangeToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
        }

        objectNode.putIfNotNull("|$NORMAL_STATUS", rmObject.normalStatus?.codeString)
    }
}
