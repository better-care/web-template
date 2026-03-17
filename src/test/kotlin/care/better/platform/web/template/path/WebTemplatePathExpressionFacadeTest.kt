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

package care.better.platform.web.template.path

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.path.exception.WebTemplatePathExpressionException
import care.better.platform.web.template.path.format.RmObjectFormat
import care.better.platform.web.template.path.model.EvaluationContext
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition

/**
 * @author Primoz Delopst
 * @since 5.0.0
 */
class WebTemplatePathExpressionFacadeTest : AbstractWebTemplateTest() {

    private val jsonMapper = ObjectMapper().apply { enable(JsonParser.Feature.ALLOW_COMMENTS) }

    private fun loadFlatComposition(resourcePath: String): Map<String, Any?> {
        val stream = WebTemplatePathExpressionFacadeTest::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Resource not found: $resourcePath")
        return stream.use { jsonMapper.readValue(it, object : TypeReference<Map<String, Any?>>() {}) }
    }

    private fun loadStructuredComposition(resourcePath: String): ObjectNode {
        val stream = WebTemplatePathExpressionFacadeTest::class.java.getResourceAsStream(resourcePath)
            ?: throw RuntimeException("Resource not found: $resourcePath")
        return stream.use { jsonMapper.readTree(it) as ObjectNode }
    }

    private val vitalSignsTemplate by lazy { getWebTemplate("/compatibility/templates/Vital Signs Pathfinder Demo.xml") }
    private val flatComp by lazy { loadFlatComposition("/compatibility/compositions/flat/Vital Signs Pathfinder Demo(1).json") }
    private val structuredComp by lazy { loadStructuredComposition("/compatibility/compositions/structured/Vital Signs Pathfinder Demo(1).json") }


