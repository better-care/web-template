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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CAREFLOW_STEP
import care.better.platform.web.template.converter.constant.WebTemplateConstants.CURRENT_STATE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.REASON
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TRANSITION
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.composition.IsmTransition

/**
 * @author Tilen Zabkar
 * @since 4.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [IsmTransition] to FLAT format.
 */
internal object IsmTransitionToFlatMapper : RmObjectToFlatMapper<IsmTransition> {
    override fun map(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: IsmTransition,
        webTemplatePath: String,
        flatConversionContext: FlatMappingContext
    ) {
        rmObject.currentState?.also {
            DvCodedTextToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$CURRENT_STATE", flatConversionContext)
        }
        rmObject.transition?.also {
            DvCodedTextToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$TRANSITION", flatConversionContext)
        }
        rmObject.careflowStep?.also {
            DvCodedTextToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$CAREFLOW_STEP", flatConversionContext)
        }
        rmObject.reason.forEachIndexed { index, reason ->
            DvTextToFlatMapper.map(webTemplateNode, valueConverter, reason, "$webTemplatePath/_$REASON:$index", flatConversionContext)
        }
    }

    override fun mapFormatted(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: IsmTransition,
        webTemplatePath: String,
        formattedFlatConversionContext: FormattedFlatMappingContext
    ) {
        rmObject.currentState?.also {
            DvCodedTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$CURRENT_STATE", formattedFlatConversionContext)
        }
        rmObject.transition?.also {
            DvCodedTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$TRANSITION", formattedFlatConversionContext)
        }
        rmObject.careflowStep?.also {
            DvCodedTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$CAREFLOW_STEP", formattedFlatConversionContext)
        }
        rmObject.reason.forEachIndexed { index, reason ->
            DvTextToFlatMapper.mapFormatted(webTemplateNode, valueConverter, reason, "$webTemplatePath/_$REASON:$index", formattedFlatConversionContext)
        }
    }
}
