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

import care.better.platform.web.template.path.model.WebTemplatePathExpressionNode.*
import care.better.platform.web.template.path.model.PathFunction
import care.better.platform.web.template.path.model.PathOperator
import care.better.platform.web.template.path.exception.WebTemplatePathExpressionException
import care.better.platform.web.template.path.model.ArgType
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import care.better.platform.web.template.path.model.WebTemplatePathExpressionNode

/**
 * @author Primoz Delopst
 * @since 5.0.0
 */
class WebTemplatePathExpressionFacadeParserTest {

    private fun parse(expression: String): WebTemplatePathExpressionNode {
        val charStream = CharStreams.fromString(expression)
        val lexer = WebTemplatePathExpressionLexer(charStream)
        val tokenStream = CommonTokenStream(lexer)
        val parser = WebTemplatePathExpressionParser(tokenStream)

        val errors = mutableListOf<String>()
        val errorListener = object : BaseErrorListener() {
            override fun syntaxError(
                recognizer: Recognizer<*, *>?,
                offendingSymbol: Any?,
                line: Int,
                charPositionInLine: Int,
                msg: String?,
                e: RecognitionException?) {
                errors.add("at position $charPositionInLine: ${msg ?: "unknown error"}")
            }
        }
        lexer.removeErrorListeners()
        lexer.addErrorListener(errorListener)
        parser.removeErrorListeners()
        parser.addErrorListener(errorListener)

        val tree = parser.expression()
        if (errors.isNotEmpty()) {
            throw WebTemplatePathExpressionException("Parse errors: ${errors.joinToString("; ")} in expression: $expression")
        }
        return WebTemplatePathExpressionNodeBuilder().visit(tree)
    }

    private fun assertPathExpression(node: WebTemplatePathExpressionNode): PathExpressionExpression {
        assertThat(node).isInstanceOf(PathExpressionExpression::class.java)
        return node as PathExpressionExpression
    }

    @Test
    fun testSimpleNavigation() {
        val ast = assertPathExpression(parse("blood_pressure/any_event/systolic"))
        assertThat(ast.steps).hasSize(3)
        assertThat(ast.steps[0]).isEqualTo(PathSegment("blood_pressure"))
        assertThat(ast.steps[1]).isEqualTo(PathSegment("any_event"))
        assertThat(ast.steps[2]).isEqualTo(PathSegment("systolic"))
        assertThat(ast.attribute).isNull()
        assertThat(ast.functions).isEmpty()
    }

    @Test
    fun testNavigationWithIndex() {
        val ast = assertPathExpression(parse("blood_pressure:0/any_event/systolic"))
        assertThat(ast.steps).hasSize(3)
        assertThat(ast.steps[0]).isEqualTo(PathSegmentWithIndex("blood_pressure", 0))
    }

    @Test
    fun testAttributeAccess() {
        val ast = assertPathExpression(parse("blood_pressure/any_event/systolic|magnitude"))
        assertThat(ast.steps).hasSize(3)
        assertThat(ast.attribute).isEqualTo(AttributeAccess("magnitude"))
    }

    @Test
    fun testWhereFilter() {
        val ast = assertPathExpression(parse("blood_pressure/any_event.where(|code = 'at0004')/systolic"))
        assertThat(ast.steps).hasSize(3)
        assertThat(ast.steps[1]).isEqualTo(PathSegmentWithPredicate("any_event", null, "code", PathOperator.EQUALS, "at0004"))
    }

    @Test
    fun testWhereFilterWithIndex() {
        val ast = assertPathExpression(parse("blood_pressure:0.where(|code = 'at0004')/systolic"))
        assertThat(ast.steps[0]).isEqualTo(PathSegmentWithPredicate("blood_pressure", 0, "code", PathOperator.EQUALS, "at0004"))
    }

    @Test
    fun testFunctionCall() {
        val ast = assertPathExpression(parse("blood_pressure/any_event.count()"))
        assertThat(ast.steps).hasSize(2)
        assertThat(ast.functions).hasSize(1)
        assertThat(ast.functions[0]).isEqualTo(FunctionCall(PathFunction.COUNT))
    }

    @Test
    fun testAllNoArgFunctions() {
        for (fn in PathFunction.entries.filter { it.argType == ArgType.NO_ARG }) {
            val ast = assertPathExpression(parse("blood_pressure.${fn.functionName}()"))
            assertThat(ast.functions).hasSize(1)
            assertThat(ast.functions[0]).isEqualTo(FunctionCall(fn))
        }
    }

