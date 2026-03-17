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

package care.better.platform.web.template.path.parser

import care.better.platform.web.template.path.model.ArgType
import care.better.platform.web.template.path.model.WebTemplatePathExpressionNode
import care.better.platform.web.template.path.model.WebTemplatePathExpressionNode.*
import care.better.platform.web.template.path.model.PathFunction
import care.better.platform.web.template.path.model.PathOperator
import care.better.platform.web.template.path.exception.WebTemplatePathExpressionException

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Visitor that converts an ANTLR parse tree into the [WebTemplatePathExpressionNode].
 */
internal class WebTemplatePathExpressionNodeBuilder : WebTemplatePathExpressionBaseVisitor<WebTemplatePathExpressionNode>() {

    private fun unquote(literal: String): String = literal.substring(1, literal.length - 1).replace(Regex("""\\(.)""")) { it.groupValues[1] }

    override fun visitExpression(ctx: WebTemplatePathExpressionParser.ExpressionContext): WebTemplatePathExpressionNode = visit(ctx.expr())

    override fun visitExpr(ctx: WebTemplatePathExpressionParser.ExprContext): WebTemplatePathExpressionNode {
        if (ctx.atom() != null) {
            return visit(ctx.atom())
        }
        return BinaryOp(
            when {
                ctx.PLUS() != null -> PathOperator.PLUS
                ctx.MINUS() != null -> PathOperator.MINUS
                ctx.STAR() != null -> PathOperator.STAR
                ctx.PERCENT() != null -> PathOperator.MOD
                ctx.DIV() != null -> PathOperator.DIV
                ctx.GT() != null -> PathOperator.GT
                ctx.GTE() != null -> PathOperator.GTE
                ctx.LT() != null -> PathOperator.LT
                ctx.LTE() != null -> PathOperator.LTE
                ctx.EQUALS() != null -> PathOperator.EQUALS
                ctx.NEQ() != null -> PathOperator.NEQ
                ctx.AND() != null -> PathOperator.AND
                ctx.OR() != null -> PathOperator.OR
                ctx.XOR() != null -> PathOperator.XOR
                ctx.IMPLIES() != null -> PathOperator.IMPLIES
                else -> throw WebTemplatePathExpressionException("Unknown operator in expression: ${ctx.text}")
            },
            visit(ctx.expr(0)),
            visit(ctx.expr(1)))
    }

    override fun visitAtom(ctx: WebTemplatePathExpressionParser.AtomContext): WebTemplatePathExpressionNode {
        if (ctx.MINUS() != null) {
            return UnaryMinus(visit(ctx.atom()))
        }

        val functions = ctx.functionInvocation().map { visitFunctionInvocation(it) as FunctionCall }
        val primaryNode = visitPrimary(ctx.primary())

        return if (functions.isEmpty()) {
            primaryNode
        } else {
            when (primaryNode) {
                is PathExpressionExpression -> PathExpressionExpression(primaryNode.steps, primaryNode.attribute, primaryNode.functions + functions)
                else -> PathExpressionExpression(emptyList(), null, functions, primaryNode)
            }
        }
    }

    override fun visitPrimary(ctx: WebTemplatePathExpressionParser.PrimaryContext): WebTemplatePathExpressionNode {
        val node = when {
            ctx.DECIMAL() != null->  NumberLiteral(ctx.DECIMAL().text.toDouble())
            ctx.INT() != null -> NumberLiteral(ctx.INT().text.toInt())
            ctx.STRING_LITERAL() != null -> StringLiteral(unquote(ctx.STRING_LITERAL().text))
            ctx.TRUE() != null -> BooleanLiteral(true)
            ctx.FALSE() != null -> BooleanLiteral(false)
            ctx.DOLLAR_THIS() != null -> ThisReference
            ctx.DOLLAR_INDEX() != null -> IndexReference
            ctx.DOLLAR_TOTAL() != null -> TotalReference
            ctx.expr() != null -> visit(ctx.expr())
            else -> null
        }
        if (node != null) {
            return node
        }

        val pathExpr = ctx.pathExpr()
        return PathExpressionExpression(
            pathExpr.step().map { visit(it) },
            pathExpr.attributeAccess()?.let { visitAttributeAccess(it) as AttributeAccess },
            emptyList())
    }

    override fun visitStep(ctx: WebTemplatePathExpressionParser.StepContext): WebTemplatePathExpressionNode {
        return when {
            ctx.IDENTIFIER() != null && ctx.COLON() != null -> {
                val key = ctx.IDENTIFIER().text
                val index = ctx.INT().text.toInt()
                val where = ctx.whereClause()
                if (where != null) {
                    val (op, value) = extractWhereClause(where)
                    PathSegmentWithPredicate(key, index, where.IDENTIFIER().text, op, value)
                } else {
                    PathSegmentWithIndex(key, index)
                }
            }

            ctx.IDENTIFIER() != null && ctx.whereClause() != null -> {
                val key = ctx.IDENTIFIER().text
                val where = ctx.whereClause()
                val (op, value) = extractWhereClause(where)
                PathSegmentWithPredicate(key, null, where.IDENTIFIER().text, op, value)
            }

            ctx.IDENTIFIER() != null -> PathSegment(ctx.IDENTIFIER().text)

            else -> throw WebTemplatePathExpressionException("Unexpected step: ${ctx.text}")
        }
    }

