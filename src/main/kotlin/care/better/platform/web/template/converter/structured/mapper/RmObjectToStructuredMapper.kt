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

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.marand.thinkehr.web.build.WebTemplateNode

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Interface used to map the RM object in RAW format to the RM object in STRUCTURED format.
 */
internal interface RmObjectToStructuredMapper<T : RmObject> {
    /**
     * Maps the RM object in RAW format to the RM object in STRUCTURED format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [valueConverter]
     * @param rmObject RM object in RAW format
     * @return RM object in STRUCTURED format
     */
    fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T): JsonNode

    /**
     * Maps the RM object in RAW format to the RM object in STRUCTURED format with formatted values.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [valueConverter]
     * @param rmObject RM object in RAW format
     * @return RM object in STRUCTURED format with formatted values
     */
    fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T): JsonNode
}
