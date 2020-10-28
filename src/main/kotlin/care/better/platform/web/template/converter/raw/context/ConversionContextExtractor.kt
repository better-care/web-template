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

package care.better.platform.web.template.converter.raw.context

import care.better.platform.web.template.converter.mapper.asTextOrNull
import care.better.platform.web.template.converter.raw.context.setter.CtxConstants
import care.better.platform.web.template.converter.structured.FlatToStructuredConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Retrieves ctx values from the RM object in STRUCTURED format and sets them to the [ConversionContext].
 *
 * Note that ctx values in STRUCTURED format can also be passed in FLAT format. In that case,
 * the context in FLAT format will be transformed into STRUCTURED format.
 *
 * @constructor Creates a new instance of [ConversionContextExtractor]
 */
internal object ConversionContextExtractor : (JsonNode, ConversionContext) -> ConversionContext {

    /**
     * Retrieves values on ctx JSON path from [JsonNode] and sets them to the [ConversionContext]
     *
     * @param structuredObjectNode RM object in STRUCTURED format
     * @param conversionContext [ConversionContext]
     * @return [ConversionContext]
     */
    override fun invoke(structuredObjectNode: JsonNode, conversionContext: ConversionContext): ConversionContext {
        val conversionContextBuilder: ConversionContext.Builder = conversionContext.createBuilder(conversionContext.getWebTemplate())
        extractContext(structuredObjectNode as ObjectNode).also {
            if (!it.isMissingNode) {
                val objectNode = it as ObjectNode
                val fieldNames = getSortedCtxFields(objectNode)
                fieldNames.forEach { key ->
                    val value = objectNode[key]
                    when {
                        value.isNull -> {
                        }
                        value.isArray -> CtxConstants.forAttributeName(key)?.ctxSetter?.set(
                            conversionContextBuilder,
                            conversionContext.valueConverter,
                            convertArray(value as ArrayNode))
                        value.isObject -> CtxConstants.forAttributeName(key)?.ctxSetter?.set(
                            conversionContextBuilder,
                            conversionContext.valueConverter,
                            convertObject(value as ObjectNode))
                        else -> CtxConstants.forAttributeName(key)?.ctxSetter?.set(
                            conversionContextBuilder,
                            conversionContext.valueConverter,
                            value.asText())
                    }
                }
            }
        }
        return conversionContextBuilder.build()
    }

    /**
     * Sorts and returns ctx fields.
     *
     * @param objectNode [ObjectNode]
     * @return [List] of sorted [ObjectNode] fields
     */
    private fun getSortedCtxFields(objectNode: ObjectNode): List<String> {
        val nameOrderMap = mutableMapOf<String, Int>()
        val iterator = objectNode.fieldNames()
        while (iterator.hasNext()) {
            val name = iterator.next()
            nameOrderMap[name] = CtxConstants.forAttributeName(name)?.ordinal ?: Integer.MAX_VALUE
        }
        return nameOrderMap.asSequence().sortedBy { it.value }.map { it.key }.toList()
    }

    /**
     * Extracts ctx from the RM object in STRUCTURED format.
     * If context is passed in FLAT format, it will be transformed into STRUCTURED format.
     *
     * @param structuredObjectNode RM object in STRUCTURED format
     * @return [JsonNode] of the ctx element
     */
    private fun extractContext(structuredObjectNode: ObjectNode): JsonNode {
        val contextNode: JsonNode = with(structuredObjectNode.get("ctx")) {
            when {
                this == null -> MissingNode.getInstance()
                this.isMissingNode -> this
                hasCtxElementHasFlatKeys(this as ObjectNode) -> {
                    val flatCtxValues: MutableMap<String, Any?> = mutableMapOf()
                    this.fields().forEach {
                        flatCtxValues["ctx/${it.key}"] = it.value.asTextOrNull()

                    }
                    FlatToStructuredConverter.getInstance().invoke(flatCtxValues).get("ctx")
                }
                else -> this
            }
        }

        val flatCtxValues: MutableMap<String, Any?> = mutableMapOf()
        structuredObjectNode.fields().forEach {
            if (it.key.startsWith("ctx/")) {
                flatCtxValues[it.key] = it.value.asTextOrNull()
            }
        }

        val convertedContextNode =
            if (flatCtxValues.isEmpty()) MissingNode.getInstance() else FlatToStructuredConverter.getInstance().invoke(flatCtxValues).get("ctx")

        return when {
            contextNode.isMissingNode -> convertedContextNode
            convertedContextNode.isMissingNode -> contextNode
            else -> {
                (convertedContextNode as ObjectNode).fields().forEach {
                    (contextNode as ObjectNode).replace(it.key, it.value)
                }
                contextNode
            }
        }
    }

    /**
     * Checks if [ObjectNode] ctx element contains entries in FLAT format.
     *
     * @param objectNode [ObjectNode] of the ctx element
     * @return [Boolean] indicating if [ObjectNode] of the ctx element has entries in FLAT format
     */
    private fun hasCtxElementHasFlatKeys(objectNode: ObjectNode): Boolean {
        val iterator = objectNode.fields().iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.key.contains(":") || next.key.contains("|")) {
                return true
            }
        }
        return false
    }

    /**
     * Converts [ArrayNode] to [List] of values.
     *
     * @param arrayNode [ArrayNode]
     * @return [List] of values
     */
    private fun convertArray(arrayNode: ArrayNode): List<Any> =
        arrayNode.filter { !it.isNull && !it.isMissingNode }.map {
            when {
                it.isArray -> convertArray(it as ArrayNode)
                it.isObject -> convertObject(it as ObjectNode)
                else -> it.asText()
            }
        }


    /**
     * Converts [ObjectNode] to the [Map] of key and values.
     *
     * @param objectNode [ObjectNode]
     * @return [Map] of key and values
     */
    private fun convertObject(objectNode: ObjectNode): Map<String, Any?> {
        val attributeIterator = objectNode.fields()
        val map: MutableMap<String, Any?> = mutableMapOf()
        while (attributeIterator.hasNext()) {
            val (attributeKey, attributeValue) = attributeIterator.next()
            if (!attributeValue.isNull) {
                map[attributeKey] =
                    when {
                        attributeValue.isArray -> convertArray(attributeValue as ArrayNode)
                        attributeValue.isObject -> convertObject(attributeValue as ObjectNode)
                        else -> attributeValue.asText()
                    }
            }
        }
        return map.toMap()
    }
}
