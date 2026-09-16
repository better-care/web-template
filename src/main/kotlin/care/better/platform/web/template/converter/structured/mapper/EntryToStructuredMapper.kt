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

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.LANGUAGE
import care.better.platform.web.template.converter.constant.WebTemplateConstants.OTHER_PARTICIPATION
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PROVIDER
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SUBJECT
import care.better.platform.web.template.converter.constant.WebTemplateConstants.WORK_FLOW_ID
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.putCollectionAsArray
import care.better.platform.web.template.converter.mapper.putSingletonAsArray
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openehr.rm.composition.Entry

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [LocatableToStructuredMapper] that maps [Entry] to STRUCTURED format.
 *
 * @constructor Creates a new instance of [EntryToStructuredMapper]
 */
internal open class EntryToStructuredMapper<T : Entry> : LocatableToStructuredMapper<T>() {

    companion object {
        private val INSTANCE: EntryToStructuredMapper<out Entry> = EntryToStructuredMapper()

        @JvmStatic
        fun getInstance(): EntryToStructuredMapper<out Entry> = INSTANCE
    }

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)
            this
        }

    override fun mapFormatted(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T): JsonNode =
        with(ConversionObjectMapper.createObjectNode()) {
            map(webTemplateNode, valueConverter, rmObject, this)
            this
        }

    override fun map(webTemplateNode: WebTemplateNode, valueConverter: ValueConverter, rmObject: T, objectNode: ObjectNode) {
        super.map(webTemplateNode, valueConverter, rmObject, objectNode)

        rmObject.language?.also {
            objectNode.putSingletonAsArray(LANGUAGE) { CodePhraseToStructuredMapper.map(webTemplateNode, valueConverter, it) }
        }

        objectNode.putCollectionAsArray("_$OTHER_PARTICIPATION", rmObject.otherParticipations) {
            ParticipationToStructuredMapper.map(webTemplateNode, valueConverter, it)
        }

        rmObject.provider?.also {
            objectNode.putSingletonAsArray("_$PROVIDER") { RmObjectToStructuredMapperDelegator.delegate(webTemplateNode, valueConverter, it) }
        }
        rmObject.subject?.also {
            val node = RmObjectToStructuredMapperDelegator.delegate(webTemplateNode, valueConverter, it)
            if ((node?.size() ?: 0) > 0) objectNode.putSingletonAsArray(SUBJECT) { node }
        }
        rmObject.workFlowId?.also {
            objectNode.putSingletonAsArray("_$WORK_FLOW_ID") { RmObjectToStructuredMapperDelegator.delegate(webTemplateNode, valueConverter, it) }
        }
    }

    override fun mapFormatted(
        webTemplateNode: WebTemplateNode,
        valueConverter: ValueConverter,
        rmObject: T,
        objectNode: ObjectNode
    ) {
        super.mapFormatted(webTemplateNode, valueConverter, rmObject, objectNode)

        rmObject.language?.also {
            objectNode.putSingletonAsArray(LANGUAGE) { CodePhraseToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it) }
        }

        objectNode.putCollectionAsArray("_$OTHER_PARTICIPATION", rmObject.otherParticipations) {
            ParticipationToStructuredMapper.mapFormatted(webTemplateNode, valueConverter, it)
        }

        rmObject.provider?.also {
            objectNode.putSingletonAsArray("_$PROVIDER") { RmObjectToStructuredMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it) }
        }
        rmObject.subject?.also {
            val node = RmObjectToStructuredMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it)
            if ((node?.size() ?: 0) > 0) objectNode.putSingletonAsArray(SUBJECT) { node }
        }
        rmObject.workFlowId?.also {
            objectNode.putSingletonAsArray("_$WORK_FLOW_ID") { RmObjectToStructuredMapperDelegator.delegateFormatted(webTemplateNode, valueConverter, it) }
        }
    }
}
