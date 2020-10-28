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

import care.better.platform.web.template.converter.mapper.putIfNotNull
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.node.ObjectNode
import com.marand.thinkehr.web.build.WebTemplateNode
import org.openehr.rm.datatypes.DvQuantified

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [DvOrderedToStructuredMapper] that maps [DvQuantified] to STRUCTURED format.
 *
 * @constructor Creates a new instance of [DvQuantifiedToStructuredMapper]
 */
internal abstract class DvQuantifiedToStructuredMapper<T : DvQuantified> : DvOrderedToStructuredMapper<T>() {

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T, objectNode: ObjectNode) {
        super.map(webTemplateNode, valueConverter, rmObject, objectNode)
        objectNode.putIfNotNull("|magnitude_status", rmObject.magnitudeStatus)
    }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T, objectNode: ObjectNode) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, objectNode)
        objectNode.putIfNotNull("|magnitude_status", rmObject.magnitudeStatus)
    }
}
