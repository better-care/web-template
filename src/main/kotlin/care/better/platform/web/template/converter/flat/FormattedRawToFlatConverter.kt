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

package care.better.platform.web.template.converter.flat

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.flat.mapper.RmObjectToFlatMapperDelegator
import care.better.platform.web.template.converter.value.ValueConverter
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.composition.Composition

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [AbstractRawToFlatConverter] that converts the RM object in RAW format to the RM object in FLAT format with formatted values.
 *
 * @constructor Creates a new instance of [FormattedRawToFlatConverter]
 * @param valueConverter [ValueConverter]
 */
internal class FormattedRawToFlatConverter(private val valueConverter: ValueConverter) : AbstractRawToFlatConverter<String>() {
    private val formattedFlatConversionContext: FormattedFlatMappingContext = FormattedFlatMappingContext()

    override fun convert(webTemplate: WebTemplate, composition: Composition): Map<String, String> {
        map(webTemplate.tree, composition, webTemplate.tree.jsonId)
        return formattedFlatConversionContext.get()
    }

    override fun <R : RmObject> convertForAqlPath(webTemplate: WebTemplate, aqlPath: String, rmObject: R): Map<String, String> {
        val webTemplateNode = webTemplate.findWebTemplateNodeByAqlPath(aqlPath)
        map(webTemplateNode, rmObject, webTemplateNode.jsonId)
        return formattedFlatConversionContext.get()
    }

    override fun <R : RmObject> convertForWebTemplatePath(webTemplate: WebTemplate, webTemplatePath: String, rmObject: R): Map<String, String> {
        val webTemplateNode = webTemplate.findWebTemplateNode(webTemplatePath)
        map(webTemplateNode, rmObject, webTemplateNode.jsonId)
        return formattedFlatConversionContext.get()
    }

    override fun <R : RmObject> mapRmObject(webTemplateNode: WebTemplateNode, rmObject: R, webTemplatePath: String) {
        RmObjectToFlatMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
    }
}
