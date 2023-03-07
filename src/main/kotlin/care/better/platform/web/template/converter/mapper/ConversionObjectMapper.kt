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

package care.better.platform.web.template.converter.mapper

import care.better.openehr.rm.RmObject
import care.better.platform.json.jackson.better.BetterObjectMapper
import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.isEmpty
import care.better.platform.web.template.converter.raw.factory.leaf.RmObjectLeafNodeFactoryDelegator
import care.better.platform.web.template.converter.raw.postprocessor.PostProcessDelegator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [BetterObjectMapper] that is used during the RM object conversion.
 */
internal object ConversionObjectMapper : BetterObjectMapper() {
    init {
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        this.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.USE_DEFAULTS))
    }

    /**
     * Converts [JsonNode] with only "|raw" entry ([JsonNode] in RAW format) to the RM object in RAW format.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param jsonNode [JsonNode]
     * @param webTemplatePath [WebTemplatePath]
     * @return RM object in RAW format
     */
    fun convertRawJsonNode(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, webTemplatePath: WebTemplatePath): Any? =
        with(ConversionObjectMapper.readValue(extractRawNodeString(jsonNode), object : TypeReference<RmObject>() {})) {
            PostProcessDelegator.delegate(
                conversionContext,
                amNode,
                this,
                webTemplatePath)
            if (this.isEmpty(conversionContext.strictMode)) null else this
        }

    private fun extractRawNodeString(jsonNode: JsonNode): String =
        if (jsonNode.has("|raw")) {
            val rawNode = jsonNode["|raw"]
            if (rawNode.isTextual) rawNode.asText() else rawNode.toString()
        } else {
            val rawNode = jsonNode["raw"]
            if (rawNode.isTextual) rawNode.asText() else rawNode.toString()
        }
}

/**
 * Returns [ObjectNode] for the web template path segment
 *
 * @param objectNode [ObjectNode]
 * @param webTemplatePathSegment Web template path segment [String]
 * @return [ObjectNode]
 * @throws [ConversionException] if [JsonNode] for the web template path segment is [ArrayNode] with multiple values.
 */
internal fun getObjectNodeForWebTemplateSegment(objectNode: ObjectNode, webTemplatePathSegment: String): ObjectNode? =
    objectNode.get(webTemplatePathSegment)?.let {
        if (it.isObject) {
            it as ObjectNode
        } else if (it.isArray && it.size() == 1 && it[0].isObject) {
            it[0] as ObjectNode
        } else {
            throw ConversionException("Expecting to convert single RM object, but multiple were provided.", webTemplatePathSegment)
        }
    }


/**
 * Validates [ObjectNode] for the web template path segment on root
 *
 * @param objectNode [ObjectNode]
 * @param webTemplatePathSegment Web template path segment [String]
 * @param rmType RM type
 * @throws [ConversionException] if  web template path segment is unknown.
 */
internal fun validateFieldsOnRoot(objectNode: ObjectNode, webTemplatePathSegment: String, rmType: String) {
    objectNode.fields().forEach { (key, _) ->
        if (!key.startsWith("transient_") && key != "ctx" && !key.startsWith("ctx/") && key != webTemplatePathSegment) {
            throw ConversionException("$rmType has no attribute $key.")
        }
    }
}

/**
 * Returns [ObjectNode] for the web template path.
 *
 * @param webTemplatePath Web template path
 * @return [ObjectNode]
 * @throws [ConversionException] if [JsonNode] for any of the web template segments is [ArrayNode] with multiple values.
 */
internal fun getObjectNodeForWebTemplatePath(objectNode: ObjectNode, webTemplatePath: String): ObjectNode? =
    getObjectNodeForWebTemplatePathRecursively(objectNode, webTemplatePath.split("/").toList(), 0)

private fun getObjectNodeForWebTemplatePathRecursively(objectNode: ObjectNode, webTemplatePathSegments: List<String>, index: Int): ObjectNode? {
    val webTemplatePathSegment = webTemplatePathSegments[index]
    val jsonNode = getObjectNodeForWebTemplateSegment(objectNode, webTemplatePathSegment)

    if (index == webTemplatePathSegments.size - 1) {
        return jsonNode
    }
    return getObjectNodeForWebTemplatePathRecursively(jsonNode ?: objectNode, webTemplatePathSegments, index + 1)
}

/**
 * Extension function that retrieves [String] or null from [JsonNode].
 *
 * @return [String] or null
 */
internal fun JsonNode.asTextOrNull(): String = this.asText(null)

/**
 * Extension function that returns [List] of entry keys for [ObjectNode].
 *
 * @return [List] of [ObjectNode] entry keys
 */
internal fun ObjectNode.getFieldNames(): List<String> {
    val iterator = fieldNames()
    val names: MutableSet<String> = mutableSetOf()
    while (iterator.hasNext()) {
        names.add(iterator.next())
    }
    return names.toList()
}