    private fun extractWhereClause(where: WebTemplatePathExpressionParser.WhereClauseContext): Pair<PathOperator, Any> {
        val op = when {
            where.whereOp().EQUALS() != null -> PathOperator.EQUALS
            where.whereOp().NEQ() != null -> PathOperator.NEQ
            where.whereOp().GT() != null -> PathOperator.GT
            where.whereOp().GTE() != null -> PathOperator.GTE
            where.whereOp().LT() != null -> PathOperator.LT
            where.whereOp().LTE() != null -> PathOperator.LTE
            else -> throw WebTemplatePathExpressionException("Unknown operator in where clause: ${where.text}")
        }
        val valueCtx = where.whereValue()
        val value: Any = when {
            valueCtx.STRING_LITERAL() != null -> unquote(valueCtx.STRING_LITERAL().text)
            valueCtx.DECIMAL() != null -> valueCtx.DECIMAL().text.toDouble()
            valueCtx.INT() != null -> valueCtx.INT().text.toInt()
            valueCtx.TRUE() != null -> true
            valueCtx.FALSE() != null -> false
            else -> throw WebTemplatePathExpressionException("Unknown value in where clause: ${where.text}")
        }
        return op to value
    }

    override fun visitAttributeAccess(ctx: WebTemplatePathExpressionParser.AttributeAccessContext): WebTemplatePathExpressionNode =
        AttributeAccess(ctx.IDENTIFIER().text)

    override fun visitFunctionInvocation(ctx: WebTemplatePathExpressionParser.FunctionInvocationContext): WebTemplatePathExpressionNode {
        val nameStr = ctx.IDENTIFIER().text
        val pathFunction = try {
            PathFunction.fromFunctionName(nameStr)
        } catch (_: IllegalArgumentException) {
            throw WebTemplatePathExpressionException("Unknown function: $nameStr")
        }
        val args = ctx.functionArgs()?.functionArg()?.map { visit(it.expr()) } ?: emptyList()
        validateFunction(pathFunction, args, ctx.text)
        return FunctionCall(pathFunction, args)
    }

    private fun validateFunction(pathFunction: PathFunction, args: List<WebTemplatePathExpressionNode>, text: String) {
        val name = pathFunction.functionName
        when (pathFunction.argType) {
            ArgType.NO_ARG -> {
                if (args.isNotEmpty()) {
                    throw WebTemplatePathExpressionException("Function $name() takes no arguments: $text")
                }
            }
            ArgType.SINGLE_STRING_ARG -> {
                if (args.size != 1 || args[0] !is StringLiteral) {
                    throw WebTemplatePathExpressionException("Function $name() requires a single string argument: $text")
                }
            }
            ArgType.TWO_STRING_ARGS -> {
                if (args.size != 2 || args.any { it !is StringLiteral }) {
                    throw WebTemplatePathExpressionException("Function $name() requires two string arguments: $text")
                }
            }
            ArgType.INT_ARGS -> {
                if (args.isEmpty() || args.size > 2 || args.any { it !is NumberLiteral || it.value !is Int }) {
                    throw WebTemplatePathExpressionException("Function $name() requires one or two integer arguments: $text")
                }
            }
            ArgType.SINGLE_INT_ARG -> {
                if (args.size != 1 || args[0] !is NumberLiteral || (args[0] as NumberLiteral).value !is Int) {
                    throw WebTemplatePathExpressionException("Function $name() requires a single integer argument: $text")
                }
            }
            ArgType.OPTIONAL_INT_ARG -> {
                if (args.size > 1 || (args.size == 1 && (args[0] !is NumberLiteral || (args[0] as NumberLiteral).value !is Int))) {
                    throw WebTemplatePathExpressionException("Function $name() takes zero or one integer argument: $text")
                }
            }
            ArgType.NUMBER_ARG -> {
                if (args.size != 1 || args[0] !is NumberLiteral) {
                    throw WebTemplatePathExpressionException("Function $name() requires a single number argument: $text")
                }
            }
            ArgType.PATH_ARG -> {
                if (args.size != 1 || args[0] !is PathExpressionExpression) {
                    throw WebTemplatePathExpressionException("Function $name() requires a single path argument: $text")
                }
            }
            ArgType.EXPR_ARG -> {
                when (pathFunction) {
                    PathFunction.IIF -> {
                        if (args.size !in 2 .. 3) {
                            throw WebTemplatePathExpressionException("Function $name() requires 2 or 3 arguments: $text")
                        }
                    }
                    PathFunction.AGGREGATE -> {
                        if (args.size !in 1..2) {
                            throw WebTemplatePathExpressionException("Function $name() requires 1 or 2 arguments: $text")
                        }
                    }
                    PathFunction.REPEAT -> {
                        if (args.size != 1) {
                            throw WebTemplatePathExpressionException("Function $name() requires 1 argument: $text")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
