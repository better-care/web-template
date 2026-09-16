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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.OTHER_PARTICIPATION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PROVIDER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SUBJECT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.WORK_FLOW_ID
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.common.PartySelf
import org.openehr.rm.composition.Entry

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [LocatableToFlatMapper] that maps [Entry] to FLAT format.
 *
 * @constructor Creates a new instance of [EntryToFlatMapper]
 */
internal open class EntryToFlatMapper<T : Entry> : LocatableToFlatMapper<T>() {
    companion object {
        private val INSTANCE: EntryToFlatMapper<out Entry> = EntryToFlatMapper()

        @JvmStatic
        fun getInstance(): EntryToFlatMapper<out Entry> = INSTANCE
    }

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        super.map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)

        rmObject.otherParticipations.forEachIndexed { index, participation ->
            ParticipationToFlatMapper.map(webTemplateNode, valueConverter, participation, "$webTemplatePath/_$OTHER_PARTICIPATION:$index", flatConversionContext)
        }
        rmObject.language?.also {
            CodePhraseToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/$LANGUAGE", flatConversionContext)
        }

        rmObject.provider?.also {
            RmObjectToFlatMapperDelegator.delegate(webTemplateNode, valueConverter, it, "$webTemplatePath/_$PROVIDER", flatConversionContext)
        }
        rmObject.subject?.also {
            if (it !is PartySelf || it.externalRef != null)
                RmObjectToFlatMapperDelegator.delegate(webTemplateNode, valueConverter, it, "$webTemplatePath/$SUBJECT", flatConversionContext)
        }
        rmObject.workFlowId?.also {
            RmObjectToFlatMapperDelegator.delegate(webTemplateNode, valueConverter, it, "$webTemplatePath/_$WORK_FLOW_ID", flatConversionContext)
        }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)

        rmObject.otherParticipations.forEachIndexed { index, participation ->
            ParticipationToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                participation,
                "$webTemplatePath/_$OTHER_PARTICIPATION:$index",
                formattedFlatConversionContext)
        }
        rmObject.language?.also {
            CodePhraseToFlatMapper.mapFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$LANGUAGE", formattedFlatConversionContext)
        }

        rmObject.provider?.also {
            RmObjectToFlatMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/_$PROVIDER", formattedFlatConversionContext)
        }
        rmObject.subject?.also {
            if (it !is PartySelf || it.externalRef != null)
                RmObjectToFlatMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it, "$webTemplatePath/$SUBJECT", formattedFlatConversionContext)
        }
        rmObject.workFlowId?.also {
            RmObjectToFlatMapperDelegator.delegateFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/_$WORK_FLOW_ID",
                formattedFlatConversionContext)
        }
    }
}