/**
 * Extension function that adds [JsonNode] to [ArrayNode] if not null.
 *
 * @param jsonNode [JsonNode]
 */
internal fun ArrayNode.addIfNotNull(jsonNode: JsonNode?) {
    if (jsonNode != null && !jsonNode.isNull) {
        this.add(jsonNode)
    }
}

/**
 * Extension function that put [Short] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: Short?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [Int] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: Int?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [Long] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: Long?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [Float] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: Float?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [Double] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: Double?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [BigDecimal] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: BigDecimal?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [BigInteger] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: BigInteger?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [String] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: String?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [Boolean] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: Boolean?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that put [ByteArray] value to [ObjectNode] if not null.
 *
 * @param key Entry key
 * @param value Entry value
 */
fun ObjectNode.putIfNotNull(key: String, value: ByteArray?) {
    if (value != null) {
        this.put(key, value)
    }
}

/**
 * Extension function that maps a single object to [JsonNode] and adds it to the [ObjectNode] as [ArrayNode].
 *
 * @param fieldName [ObjectNode] entry key
 * @param mapper Supplier that maps a single object to [JsonNode]
 */
internal inline fun ObjectNode.putSingletonAsArray(fieldName: String, mapper: () -> JsonNode?) {
    val jsonNode = mapper.invoke()
    if (jsonNode != null && !jsonNode.isNull) {
        this.putArray(fieldName).addIfNotNull(mapper.invoke())
    }
}

/**
 * Extension function that returns [JsonNode] of the "" key if it is the only one, otherwise, it returns [ObjectNode].
 *
 * @return [JsonNode] of the "" key if it is the only one, otherwise, it returns [ObjectNode].
 */
internal fun ObjectNode.resolve(): JsonNode {
    return if (this.getFieldNames().size == 1 && this.has("")) {
        this.get("")
    } else {
        this
    }
}

/**
 *  Extension function that maps [Collection] of RM objects in RAW format to [ArrayNode] and adds it to the [ObjectNode].
 *
 * @param fieldName [ObjectNode] entry key
 * @param collection [Collection] of RM objects in RAW format
 * @param mapper Function that maps RM object in RAW format to the [JsonNode]
 */
internal inline fun <R : RmObject> ObjectNode.putCollectionAsArray(fieldName: String, collection: Collection<R>, mapper: (R) -> JsonNode?) {
    if (collection.isNotEmpty()) {
        with(this.putArray(fieldName)) {
            collection.forEach {
                this.addIfNotNull(mapper.invoke(it))
            }
        }
    }
}

/**
 * Extension function that recursively check if [JsonNode] is empty.
 *
 * @return [Boolean] that indicates if [JsonNode] is empty.
 */
internal fun JsonNode.isEmptyInDepth(): Boolean =
    when {
        this.isMissingNode -> true
        this.isNull -> true
        this.isTextual && this.asText().isBlank() -> true
        this.isObject -> {
            if (RmObjectLeafNodeFactoryDelegator.delegateIsEmpty(this as ObjectNode)) {
                true
            } else {
                val fieldNames = this.getFieldNames()
                fieldNames.all { this[it].isEmptyInDepth() }
            }
        }
        this.isArray -> this.all { it.isEmptyInDepth() }
        else -> false

    }

/**
 * Extension function that converts [ArrayNode] to the singleton [ValueNode], [ObjectNode] or [NullNode].
 * NOTE: The fields of the last element in the table will have the greatest priority.
 *
 * @return [ValueNode], [ObjectNode] or [NullNode]
 */
internal fun ArrayNode.toSingletonReversed(conversionContext: ConversionContext, webTemplatePath: WebTemplatePath): JsonNode {
    val size = size()
    if (size > 1 && conversionContext.strictMode) {
        throw ConversionException("JSON array with single value is expected", webTemplatePath.toString())
    }

    return if (size == 1) get(0) else mergeToSingletonReversed()
}

private fun ArrayNode.mergeToSingletonReversed(): JsonNode {
    val objectNode = ConversionObjectMapper.createObjectNode()

    this.reversed().forEach { node ->
        if (!node.isNull && !node.isMissingNode && !node.isTextual && node.asText().isBlank()) {
            if (node.isValueNode) {
                objectNode.set<JsonNode>("", node)
            } else {
                node.fields().forEach {
                    if (!objectNode.has(it.key)) {
                        objectNode.set<JsonNode>(it.key, it.value)
                    }
                }
            }
        }
    }

    return when {
        objectNode.isEmpty -> ConversionObjectMapper.nullNode()
        objectNode.getFieldNames().size == 1 && objectNode.has("") -> objectNode.get("")
        else -> objectNode
    }
}
