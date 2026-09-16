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

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ACTIVITY_ID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.COMPOSITION_UID
import care.better.platform.web.template.converter.constant.WebTemplateConstants.INSTRUCTION_DETAILS
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ISM_TRANSITION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PATH
import care.better.platform.web.template.converter.constant.WebTemplateConstants.TIME
import care.better.platform.web.template.converter.flat.context.AbstractFlatMappingContext
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.composition.Action

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [EntryToFlatMapper] that maps [Action] to FLAT format.
 */
internal object ActionToFlatMapper : CareEntryToFlatMapper<Action>() {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Action,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
        rmObject.time?.also {
            DvDateTimeToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$TIME", flatConversionContext)
        }
        rmObject.ismTransition?.also {
            IsmTransitionToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$ISM_TRANSITION", flatConversionContext)
        }
        mapAction(rmObject, webTemplatePath, flatConversionContext)
    }

    @Suppress("UNCHECKED_CAST")
    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: Action,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
        rmObject.time?.also {
            DvDateTimeToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$TIME", formattedFlatConversionContext)
        }
        rmObject.ismTransition?.also {
            IsmTransitionToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$ISM_TRANSITION", formattedFlatConversionContext)
        }
        mapAction(rmObject, webTemplatePath, formattedFlatConversionContext)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> mapAction(action: Action, webTemplatePath: String, flatMappingContext: AbstractFlatMappingContext<T>) {
        action.instructionDetails?.also { details ->
            details.instructionId?.also {
                flatMappingContext["$webTemplatePath/_$INSTRUCTION_DETAILS|$COMPOSITION_UID"] = it.id?.value as T?
                flatMappingContext["$webTemplatePath/_$INSTRUCTION_DETAILS|$PATH"] = it.path as T?
            }
            flatMappingContext["$webTemplatePath/_$INSTRUCTION_DETAILS|$ACTIVITY_ID"] = details.activityId as T?
        }
    }
}

