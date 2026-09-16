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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOWER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOWER_INCLUDED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LOWER_UNBOUNDED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UPPER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UPPER_INCLUDED
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UPPER_UNBOUNDED
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.datatypes.DvInterval

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectToFlatMapper] that maps [DvInterval] to FLAT format.
 */
internal object DvIntervalToFlatMapper : RmObjectToFlatMapper<DvInterval> {
    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvInterval,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.lower?.also {
            RmObjectToFlatMapperDelegator.delegate(
                webTemplateNode.children.first { child -> child.jsonId == LOWER },
                valueConverter,
                it,
                "$webTemplatePath/$LOWER",
                flatConversionContext)
        }
        rmObject.upper?.also {
            RmObjectToFlatMapperDelegator.delegate(
                webTemplateNode.children.first { child -> child.jsonId == UPPER },
                valueConverter,
                it,
                "$webTemplatePath/$UPPER",
                flatConversionContext)
        }

        if (rmObject.lowerIncluded == false) flatConversionContext["$webTemplatePath|$LOWER_INCLUDED"] = false
        if (rmObject.upperIncluded == false) flatConversionContext["$webTemplatePath|$UPPER_INCLUDED"] = false
        if (rmObject.lowerUnbounded) flatConversionContext["$webTemplatePath|$LOWER_UNBOUNDED"] = true
        if (rmObject.upperUnbounded) flatConversionContext["$webTemplatePath|$UPPER_UNBOUNDED"] = true
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: DvInterval,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.lower?.also {
            RmObjectToFlatMapperDelegator.delegateFormatted(
                webTemplateNode.children.first { child -> child.jsonId == LOWER },
                valueConverter,
                it,
                "$webTemplatePath/$LOWER",
                formattedFlatConversionContext)
        }
        rmObject.upper?.also {
            RmObjectToFlatMapperDelegator.delegateFormatted(
                webTemplateNode.children.first { child -> child.jsonId == UPPER },
                valueConverter,
                it,
                "$webTemplatePath/$UPPER",
                formattedFlatConversionContext)
        }

        if (rmObject.lowerIncluded == false) formattedFlatConversionContext["$webTemplatePath|$LOWER_INCLUDED"] = "false"
        if (rmObject.upperIncluded == false) formattedFlatConversionContext["$webTemplatePath|$UPPER_INCLUDED"] = "false"
        if (rmObject.lowerUnbounded) formattedFlatConversionContext["$webTemplatePath|$LOWER_UNBOUNDED"] = "true"
        if (rmObject.upperUnbounded) formattedFlatConversionContext["$webTemplatePath|$UPPER_UNBOUNDED"] = "true"
    }
}
