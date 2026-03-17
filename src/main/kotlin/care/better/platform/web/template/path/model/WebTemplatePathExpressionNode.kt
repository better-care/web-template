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

package care.better.platform.web.template.path.model

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Sealed class hierarchy representing nodes for WebTemplate path expressions.
 */
sealed class WebTemplatePathExpressionNode {

    data class PathSegment(val key: String) : WebTemplatePathExpressionNode()

    data class PathSegmentWithIndex(val key: String, val index: Int) : WebTemplatePathExpressionNode()

    data class PathSegmentWithPredicate(val key: String, val index: Int?, val attributeName: String, val operator: PathOperator, val value: Any) : WebTemplatePathExpressionNode()

    data class AttributeAccess(val name: String) : WebTemplatePathExpressionNode()

    data class FunctionCall(val name: PathFunction, val args: List<WebTemplatePathExpressionNode> = emptyList()) : WebTemplatePathExpressionNode()

    data class PathExpressionExpression(
        val steps: List<WebTemplatePathExpressionNode>,
        val attribute: AttributeAccess?,
        val functions: List<FunctionCall>,
        val source: WebTemplatePathExpressionNode? = null) : WebTemplatePathExpressionNode()

    data class BinaryOp(
        val operator: PathOperator,
        val left: WebTemplatePathExpressionNode,
        val right: WebTemplatePathExpressionNode) : WebTemplatePathExpressionNode()

    data class UnaryMinus(val operand: WebTemplatePathExpressionNode) : WebTemplatePathExpressionNode()

    data class NumberLiteral(val value: Number) : WebTemplatePathExpressionNode()

    data class StringLiteral(val value: String) : WebTemplatePathExpressionNode()

    data class BooleanLiteral(val value: Boolean) : WebTemplatePathExpressionNode()

    data object ThisReference : WebTemplatePathExpressionNode()

    data object IndexReference : WebTemplatePathExpressionNode()

    data object TotalReference : WebTemplatePathExpressionNode()
}