    @Test
    fun testEvaluatePathFlatNavigation() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", flatComp)
        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it is Number }
    }

    @Test
    fun testEvaluatePathFlatCount() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.count()", flatComp)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Number::class.java)
        assertThat((result[0] as Number).toInt()).isGreaterThan(0)
    }

    @Test
    fun testEvaluatePathFlatExists() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", flatComp)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathFlatEmpty() {
        assertThatThrownBy { vitalSignsTemplate.evaluateExpression("nonexistent.empty()", flatComp) }
            .isInstanceOf(WebTemplatePathExpressionException::class.java)
            .hasMessageContaining("Invalid path segment 'nonexistent'")
    }

    @Test
    fun testEvaluatePathFlatSelect() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event.select(systolic|magnitude)", flatComp)
        assertThat(result).isNotEmpty
        assertThat(result).allMatch { it is Number }
    }

    @Test
    fun testEvaluatePathFlatMath() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure:0/any_event:0/systolic|magnitude + 10", flatComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(22.0)
    }

    @Test
    fun testEvaluatePathFlatSum() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude.sum()", flatComp)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Number::class.java)
    }

    @Test
    fun testEvaluatePathFlatComparison() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure:0/any_event:0/systolic|magnitude > 10", flatComp)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathFlatBooleanOperators() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.exists() and blood_pressure/any_event.exists()", flatComp)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathFlatStringConcat() {
        val result = vitalSignsTemplate.evaluateExpression("'value: ' + blood_pressure:0/any_event:0/systolic|magnitude.toString()", flatComp)
        assertThat(result[0] as String).isIn("value: 12.0", "value: 12")
    }

    @Test
    fun testEvaluatePathFlatIif() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.iif(true, 'found', 'missing')", flatComp)
        assertThat(result[0]).isEqualTo("found")
    }

    @Test
    fun testEvaluatePathFlatLengthFunction() {
        val webTemplate = getWebTemplate("/compatibility/templates/Tanner stages.xml")
        val comp = loadFlatComposition("/compatibility/compositions/flat/Tanner stages(1).json")
        val result = webTemplate.evaluateExpression("tanner_stages/any_point_in_time_event:0/comment.length()", comp)
        assertThat(result[0]).isEqualTo(5) // "teste"
    }

    @Test
    fun testEvaluatePathStructuredNavigation() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", structuredComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathStructuredCount() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.count()", structuredComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toInt()).isEqualTo(1)
    }

    @Test
    fun testEvaluatePathStructuredExists() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", structuredComp)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathStructuredSelect() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event.select(systolic|magnitude)", structuredComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathStructuredMath() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure:0/any_event:0/systolic|magnitude * 2", structuredComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(24.0)
    }

    @Test
    fun testEvaluatePathStructuredLower() {
        val webTemplate = getWebTemplate("/compatibility/templates/Tanner stages.xml")
        val comp = loadStructuredComposition("/compatibility/compositions/structured/Tanner stages(1).json")
        val result = webTemplate.evaluateExpression("tanner_stages/any_point_in_time_event/genitals|value.lower()", comp)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo("stage 4")
    }

    @Test
    fun testEvaluatePathStructuredComparison() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure:0/any_event:0/systolic|magnitude > 10", structuredComp)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathStructuredBooleanOperators() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.exists() and blood_pressure/any_event.exists()", structuredComp)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathStructuredIif() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.iif(true, 'found', 'missing')", structuredComp)
        assertThat(result[0]).isEqualTo("found")
    }

    @Test
    fun testEvaluatePathStructuredChildrenFunction() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event.children()", structuredComp)
        assertThat(result).isNotEmpty
    }

    @Test
    fun testEvaluatePathFlatInputWithStructuredFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", flatComp, RmObjectFormat.STRUCTURED)
        assertThat(result).isNotEmpty
        assertThat(result[0]).isInstanceOf(Number::class.java)
    }

    @Test
    fun testEvaluatePathStructuredInputWithFlatFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic", structuredComp, RmObjectFormat.FLAT)
        assertThat(result).isNotEmpty
        assertThat(result[0]).isInstanceOf(Map::class.java)
    }

    @Test
    fun testEvaluatePathExistsFormatConsistency() {
        val resultFlat = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", flatComp)
        val resultStructured = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", structuredComp)
        assertThat(resultFlat[0]).isEqualTo(true)
        assertThat(resultStructured[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathInvalidPathThrows() {
        assertThatThrownBy { vitalSignsTemplate.evaluateExpression("blood_pressure/invalid_segment", structuredComp) }
            .isInstanceOf(WebTemplatePathExpressionException::class.java)
            .hasMessageContaining("Invalid path segment 'invalid_segment'")
    }

    @Test
    fun testEvaluatePathValidPathSucceeds() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", structuredComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathOfTypeRmTypeFiltering() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.ofType('OBSERVATION')", structuredComp)
        assertThat(result).hasSize(1)
    }

    @Test
    fun testEvaluatePathStructuredInputWithRawFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure", structuredComp, RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(RmObject::class.java)
    }

    @Test
    fun testEvaluatePathStructuredInputWithRawFormatPrimitiveValue() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", structuredComp, RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Number::class.java)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathFlatInputWithRawFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure", flatComp, RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(RmObject::class.java)
    }

    private val rawComp: Composition by lazy { getComposition("/compatibility/compositions/raw/Vital Signs Pathfinder Demo(1).xml") }

    @Test
    fun testEvaluatePathRawInputNavigation() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", rawComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathRawInputCount() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.count()", rawComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toInt()).isEqualTo(1)
    }

    @Test
    fun testEvaluatePathRawInputExists() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", rawComp)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathRawInputSelect() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event.select(systolic|magnitude)", rawComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathRawInputMath() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure:0/any_event:0/systolic|magnitude + 10", rawComp)
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(22.0)
    }

    @Test
    fun testEvaluatePathRawInputComparison() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure:0/any_event:0/systolic|magnitude > 10", rawComp)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathRawInputBooleanOperators() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.exists() and blood_pressure/any_event.exists()", rawComp)
        assertThat(result[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathRawInputIif() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.iif(true, 'found', 'missing')", rawComp)
        assertThat(result[0]).isEqualTo("found")
    }

    @Test
    fun testEvaluatePathRawInputSum() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude.sum()", rawComp)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Number::class.java)
    }

    @Test
    fun testEvaluatePathRawInputStringConcat() {
        val result = vitalSignsTemplate.evaluateExpression("'value: ' + blood_pressure:0/any_event:0/systolic|magnitude.toString()", rawComp)
        assertThat(result[0] as String).isIn("value: 12.0", "value: 12")
    }

    @Test
    fun testEvaluatePathRawInputOfType() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure.ofType('OBSERVATION')", rawComp)
        assertThat(result).hasSize(1)
    }

    @Test
    fun testEvaluatePathRawInputChildren() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event.children()", rawComp)
        assertThat(result).isNotEmpty
    }

    @Test
    fun testEvaluatePathFlatInputToStructuredFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic", flatComp, RmObjectFormat.STRUCTURED)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(JsonNode::class.java)
        assertThat((result[0] as JsonNode).isObject).isTrue()
    }

    @Test
    fun testEvaluatePathFlatInputToRawFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure", flatComp, RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(RmObject::class.java)
    }

    @Test
    fun testEvaluatePathStructuredInputToFlatFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic", structuredComp, RmObjectFormat.FLAT)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Map::class.java)
        @Suppress("UNCHECKED_CAST")
        val flatMap = result[0] as Map<String, Any?>
        assertThat(flatMap).containsKey("|magnitude")
        assertThat(flatMap).containsKey("|unit")
    }

    @Test
    fun testEvaluatePathStructuredInputToRawFormatComplexNode() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure", structuredComp, RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(RmObject::class.java)
    }

    @Test
    fun testEvaluatePathRawInputToFlatFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic", rawComp, format = RmObjectFormat.FLAT)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Map::class.java)
        @Suppress("UNCHECKED_CAST")
        val flatMap = result[0] as Map<String, Any?>
        assertThat(flatMap).containsKey("|magnitude")
    }

    @Test
    fun testEvaluatePathRawInputToRawFormatRoundTrip() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure", rawComp, format = RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(RmObject::class.java)
    }

    @Test
    fun testEvaluatePathFlatInputToRawFormatPrimitive() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", flatComp, RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Number::class.java)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathRawInputToStructuredFormat() {
        val result = vitalSignsTemplate.evaluateExpression("blood_pressure/any_event/systolic", rawComp, format = RmObjectFormat.STRUCTURED)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(JsonNode::class.java)
        assertThat((result[0] as JsonNode).isObject).isTrue()
    }

    @Test
    fun testEvaluatePathPrimitiveValueConsistentAcrossFormats() {
        val flatResult = vitalSignsTemplate.evaluateExpression(
            "blood_pressure/any_event/systolic|magnitude", flatComp, RmObjectFormat.STRUCTURED)
        val structuredResult = vitalSignsTemplate.evaluateExpression(
            "blood_pressure/any_event/systolic|magnitude", structuredComp, RmObjectFormat.STRUCTURED)
        val rawResult = vitalSignsTemplate.evaluateExpression(
            "blood_pressure/any_event/systolic|magnitude", rawComp, format = RmObjectFormat.STRUCTURED)
        assertThat((flatResult[0] as Number).toDouble()).isEqualTo(12.0)
        assertThat((structuredResult[0] as Number).toDouble()).isEqualTo(12.0)
        assertThat((rawResult[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathCountConsistentAcrossFormats() {
        val flatResult = vitalSignsTemplate.evaluateExpression("blood_pressure.count()", flatComp, RmObjectFormat.STRUCTURED)
        val structuredResult = vitalSignsTemplate.evaluateExpression("blood_pressure.count()", structuredComp, RmObjectFormat.STRUCTURED)
        val rawResult = vitalSignsTemplate.evaluateExpression("blood_pressure.count()", rawComp, format = RmObjectFormat.STRUCTURED)
        assertThat((flatResult[0] as Number).toInt()).isEqualTo(1)
        assertThat((structuredResult[0] as Number).toInt()).isEqualTo(1)
        assertThat((rawResult[0] as Number).toInt()).isEqualTo(1)
    }

    @Test
    fun testEvaluatePathExistsConsistentAcrossFormats() {
        val flatResult = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", flatComp, RmObjectFormat.STRUCTURED)
        val structuredResult = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", structuredComp, RmObjectFormat.STRUCTURED)
        val rawResult = vitalSignsTemplate.evaluateExpression("blood_pressure.exists()", rawComp, format = RmObjectFormat.STRUCTURED)
        assertThat(flatResult[0]).isEqualTo(true)
        assertThat(structuredResult[0]).isEqualTo(true)
        assertThat(rawResult[0]).isEqualTo(true)
    }

    @Test
    fun testEvaluatePathMathConsistentAcrossFormats() {
        val flatResult = vitalSignsTemplate.evaluateExpression(
            "blood_pressure:0/any_event:0/systolic|magnitude * 2", flatComp, RmObjectFormat.STRUCTURED)
        val structuredResult = vitalSignsTemplate.evaluateExpression(
            "blood_pressure:0/any_event:0/systolic|magnitude * 2", structuredComp, RmObjectFormat.STRUCTURED)
        val rawResult = vitalSignsTemplate.evaluateExpression(
            "blood_pressure:0/any_event:0/systolic|magnitude * 2", rawComp, format = RmObjectFormat.STRUCTURED)
        assertThat((flatResult[0] as Number).toDouble()).isEqualTo(24.0)
        assertThat((structuredResult[0] as Number).toDouble()).isEqualTo(24.0)
        assertThat((rawResult[0] as Number).toDouble()).isEqualTo(24.0)
    }

    @Test
    fun testEvaluatePathWithWebTemplatePathContext() {
        val bloodPressureNode = structuredComp.get("vital_signs_pathfinder_demo").get("blood_pressure").get(0) as ObjectNode
        val result = vitalSignsTemplate.evaluateExpression(
            "any_event/systolic|magnitude",
            bloodPressureNode,
            evaluationContext = EvaluationContext(webTemplatePath = "vital_signs_pathfinder_demo/blood_pressure"))
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathWithDefaultEvaluationContext() {
        val result = vitalSignsTemplate.evaluateExpression(
            "blood_pressure/any_event/systolic|magnitude",
            structuredComp,
            evaluationContext = EvaluationContext())
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    @Test
    fun testEvaluatePathWithAqlPathContext() {
        val bloodPressureNode = structuredComp.get("vital_signs_pathfinder_demo").get("blood_pressure").get(0) as ObjectNode
        val aqlPath = "/content[openEHR-EHR-OBSERVATION.blood_pressure.v2]"

        val webTemplateNode = vitalSignsTemplate.findWebTemplateNodeByAqlPath(aqlPath)
        assertThat(webTemplateNode.rmType).isEqualTo("OBSERVATION")

        val result = vitalSignsTemplate.evaluateExpression(
            "any_event/systolic|magnitude",
            bloodPressureNode,
            evaluationContext = EvaluationContext.ofAqlPath(aqlPath))
        assertThat(result).hasSize(1)
        assertThat((result[0] as Number).toDouble()).isEqualTo(12.0)
    }

    private val tannerTemplate by lazy { getWebTemplate("/compatibility/templates/Tanner stages.xml") }
    private val tannerFlat by lazy { loadFlatComposition("/compatibility/compositions/flat/Tanner stages(1).json") }
    private val tannerStructured by lazy { loadStructuredComposition("/compatibility/compositions/structured/Tanner stages(1).json") }
    private val tannerRaw: Composition by lazy { getComposition("/compatibility/compositions/raw/Tanner stages(1).xml") }

    @Test
    fun testTannerStagesRawInputNavigation() {
        val result = tannerTemplate.evaluateExpression("tanner_stages/any_point_in_time_event/genitals|value", tannerRaw)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo("Stage 4")
    }

    @Test
    fun testTannerStagesRawInputLower() {
        val result = tannerTemplate.evaluateExpression("tanner_stages/any_point_in_time_event/genitals|value.lower()", tannerRaw)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo("stage 4")
    }

    @Test
    fun testTannerStagesRawInputUpper() {
        val result = tannerTemplate.evaluateExpression("tanner_stages/any_point_in_time_event/genitals|value.upper()", tannerRaw)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo("STAGE 4")
    }

    @Test
    fun testTannerStagesConsistentAcrossFormats() {
        val flatResult = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals|value", tannerFlat, RmObjectFormat.STRUCTURED)
        val structuredResult = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals|value", tannerStructured, RmObjectFormat.STRUCTURED)
        val rawResult = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals|value", tannerRaw, format = RmObjectFormat.STRUCTURED)
        assertThat(flatResult[0]).isEqualTo("Stage 4")
        assertThat(structuredResult[0]).isEqualTo("Stage 4")
        assertThat(rawResult[0]).isEqualTo("Stage 4")
    }

    @Test
    fun testTannerStagesStructuredToFlatFormat() {
        val result = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals", tannerStructured, RmObjectFormat.FLAT)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Map::class.java)
        @Suppress("UNCHECKED_CAST")
        val flatMap = result[0] as Map<String, Any?>
        assertThat(flatMap).containsKey("|value")
        assertThat(flatMap["|value"]).isEqualTo("Stage 4")
    }

    @Test
    fun testTannerStagesStructuredToRawFormat() {
        val result = tannerTemplate.evaluateExpression(
            "tanner_stages", tannerStructured, RmObjectFormat.RAW)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(RmObject::class.java)
    }

    @Test
    fun testTannerStagesRawInputToFlatFormat() {
        val result = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals", tannerRaw, format = RmObjectFormat.FLAT)
        assertThat(result).hasSize(1)
        assertThat(result[0]).isInstanceOf(Map::class.java)
        @Suppress("UNCHECKED_CAST")
        val flatMap = result[0] as Map<String, Any?>
        assertThat(flatMap).containsKey("|value")
    }

    @Test
    fun testTannerStagesSubstringConsistentAcrossFormats() {
        val flatResult = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals|value.substring(0, 5)", tannerFlat, RmObjectFormat.STRUCTURED)
        val structuredResult = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals|value.substring(0, 5)", tannerStructured, RmObjectFormat.STRUCTURED)
        val rawResult = tannerTemplate.evaluateExpression(
            "tanner_stages/any_point_in_time_event/genitals|value.substring(0, 5)", tannerRaw, format = RmObjectFormat.STRUCTURED)
        assertThat(flatResult[0]).isEqualTo("Stage")
        assertThat(structuredResult[0]).isEqualTo("Stage")
        assertThat(rawResult[0]).isEqualTo("Stage")
    }
}
