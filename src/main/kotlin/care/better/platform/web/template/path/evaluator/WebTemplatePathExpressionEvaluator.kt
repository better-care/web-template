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

package care.better.platform.web.template.path.evaluator

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.path.model.WebTemplatePathExpressionNode
import care.better.platform.web.template.path.model.WebTemplatePathExpressionNode.*
import care.better.platform.web.template.path.model.PathFunction
import care.better.platform.web.template.path.model.PathOperator
import care.better.platform.web.template.path.exception.WebTemplatePathExpressionException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.truncate
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Evaluates a [WebTemplatePathExpressionNode] against a STRUCTURED RM object.
 */
internal class WebTemplatePathExpressionEvaluator private constructor(private val tracked: List<TrackedNode>) {

    companion object {
        private const val REGEX_TIMEOUT_SECONDS = 5L

        /**
         * Evaluates WebTemplate path expression against a structured RM object with web template awareness.
         *
         * @param node [WebTemplatePathExpressionNode]
         * @param structuredRmObject Structured RM object
         * @param webTemplateNode [WebTemplateNode] for path validation
         * @return [List] of [TrackedNode]
         */
        internal fun evaluate(node: WebTemplatePathExpressionNode, structuredRmObject: ObjectNode, webTemplateNode: WebTemplateNode): List<TrackedNode> =
            WebTemplatePathExpressionEvaluator(stripWithTemplate(structuredRmObject, webTemplateNode)).eval(node).tracked

        private fun <T> withRegexTimeout(pattern: String, block: () -> T): T {
            val executor = Executors.newSingleThreadExecutor()
            try {
                val future = executor.submit(Callable { block() })
                return try {
                    future.get(REGEX_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                } catch (_: TimeoutException) {
                    future.cancel(true)
                    throw WebTemplatePathExpressionException("Regex evaluation timed out for pattern: $pattern")
                }
            } finally {
                executor.shutdownNow()
            }
        }

        private fun stripWithTemplate(structuredRmObject: ObjectNode, webTemplateNode: WebTemplateNode): List<TrackedNode> {
            val compositionFields = structuredRmObject.fieldNames().asSequence().toList().filter { it != "ctx" }
            return if (compositionFields.size == 1) {
                flattenArrays(structuredRmObject.get(compositionFields[0])).map { TrackedNode(it, webTemplateNode) }
            } else {
                listOf(TrackedNode(structuredRmObject, webTemplateNode))
            }
        }

        private fun flattenArrays(node: JsonNode): List<JsonNode> = if (node is ArrayNode) node.toList() else listOf(node)
    }

    private data class EvalContext(val value: Any? = null, val index: Int? = null, val total: Any? = null)

    private data class EvaluationState(val tracked: List<TrackedNode>) {
        val nodes: List<JsonNode> = tracked.map { it.json }
    }

    private fun eval(node: WebTemplatePathExpressionNode, context: EvalContext = EvalContext()): EvaluationState =
        when (node) {
            is PathExpressionExpression -> evaluatePathExpression(node, context)
            is BinaryOp -> evaluateBinaryOp(node, context)
            is UnaryMinus -> evaluateUnaryMinus(node, context)
            is NumberLiteral -> convertValueToEvaluationState(node.value)
            is StringLiteral -> convertValueToEvaluationState(node.value)
            is BooleanLiteral -> convertValueToEvaluationState(node.value)
            is ThisReference -> convertValueToEvaluationState(context.value)
            is IndexReference -> convertValueToEvaluationState(context.index)
            is TotalReference -> convertValueToEvaluationState(context.total)
            else -> throw WebTemplatePathExpressionException("Unexpected top-level node: $node")
        }

    private fun convertValueToEvaluationState(value: Any?): EvaluationState =
        if (value == null) {
            EvaluationState(emptyList())
        } else {
            EvaluationState(listOf(TrackedNode(valueToNode(value), null)))
        }

    private fun evaluatePathExpression(expression: PathExpressionExpression, context: EvalContext): EvaluationState {
        val navigated = expression.steps.fold(if (expression.source != null) eval(expression.source, context) else EvaluationState(tracked)) { acc, node ->
            evaluatePathSegment(node, acc)
        }

        return expression.functions.fold(if (expression.attribute != null) attributeAccess(expression.attribute, navigated) else navigated) { acc, fn ->
            applyFunction(fn, acc, context)
        }
    }

    private fun evaluateBinaryOp(operator: BinaryOp, context: EvalContext): EvaluationState {
        return when (operator.operator) {
            PathOperator.PLUS, PathOperator.MINUS, PathOperator.STAR, PathOperator.DIV, PathOperator.MOD -> {
                val leftState = eval(operator.left, context)
                val rightState = eval(operator.right, context)
                if (operator.operator == PathOperator.PLUS && (resolveToValueOrNull(leftState) is String || resolveToValueOrNull(rightState) is String)) {
                    val left = resolveToValueOrNull(leftState)?.toString() ?: ""
                    val right = resolveToValueOrNull(rightState)?.toString() ?: ""
                    return convertValueToEvaluationState(left + right)
                }
                val left = resolveToNumber(leftState)
                val right = resolveToNumber(rightState)
                val result: Number = when (operator.operator) {
                    PathOperator.PLUS -> left.toDouble() + right.toDouble()
                    PathOperator.MINUS -> left.toDouble() - right.toDouble()
                    PathOperator.STAR -> left.toDouble() * right.toDouble()
                    PathOperator.DIV -> left.toDouble() / right.toDouble()
                    PathOperator.MOD -> left.toDouble() % right.toDouble()
                    else -> throw WebTemplatePathExpressionException("Operator $operator is not supported.")
                }
                convertValueToEvaluationState(simplifyNumber(result))
            }

            PathOperator.GT, PathOperator.GTE, PathOperator.LT, PathOperator.LTE -> {
                val cmp = compareValues(resolveToComparable(eval(operator.left, context)), resolveToComparable(eval(operator.right, context)))
                convertValueToEvaluationState(
                    when (operator.operator) {
                        PathOperator.GT -> cmp > 0
                        PathOperator.GTE -> cmp >= 0
                        PathOperator.LT -> cmp < 0
                        PathOperator.LTE -> cmp <= 0
                        else -> throw WebTemplatePathExpressionException("Operator $operator is not supported.")
                    })
            }

            PathOperator.EQUALS, PathOperator.NEQ -> {
                val equal = valuesEqual(resolveToValueOrNull(eval(operator.left, context)), resolveToValueOrNull(eval(operator.right, context)))
                convertValueToEvaluationState(if (operator.operator == PathOperator.EQUALS) equal else !equal)
            }

            PathOperator.AND -> convertValueToEvaluationState(resolveToBoolean(eval(operator.left, context)) && resolveToBoolean(eval(operator.right, context)))
            PathOperator.OR -> convertValueToEvaluationState(resolveToBoolean(eval(operator.left, context)) || resolveToBoolean(eval(operator.right, context)))
            PathOperator.XOR -> convertValueToEvaluationState(resolveToBoolean(eval(operator.left, context)) xor resolveToBoolean(eval(operator.right, context)))
            PathOperator.IMPLIES -> convertValueToEvaluationState(!resolveToBoolean(eval(operator.left, context)) || resolveToBoolean(eval(operator.right, context)))
        }
    }

    private fun evaluateUnaryMinus(node: UnaryMinus, context: EvalContext): EvaluationState =
        convertValueToEvaluationState(simplifyNumber(-resolveToNumber(eval(node.operand, context)).toDouble()))

    private fun evaluatePathSegment(step: WebTemplatePathExpressionNode, state: EvaluationState): EvaluationState {
        return when (step) {
            is PathSegment -> EvaluationState(navigatePathSegment(step.key, state.tracked))
            is PathSegmentWithIndex -> EvaluationState(navigatePathSegmentWithIndex(step.key, step.index, state.tracked))
            is PathSegmentWithPredicate -> EvaluationState(
                navigatePathSegmentWithPredicate(
                    step.key,
                    step.index,
                    step.attributeName,
                    step.operator,
                    step.value,
                    state.tracked))

            else -> throw WebTemplatePathExpressionException("Unexpected step type: $step")
        }
    }

    private fun convertStateToValues(state: EvaluationState): List<Any?> =
        state.nodes.map { nodeToValue(it) }

    private fun resolveChildTemplate(webTemplateNode: WebTemplateNode?, key: String): WebTemplateNode? =
        if (webTemplateNode == null) {
            null
        } else {
            webTemplateNode.children.firstOrNull { it.jsonIdMatches(key) }
                ?: throw WebTemplatePathExpressionException(
                    "Invalid path segment '$key': not a child of '${webTemplateNode.jsonId}' " +
                        "(rmType: ${webTemplateNode.rmType}). " +
                        "Valid children: ${webTemplateNode.children.map { it.jsonId }}"
                )
        }

    private fun navigatePathSegment(key: String, tracked: List<TrackedNode>): List<TrackedNode> =
        tracked.flatMap { t ->
            val childTemplate = resolveChildTemplate(t.template, key)
            if (t.json is ObjectNode && t.json.has(key)) {
                flattenArrays(t.json.get(key)).map { TrackedNode(it, childTemplate) }
            } else {
                emptyList()
            }
        }

    private fun navigatePathSegmentWithIndex(key: String, index: Int, tracked: List<TrackedNode>): List<TrackedNode> =
        tracked.flatMap { t ->
            val childTemplate = resolveChildTemplate(t.template, key)
            if (t.json is ObjectNode && t.json.has(key)) {
                val child = t.json.get(key)
                val nodes = if (child is ArrayNode) {
                    if (index < child.size()) listOf(child.get(index)) else emptyList()
                } else if (index == 0) {
                    listOf(child)
                } else {
                    emptyList()
                }
                nodes.map { TrackedNode(it, childTemplate) }
            } else {
                emptyList()
            }
        }

    private fun navigatePathSegmentWithPredicate(
        key: String,
        index: Int?,
        attributeName: String,
        operator: PathOperator,
        value: Any,
        tracked: List<TrackedNode>): List<TrackedNode> =
        tracked.flatMap { trackedNode ->
            val childTemplate = resolveChildTemplate(trackedNode.template, key)
            if (trackedNode.json is ObjectNode && trackedNode.json.has(key)) {
                val child = trackedNode.json.get(key)
                val elements = if (index != null) {
                    if (child is ArrayNode) {
                        if (index < child.size()) listOf(child.get(index)) else emptyList()
                    } else if (index == 0) {
                        listOf(child)
                    } else {
                        emptyList()
                    }
                } else {
                    flattenArrays(child)
                }
                elements.filter { element ->
                    element is ObjectNode &&
                        element.has("|$attributeName") && matchesPredicate(element.get("|$attributeName"), operator, value)
                }.map { TrackedNode(it, childTemplate) }
            } else {
                emptyList()
            }
        }

    private fun matchesPredicate(attributeNode: JsonNode, operator: PathOperator, value: Any): Boolean {
        return when (value) {
            is Boolean -> {
                val nodeValue = attributeNode.asText().toBooleanStrictOrNull() ?: return false
                when (operator) {
                    PathOperator.EQUALS -> nodeValue == value
                    PathOperator.NEQ -> nodeValue != value
                    else -> throw WebTemplatePathExpressionException("Operator $operator is not supported for boolean predicates")
                }
            }

            is Number -> {
                val nodeValue = attributeNode.asText().toDoubleOrNull() ?: return false
                val comparable = nodeValue.compareTo(value.toDouble())
                when (operator) {
                    PathOperator.EQUALS -> comparable == 0
                    PathOperator.NEQ -> comparable != 0
                    PathOperator.GT -> comparable > 0
                    PathOperator.GTE -> comparable >= 0
                    PathOperator.LT -> comparable < 0
                    PathOperator.LTE -> comparable <= 0
                    else -> throw WebTemplatePathExpressionException("Operator $operator is not supported for numeric predicates")
                }
            }

            else -> {
                val nodeText = attributeNode.asText()
                val stringValue = value as String
                when (operator) {
                    PathOperator.EQUALS -> nodeText == stringValue
                    PathOperator.NEQ -> nodeText != stringValue
                    PathOperator.GT -> nodeText > stringValue
                    PathOperator.GTE -> nodeText >= stringValue
                    PathOperator.LT -> nodeText < stringValue
                    PathOperator.LTE -> nodeText <= stringValue
                    else -> throw WebTemplatePathExpressionException("Operator $operator is not supported for string predicates")
                }
            }
        }
    }

    private fun attributeAccess(attribute: AttributeAccess, state: EvaluationState): EvaluationState =
        EvaluationState(
            state.nodes
                .mapNotNull { node -> if (node is ObjectNode && node.has("|${attribute.name}")) node.get("|${attribute.name}") else null }
                .map { TrackedNode(it, null) })

    private fun applyFunction(function: FunctionCall, state: EvaluationState, context: EvalContext): EvaluationState {
        return when (function.name) {
            PathFunction.COUNT -> convertValueToEvaluationState(state.nodes.size)
            PathFunction.EXISTS -> convertValueToEvaluationState(state.nodes.isNotEmpty())
            PathFunction.EMPTY -> convertValueToEvaluationState(state.nodes.isEmpty())
            PathFunction.FIRST -> first(state)
            PathFunction.LAST -> last(state)
            PathFunction.TAIL -> tail(state)
            PathFunction.SINGLE -> single(state)
            PathFunction.SELECT -> applySelect(function, state)
            PathFunction.UNION -> applyUnion(function, state)
            PathFunction.COMBINE -> combine(function, state)
            PathFunction.INTERSECT -> intersect(function, state)
            PathFunction.EXCLUDE -> exclude(function, state)
            PathFunction.SORT -> sort(state)
            PathFunction.DISTINCT -> distinct(state)
            PathFunction.IS_DISTINCT -> convertValueToEvaluationState(convertStateToValues(state).let { it.size == it.distinct().size })
            PathFunction.LOWER -> transformNodes(state) { TextNode(it.asText().lowercase()) }
            PathFunction.UPPER -> transformNodes(state) { TextNode(it.asText().uppercase()) }
            PathFunction.SUM -> aggregate(state) { values -> values.sumOf { toNumber(it).toDouble() } }
            PathFunction.MIN -> aggregate(state) { values -> values.minOfOrNull { toNumber(it).toDouble() } }
            PathFunction.MAX -> aggregate(state) { values -> values.maxOfOrNull { toNumber(it).toDouble() } }
            PathFunction.AVG -> aggregate(state) { values -> if (values.isEmpty()) null else values.sumOf { toNumber(it).toDouble() } / values.size }
            PathFunction.ALL -> convertValueToEvaluationState(convertStateToValues(state).all { isTruthy(it) })
            PathFunction.ANY -> convertValueToEvaluationState(convertStateToValues(state).any { isTruthy(it) })
            PathFunction.NONE -> convertValueToEvaluationState(convertStateToValues(state).none { isTruthy(it) })
            PathFunction.OF_TYPE -> ofType(function, state)
            PathFunction.SUBSTRING -> substring(function, state)
            PathFunction.MATCHES -> matches(function, state)
            PathFunction.REPLACE_MATCHES -> replaceMatches(function, state)
            PathFunction.SKIP -> skip(function, state)
            PathFunction.TAKE -> take(function, state)
            PathFunction.INDEX_OF -> transformNodes(state) { node -> JsonNodeFactory.instance.numberNode(node.asText().indexOf((function.args[0] as StringLiteral).value)) }
            PathFunction.STARTS_WITH -> transformNodes(state) { node -> JsonNodeFactory.instance.booleanNode(node.asText().startsWith((function.args[0] as StringLiteral).value)) }
            PathFunction.ENDS_WITH -> transformNodes(state) { node -> JsonNodeFactory.instance.booleanNode(node.asText().endsWith((function.args[0] as StringLiteral).value)) }
            PathFunction.CONTAINS -> transformNodes(state) { node -> JsonNodeFactory.instance.booleanNode(node.asText().contains((function.args[0] as StringLiteral).value)) }
            PathFunction.REPLACE -> transformNodes(state) { node -> TextNode(node.asText().replace((function.args[0] as StringLiteral).value, (function.args[1] as StringLiteral).value)) }
            PathFunction.LENGTH -> transformNodes(state) { node -> JsonNodeFactory.instance.numberNode(node.asText().length) }
            PathFunction.TO_CHARS -> toChars(state)
            PathFunction.NOT -> transformNodes(state) { node -> JsonNodeFactory.instance.booleanNode(!isTruthy(nodeToValue(node))) }
            PathFunction.ABS -> transformNodes(state) { node -> numberToNode(simplifyNumber(abs(toNumber(nodeToValue(node)).toDouble()))) }
            PathFunction.CEILING -> transformNodes(state) { node -> numberToNode(simplifyNumber(ceil(toNumber(nodeToValue(node)).toDouble()))) }
            PathFunction.FLOOR -> transformNodes(state) { node -> numberToNode(simplifyNumber(floor(toNumber(nodeToValue(node)).toDouble()))) }
            PathFunction.TRUNCATE -> transformNodes(state) { node -> numberToNode(simplifyNumber(truncate(toNumber(nodeToValue(node)).toDouble()))) }
            PathFunction.SQRT -> transformNodes(state) { node -> numberToNode(simplifyNumber(sqrt(toNumber(nodeToValue(node)).toDouble()))) }
            PathFunction.POWER -> transformNodes(state) { node -> numberToNode(simplifyNumber(toNumber(nodeToValue(node)).toDouble().pow((function.args[0] as NumberLiteral).value.toDouble()))) }
            PathFunction.LN -> transformNodes(state) { node -> numberToNode(simplifyNumber(ln(toNumber(nodeToValue(node)).toDouble()))) }
            PathFunction.LOG -> transformNodes(state) { node -> numberToNode(simplifyNumber(ln(toNumber(nodeToValue(node)).toDouble()) / ln((function.args[0] as NumberLiteral).value.toDouble()))) }
            PathFunction.EXP -> transformNodes(state) { node -> numberToNode(simplifyNumber(exp(toNumber(nodeToValue(node)).toDouble()))) }
            PathFunction.TO_INTEGER -> transformNodes(state) { node -> JsonNodeFactory.instance.numberNode(toNumber(nodeToValue(node)).toInt()) }
            PathFunction.TO_DECIMAL -> transformNodes(state) { node -> JsonNodeFactory.instance.numberNode(toNumber(nodeToValue(node)).toDouble()) }
            PathFunction.TO_BOOLEAN -> transformNodes(state) { node -> JsonNodeFactory.instance.booleanNode(toBooleanValue(nodeToValue(node))) }
            PathFunction.TO_STRING -> transformNodes(state) { node -> TextNode(nodeToValue(node)?.toString() ?: "") }
            PathFunction.ALL_TRUE -> convertValueToEvaluationState(convertStateToValues(state).all { it == true })
            PathFunction.ANY_TRUE -> convertValueToEvaluationState(convertStateToValues(state).any { it == true })
            PathFunction.ALL_FALSE -> convertValueToEvaluationState(convertStateToValues(state).all { it == false })
            PathFunction.ANY_FALSE -> convertValueToEvaluationState(convertStateToValues(state).any { it == false })
            PathFunction.NOW -> convertValueToEvaluationState(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
            PathFunction.TODAY -> convertValueToEvaluationState(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
            PathFunction.TIME_OF_DAY -> convertValueToEvaluationState(LocalTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME))
            PathFunction.CHILDREN -> children(state)
            PathFunction.DESCENDANTS -> descendants(state)
            PathFunction.SUBSET_OF -> subsetOf(function, state)
            PathFunction.SUPERSET_OF -> supersetOf(function, state)
            PathFunction.IIF -> iif(function, state, context)
            PathFunction.AGGREGATE -> aggregateExpression(function, state, context)
            PathFunction.REPEAT -> repeat(function, state, context)
            PathFunction.ROUND -> {
                val precision = if (function.args.isNotEmpty()) (function.args[0] as NumberLiteral).value.toInt() else 0
                transformNodes(state) { node ->
                    val factor = 10.0.pow(precision.toDouble())
                    numberToNode(simplifyNumber((toNumber(nodeToValue(node)).toDouble() * factor).roundToInt() / factor))
                }
            }
        }
    }

    private fun first(state: EvaluationState): EvaluationState =
        EvaluationState(if (state.tracked.isEmpty()) emptyList() else listOf(state.tracked.first()))

    private fun last(state: EvaluationState): EvaluationState =
        EvaluationState(if (state.tracked.isEmpty()) emptyList() else listOf(state.tracked.last()))

    private fun tail(state: EvaluationState): EvaluationState =
        EvaluationState(if (state.tracked.size <= 1) emptyList() else state.tracked.drop(1))

    private fun single(state: EvaluationState): EvaluationState =
        if (state.tracked.size != 1) {
            throw WebTemplatePathExpressionException("single() requires exactly one element, found ${state.tracked.size}")
        } else {
            EvaluationState(state.tracked)
        }

    private fun applySelect(function: FunctionCall, state: EvaluationState): EvaluationState {
        val pathExpression = function.args[0] as PathExpressionExpression
        val result = state.tracked.flatMap { t ->
            val navigated = pathExpression.steps.fold(listOf(t)) { acc, step ->
                when (step) {
                    is PathSegment -> navigatePathSegment(step.key, acc)
                    is PathSegmentWithIndex -> navigatePathSegmentWithIndex(step.key, step.index, acc)
                    is PathSegmentWithPredicate -> navigatePathSegmentWithPredicate(step.key, step.index, step.attributeName, step.operator, step.value, acc)
                    else -> throw WebTemplatePathExpressionException("Unexpected step in select: $step")
                }
            }
            if (pathExpression.attribute != null) {
                val attrKey = "|${pathExpression.attribute.name}"
                navigated.mapNotNull { n -> if (n.json is ObjectNode && n.json.has(attrKey)) TrackedNode(n.json.get(attrKey), null) else null }
            } else {
                navigated
            }
        }
        return EvaluationState(result)
    }

    private fun applyUnion(function: FunctionCall, state: EvaluationState): EvaluationState {
        val other = evaluateTrackedPathArgFromRoot(function.args[0] as PathExpressionExpression)
        val (unionNodes, _) = (state.tracked + other).fold(Pair(emptyList<TrackedNode>(), emptySet<String>())) { (acc, seen), node ->
            val key = node.json.toString()
            if (key in seen) Pair(acc, seen) else Pair(acc + node, seen + key)
        }
        return EvaluationState(unionNodes)
    }

    private fun combine(function: FunctionCall, state: EvaluationState): EvaluationState =
        EvaluationState(state.tracked + evaluateTrackedPathArgFromRoot(function.args[0] as PathExpressionExpression))

    private fun intersect(function: FunctionCall, state: EvaluationState): EvaluationState =
        evaluateTrackedPathArgFromRoot(function.args[0] as PathExpressionExpression).map { nodeToValue(it.json) }.toSet().let { otherValues ->
            EvaluationState(state.tracked.filter { nodeToValue(it.json) in otherValues })
        }

    private fun exclude(function: FunctionCall, state: EvaluationState): EvaluationState =
        evaluateTrackedPathArgFromRoot(function.args[0] as PathExpressionExpression).map { nodeToValue(it.json) }.toSet().let { otherValues ->
            EvaluationState(state.tracked.filter { nodeToValue(it.json) !in otherValues })
        }

    private fun evaluateTrackedPathArgFromRoot(pathExpression: PathExpressionExpression): List<TrackedNode> {
        val segment = pathExpression.steps.fold(tracked) { acc, step ->
            when (step) {
                is PathSegment -> navigatePathSegment(step.key, acc)
                is PathSegmentWithIndex -> navigatePathSegmentWithIndex(step.key, step.index, acc)
                is PathSegmentWithPredicate -> navigatePathSegmentWithPredicate(step.key, step.index, step.attributeName, step.operator, step.value, acc)
                else -> throw WebTemplatePathExpressionException("Unexpected step in path arg: $step")
            }
        }
        if (pathExpression.attribute != null) {
            val attrKey = "|${pathExpression.attribute.name}"
            return segment.mapNotNull { t -> if (t.json is ObjectNode && t.json.has(attrKey)) TrackedNode(t.json.get(attrKey), null) else null }
        }
        return segment
    }

    private fun sort(state: EvaluationState): EvaluationState =
        EvaluationState(state.tracked.sortedWith(compareBy<TrackedNode> { !it.json.isNumber }.thenComparing { a, b ->
            if (a.json.isNumber) a.json.doubleValue().compareTo(b.json.doubleValue()) else a.json.asText().compareTo(b.json.asText())
        }))

    private fun distinct(state: EvaluationState): EvaluationState {
        val (distinctNodes, _) = state.tracked.fold(Pair(emptyList<TrackedNode>(), emptySet<String>())) { (acc, seen), node ->
            val key = node.json.toString()
            if (key in seen) Pair(acc, seen) else Pair(acc + node, seen + key)
        }
        return EvaluationState(distinctNodes)
    }

    private fun transformNodes(state: EvaluationState, transform: (JsonNode) -> JsonNode): EvaluationState =
        EvaluationState(state.nodes.map { TrackedNode(transform(it), null) })

    private fun aggregate(state: EvaluationState, aggregate: (List<Any?>) -> Number?): EvaluationState =
        aggregate(convertStateToValues(state)).let {
            if (it != null) convertValueToEvaluationState(simplifyNumber(it)) else EvaluationState(emptyList())
        }

    private fun ofType(function: FunctionCall, state: EvaluationState): EvaluationState {
        val typeName = (function.args[0] as StringLiteral).value
        val jsonType = JsonPrimitiveType.fromName(typeName.lowercase())
        return if (jsonType != null) {
            EvaluationState(state.tracked.filter { jsonType.matches(it.json) })
        } else {
            EvaluationState(state.tracked.filter { it.template?.rmType?.equals(typeName, ignoreCase = true) == true })
        }
    }

    private fun substring(function: FunctionCall, state: EvaluationState): EvaluationState {
        val start = (function.args[0] as NumberLiteral).value.toInt()
        val length = if (function.args.size > 1) (function.args[1] as NumberLiteral).value.toInt() else null
        return transformNodes(state) { node ->
            val text = node.asText()
            val result = if (start >= text.length) {
                ""
            } else if (length != null) {
                text.substring(start, (start + length).coerceAtMost(text.length))
            } else {
                text.substring(start)
            }
            TextNode(result)
        }
    }

    private fun matches(function: FunctionCall, state: EvaluationState): EvaluationState {
        val pattern = (function.args[0] as StringLiteral).value
        val regex = Regex(pattern)
        return transformNodes(state) { node ->
            JsonNodeFactory.instance.booleanNode(withRegexTimeout(pattern) { regex.matches(node.asText()) })
        }
    }

    private fun replaceMatches(function: FunctionCall, state: EvaluationState): EvaluationState {
        val pattern = (function.args[0] as StringLiteral).value
        val regex = Regex(pattern)
        val replacement = (function.args[1] as StringLiteral).value
        return transformNodes(state) { node ->
            TextNode(withRegexTimeout(pattern) { regex.replace(node.asText(), replacement) })
        }
    }

    private fun skip(function: FunctionCall, state: EvaluationState): EvaluationState =
        EvaluationState(state.tracked.drop((function.args[0] as NumberLiteral).value.toInt()))

    private fun take(function: FunctionCall, state: EvaluationState): EvaluationState =
        EvaluationState(state.tracked.take((function.args[0] as NumberLiteral).value.toInt()))

    private fun toChars(state: EvaluationState): EvaluationState =
        EvaluationState(state.nodes.flatMap { node -> node.asText().toList().map { TrackedNode(TextNode(it.toString()), null) } })

    private fun children(state: EvaluationState): EvaluationState =
        EvaluationState(
            state.nodes.flatMap { node ->
                if (node is ObjectNode) {
                    node.properties().asSequence().flatMap { (_, v) -> flattenArrays(v) }.toList()
                } else {
                    emptyList()
                }
            }.map { TrackedNode(it, null) })

    private fun descendants(state: EvaluationState): EvaluationState {

        fun collect(node: JsonNode): List<JsonNode> =
            if (node is ObjectNode) {
                node.properties().asSequence()
                    .flatMap { (_, v) -> flattenArrays(v).asSequence() }
                    .flatMap { child -> sequenceOf(child) + collect(child).asSequence() }
                    .toList()
            } else {
                emptyList()
            }

        return EvaluationState(
            state.nodes
                .flatMap { collect(it) }
                .map { TrackedNode(it, null) }
        )
    }

    private fun subsetOf(function: FunctionCall, state: EvaluationState): EvaluationState =
        convertValueToEvaluationState(
            evaluateTrackedPathArgFromRoot(function.args[0] as PathExpressionExpression).map { nodeToValue(it.json) }.toSet()
                .containsAll(convertStateToValues(state).toSet()))

    private fun supersetOf(function: FunctionCall, state: EvaluationState): EvaluationState =
        convertValueToEvaluationState(
            convertStateToValues(state).toSet()
                .containsAll(evaluateTrackedPathArgFromRoot(function.args[0] as PathExpressionExpression).map { nodeToValue(it.json) }.toSet()))

    private fun iif(function: FunctionCall, state: EvaluationState, context: EvalContext): EvaluationState {
        val falseArg = function.args.getOrNull(2)
        val trackedNode = state.tracked.firstOrNull()
        val branchContext = if (trackedNode != null) context.copy(value = nodeToValue(trackedNode.json)) else context
        val condition = trackedNode?.let {
            isTruthy(resolveToValueOrNull(eval(function.args[0], branchContext)))
        } ?: false

        return when {
            condition -> eval(function.args[1], branchContext)
            falseArg != null -> eval(falseArg, branchContext)
            else -> EvaluationState(emptyList())
        }
    }

    private fun aggregateExpression(function: FunctionCall, state: EvaluationState, context: EvalContext): EvaluationState =
        convertStateToValues(state).foldIndexed(if (function.args.size > 1) resolveToValueOrNull(eval(function.args[1], context)) else null) { i, acc, value ->
            resolveToValueOrNull(eval(function.args[0], context.copy(value = value, index = i, total = acc)))
        }.let {
            if (it != null) convertValueToEvaluationState(it) else EvaluationState(emptyList())
        }

    private fun repeat(function: FunctionCall, state: EvaluationState, context: EvalContext): EvaluationState {
        val projection = function.args[0]

        tailrec fun loop(current: List<Any?>, acc: List<TrackedNode>, remaining: Int): EvaluationState {
            if (remaining == 0) {
                throw WebTemplatePathExpressionException("repeat() exceeded maximum of 100 iterations")
            }

            val next = current.mapIndexedNotNull { i, value ->
                resolveToValueOrNull(eval(projection, context.copy(value = value, index = i)))?.takeIf { it != Unit }
            }

            if (next.isEmpty() || next == current) {
                return EvaluationState(acc)
            }

            return loop(next, acc + next.map { TrackedNode(valueToNode(it), null) }, remaining - 1)
        }

        return loop(convertStateToValues(state), emptyList(), 100)
    }

    private fun nodeToValue(node: JsonNode): Any? =
        when {
            node.isNumber -> node.numberValue()
            node.isBoolean -> node.booleanValue()
            node.isTextual -> node.textValue()
            node.isNull -> null
            else -> node
        }

    private fun valueToNode(value: Any?): JsonNode =
        when (value) {
            is Number -> numberToNode(value)
            is Boolean -> JsonNodeFactory.instance.booleanNode(value)
            is String -> TextNode(value)
            is JsonNode -> value
            null -> JsonNodeFactory.instance.nullNode()
            else -> TextNode(value.toString())
        }

    private fun numberToNode(number: Number): JsonNode =
        simplifyNumber(number).let {
            return if (it is Long) {
                JsonNodeFactory.instance.numberNode(it)
            } else {
                JsonNodeFactory.instance.numberNode(it.toDouble())
            }
        }

    private fun resolveToNumber(state: EvaluationState): Number =
        if (state.nodes.isEmpty()) {
            throw WebTemplatePathExpressionException("Cannot convert empty collection to number")
        } else {
            toNumber(nodeToValue(state.nodes.first()))
        }

    private fun resolveToValueOrNull(state: EvaluationState): Any? =
        if (state.nodes.isEmpty()) null else nodeToValue(state.nodes.first())

    private fun resolveToBoolean(state: EvaluationState): Boolean = isTruthy(resolveToValueOrNull(state))

    @Suppress("UNCHECKED_CAST")
    private fun resolveToComparable(state: EvaluationState): Comparable<Any> =
        when (val value = resolveToValueOrNull(state)) {
            is Number -> value.toDouble() as Comparable<Any>
            is String -> value as Comparable<Any>
            is Boolean -> value as Comparable<Any>
            else -> throw WebTemplatePathExpressionException("Cannot compare value: $value")
        }

    private fun valuesEqual(left: Any?, right: Any?): Boolean =
        when {
            left == right -> true
            (left is Number && right is Number) -> left.toDouble() == right.toDouble()
            else -> false
        }

    private fun toNumber(value: Any?): Number =
        when (value) {
            is Number -> value
            is String -> value.toDoubleOrNull() ?: throw WebTemplatePathExpressionException("Cannot convert '$value' to number")
            is Boolean -> if (value) 1 else 0
            else -> throw WebTemplatePathExpressionException("Cannot convert ${value?.javaClass?.simpleName} to number")
        }

    private fun toBooleanValue(value: Any?): Boolean =
        when (value) {
            is Boolean -> value
            is String -> when (value.lowercase()) {
                "true" -> true
                "false" -> false
                else -> throw WebTemplatePathExpressionException("Cannot convert '$value' to boolean")
            }

            is Number -> value.toDouble() != 0.0
            else -> throw WebTemplatePathExpressionException("Cannot convert ${value?.javaClass?.simpleName} to boolean")
        }

    private fun simplifyNumber(number: Number): Number =
        number.toDouble().let { if (it == it.toLong().toDouble()) it.toLong() else it }

    private fun isTruthy(value: Any?): Boolean =
        when (value) {
            null -> false
            is Boolean -> value
            is Number -> value.toDouble() != 0.0
            is String -> value.isNotEmpty()
            is JsonNode -> when {
                value.isBoolean -> value.booleanValue()
                value.isNumber -> value.numberValue().toDouble() != 0.0
                value.isTextual -> value.textValue().isNotEmpty()
                value.isNull -> false
                else -> true
            }

            else -> true
        }

}

private enum class JsonPrimitiveType(private val matcher: (JsonNode) -> Boolean, vararg names: String) {
    STRING(JsonNode::isTextual, "string"),
    NUMBER(JsonNode::isNumber, "number", "decimal"),
    INTEGER({ it.isInt || it.isLong }, "integer"),
    BOOLEAN(JsonNode::isBoolean, "boolean"),
    OBJECT(JsonNode::isObject, "object"),
    ARRAY(JsonNode::isArray, "array");

    private val typeNames: Set<String> = names.toSet()

    fun matches(node: JsonNode): Boolean = matcher(node)

    companion object {
        private val byName: Map<String, JsonPrimitiveType> =
            entries.flatMap { type -> type.typeNames.map { name -> name to type } }.toMap()

        fun fromName(name: String): JsonPrimitiveType? = byName[name]
    }
}

internal data class TrackedNode(val json: JsonNode, val template: WebTemplateNode?)