    @Test
    fun testFunctionChaining() {
        val ast = assertPathExpression(parse("blood_pressure/any_event.first().count()"))
        assertThat(ast.functions).hasSize(2)
        assertThat(ast.functions[0]).isEqualTo(FunctionCall(PathFunction.FIRST))
        assertThat(ast.functions[1]).isEqualTo(FunctionCall(PathFunction.COUNT))
    }

    @Test
    fun testSelectFunction() {
        val ast = assertPathExpression(parse("blood_pressure/any_event.select(systolic|magnitude)"))
        assertThat(ast.functions).hasSize(1)
        val fn = ast.functions[0]
        assertThat(fn.name).isEqualTo(PathFunction.SELECT)
        assertThat(fn.args).hasSize(1)
        val pathArg = fn.args[0] as PathExpressionExpression
        assertThat(pathArg.steps).hasSize(1)
        assertThat(pathArg.steps[0]).isEqualTo(PathSegment("systolic"))
        assertThat(pathArg.attribute).isEqualTo(AttributeAccess("magnitude"))
    }

    @Test
    fun testUnionFunction() {
        val ast = assertPathExpression(parse("blood_pressure/systolic.union(blood_pressure/diastolic)"))
        assertThat(ast.functions).hasSize(1)
        val fn = ast.functions[0]
        assertThat(fn.name).isEqualTo(PathFunction.UNION)
        val pathArg = fn.args[0] as PathExpressionExpression
        assertThat(pathArg.steps).hasSize(2)
        assertThat(pathArg.steps[0]).isEqualTo(PathSegment("blood_pressure"))
        assertThat(pathArg.steps[1]).isEqualTo(PathSegment("diastolic"))
    }

    @Test
    fun testMatchesFunction() {
        val ast = assertPathExpression(parse("blood_pressure/systolic|value.matches('\\\\d+')"))
        assertThat(ast.functions).hasSize(1)
        val fn = ast.functions[0]
        assertThat(fn.name).isEqualTo(PathFunction.MATCHES)
        assertThat(fn.args[0]).isEqualTo(StringLiteral("\\d+"))
    }

    @Test
    fun testReplaceMatchesFunction() {
        val ast = assertPathExpression(parse("blood_pressure/systolic|value.replaceMatches('old', 'new')"))
        assertThat(ast.functions).hasSize(1)
        val fn = ast.functions[0]
        assertThat(fn.name).isEqualTo(PathFunction.REPLACE_MATCHES)
        assertThat(fn.args[0]).isEqualTo(StringLiteral("old"))
        assertThat(fn.args[1]).isEqualTo(StringLiteral("new"))
    }

    @Test
    fun testOfTypeFunction() {
        val ast = assertPathExpression(parse("blood_pressure/any_event.ofType('number')"))
        assertThat(ast.functions[0].name).isEqualTo(PathFunction.OF_TYPE)
        assertThat(ast.functions[0].args[0]).isEqualTo(StringLiteral("number"))
    }

    @Test
    fun testSubstringFunction() {
        val ast = assertPathExpression(parse("blood_pressure/systolic|value.substring(0, 5)"))
        assertThat(ast.functions[0].name).isEqualTo(PathFunction.SUBSTRING)
        assertThat(ast.functions[0].args[0]).isEqualTo(NumberLiteral(0))
        assertThat(ast.functions[0].args[1]).isEqualTo(NumberLiteral(5))
    }

    @Test
    fun testSubstringWithSingleArg() {
        val ast = assertPathExpression(parse("blood_pressure/systolic|value.substring(3)"))
        assertThat(ast.functions[0].args).hasSize(1)
        assertThat(ast.functions[0].args[0]).isEqualTo(NumberLiteral(3))
    }

    @Test
    fun testMathAddition() {
        val node = parse("blood_pressure/systolic|magnitude + 10")
        assertThat(node).isInstanceOf(BinaryOp::class.java)
        val op = node as BinaryOp
        assertThat(op.operator).isEqualTo(PathOperator.PLUS)
        assertThat(op.left).isInstanceOf(PathExpressionExpression::class.java)
        assertThat(op.right).isEqualTo(NumberLiteral(10))
    }

    @Test
    fun testMathMultiplication() {
        val node = parse("blood_pressure/systolic|magnitude * 2")
        assertThat(node).isInstanceOf(BinaryOp::class.java)
        val op = node as BinaryOp
        assertThat(op.operator).isEqualTo(PathOperator.STAR)
    }

