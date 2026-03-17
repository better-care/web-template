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

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.path.facade.WebTemplatePathExpressionFacade
import care.better.platform.web.template.path.exception.WebTemplatePathExpressionException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 * @since 5.0.0
 */
class WebTemplatePathEvaluatorTest : AbstractWebTemplateTest() {

    private val objectMapper = ObjectMapper().apply { enable(JsonParser.Feature.ALLOW_COMMENTS) }

    private fun loadStructuredComposition(resourcePath: String): ObjectNode {
        val stream = WebTemplatePathEvaluatorTest::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Resource not found: $resourcePath")
        return stream.use { objectMapper.readTree(it) as ObjectNode }
    }

    private val tannerStages: ObjectNode by lazy {
        loadStructuredComposition("/compatibility/compositions/structured/Tanner stages(1).json")
    }

    private val vitalSigns: ObjectNode by lazy {
        loadStructuredComposition("/compatibility/compositions/structured/Vital Signs Pathfinder Demo(1).json")
    }

    private val tannerStagesTemplate: WebTemplate by lazy { getWebTemplate("/compatibility/templates/Tanner stages.xml") }

    private val vitalSignsTemplate by lazy { getWebTemplate("/compatibility/templates/Vital Signs Pathfinder Demo.xml") }

    private fun evalStructured(expression: String, composition: ObjectNode = vitalSigns, template: WebTemplate = vitalSignsTemplate): List<TrackedNode> =
        WebTemplatePathExpressionFacade.evaluate(expression, composition, template.tree)

    private fun structuredNodes(result: List<TrackedNode>): List<JsonNode> = result.map { it.json }

    private fun scalarValue(result: List<TrackedNode>): Any? {
        assertThat(result).hasSize(1)
        val node = result[0].json
        return when {
            node.isBoolean -> node.booleanValue()
            node.isInt || node.isLong -> node.longValue()
            node.isNumber -> node.doubleValue()
            node.isTextual -> node.textValue()
            node.isNull -> null
            else -> node
        }
    }

