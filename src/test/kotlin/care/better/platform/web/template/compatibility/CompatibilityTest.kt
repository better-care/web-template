/* Copyright 2023 Better Ltd (www.better.care)
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

package care.better.platform.web.template.compatibility

import care.better.platform.jaxb.JaxbRegistry
import care.better.platform.json.jackson.better.BetterObjectMapper
import care.better.platform.json.jackson.time.OpenEhrTimeModule
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.mapper.WebTemplateObjectMapper
import care.better.platform.web.template.converter.FromRawConversion
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.io.File
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.writeText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class CompatibilityTest : AbstractWebTemplateTest() {

    private val serverResources: List<CompatibilityServerResourceDto> = listOf(
            CompatibilityServerResourceDto("OPENeP - Medication Administration", Pair(1, 3), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("LAB - Laboratory Test Report", Pair(1, 3), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("ZN - Vital Functions Encounter", Pair(1, 3), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("OPENeP - Inpatient Prescription", Pair(1, 3), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("ZN - Nursing careplan Encounter", Pair(1, 2), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("MED - Allergy history Summary", Pair(1, 2), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto(
            "MED - Nutrition Order",
            Pair(1, 1),
            "sl",
            setOf("sl", "en"),
            mapOf(Pair("nutrition_order/nutrition/nutrition_order:0", "932ba8bf-b3ef-4396-958a-5394686bf347"))),
            CompatibilityServerResourceDto("MED - Nutrition administration", Pair(1, 1), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("ZN - APACHE", Pair(1, 2), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("Malnutrition universal screening tool", Pair(1, 2), "en", setOf("en")),
            CompatibilityServerResourceDto("NCD Nurse Patient Encounter", Pair(1, 1), "en", setOf("en")),
            CompatibilityServerResourceDto("Waterlow pressure damage risk assessment", Pair(1, 1), "en", setOf("en")),
            CompatibilityServerResourceDto("Vital Signs Pathfinder Demo", Pair(1, 2), "en", setOf("en")),
            CompatibilityServerResourceDto("VBHC PANCR Clinical Outcomes Follow-up", Pair(1, 1), "en", setOf("en")),
            CompatibilityServerResourceDto("Adverse Reaction List.v1", Pair(1, 1), "en", setOf("en")),
            CompatibilityServerResourceDto("Histopathology Report", Pair(1, 1), "sl", setOf("sl", "en")),
            CompatibilityServerResourceDto("Cytology Report", Pair(1, 2), "sl", setOf("sl", "en")))

    private val inputResources: List<CompatibilityInputResourceDto> = listOf(
        CompatibilityInputResourceDto("Falls care plan", true, Pair(1, 1), "sl", setOf("sl", "en")),
        CompatibilityInputResourceDto(
            "MED - Nutrition Order",
            true,
            Pair(2, 2),
            "sl",
            setOf("sl", "en"),
            mapOf(Pair("nutrition_order/nutrition/nutrition_order:0", "5817aecf-10cc-463e-afb8-8c2611f98152"))),
        CompatibilityInputResourceDto(
            "VBHC CRC Clinical Outcomes Baseline",
            false,
            Pair(1, 1),
            "en",
            setOf("en"),
            mapOf(Pair("medtronic_vbhc_crc_clinical_outcomes_baseline/baseline_treatment_factors/treatment", "e9a3fedc-f8ba-431f-9c48-b35d6012fb10"))),
        CompatibilityInputResourceDto("VBHC CRC Clinical Outcomes Follow-up", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("VBHC CRC PROM", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("CHAQ", false, Pair(2, 2), "en", setOf("en"), nullable = true, onlyGenericFields = true),
        CompatibilityInputResourceDto("JIA Body map", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("JIA JADAS", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("Tanner stages", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("Anamnesis", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("Cytology Report", false, Pair(3, 3), "en", setOf("en")),
        CompatibilityInputResourceDto("HPV Report", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("Colposcopy", false, Pair(1, 1), "en", setOf("en")),
        CompatibilityInputResourceDto("Vital Signs microseconds", true, Pair(1, 1), "en", setOf("en")))

    @Test
    fun testCompatibilityFromServer() {
        serverResources.forEach {
            val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
                getTemplate("/compatibility/templates/${it.name}.xml"),
                WebTemplateBuilderContext(it.language, it.languages))

            for (i in it.range.first .. it.range.second) {
                val flatComposition = CompatibilityMapper.readValue(
                    getJson("/compatibility/compositions/flat/${it.name}($i).json"),
                    object : TypeReference<Map<String, Any>>() {})

                val structuredComposition = CompatibilityMapper.readTree(getJson("/compatibility/compositions/structured/${it.name}($i).json")) as ObjectNode

                val rawComposition = getComposition("/compatibility/compositions/raw/${it.name}($i).xml")

                testFlatToRaw(it.name, webTemplate, flatComposition, rawComposition, it.instructionUidEntries)
                testStructuredToRaw(it.name, webTemplate, structuredComposition, rawComposition, it.instructionUidEntries)
                testRawToFlat(it.name, webTemplate, rawComposition, "/compatibility/compositions/flat/${it.name}($i).json")
                testRawToStructured(it.name, webTemplate, rawComposition, "/compatibility/compositions/structured/${it.name}($i).json")
            }
        }
    }

    @Test
    fun testCompatibilityFromInput() {
        inputResources.forEach {
            val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
                getTemplate("/compatibility/templates/${it.name}.xml"),
                WebTemplateBuilderContext(it.language, it.languages))

            for (i in it.range.first .. it.range.second) {
                val context = ConversionContext.create().withUidGenerator(getUidGenerator(it.instructionUidEntries)).build()
                if (it.flat) {
                    val inputFlatComposition = CompatibilityMapper.readValue(
                        getJson("/compatibility/compositions/input/${it.name}($i).json"),
                        object : TypeReference<Map<String, Any>>() {})
                    val convertedComposition: Composition? = webTemplate.convertFromFlatToRaw(inputFlatComposition, context)

                    if (it.nullable){
                        assertThat(convertedComposition).isNull()
                        return@forEach
                    }

                    assertThat(convertedComposition).isNotNull()

                    val rawComposition = getComposition("/compatibility/compositions/raw/${it.name}($i).xml")

                    testFlatToRaw(it.name, webTemplate, inputFlatComposition, rawComposition, it.instructionUidEntries)
                    testRawToFlat(it.name, webTemplate, convertedComposition!!, "/compatibility/compositions/flat/${it.name}($i).json")
                    testRawToStructured(it.name, webTemplate, convertedComposition, "/compatibility/compositions/structured/${it.name}($i).json")

                } else {
                    val inputStructuredComposition = CompatibilityMapper.readTree(getJson("/compatibility/compositions/input/${it.name}($i).json")) as ObjectNode
                    val convertedComposition: Composition? = webTemplate.convertFromStructuredToRaw(inputStructuredComposition, context)

                    if (it.nullable){
                        assertThat(convertedComposition).isNull()
                        return@forEach
                    } else if (it.onlyGenericFields) {
                        assertThat(convertedComposition).isNotNull()
                        assertThat(convertedComposition?.feederAudit?.originalContent).isNotNull()
                        return@forEach
                    }

                    assertThat(convertedComposition).isNotNull()

                    val rawComposition = getComposition("/compatibility/compositions/raw/${it.name}($i).xml")

                    testStructuredToRaw(it.name, webTemplate, inputStructuredComposition, rawComposition, it.instructionUidEntries)
                    testRawToFlat(it.name, webTemplate, convertedComposition!!, "/compatibility/compositions/flat/${it.name}($i).json")
                    testRawToStructured(it.name, webTemplate, convertedComposition, "/compatibility/compositions/structured/${it.name}($i).json")
                }
            }
        }
    }

    @Test
    fun testWebTemplateCompatibility() {
        (serverResources + inputResources).map { it.name }.distinct().forEach { name ->
            val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
                getTemplate("/compatibility/templates/$name.xml"),
                WebTemplateBuilderContext("en", setOf("en")))

            assertJsonEquals("Web template incompatible: $name", "/compatibility/web-templates/$name.json", webTemplate)
        }
    }

    private fun testFlatToRaw(
            name: String,
            webTemplate: WebTemplate,
            inputFlatComposition: Map<String, Any?>,
            referenceRawComposition: Composition,
            instructionUidEntries: Map<String, String>) {
        val context = ConversionContext.create().withUidGenerator(getUidGenerator(instructionUidEntries)).build()
        val rawComposition: Composition = webTemplate.convertFromFlatToRaw(inputFlatComposition, context)!!
        assertThat(rawComposition).describedAs("Incompatible flat to raw: $name").usingRecursiveComparison().isEqualTo(referenceRawComposition)

        assertThat(with(StringWriter()) {
            JaxbRegistry.getInstance().marshaller.marshal(rawComposition, this)
            this.toString()
        })
            .describedAs("Incompatible flat to raw: $name")
            .isEqualTo(
                    with(StringWriter()) {
                        JaxbRegistry.getInstance().marshaller.marshal(referenceRawComposition, this)
                        this.toString()
                    }
            )
    }

    private fun testStructuredToRaw(
            name: String,
            webTemplate: WebTemplate,
            inputStructuredComposition: ObjectNode,
            referenceRawComposition: Composition,
            instructionUidEntries: Map<String, String>) {
        val context = ConversionContext.create().withUidGenerator(getUidGenerator(instructionUidEntries)).build()
        val rawComposition: Composition = webTemplate.convertFromStructuredToRaw(inputStructuredComposition, context)!!
        assertThat(rawComposition).describedAs("Incompatible structured to raw: $name").usingRecursiveComparison().isEqualTo(referenceRawComposition)

        assertThat(with(StringWriter()) {
            JaxbRegistry.getInstance().marshaller.marshal(rawComposition, this)
            this.toString()
        })
            .describedAs("Incompatible structured to raw: $name")
            .isEqualTo(
                    with(StringWriter()) {
                        JaxbRegistry.getInstance().marshaller.marshal(referenceRawComposition, this)
                        this.toString()
                    }
            )
    }

    private fun testRawToFlat(name: String, webTemplate: WebTemplate, inputRawComposition: Composition, referenceFlatCompositionFile: String) {
        val flatComposition: Map<String, Any?> = with(webTemplate.convertFromRawToFlat(inputRawComposition, FromRawConversion.create())) {
            //Ensure that map is serialized and deserialized in the same way as the reference map
            CompatibilityMapper.readValue(CompatibilityMapper.writeValueAsString(this), object : TypeReference<Map<String, Any?>>() {})
        }

        assertJsonEquals("Raw to flat conversion incompatible: $name", referenceFlatCompositionFile, flatComposition)
    }

    private fun testRawToStructured(name: String, webTemplate: WebTemplate, inputRawComposition: Composition, referenceStructuredCompositionFile: String) {
        val structuredComposition: JsonNode = with(webTemplate.convertFromRawToStructured(inputRawComposition, FromRawConversion.create())!!) {
            //Ensure that JSON node is serialized and deserialized in the same way as reference JSON node
            CompatibilityMapper.readTree(CompatibilityMapper.writeValueAsString(this))
        }

        assertJsonEquals("Raw to flat conversion incompatible: $name", referenceStructuredCompositionFile, structuredComposition)
    }

    private fun assertJsonEquals(description: String, referenceJsonFile: String, value: Any) {
        val actualJson = WebTemplateObjectMapper.getWriter(pretty = true).writeValueAsString(value)
        val tempFileOnFailure = Files.createTempFile(tempDirectory, File(referenceJsonFile).name, ".json")
        try {
            JSONAssert.assertEquals(
                    "$description\nExpected: contents of $referenceJsonFile\nActual: contents of $tempFileOnFailure\n",
                    getJson(referenceJsonFile),
                    actualJson,
                    JSONCompareMode.LENIENT)
            tempFileOnFailure.deleteIfExists()
        } catch (e: AssertionError) {
            tempFileOnFailure.writeText(actualJson)
            throw e
        }
    }

    private data class CompatibilityServerResourceDto(
            override val name: String,
            val range: Pair<Int, Int>,
            val language: String,
            val languages: Set<String>,
            val instructionUidEntries: Map<String, String> = mutableMapOf()) : Named

    private data class CompatibilityInputResourceDto(
            override val name: String,
            val flat: Boolean,
            val range: Pair<Int, Int>,
            val language: String,
            val languages: Set<String>,
            val instructionUidEntries: Map<String, String> = mutableMapOf(),
            val nullable: Boolean = false,
            val onlyGenericFields: Boolean = false) : Named

    private interface Named {
        val name: String
    }

    private fun getUidGenerator(instructionUidEntries: Map<String, String>): (String) -> String = { instructionUidEntries[it] ?: UUID.randomUUID().toString() }

    private object CompatibilityMapper : BetterObjectMapper() {
        init {
            registerModule(JodaModule())
            registerModule(JavaTimeModule())
            registerModule(OpenEhrTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            enable(JsonParser.Feature.ALLOW_COMMENTS)
            setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.USE_DEFAULTS))
            configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        }
    }

    companion object {
        private lateinit var tempDirectory: Path
        @JvmStatic
        @BeforeAll
        fun setUp() {
            tempDirectory = Files.createTempDirectory("CompatibilityTest")
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            if (tempDirectory.listDirectoryEntries().isEmpty()) {
                tempDirectory.deleteIfExists()
            }
        }
    }
}