    @Test
    fun testMathPrecedence() {
        val node = parse("2 + 3 * 4")
        assertThat(node).isInstanceOf(BinaryOp::class.java)
        val op = node as BinaryOp
        assertThat(op.operator).isEqualTo(PathOperator.PLUS)
        assertThat(op.left).isEqualTo(NumberLiteral(2))
        assertThat(op.right).isInstanceOf(BinaryOp::class.java)
        val right = op.right as BinaryOp
        assertThat(right.operator).isEqualTo(PathOperator.STAR)
        assertThat(right.left).isEqualTo(NumberLiteral(3))
        assertThat(right.right).isEqualTo(NumberLiteral(4))
    }

    @Test
    fun testMathWithDecimal() {
        val node = parse("blood_pressure/systolic|magnitude + 1.5")
        val op = node as BinaryOp
        assertThat(op.right).isEqualTo(NumberLiteral(1.5))
    }

    @Test
    fun testUnaryMinus() {
        val node = parse("-5")
        assertThat(node).isInstanceOf(UnaryMinus::class.java)
        assertThat((node as UnaryMinus).operand).isEqualTo(NumberLiteral(5))
    }

    @Test
    fun testMathDivision() {
        val node = parse("10 div 2")
        assertThat(node).isInstanceOf(BinaryOp::class.java)
        val op = node as BinaryOp
        assertThat(op.operator).isEqualTo(PathOperator.DIV)
        assertThat(op.left).isEqualTo(NumberLiteral(10))
        assertThat(op.right).isEqualTo(NumberLiteral(2))
    }

    @Test
    fun testMathModulo() {
        val node = parse("10 % 3")
        val op = node as BinaryOp
        assertThat(op.operator).isEqualTo(PathOperator.MOD)
    }

    @Test
    fun testMathBetweenPathExpressions() {
        val node = parse("blood_pressure/systolic|magnitude + blood_pressure/diastolic|magnitude")
        assertThat(node).isInstanceOf(BinaryOp::class.java)
        val op = node as BinaryOp
        assertThat(op.operator).isEqualTo(PathOperator.PLUS)
        assertThat(op.left).isInstanceOf(PathExpressionExpression::class.java)
        assertThat(op.right).isInstanceOf(PathExpressionExpression::class.java)
    }

    @Test
    fun testSingleKey() {
        val ast = assertPathExpression(parse("blood_pressure"))
        assertThat(ast.steps).hasSize(1)
        assertThat(ast.steps[0]).isEqualTo(PathSegment("blood_pressure"))
    }

    @Test
    fun testInvalidExpression() {
        assertThatThrownBy { parse("[invalid") }
            .isInstanceOf(WebTemplatePathExpressionException::class.java)
    }

    @Test
    fun testWildcardNotSupported() {
        assertThatThrownBy { parse("blood_pressure/*/systolic") }
            .isInstanceOf(WebTemplatePathExpressionException::class.java)
    }

    @Test
    fun testEscapedStringInWhere() {
        val ast = assertPathExpression(parse("any_event.where(|value = 'it\\'s')"))
        assertThat(ast.steps[0]).isEqualTo(PathSegmentWithPredicate("any_event", null, "value", PathOperator.EQUALS, "it's"))
    }

    @Test
    fun testUnknownFunctionThrows() {
        assertThatThrownBy { parse("blood_pressure.unknownFunc()") }
            .isInstanceOf(WebTemplatePathExpressionException::class.java)
            .hasMessageContaining("Unknown function")
    }

    @Test
    fun testStringLiteral() {
        val node = parse("'hello'")
        assertThat(node).isInstanceOf(StringLiteral::class.java)
        assertThat((node as StringLiteral).value).isEqualTo("hello")
    }

    @Test
    fun testNumberLiteral() {
        val node = parse("42")
        assertThat(node).isInstanceOf(NumberLiteral::class.java)
        assertThat((node as NumberLiteral).value).isEqualTo(42)
    }


    @Test
    fun testComparisonOperators() {
        for ((expected, symbol) in listOf(
            PathOperator.GT to ">",
            PathOperator.GTE to ">=",
            PathOperator.LT to "<",
            PathOperator.LTE to "<="
        )) {
            val node = parse("5 $symbol 3")
            assertThat(node).isInstanceOf(BinaryOp::class.java)
            val binOp = node as BinaryOp
            assertThat(binOp.operator).isEqualTo(expected)
            assertThat(binOp.left).isEqualTo(NumberLiteral(5))
            assertThat(binOp.right).isEqualTo(NumberLiteral(3))
        }
    }

