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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.FEEDER_AUDIT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LINK
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NAME
import care.better.platform.web.template.converter.constant.WebTemplateConstants.UID
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.common.Locatable
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [RmObjectToFlatMapper] that maps [Locatable] to FLAT format.
 *
 * @constructor Creates a new instance of [LocatableToFlatMapper]
 */
@Suppress("MoveLambdaOutsideParentheses")
internal open class LocatableToFlatMapper<T : Locatable> : RmObjectToFlatMapper<T> {
    companion object {
        private val INSTANCE: LocatableToFlatMapper<out Locatable> = LocatableToFlatMapper()

        @JvmStatic
        fun getInstance(): LocatableToFlatMapper<out Locatable> = INSTANCE
    }

    override fun map(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObject.links.forEachIndexed { index, link ->
            LinkToFlatMapper.map(webTemplateNode, valueConverter, link, "$webTemplatePath/_$LINK:$index", flatConversionContext)
        }
        flatConversionContext["$webTemplatePath/_$UID"] = rmObject.uid?.value

        rmObject.name?.also { name ->
            addCustomName(
                webTemplateNode,
                name,
                { RmObjectToFlatMapperDelegator.delegate(webTemplateNode, valueConverter, it, "$webTemplatePath/_$NAME", flatConversionContext) })
        }
        rmObject.feederAudit?.also { FeederAuditToFlatMapper.map(webTemplateNode, valueConverter, it, "$webTemplatePath/_$FEEDER_AUDIT", flatConversionContext) }
    }

    override fun mapFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObject.links.forEachIndexed { index, link ->
            LinkToFlatMapper.mapFormatted(webTemplateNode, valueConverter, link, "$webTemplatePath/_$LINK:$index", formattedFlatConversionContext)
        }

        formattedFlatConversionContext["$webTemplatePath/_$UID"] = rmObject.uid?.value

        rmObject.name?.also { name ->
            addCustomName(
                webTemplateNode,
                name,
                {
                    RmObjectToFlatMapperDelegator.delegateFormatted(
                        webTemplateNode,
                        valueConverter,
                        it,
                        "$webTemplatePath/_$NAME",
                        formattedFlatConversionContext)
                })
        }

        rmObject.feederAudit?.also {
            FeederAuditToFlatMapper.mapFormatted(
                webTemplateNode,
                valueConverter,
                it,
                "$webTemplatePath/_$FEEDER_AUDIT",
                formattedFlatConversionContext)
        }
    }

    private fun addCustomName(webTemplateNode: WebTemplateNode, indexedName: DvText, nameConsumer: (DvText) -> Unit) {
        indexedName.value?.also {
            val name =
                if (it == webTemplateNode.name) {
                    it
                } else {
                    val index = it.lastIndexOf("#")
                    if (index == -1) it else it.substring(0, index).trim()
                }

            if (name != webTemplateNode.localizedName && name != webTemplateNode.name && webTemplateNode.localizedNames.values.none { ln -> name == ln }) {
                nameConsumer.invoke(indexedName)
            }
        }
    }
}
