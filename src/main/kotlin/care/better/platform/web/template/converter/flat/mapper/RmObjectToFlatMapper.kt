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

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import com.marand.thinkehr.web.build.WebTemplateNode

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Interface used to map the RM object in RAW format to the RM object in FLAT format.
 */
internal interface RmObjectToFlatMapper<T : RmObject> {

    /**
     * Maps the RM object in RAW format to the RM object in FLAT format.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [ValueConverter]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path to the RM object
     * @param flatConversionContext [FlatMappingContext]
     */
    fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext)

    /**
     * Maps RM object in RAW format to the RM object in FLAT format with formatted values.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [ValueConverter]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path to the RM object
     * @param formattedFlatConversionContext [FormattedFlatMappingContext]
     */
    fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext)
}