    @Test
    fun testEqualityOperators() {
        val eq = parse("5 = 5") as BinaryOp
        assertThat(eq.operator).isEqualTo(PathOperator.EQUALS)

        val neq = parse("5 != 3") as BinaryOp
        assertThat(neq.operator).isEqualTo(PathOperator.NEQ)
    }

    @Test
    fun testBooleanOperators() {
        val andOp = parse("true and false") as BinaryOp
        assertThat(andOp.operator).isEqualTo(PathOperator.AND)
        assertThat(andOp.left).isEqualTo(BooleanLiteral(true))
        assertThat(andOp.right).isEqualTo(BooleanLiteral(false))

        val orOp = parse("true or false") as BinaryOp
        assertThat(orOp.operator).isEqualTo(PathOperator.OR)

        val xorOp = parse("true xor false") as BinaryOp
        assertThat(xorOp.operator).isEqualTo(PathOperator.XOR)

        val impliesOp = parse("true implies false") as BinaryOp
        assertThat(impliesOp.operator).isEqualTo(PathOperator.IMPLIES)
    }

    @Test
    fun testBooleanLiterals() {
        assertThat(parse("true")).isEqualTo(BooleanLiteral(true))
        assertThat(parse("false")).isEqualTo(BooleanLiteral(false))
    }

    @Test
    fun testDollarThis() {
        assertThat(parse("\$this")).isEqualTo(ThisReference)
    }

    @Test
    fun testDollarIndex() {
        assertThat(parse("\$index")).isEqualTo(IndexReference)
    }

    @Test
    fun testDollarTotal() {
        assertThat(parse("\$total")).isEqualTo(TotalReference)
    }

    @Test
    fun testOperatorPrecedence() {
        val node = parse("1 + 2 > 3 = true") as BinaryOp
        assertThat(node.operator).isEqualTo(PathOperator.EQUALS)
        val leftComp = node.left as BinaryOp
        assertThat(leftComp.operator).isEqualTo(PathOperator.GT)

        val node2 = parse("true or false and true") as BinaryOp
        assertThat(node2.operator).isEqualTo(PathOperator.OR)
        val rightAnd = node2.right as BinaryOp
        assertThat(rightAnd.operator).isEqualTo(PathOperator.AND)
    }

    @Test
    fun testExpressionBasedFunctionArgs() {
        val ast = assertPathExpression(parse("blood_pressure.iif(5 > 3, 'high', 'low')"))
        val fn = ast.functions[0]
        assertThat(fn.name).isEqualTo(PathFunction.IIF)
        assertThat(fn.args).hasSize(3)
        assertThat(fn.args[0]).isInstanceOf(BinaryOp::class.java)
        assertThat(fn.args[1]).isEqualTo(StringLiteral("high"))
        assertThat(fn.args[2]).isEqualTo(StringLiteral("low"))
    }

    @Test
    fun testNewStringArgFunctions() {
        val expectedFunctions = mapOf(
            "indexOf" to PathFunction.INDEX_OF,
            "startsWith" to PathFunction.STARTS_WITH,
            "endsWith" to PathFunction.ENDS_WITH,
            "contains" to PathFunction.CONTAINS)

        for ((name, expected) in expectedFunctions) {
            val ast = assertPathExpression(parse("blood_pressure|value.$name('test')"))
            assertThat(ast.functions[0].name).isEqualTo(expected)
            assertThat(ast.functions[0].args[0]).isEqualTo(StringLiteral("test"))
        }
    }

    @Test
    fun testReplaceFunction() {
        val ast = assertPathExpression(parse("blood_pressure|value.replace('old', 'new')"))
        assertThat(ast.functions[0].name).isEqualTo(PathFunction.REPLACE)
        assertThat(ast.functions[0].args[0]).isEqualTo(StringLiteral("old"))
        assertThat(ast.functions[0].args[1]).isEqualTo(StringLiteral("new"))
    }

    @Test
    fun testSkipTakeFunctions() {
        val skipAst = assertPathExpression(parse("blood_pressure.skip(2)"))
        assertThat(skipAst.functions[0].name).isEqualTo(PathFunction.SKIP)
        assertThat(skipAst.functions[0].args[0]).isEqualTo(NumberLiteral(2))

        val takeAst = assertPathExpression(parse("blood_pressure.take(3)"))
        assertThat(takeAst.functions[0].name).isEqualTo(PathFunction.TAKE)
        assertThat(takeAst.functions[0].args[0]).isEqualTo(NumberLiteral(3))
    }

