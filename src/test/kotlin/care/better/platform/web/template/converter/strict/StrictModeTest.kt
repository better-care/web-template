package care.better.platform.web.template.converter.strict

import care.better.platform.path.NameAndNodeMatchingPathValueExtractor
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.datatypes.DvQuantity

/**
 * @author Primoz Delopst
 */
class StrictModeTest : AbstractWebTemplateTest() {

    @Test
    fun testStrictDateTimeParsing() {
        val template = getTemplate("/convert/templates/MED - Document.opt")

        val context = ConversionContext.create()
            .withStrictMode()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("composer")
            .build()

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("medical_document/document/date_last_reviewed", "2012-12-01")
            .put("medical_document/document/content", "Hello world!")
            .put("medical_document/document/status", "at0007")
            .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(values, context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageContaining("")
    }

    @Test
    fun testDependantValuesNotRemoved() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(
            getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Dependant Values.json"),
            object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(0)

        val strictComposition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().withStrictMode().build())

        val strictQuantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(strictComposition)
        assertThat(strictQuantities).hasSize(1)
        assertThat((strictQuantities[0] as DvQuantity).units).isNotNull
    }

    @Test
    fun testNullInFlatFormat() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(
            getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Null Leaf Value.json"),
            object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(1)
        assertThat((quantities[0] as DvQuantity).units).isNull()

        val strictComposition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().withStrictMode().build())

        val strictQuantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(strictComposition)
        assertThat(strictQuantities).hasSize(1)
        assertThat((strictQuantities[0] as DvQuantity).units).isNull()
    }

    @Test
    fun testBlankInFlatFormat() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(
            getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Blank Leaf Value.json"),
            object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(1)
        assertThat((quantities[0] as DvQuantity).units).isNull()

        val strictComposition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().withStrictMode().build())

        val strictQuantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(strictComposition)
        assertThat(strictQuantities).hasSize(1)
        assertThat((strictQuantities[0] as DvQuantity).units).isEqualTo("")
    }

    @Test
    fun testNullInStructuredFormat() {
        val compositionObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Structured - Null Leaf Value.json")) as ObjectNode

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode =
            webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().build())

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(1)
        assertThat((quantities[0] as DvQuantity).units).isNull()

        val strictComposition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().withStrictMode().build())

        val strictQuantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(strictComposition)
        assertThat(strictQuantities).hasSize(1)
        assertThat((strictQuantities[0] as DvQuantity).units).isNull()
    }

    @Test
    fun testBlankInStructuredFormat() {
        val compositionObjectNode: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Structured - Blank Leaf Value.json")) as ObjectNode

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().build())

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(1)
        assertThat((quantities[0] as DvQuantity).units).isNull()

        val strictComposition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().withStrictMode().build())

        val strictQuantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(strictComposition)
        assertThat(strictQuantities).hasSize(1)
        assertThat((strictQuantities[0] as DvQuantity).units).isEqualTo("")
    }

    @Test
    fun testNullDvQuantity() {
        val compositionObjectNode: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Null Leaf Node.json")) as ObjectNode
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"),
            WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))

        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")

        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("Composer")
            .withStrictMode()
            .build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode as ObjectNode, context)

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(0)
    }

    @Test
    fun testMultipleElementArrayForSingletonNode() {
        val json = getJson("/convert/compositions/strict/ISPEK - ZN - Vital Functions Encounter - Array For Singleton.json")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/ZN - Vital Functions Encounter.xml"),
            WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(json) as ObjectNode, context)
        assertThat(composition).isNotNull

        val strictContext = ConversionContext.create()
            .withStrictMode()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("Composer")
            .build()

        assertThatThrownBy { webTemplate.convertFromStructuredToRaw<Composition>(getObjectMapper().readTree(json) as ObjectNode, strictContext) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("JSON array with single value is expected (path: vital_functions/vital_signs/body_weight:0/any_event:0/time).")
    }

    @Test
    fun testBlankAttributeWithOtherAttributesInLeafNode() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(
            getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Blank Attribute.json"),
            object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(1)
        assertThat((quantities[0] as DvQuantity).magnitude).isEqualTo(66.7)
        assertThat((quantities[0] as DvQuantity).units).isEqualTo("mmol/l")

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(compositionFlatMap, ConversionContext.create().withStrictMode().build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Object node with blank attribute name and multiple fields (path: laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value).")
    }

    @Test
    fun testEmptyObservation() {
        val compositionObjectNode: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Empty Node.json")) as ObjectNode

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test")
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().build())
        assertThat(composition).isNull()

        val strictComposition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().withStrictMode().build())

        val strictObservations = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(strictComposition)
        assertThat(strictObservations).hasSize(1)
    }

    @Test
    fun testEmptyLeafObjectNode() {
        val compositionObjectNode: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/strict/IDCR_-_Laboratory_Test_Report(1) - Empty Leaf Node.json")) as ObjectNode

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_test_report/laboratory_test/laboratory_test_panel/laboratory_result/result_value")
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().build())

        val quantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(composition)
        assertThat(quantities).hasSize(0)

        val strictComposition: Composition? = webTemplate.convertFromStructuredToRaw(compositionObjectNode, ConversionContext.create().withStrictMode().build())

        val strictQuantities = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path).getValue(strictComposition)
        assertThat(strictQuantities).hasSize(1)
    }
}