    @Test
    fun testNavigateByKey() {
        val nodes = structuredNodes(evalStructured("blood_pressure"))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].isObject).isTrue()
    }

    @Test
    fun testNavigateMultipleLevels() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event"))
        assertThat(nodes).isNotEmpty
    }

    @Test
    fun testNavigateToAttribute() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event/systolic|magnitude"))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].doubleValue()).isEqualTo(12.0)
    }

    @Test
    fun testNavigateByKeyWithIndex() {
        val nodes = structuredNodes(evalStructured("blood_pressure:0/any_event:0/systolic|magnitude"))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].doubleValue()).isEqualTo(12.0)
    }

    @Test
    fun testCountFunction() {
        assertThat((scalarValue(evalStructured("blood_pressure/any_event.count()")) as Long).toInt()).isGreaterThan(0)
    }

    @Test
    fun testExistsFunction() {
        assertThat(scalarValue(evalStructured("blood_pressure.exists()"))).isEqualTo(true)
    }

    @Test
    fun testExistsFunctionNonexistent() {
        assertThat(scalarValue(evalStructured("blood_pressure:99.exists()"))).isEqualTo(false)
    }

    @Test
    fun testEmptyFunction() {
        assertThat(scalarValue(evalStructured("blood_pressure:99.empty()"))).isEqualTo(true)
    }

    @Test
    fun testWhereFilter() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals.where(|code = 'at0010')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testWhereFilterNoMatch() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals.where(|code = 'nonexistent')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).isEmpty()
    }

    @Test
    fun testWhereFilterNotEquals() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals.where(|code != 'nonexistent')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testWhereFilterNotEqualsNoMatch() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals.where(|code != 'at0010')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).isEmpty()
    }

    @Test
    fun testFirstFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event.first()"))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testLastFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event.last()"))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testFirstOnEmptyResult() {
        val nodes = structuredNodes(evalStructured("blood_pressure:99.first()"))
        assertThat(nodes).isEmpty()
    }

    @Test
    fun testTannerStagesNavigation() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].asText()).isEqualTo("Stage 4")
    }

    @Test
    fun testSelectAttribute() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event.select(systolic|magnitude)"))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].doubleValue()).isEqualTo(12.0)
    }

    @Test
    fun testSelectMultipleSteps() {
        val nodes = structuredNodes(evalStructured("blood_pressure.select(any_event/systolic|magnitude)"))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].doubleValue()).isEqualTo(12.0)
    }

    @Test
    fun testSelectWithWhere() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event.select(genitals.where(|code = 'at0010'))", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testSortFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event/systolic|magnitude.sort()"))
        assertThat(nodes).isNotEmpty
    }

    @Test
    fun testDistinctFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event/systolic|magnitude.distinct()"))
        assertThat(nodes).isNotEmpty
    }

    @Test
    fun testLowerFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.lower()", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].asText()).isEqualTo("stage 4")
    }

    @Test
    fun testUpperFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.upper()", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].asText()).isEqualTo("STAGE 4")
    }

    @Test
    fun testSumFunction() {
        val result = scalarValue(evalStructured("blood_pressure/any_event/systolic|magnitude.sum()"))
        assertThat(result).isInstanceOf(Number::class.java)
    }

    @Test
    fun testMinFunction() {
        val result = scalarValue(evalStructured("blood_pressure/any_event/systolic|magnitude.min()"))
        assertThat(result).isInstanceOf(Number::class.java)
    }

    @Test
    fun testMaxFunction() {
        val result = scalarValue(evalStructured("blood_pressure/any_event/systolic|magnitude.max()"))
        assertThat(result).isInstanceOf(Number::class.java)
    }

    @Test
    fun testAvgFunction() {
        val result = scalarValue(evalStructured("blood_pressure/any_event/systolic|magnitude.avg()"))
        assertThat(result).isInstanceOf(Number::class.java)
    }

    @Test
    fun testAllFunction() {
        assertThat(scalarValue(evalStructured("blood_pressure/any_event/systolic|magnitude.all()"))).isEqualTo(true)
    }

    @Test
    fun testAnyFunction() {
        assertThat(scalarValue(evalStructured("blood_pressure/any_event/systolic|magnitude.any()"))).isEqualTo(true)
    }

    @Test
    fun testNoneFunction() {
        assertThat(scalarValue(evalStructured("blood_pressure:99.none()"))).isEqualTo(true)
    }

    @Test
    fun testSubstringFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.substring(0, 5)", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].asText()).isEqualTo("Stage")
    }

    @Test
    fun testMatchesFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.matches('Stage.*')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].booleanValue()).isTrue()
    }

    @Test
    fun testReplaceMatchesFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.replaceMatches('Stage', 'Level')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].asText()).isEqualTo("Level 4")
    }

    @Test
    fun testOfTypeFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event/systolic|magnitude.ofType('number')"))
        assertThat(nodes).isNotEmpty
    }

    @Test
    fun testMathAddition() {
        val result = scalarValue(evalStructured("blood_pressure:0/any_event:0/systolic|magnitude + 10"))
        assertThat((result as Number).toDouble()).isEqualTo(22.0)
    }

    @Test
    fun testMathSubtraction() {
        val result = scalarValue(evalStructured("blood_pressure:0/any_event:0/systolic|magnitude - 2"))
        assertThat((result as Number).toDouble()).isEqualTo(10.0)
    }

    @Test
    fun testMathMultiplication() {
        val result = scalarValue(evalStructured("blood_pressure:0/any_event:0/systolic|magnitude * 2"))
        assertThat((result as Number).toDouble()).isEqualTo(24.0)
    }

    @Test
    fun testMathDivision() {
        val result = scalarValue(evalStructured("10 div 2"))
        assertThat((result as Number).toDouble()).isEqualTo(5.0)
    }

    @Test
    fun testMathPrecedence() {
        val result = scalarValue(evalStructured("2 + 3 * 4"))
        assertThat((result as Number).toLong()).isEqualTo(14L)
    }

    @Test
    fun testFunctionChaining() {
        val result = scalarValue(evalStructured("blood_pressure/any_event.first().count()"))
        assertThat(result).isInstanceOf(Number::class.java)
    }

    @Test
    fun testUnionFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event/systolic.union(blood_pressure/any_event/diastolic)"))
        assertThat(nodes).isNotEmpty
    }

    @Test
    fun testComparisonOperators() {
        assertThat(scalarValue(evalStructured("5 > 3"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("3 > 5"))).isEqualTo(false)
        assertThat(scalarValue(evalStructured("5 >= 5"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("3 < 5"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("5 <= 5"))).isEqualTo(true)
    }

    @Test
    fun testEqualityOperators() {
        assertThat(scalarValue(evalStructured("5 = 5"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("5 != 3"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("5 != 5"))).isEqualTo(false)
    }

    @Test
    fun testBooleanOperators() {
        assertThat(scalarValue(evalStructured("true and true"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("true and false"))).isEqualTo(false)
        assertThat(scalarValue(evalStructured("true or false"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("true xor false"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("true xor true"))).isEqualTo(false)
        assertThat(scalarValue(evalStructured("true implies true"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("true implies false"))).isEqualTo(false)
        assertThat(scalarValue(evalStructured("false implies false"))).isEqualTo(true)
    }

    @Test
    fun testStringConcatenation() {
        assertThat(scalarValue(evalStructured("'hello' + ' ' + 'world'"))).isEqualTo("hello world")
    }


    @Test
    fun testTailFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event.tail()"))
        assertThat(nodes.size).isLessThanOrEqualTo(
            structuredNodes(evalStructured("blood_pressure/any_event")).size
        )
    }

    @Test
    fun testSingleFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure:0/any_event:0/systolic|magnitude.single()"))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testSkipFunction() {
        val all = structuredNodes(evalStructured("blood_pressure/any_event/systolic|magnitude"))
        val skipped = structuredNodes(evalStructured("blood_pressure/any_event/systolic|magnitude.skip(1)"))
        assertThat(skipped.size).isEqualTo((all.size - 1).coerceAtLeast(0))
    }

    @Test
    fun testTakeFunction() {
        val taken = structuredNodes(evalStructured("blood_pressure/any_event/systolic|magnitude.take(1)"))
        assertThat(taken).hasSize(1)
    }

    @Test
    fun testIsDistinctFunction() {
        assertThat(scalarValue(evalStructured("blood_pressure:0/any_event:0/systolic|magnitude.isDistinct()"))).isEqualTo(true)
    }

    @Test
    fun testIndexOfFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.indexOf('age')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].intValue()).isEqualTo(2)
    }

    @Test
    fun testStartsWithFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.startsWith('Stage')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].booleanValue()).isTrue()
    }

    @Test
    fun testEndsWithFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.endsWith('4')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].booleanValue()).isTrue()
    }

    @Test
    fun testContainsFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.contains('age')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].booleanValue()).isTrue()
    }

    @Test
    fun testReplaceFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.replace('Stage', 'Level')", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].asText()).isEqualTo("Level 4")
    }

    @Test
    fun testLengthFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.length()", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(1)
        assertThat(nodes[0].intValue()).isEqualTo(7)
    }

    @Test
    fun testToCharsFunction() {
        val nodes = structuredNodes(evalStructured("tanner_stages/any_point_in_time_event/genitals|value.toChars()", tannerStages, tannerStagesTemplate))
        assertThat(nodes).hasSize(7)
        assertThat(nodes[0].asText()).isEqualTo("S")
    }

    @Test
    fun testNotFunction() {
        assertThat(scalarValue(evalStructured("true.not()"))).isEqualTo(false)
        assertThat(scalarValue(evalStructured("false.not()"))).isEqualTo(true)
    }

    @Test
    fun testMathFunctions() {
        assertThat(scalarValue(evalStructured("(-5).abs()"))).isEqualTo(5L)
        assertThat(scalarValue(evalStructured("1.5.ceiling()"))).isEqualTo(2L)
        assertThat(scalarValue(evalStructured("1.5.floor()"))).isEqualTo(1L)
        assertThat(scalarValue(evalStructured("1.9.truncate()"))).isEqualTo(1L)
        assertThat(scalarValue(evalStructured("1.5.round()"))).isEqualTo(2L)
        assertThat(scalarValue(evalStructured("9.sqrt()"))).isEqualTo(3L)
        assertThat(scalarValue(evalStructured("2.power(3)"))).isEqualTo(8L)
        assertThat((scalarValue(evalStructured("1.ln()")) as Number).toDouble()).isEqualTo(0.0)
        assertThat((scalarValue(evalStructured("100.log(10)")) as Number).toDouble()).isEqualTo(2.0)
        assertThat((scalarValue(evalStructured("0.exp()")) as Number).toDouble()).isEqualTo(1.0)
    }

    @Test
    fun testTypeConversionFunctions() {
        assertThat((scalarValue(evalStructured("1.5.toInteger()")) as Number).toInt()).isEqualTo(1)
        assertThat(scalarValue(evalStructured("1.toDecimal()"))).isEqualTo(1.0)
        assertThat(scalarValue(evalStructured("'true'.toBoolean()"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("42.toString()"))).isEqualTo("42")
    }

    @Test
    fun testBooleanAggregateFunctions() {
        assertThat(scalarValue(evalStructured("true.allTrue()"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("true.anyTrue()"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("false.allFalse()"))).isEqualTo(true)
        assertThat(scalarValue(evalStructured("false.anyFalse()"))).isEqualTo(true)
    }

    @Test
    fun testDateTimeFunctions() {
        val now = scalarValue(evalStructured("blood_pressure.now()"))
        assertThat(now).isInstanceOf(String::class.java)
        assertThat(now as String).matches("\\d{4}-\\d{2}-\\d{2}T.*")

        val today = scalarValue(evalStructured("blood_pressure.today()"))
        assertThat(today).isInstanceOf(String::class.java)
        assertThat(today as String).matches("\\d{4}-\\d{2}-\\d{2}")

        val time = scalarValue(evalStructured("blood_pressure.timeOfDay()"))
        assertThat(time).isInstanceOf(String::class.java)
    }

    @Test
    fun testChildrenFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event.children()"))
        assertThat(nodes).isNotEmpty
    }

    @Test
    fun testDescendantsFunction() {
        val nodes = structuredNodes(evalStructured("blood_pressure/any_event.descendants()"))
        assertThat(nodes).isNotEmpty
    }

    @Test
    fun testIifFunction() {
        assertThat(scalarValue(evalStructured("blood_pressure.iif(true, 'yes', 'no')"))).isEqualTo("yes")
        assertThat(scalarValue(evalStructured("blood_pressure.iif(false, 'yes', 'no')"))).isEqualTo("no")
    }

    @Test
    fun testIifWithThisReference() {
        assertThat(scalarValue(evalStructured(
            "blood_pressure:0/any_event:0/systolic|magnitude.iif(\$this >= 0, \$this, 0)"))).isEqualTo(12L)
        assertThat(scalarValue(evalStructured(
            "blood_pressure:0/any_event:0/systolic|magnitude.iif(\$this > 100, \$this, 0)"))).isEqualTo(0L)
    }

    @Test
    fun testAggregateFunction() {
        assertThat(scalarValue(evalStructured(
            "blood_pressure:0/any_event:0/systolic|magnitude.aggregate(\$total + \$this, 0)"))).isEqualTo(12L)
    }

    @Test
    fun testComparisonWithPathValues() {
        assertThat(scalarValue(evalStructured(
            "blood_pressure:0/any_event:0/systolic|magnitude > 10"
        ))).isEqualTo(true)
    }


    private fun evalWithTemplate(expression: String, composition: ObjectNode = vitalSigns): List<TrackedNode> =
        WebTemplatePathExpressionFacade.evaluate(expression, composition, vitalSignsTemplate.tree)

    @Test
    fun testInvalidPathWithWebTemplateThrows() {
        assertThatThrownBy { evalWithTemplate("blood_pressure/invalid_key") }
            .isInstanceOf(WebTemplatePathExpressionException::class.java)
            .hasMessageContaining("Invalid path segment 'invalid_key'")
            .hasMessageContaining("blood_pressure")
    }

    @Test
    fun testOfTypeWithRmType() {
        val nodes = structuredNodes(evalWithTemplate("blood_pressure.ofType('OBSERVATION')"))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testOfTypeWithRmTypeCaseInsensitive() {
        val nodes = structuredNodes(evalWithTemplate("blood_pressure.ofType('observation')"))
        assertThat(nodes).hasSize(1)
    }

    @Test
    fun testOfTypeWithRmTypeNoMatch() {
        val nodes = structuredNodes(evalWithTemplate("blood_pressure.ofType('ACTION')"))
        assertThat(nodes).isEmpty()
    }

    @Test
    fun testOfTypeJsonTypesStillWork() {
        val nodes = structuredNodes(evalWithTemplate("blood_pressure/any_event/systolic|magnitude.ofType('number')"))
        assertThat(nodes).isNotEmpty
    }
}
