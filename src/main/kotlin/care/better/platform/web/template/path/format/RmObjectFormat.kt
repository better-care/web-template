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

package care.better.platform.web.template.path.format

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.path.evaluator.TrackedNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.openehr.rm.RmObject

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Enum representing the desired result format for WebTemplate path expression evaluation.
 */
enum class RmObjectFormat {
    FLAT,
    STRUCTURED,
    RAW;

    companion object {
        /**
         * Converts a [List] of evaluation results to the desired format.
         *
         * - [STRUCTURED]: Primitive [JsonNode] values are unwrapped to Kotlin types ([Number], [String], [Boolean], null) and complex nodes are returned as [JsonNode].
         * - [FLAT]: Primitive values are unwrapped and complex nodes are flattened to [Map] of [String] to value.
         * - [RAW]: Primitive values are unwrapped and complex nodes with a tracked [WebTemplateNode] are converted to [RmObject] via [WebTemplate].
         *
         * @param trackedNodes Evaluated [TrackedNode] results
         * @param format Target format
         * @param webTemplate [WebTemplate] required for [RAW] conversion
         * @return [List] of converted values
         */
        @JvmStatic
        @JvmOverloads
        internal fun convert(
            trackedNodes: List<TrackedNode>,
            format: RmObjectFormat,
            webTemplate: WebTemplate? = null): List<Any?> =
            when (format) {
                STRUCTURED -> trackedNodes.map { unwrapPrimitive(it.json) }
                FLAT -> trackedNodes.map { flattenOrUnwrap(it.json) }
                RAW -> trackedNodes.map { convertTrackedToRaw(it, webTemplate) }
            }

        private fun convertTrackedToRaw(tracked: TrackedNode, webTemplate: WebTemplate?): Any? {
            val json = tracked.json
            if (json.isValueNode || json.isNull) {
                return unwrapPrimitive(json)
            }
            if (tracked.template != null && webTemplate != null) {
                return webTemplate.convertFromStructuredToRaw(
                    JsonNodeFactory.instance.objectNode().apply {
                        set<JsonNode>(tracked.template.jsonId, json)
                    },
                    ConversionContext.createForAqlPath(tracked.template.path).build())
            }
            return json
        }

        @JvmStatic
        private fun unwrapPrimitive(node: JsonNode): Any? =
            when {
                node.isNumber -> node.numberValue()
                node.isTextual -> node.textValue()
                node.isBoolean -> node.booleanValue()
                node.isNull -> null
                else -> node
            }

        private fun flattenOrUnwrap(node: JsonNode): Any? =
            when {
                node.isValueNode || node.isNull -> unwrapPrimitive(node)
                else -> flattenNode(node)
            }

        private fun flattenNode(node: JsonNode): Map<String, Any?> =
            LinkedHashMap<String, Any?>().apply {
                flattenNode(node, "", this)
            }

        private fun flattenNode(node: JsonNode, prefix: String, result: MutableMap<String, Any?>) {
            when {
                node.isValueNode || node.isNull -> {
                    result[prefix.ifEmpty { "0" }] = unwrapPrimitive(node)
                }

                node.isObject -> {
                    if (!node.fieldNames().hasNext()) {
                        result[prefix.ifEmpty { "0" }] = null
                        return
                    }
                    for (entry in node.properties()) {
                        val fieldName = entry.key
                        flattenNode(
                            entry.value,
                            when {
                                prefix.isEmpty() -> fieldName
                                fieldName.startsWith("|") -> "$prefix$fieldName"
                                else -> "$prefix/$fieldName"
                            },
                            result)
                    }
                }

                node.isArray -> {
                    for ((idx, child) in node.withIndex()) {
                        flattenNode(child, if (prefix.isEmpty()) "$idx" else "$prefix:$idx", result)
                    }
                }
            }
        }
    }
}