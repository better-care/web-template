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

package care.better.platform.web.template.converter.flat.mapper

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ACTION_ARCHETYPE_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TIMING
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.raw.factory.node.DEFAULT_ACTION_ARCHETYPE_ID
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.composition.Activity

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [LocatableToFlatMapper] that maps [Activity] to FLAT format.
 */
internal object ActivityToFlatMapper : LocatableToFlatMapper<Activity>() {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Activity,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
        rmObject.timing?.also {
            DvParsableToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$TIMING", flatConversionContext)
        }
        if (rmObject.actionArchetypeId != null && rmObject.actionArchetypeId != DEFAULT_ACTION_ARCHETYPE_ID) {
            flatConversionContext["$webTemplatePath/$ACTION_ARCHETYPE_ID"] = rmObject.actionArchetypeId
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Activity,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
        rmObject.timing?.also {
            DvParsableToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$TIMING", formattedFlatConversionContext)
        }
        if (rmObject.actionArchetypeId != null && rmObject.actionArchetypeId != DEFAULT_ACTION_ARCHETYPE_ID) {
            formattedFlatConversionContext["$webTemplatePath/$ACTION_ARCHETYPE_ID"] = rmObject.actionArchetypeId
        }
    }
}
