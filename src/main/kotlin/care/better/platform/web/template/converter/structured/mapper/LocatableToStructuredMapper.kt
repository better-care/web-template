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

import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.openehr.rm.common.Locatable
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [RmObjectToStructuredMapper] that maps [Locatable] to STRUCTURED format.
 *
 * @constructor Creates a new instance of [LocatableToStructuredMapper]
 */
internal open class LocatableToStructuredMapper<T : Locatable> : RmObjectToStructuredMapper<T> {
    companion object {
        private val INSTANCE: LocatableToStructuredMapper<out Locatable> = LocatableToStructuredMapper()

        @JvmStatic
        fun getInstance(): LocatableToStructuredMapper<out Locatable> = INSTANCE
    }

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            mapFormatted(webTemplateNode, valueConverter, rmObject, this)
            this
        }

    @Suppress("MoveLambdaOutsideParentheses")
    protected open fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T, objectNode: ObjectNode) {
        objectNode.putCollectionAsArray("_link", rmObject.links) { LinkToStructuredMapper.map(webTemplateNode, valueConverter, it) }
        rmObject.uid?.also { objectNode.putArray("_uid").add(TextNode(it.value)) }

        rmObject.name?.also { name ->
            addCustomName(
                webTemplateNode,
                name,
                { objectNode.putSingletonAsArray("_name") { RmObjectToStructuredMapperDelegator.delegate(webTemplateNode, valueConverter, it) } })
        }

        rmObject.feederAudit?.also {
            objectNode.putSingletonAsArray("_feeder_audit") { FeederAuditToStructuredMapper.map(webTemplateNode, valueConverter, it) }
        }
    }

    @Suppress("MoveLambdaOutsideParentheses")
    protected open fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T, objectNode: ObjectNode) {
        objectNode.putCollectionAsArray("_link", rmObject.links) { LinkToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
        rmObject.uid?.also { objectNode.putArray("_uid").add(TextNode(it.value)) }

        rmObject.name?.also { name ->
            addCustomName(
                webTemplateNode,
                name,
                { objectNode.putSingletonAsArray("_name") { RmObjectToStructuredMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it) } })
        }

        rmObject.feederAudit?.also {
            objectNode.putSingletonAsArray("_feeder_audit") { FeederAuditToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
        }
    }

    private fun addCustomName(webTemplateNode: WebTemplateNode, indexedName: DvText, nameConsumer: (DvText) -> Unit) {
        indexedName.value?.also {
            val index = it.lastIndexOf("#")
            val name = if (index == -1) it else it.substring(0, index).trim()
            if (name != webTemplateNode.localizedName && name != webTemplateNode.name && webTemplateNode.localizedNames.values.none { ln -> name == ln }) {
                nameConsumer.invoke(indexedName)
            }
        }
    }
}