    @Test
    fun testRoundFunction() {
        val ast1 = assertPathExpression(parse("blood_pressure|magnitude.round()"))
        assertThat(ast1.functions[0]).isEqualTo(FunctionCall(PathFunction.ROUND))

        val ast2 = assertPathExpression(parse("blood_pressure|magnitude.round(2)"))
        assertThat(ast2.functions[0].name).isEqualTo(PathFunction.ROUND)
        assertThat(ast2.functions[0].args[0]).isEqualTo(NumberLiteral(2))
    }

    @Test
    fun testPowerAndLogFunctions() {
        val powerAst = assertPathExpression(parse("blood_pressure|magnitude.power(2)"))
        assertThat(powerAst.functions[0].name).isEqualTo(PathFunction.POWER)
        assertThat(powerAst.functions[0].args[0]).isEqualTo(NumberLiteral(2))

        val logAst = assertPathExpression(parse("blood_pressure|magnitude.log(10)"))
        assertThat(logAst.functions[0].name).isEqualTo(PathFunction.LOG)
        assertThat(logAst.functions[0].args[0]).isEqualTo(NumberLiteral(10))
    }

    @Test
    fun testPathArgFunctions() {
        val expectedFunctions = mapOf(
            "combine" to PathFunction.COMBINE,
            "intersect" to PathFunction.INTERSECT,
            "exclude" to PathFunction.EXCLUDE,
            "subsetOf" to PathFunction.SUBSET_OF,
            "supersetOf" to PathFunction.SUPERSET_OF
        )
        for ((name, expected) in expectedFunctions) {
            val ast = assertPathExpression(parse("blood_pressure/systolic.$name(blood_pressure/diastolic)"))
            assertThat(ast.functions[0].name).isEqualTo(expected)
            assertThat(ast.functions[0].args[0]).isInstanceOf(PathExpressionExpression::class.java)
        }
    }

    @Test
    fun testAggregateFunction() {
        val ast = assertPathExpression(parse("blood_pressure|magnitude.aggregate(\$total + \$this, 0)"))
        val fn = ast.functions[0]
        assertThat(fn.name).isEqualTo(PathFunction.AGGREGATE)
        assertThat(fn.args).hasSize(2)
        assertThat(fn.args[0]).isInstanceOf(BinaryOp::class.java)
        assertThat(fn.args[1]).isEqualTo(NumberLiteral(0))
    }

    @Test
    fun testRepeatFunction() {
        val ast = assertPathExpression(parse("blood_pressure.repeat(\$this)"))
        val fn = ast.functions[0]
        assertThat(fn.name).isEqualTo(PathFunction.REPEAT)
        assertThat(fn.args).hasSize(1)
    }

    @Test
    fun testWhereFilterOperators() {
        val operators = listOf(
            "!=" to PathOperator.NEQ,
            ">" to PathOperator.GT,
            ">=" to PathOperator.GTE,
            "<" to PathOperator.LT,
            "<=" to PathOperator.LTE
        )
        for ((symbol, expected) in operators) {
            val ast = assertPathExpression(parse("items.where(|code $symbol 'at0004')"))
            assertThat(ast.steps[0]).isEqualTo(PathSegmentWithPredicate("items", null, "code", expected, "at0004"))
        }
    }

    @Test
    fun testWhereFilterWithNumericValue() {
        val ast = assertPathExpression(parse("items.where(|count > 5)"))
        assertThat(ast.steps[0]).isEqualTo(PathSegmentWithPredicate("items", null, "count", PathOperator.GT, 5))
    }

    @Test
    fun testWhereFilterWithDecimalValue() {
        val ast = assertPathExpression(parse("items.where(|score >= 3.5)"))
        assertThat(ast.steps[0]).isEqualTo(PathSegmentWithPredicate("items", null, "score", PathOperator.GTE, 3.5))
    }

    @Test
    fun testWhereFilterWithBooleanValue() {
        val astTrue = assertPathExpression(parse("items.where(|active = true)"))
        assertThat(astTrue.steps[0]).isEqualTo(PathSegmentWithPredicate("items", null, "active", PathOperator.EQUALS, true))

        val astFalse = assertPathExpression(parse("items.where(|active != false)"))
        assertThat(astFalse.steps[0]).isEqualTo(PathSegmentWithPredicate("items", null, "active", PathOperator.NEQ, false))
    }

    @Test
    fun testStringConcatenation() {
        val node = parse("'hello' + ' ' + 'world'") as BinaryOp
        assertThat(node.operator).isEqualTo(PathOperator.PLUS)
    }
}
