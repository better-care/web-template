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

package care.better.platform.web.template.converter

import care.better.platform.path.NameAndNodeMatchingPathValueExtractor
import care.better.platform.path.SimplePathValueExtractor
import care.better.platform.utils.DateTimeConversionUtils
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.exception.UnknownPathBuilderException
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateOrdinalCodedValue
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.jupiter.api.Test
import org.openehr.rm.common.Link
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvEhrUri
import org.openehr.rm.datatypes.DvQuantity
import org.openehr.rm.datatypes.DvText
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.OffsetDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class LocaleTest : AbstractWebTemplateTest() {

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDefaultValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val textNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/text")
        assertThat(textNode.getInput()?.defaultValue).isEqualTo("hello world!")

        val quantityNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/quantity")
        assertThat(quantityNode.inputs[0].defaultValue).isEqualTo(23.0)
        assertThat(quantityNode.inputs[1].defaultValue).isEqualTo("mm[Hg]")

        val countNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/count")
        assertThat(countNode.getInput()?.defaultValue).isEqualTo(3L)

        val dateTimeNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/datetime")
        assertThat(dateTimeNode.getInput()?.defaultValue).isEqualTo(OffsetDateTime.of(2013, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))

        val durationNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/duration")
        assertThat(durationNode.inputs[0].defaultValue).isEqualTo(0)
        assertThat(durationNode.inputs[1].defaultValue).isEqualTo(2)

        val ordinalNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/ordinal")
        assertThat(ordinalNode.getInput()?.defaultValue).isEqualTo("at0030")

        val codedValue: WebTemplateCodedValue = ordinalNode.getInput()?.list?.get(0)!!
        assertThat(codedValue).isInstanceOf(WebTemplateOrdinalCodedValue::class.java)
        assertThat((codedValue as WebTemplateOrdinalCodedValue).ordinal).isEqualTo(1)

        val booleanNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/boolean")
        assertThat(booleanNode.getInput()?.defaultValue).isEqualTo(true)

        val proportionNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/proportion")
        assertThat(proportionNode.getInput()?.defaultValue).isEqualTo(33.0f)

        val identifierNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/identifier")
        assertThat(identifierNode.getInput()?.defaultValue).isEqualTo("abcdef")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testValidation() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N.opt"), WebTemplateBuilderContext("en"))
        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing/testing/count1")
        assertThat(firstNode.getInput()?.validation?.range?.getMinimal()).isEqualTo(1)
        assertThat(firstNode.getInput()?.validation?.range?.getMaximal()).isEqualTo(20)

        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing/testing/count2")
        assertThat(secondNode.getInput()?.validation?.range?.getMinimal()).isNull()
        assertThat(secondNode.getInput()?.validation?.range?.getMaximal()).isEqualTo(20)

        val thirdNode: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing/testing/count3")
        assertThat(thirdNode.getInput()?.validation).isNull()

        val fourthNode: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing/testing/count4")
        assertThat(fourthNode.getInput()?.validation?.range?.getMinimal()).isEqualTo(3)
        assertThat(fourthNode.getInput()?.validation?.range?.getMaximal()).isNull()
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testName() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N.opt"), WebTemplateBuilderContext("en"))
        val values: Map<String, Any> = mapOf(
            "test_encounter/testing/testing/count1" to 12,
            "test_encounter/testing/testing/count1/_name|code" to "at0001",
            "test_encounter/testing/testing/count1/_name|value" to "Hello world"
        )

        val context = ConversionContext.create().withComposerName("Joe").withLanguage("en").withTerritory("CA").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        val extractor =
            SimplePathValueExtractor("/content[openEHR-EHR-OBSERVATION.testing.v1 and name/value='Testing']/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.testing.v1 and name/value='Testing']/items[at0064]")
        val value = extractor.getValue(composition!!)
        val actual = value[0] as Element
        val name = actual.name as DvCodedText?
        assertThat(name?.definingCode?.codeString).isEqualTo("at0001")
        assertThat(name?.value).isEqualTo("Hello world")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSerialization() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val mapper = ObjectMapper().apply {
            this.registerModule(JavaTimeModule())
        }
        val jsonNode: JsonNode = mapper.valueToTree(webTemplate)
        val node = jsonNode.path("tree").path("children").path(0)
        assertThat(node.path("aqlPath").isMissingNode).isFalse
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testBoolean() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(mapOf("encounter/testing/boolean" to true), context)

        val formatted: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(formatted).contains(entry("encounter/testing:0/boolean", "true"))

        val actual: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(actual).contains(entry("encounter/testing:0/boolean", true))

        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(node).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testBooleanJson() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val json = """
            {
               "encounter":{
                  "testing":[
                     {
                        "boolean":[
                           true
                        ]
                     }
                  ]
               }
            }
        """.trimIndent()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(mapper.readValue(json, ObjectNode::class.java), context)
        val formatted: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(formatted).contains(entry("encounter/testing:0/boolean", "true"))

        val actual: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(actual).contains(entry("encounter/testing:0/boolean", true))

        val retrievedJson: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        val booleanNode = retrievedJson!!.path("encounter").path("testing").path(0).path("boolean").path(0)
        assertThat(booleanNode.isMissingNode).isFalse
        assertThat(booleanNode.isBoolean).isTrue
        assertThat(booleanNode.asBoolean()).isTrue
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDuration() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(mapOf("encounter/testing/duration|xyz" to 10), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("DV_DURATION has no attribute |xyz (path: encounter/testing:0/duration|xyz).")

    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondDuration() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(mapOf("encounter/testing/duration|year" to true), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Invalid value of duration field '|year': true (path: encounter/testing:0/duration).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testWtTestDefaultValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))

        val textNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/text")
        assertThat(textNode.getInput()?.defaultValue).isEqualTo("hello world!")

        val quantityNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/quantity")
        assertThat(quantityNode.inputs[0].suffix).isEqualTo("magnitude")
        assertThat(quantityNode.inputs[0].defaultValue).isEqualTo(23.0)
        assertThat(quantityNode.inputs[0].list).isEmpty()
        assertThat(quantityNode.inputs[1].suffix).isEqualTo("unit")
        assertThat(quantityNode.inputs[1].defaultValue).isEqualTo("mm[Hg]")
        assertThat(quantityNode.inputs[1].list[0].validation?.range).isNotNull

        val countNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/count")
        assertThat(countNode.getInput()?.defaultValue).isEqualTo(3L)

        val dateTimeNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/datetime")
        val dateTime = DateTimeConversionUtils.toOffsetDateTime("2013-01-01T00:00:00.000Z")
        assertThat(dateTimeNode.getInput()?.defaultValue).isEqualTo(dateTime)

        val durationNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/duration")
        assertThat(durationNode.getInput("month")?.defaultValue).isEqualTo(2)

        val ordinalNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/ordinal")
        assertThat(ordinalNode.getInput()?.defaultValue).isEqualTo("at0030")

        val booleanNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/boolean")
        assertThat(booleanNode.getInput()?.defaultValue).isEqualTo(true)

        val proportionNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/proportion")
        assertThat(proportionNode.getInput()?.defaultValue).isEqualTo(33.0f)

        val identifierNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/identifier")
        assertThat(identifierNode.inputs[0].defaultValue).isEqualTo("abcdef")
        assertThat(identifierNode.inputs[1].defaultValue).isEqualTo("type")
        assertThat(identifierNode.inputs[2].defaultValue).isEqualTo("issuer")
        assertThat(identifierNode.inputs[3].defaultValue).isEqualTo("assigner")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIdentifier() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("sl"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "encounter/testing/identifier" to "id",
                "encounter/testing/identifier|issuer" to "issuer",
                "encounter/testing/identifier|assigner" to "assigner",
                "encounter/testing/identifier|type" to "type",
                "encounter/testing/text" to "hi there",
                "encounter/testing/quantity|magnitude" to "17,1",
                "encounter/testing/count" to "1",
                "encounter/testing/datetime" to "2013-1-1T01:00:17.000Z",
                "encounter/testing/duration|year" to "1",
                "encounter/testing/ordinal|at0030" to "on",
                "encounter/testing/boolean" to "true",
                "encounter/testing/proportion" to "37,0",
                "encounter/testing/parsable" to "<html><body>hello world!</body></html>"
            ),
            context
        )
        assertThat(webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())).contains(
            entry("encounter/testing:0/identifier", "id"),
            entry("encounter/testing:0/identifier|issuer", "issuer"),
            entry("encounter/testing:0/identifier|assigner", "assigner"),
            entry("encounter/testing:0/identifier|type", "type"),
            entry("encounter/testing:0/text", "hi there"),
            entry("encounter/testing:0/quantity|magnitude", "17.1"),
            entry("encounter/testing:0/quantity|unit", "mm[Hg]"),
            entry("encounter/testing:0/count", "1"),
            entry("encounter/testing:0/datetime", "2013-01-01T01:00:17Z"),
            entry("encounter/testing:0/duration|year", "1"),
            entry("encounter/testing:0/ordinal|code", "at0030"),
            entry("encounter/testing:0/ordinal|ordinal", "2"),
            entry("encounter/testing:0/boolean", "true"),
            entry("encounter/testing:0/proportion", "37.0%"),
            entry("encounter/testing:0/parsable", "<html><body>hello world!</body></html>")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testEmptyIdentifier() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("sl"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "encounter/testing/identifier" to "",
                "encounter/testing/identifier|issuer" to "issuer",
                "encounter/testing/identifier|assigner" to "assigner",
                "encounter/testing/identifier|type" to "type",
                "encounter/testing/text" to "hi there",
                "encounter/testing/quantity|magnitude" to "17,1",
                "encounter/testing/count" to "1",
                "encounter/testing/datetime" to "2013-1-1T01:00:17.000Z",
                "encounter/testing/duration|year" to "1",
                "encounter/testing/ordinal|at0030" to "on",
                "encounter/testing/boolean" to "true",
                "encounter/testing/proportion" to "37,0",
                "encounter/testing/parsable" to "<html><body>hello world!</body></html>"
            ),
            context
        )
        val retrieved: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieved).contains(
            entry("encounter/testing:0/text", "hi there"),
            entry("encounter/testing:0/quantity|magnitude", "17.1"),
            entry("encounter/testing:0/quantity|unit", "mm[Hg]"),
            entry("encounter/testing:0/count", "1"),
            entry("encounter/testing:0/datetime", "2013-01-01T01:00:17Z"),
            entry("encounter/testing:0/duration|year", "1"),
            entry("encounter/testing:0/ordinal|code", "at0030"),
            entry("encounter/testing:0/ordinal|ordinal", "2"),
            entry("encounter/testing:0/boolean", "true"),
            entry("encounter/testing:0/proportion", "37.0%"),
            entry("encounter/testing:0/parsable", "<html><body>hello world!</body></html>")
        )

        assertThat(retrieved).doesNotContain(
            entry("encounter/testing:0/identifier", ""),
            entry("encounter/testing:0/identifier|issuer", "issuer"),
            entry("encounter/testing:0/identifier|assigner", "assigner"),
            entry("encounter/testing:0/identifier|type", "type")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testNoCtx() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("sl"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf("encounter/testing/text" to "hi there"),
            context
        )
        assertThat(composition).isNotNull

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(
            webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create()),
            ConversionContext.create().build()
        )
        assertThat(secondComposition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testTiming() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Medications.xml"), WebTemplateBuilderContext("sl"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "en",
                "ctx/territory" to "US",
                "ctx/composer_name" to "Testing Nurse",
                "ctx/time" to "2016-04-21T16:32:39.271+02:00",
                "ctx/id_namespace" to "HOSPITAL-NS",
                "ctx/id_scheme" to "HOSPITAL-NS",
                "ctx/participation_name" to "Testing Doctor",
                "ctx/participation_function" to "requester",
                "ctx/participation_mode" to "face-to-face communication",
                "ctx/participation_id" to "199",
                "ctx/participation_name:1" to "Testing Nurse",
                "ctx/participation_function:1" to "performer",
                "ctx/participation_id:1" to "198",
                "ctx/health_care_facility|name" to "Hospital",
                "ctx/health_care_facility|id" to "9091",
                "medications/medication_instruction:0/order:0/medicine" to "Medicine 19",
                "medications/medication_instruction:0/order:0/directions" to "Directions 96",
                "medications/medication_instruction:0/order:0/timing" to "R1",
                "medications/medication_instruction:0/narrative" to "Human readable instruction narrative"
            ),
            context
        )

        assertThat(composition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCodedText() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("sl"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            context
        )

        val retrieved: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieved).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_status|terminology", "local"),
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_status|code", "at0037"),
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_status|value", "Začasen")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDiffStructure() {
        val firstWebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("sl"))
        val secondWebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("sl"))
        FileOutputStream(File(System.getProperty("java.io.tmpdir") + File.separator + "demo1.json")).use { outputStream ->
            firstWebTemplate.write(outputStream, true)
        }
        FileOutputStream(File(System.getProperty("java.io.tmpdir") + File.separator + "demo2.json")).use { outputStream ->
            secondWebTemplate.write(outputStream, true)
        }
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultipleEvents() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("sl"))
        val values: MutableMap<String, String> = HashMap()
        values["ctx/language"] = "en"
        values["ctx/territory"] = "IE"
        values["ctx/composer_name"] = "John Blake"
        values["vitals/vitals/body_temperature/any_event/time"] = "2014-01-17T22:10:13.000+01:00"
        values["vitals/vitals/body_temperature/any_event/temperature|magnitude"] = "37.1"
        values["vitals/vitals/body_temperature/any_event/temperature|unit"] = "°C"
        values["vitals/vitals/body_temperature/any_event:1/time"] = "2014-01-18T07:41:07.000+01:00"
        values["vitals/vitals/body_temperature/any_event:1/temperature|magnitude"] = "38.1"
        values["vitals/vitals/body_temperature/any_event:1/temperature|unit"] = "°C"
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())

        val retrieved: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieved).contains(
            entry("vitals/vitals/body_temperature:0/any_event:0/time", "2014-01-17T22:10:13+01:00"),
            entry("vitals/vitals/body_temperature:0/any_event:1/time", "2014-01-18T07:41:07+01:00"),
            entry("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", "37.1"),
            entry("vitals/vitals/body_temperature:0/any_event:1/temperature|magnitude", "38.1")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDependentValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("sl"))
        val values: Map<String, String> = mapOf(
            Pair("ctx/language", "en"),
            Pair("ctx/territory", "IE"),
            Pair("ctx/composer_name", "John Blake"),
            Pair("vitals/vitals/body_temperature/any_event/time", ""),
            Pair("vitals/vitals/body_temperature/any_event/temperature|magnitude", ""),
            Pair("vitals/vitals/body_temperature/any_event/temperature", ""),
            Pair("vitals/vitals/body_temperature/any_event/temperature|unit", "°C"),
            Pair("vitals/vitals/body_temperature/any_event:1/time", "2014-01-18T07:41:07.000+01:00"),
            Pair("vitals/vitals/body_temperature/any_event:1/temperature|magnitude", "38.1"),
            Pair("vitals/vitals/body_temperature/any_event:1/temperature|unit", "°C")
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/body_temperature:0/any_event:0/time", "2014-01-18T07:41:07+01:00"),
            entry("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", "38.1")
        )

        assertThat(flatMap.containsKey("vitals/vitals/body_temperature:0/any_event:1/time")).isFalse
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInterval() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "encounter/testing/intervalquantity/lower|magnitude" to "101,0",
                "encounter/testing/intervalquantity/lower|unit" to "mm[Hg]",
                "encounter/testing/intervalquantity/upper|magnitude" to "107,0",
                "encounter/testing/intervalquantity/upper|unit" to "mm[Hg]"
            ),
            context
        )

        val actual: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(actual).contains(
            entry("encounter/testing:0/intervalquantity/lower|magnitude", "101.0"),
            entry("encounter/testing:0/intervalquantity/lower|unit", "mm[Hg]"),
            entry("encounter/testing:0/intervalquantity/upper|magnitude", "107.0"),
            entry("encounter/testing:0/intervalquantity/upper|unit", "mm[Hg]")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLinks() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "encounter/testing/intervalquantity/lower|magnitude" to "101,0",
                "encounter/testing/intervalquantity/lower|unit" to "mm[Hg]",
                "encounter/testing/intervalquantity/upper|magnitude" to "107,0",
                "encounter/testing/intervalquantity/upper|unit" to "mm[Hg]"
            ),
            context
        )

        val link = Link().apply {
            this.type = DvText("type")
            this.meaning = DvText("meaning")
            this.target = DvEhrUri.create("abc", "def", "")
        }

        composition!!.links.add(link)
        val actual: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(webTemplate.convertFromFlatToRaw<Composition>(actual, context)).isNotNull
    }

    @Test
    @Throws(Exception::class)
    fun testDiabetes() {
        val templateName = "/convert/templates/Diabetes Encounter ver2.xml"
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate(templateName), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "diabetes_encounter_ver2/problem_diagnosis/problem_diagnosis" to "test",
                "diabetes_encounter_ver2/haemoglobin_a1c/any_event/hba1c" to "20",
                "diabetes_encounter_ver2/blood_glucose/any_event/glucose_challenge/dose" to "10",
                "diabetes_encounter_ver2/blood_glucose/any_event/glucose_challenge/route" to "at0.105"
            ),
            context
        )

        assertThat(composition).isNotNull

        buildAndExport(templateName, "diabetes2", "sl", setOf("sl", "en"))
    }

    @Test
    @Throws(Exception::class)
    fun testDiagnosis() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Diagnosis.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "diagnosis/diagnosis/diagnosis|code" to "A01",
                "diagnosis/diagnosis/diagnosis|value" to "test"
            ),
            context
        )

        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(entry("diagnosis/diagnosis:0/diagnosis|terminology", "ICD10"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCodedTextWithOther() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Forms Demo.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("forms_demo/vitals/body_temperature/any_event/symptoms")
        assertThat(node.getInput()).isNotNull
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "forms_demo/vitals/body_temperature/any_event/body_exposure" to "at0033",
                "forms_demo/vitals/body_temperature/any_event/description_of_thermal_stress" to "stress 1",
                "forms_demo/vitals/body_temperature/any_event/symptoms|other" to "other symptom",
                "forms_demo/vitals/body_temperature/any_event/temperature" to "38",
                "forms_demo/vitals/body_temperature/any_event/temperature|unit" to "°C",
                "forms_demo/vitals/body_temperature/site_of_measurement" to "at0.60"
            ), context
        )
        assertThat(composition).isNotNull

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("forms_demo/vitals/body_temperature:0/any_event:0/symptoms|other", "other symptom"))

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "forms_demo/vitals/body_temperature/any_event/description_of_thermal_stress" to "s1",
                "forms_demo/vitals/body_temperature/any_event/temperature" to "38",
                "forms_demo/vitals/body_temperature/any_event/temperature|unit" to "°C",
                "forms_demo/vitals/body_temperature/any_event/symptoms" to "at0.64",
                "forms_demo/vitals/body_temperature/any_event/body_exposure" to "at0032",
                "forms_demo/vitals/body_temperature/any_event:1/description_of_thermal_stress" to "s2",
                "forms_demo/vitals/body_temperature/any_event:1/temperature" to "39",
                "forms_demo/vitals/body_temperature/any_event:1/temperature|unit" to "°C",
                "forms_demo/vitals/body_temperature/any_event:1/symptoms|other" to "xxx",
                "forms_demo/vitals/body_temperature/any_event:1/body_exposure" to "at0033",
                "forms_demo/vitals/body_temperature/site_of_measurement" to "at0.60"
            ), context
        )
        assertThat(secondComposition).isNotNull

        val secodFlatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secodFlatMap).contains(
            entry("forms_demo/vitals/body_temperature:0/any_event:0/symptoms|code", "at0.64"),
            entry("forms_demo/vitals/body_temperature:0/any_event:1/symptoms|other", "xxx")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOccurencesBug() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Breast pre-operative conference report.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "pre-op_conference_report/context/report_name" to "at0.0.22",
                "pre-op_conference_report/context/episode_code" to "15-08062009",
                "pre-op_conference_report/context/round_number" to "1",
                "pre-op_conference_report/context/start_time" to DateTime(),
                "pre-op_conference_report/context/report_id" to "ASM_691",
                "pre-op_conference_report/context/breast_location:0/specific_location/lesion_unique_number" to "1",
                "pre-op_conference_report/context/breast_location:0/specific_location/side" to "at0004",
                "pre-op_conference_report/context/breast_location:0/specific_location/breast_compass_position" to "2",
                "pre-op_conference_report/context/breast_location:1/specific_location/lesion_unique_number" to "2",
                "pre-op_conference_report/context/breast_location:1/specific_location/side" to "at0003",
                "pre-op_conference_report/context/breast_location:1/specific_location/breast_compass_position" to "3",
                "pre-op_conference_report/breast_pre-op_conclusion_tmds/pre_operative_conclusion" to "at0005"
            ), context
        )
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOrdinalLanguage() {
        val webTemplate =
            WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Simple Vital Functions.opt"), WebTemplateBuilderContext("en", setOf("en", "sl")))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "simple_vital_functions/story_or_history/pain/observed_current_intensity/degree" to "at0169"
            ),
            context
        )
        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(entry("simple_vital_functions/story_or_history/pain/observed_current_intensity/degree|value", "Nepomemben"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testUndefinedItems() {
        val webTemplate =
            WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Simple Body Observation.opt"), WebTemplateBuilderContext("en", setOf("en", "sl")))
        val node: WebTemplateNode? = try {
            webTemplate.findWebTemplateNode("simple_body_observation/simptomi_bolečine/pain/location/location_in_body/items")
        } catch (_: UnknownPathBuilderException) {
            null
        }
        assertThat(node).isNull()
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testWebTemplateBuilder() {
        buildAndExport("/convert/templates/Testing3.opt", "testing3")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing3.opt"), WebTemplateBuilderContext("en", setOf("en", "sl")))

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/quantity")
        assertThat(node.inputs).hasSize(2)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDateDefaultValue() {
        buildAndExport("/convert/templates/Testing3.opt", "testing3")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing3.opt"), WebTemplateBuilderContext("en", setOf("en", "sl")))

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/date")
        assertThat(node.inputs).hasSize(1)

        val localDate = LocalDate(2013, 1, 1)
        assertThat(node.getInput()?.defaultValue).isEqualTo(localDate)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testTimeDefaultValue() {
        buildAndExport("/convert/templates/Testing3.opt", "testing3")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing3.opt"), WebTemplateBuilderContext("en", setOf("en", "sl")))

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/time")
        assertThat(node.inputs).hasSize(1)

        val localTime = LocalTime(20, 13)
        assertThat(node.getInput()?.defaultValue).isEqualTo(localTime)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDischargeSummary() {
        buildAndExport("/convert/templates/Discharge plan.opt", "dischargeplan", "en", setOf("sl", "en"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAqlNodeNames() {
        val webTemplate =
            WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ZN - Vital Functions Encounter.opt"), WebTemplateBuilderContext("en", setOf("en", "sl")))

        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("/context/context_detail")
        assertThat(firstNode.path).isEqualTo("/context/other_context[at0001]/items[openEHR-EHR-CLUSTER.composition_context_detail.v1]")

        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("/vital_signs/body_temperature")
        assertThat(secondNode.path).isEqualTo("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Vital signs']/items[openEHR-EHR-OBSERVATION.body_temperature-zn.v1]")

        val thirdNode: WebTemplateNode = webTemplate.findWebTemplateNode("vital_functions/vital_signs/body_temperature")
        assertThat(thirdNode.path).isEqualTo("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Vital signs']/items[openEHR-EHR-OBSERVATION.body_temperature-zn.v1]")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMissingUnits() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/inference_engine_result_set3.opt"), WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "inference_engine_result_set_composition/inference_engine_result_set_observation/inference_engine_result_set/result/likelihood|unit" to "%",
                "inference_engine_result_set_composition/inference_engine_result_set_observation/inference_engine_result_set/result/likelihood" to "0.1",
                "inference_engine_result_set_composition/inference_engine_result_set_observation/inference_engine_result_set/result/disease_code" to "R81"
            ),
            context
        )

        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLabelsOnXoredDataValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ZN - Diabetes monthly check-up.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("diabetes_monthly_check-up/assessment_of_diabetes_symptoms/hipoglycemia/value")
        assertThat(node.localizedName).isEqualTo("Hipoglycemia")
        assertThat(node.localizedNames).isNotEmpty

        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("diabetes_monthly_check-up/assessment_of_diabetes_symptoms/hipoglycemia/value2")
        assertThat(secondNode.localizedName).isEqualTo("Hipoglycemia")
        assertThat(secondNode.localizedNames).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIdsOnXoredDataValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Thyroid Examination Encounter.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))

        val node: WebTemplateNode =
            webTemplate.findWebTemplateNode("thyroid_examination_encounter/thyroid_examination_findings/thyroid_palpation_findings/thyroid_mass_finding/location_of_mass/coordinates/x_offset")
        assertThat(node.children[0].jsonId).isEqualTo("quantity_value")
        assertThat(node.children[0].alternativeJsonId).isEqualTo("value")
        assertThat(node.children[1].jsonId).isEqualTo("count_value")
        assertThat(node.children[1].alternativeJsonId).isEqualTo("value2")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOccurencesOnXoredDataValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Thyroid Examination Encounter.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))

        val node: WebTemplateNode =
            webTemplate.findWebTemplateNode("thyroid_examination_encounter/thyroid_examination_findings/thyroid_palpation_findings/thyroid_mass_finding/location_of_mass/coordinates/x_offset")
        assertThat(node.children[0].occurences?.min).isEqualTo(0)
        assertThat(node.children[1].occurences?.min).isEqualTo(0)
    }


    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testWebTemplateCompositionOrder() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val jsonNode: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/DemoVitalsComposition.json")) as ObjectNode

        val conversionContext = ConversionContext.create().withValueConvert(LocaleBasedValueConverter(Locale("en", "SI"))).build()
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(jsonNode, conversionContext)
        assertThat(composition!!.content).hasSize(1)
        assertThat(composition.content[0]).isInstanceOf(Section::class.java)

        val section = composition.content[0] as Section
        assertThat(section.items).hasSize(2)
        assertThat(section.items[0]).isInstanceOf(Observation::class.java)
        assertThat(section.items[1]).isInstanceOf(Observation::class.java)

        val firstObservation = section.items[0] as Observation
        val secondObservation = section.items[1] as Observation
        assertThat(firstObservation.archetypeNodeId).contains("lab_test-hba1c")
        assertThat(secondObservation.archetypeNodeId).contains("body_temperature")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testRetrievedXoredDataValues() {
        val composition = getComposition("/convert/compositions/Clinical Notes Report.xml")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Clinical Notes Report.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))

        val flatMap: MutableMap<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(entry("clinical_notes_report/clinical_notes/clinical_synopses/synopsis/text_value", "matija je tukaj1"))
        flatMap.remove("clinical_notes_report/clinical_notes/clinical_synopses/synopsis/text_value")
        flatMap["clinical_notes_report/clinical_notes/clinical_synopses/synopsis/parsable_value"] = "html text"
        flatMap["clinical_notes_report/clinical_notes/clinical_synopses/synopsis/parsable_value|formalism"] = "text/plain"

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val secondFlatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(webTemplate.convertFromFlatToRaw<Composition>(flatMap, context)!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("clinical_notes_report/clinical_notes/clinical_synopses/synopsis/parsable_value", "html text"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testProportion() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ICU -Ventilator device Report.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/i_e_inspiration_expiration")
        assertThat(node.inputs).hasSize(2)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testRuntimeNameConstraints() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ICU -Ventilator device Report.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/cpap")
        assertThat(firstNode.getInput()).isNotNull
        assertThat(firstNode.nodeId).isEqualTo("at0015")

        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep")
        assertThat(secondNode.getInput()).isNotNull
        assertThat(secondNode.nodeId).isEqualTo("at0015")

        val thirdNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap")
        assertThat(thirdNode.getInput()).isNotNull
        assertThat(thirdNode.nodeId).isEqualTo("at0015")

        val fourthNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap")
        assertThat(fourthNode.getInput()).isNotNull
        assertThat(fourthNode.nodeId).isEqualTo("at0015")

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap|magnitude" to 101.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap|unit" to "mbar",
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap|magnitude" to 102.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap|unit" to "mbar",
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep|magnitude" to 103.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep|unit" to "mbar",
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/map_-_mean_airway_pressure_central_pressure|magnitude" to 104.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/map_-_mean_airway_pressure_central_pressure|unit" to "mbar"
            ),
            context
        )

        val extractor =
            SimplePathValueExtractor("﻿/content[openEHR-EHR-SECTION.adhoc.v1 and name/value='NBP840']/items[openEHR-EHR-OBSERVATION.ventilator_vital_signs.v1 and name/value='NBP840  observtions']/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.ventilator_settings2.v1]/items[at0015]")
        val values = extractor.getValue(composition)
        assertThat(values).hasSize(4)

        val thirdElement = values[0] as Element
        assertThat(thirdElement.name!!.value).isEqualTo("EPAP")

        val secondElement = values[1] as Element
        assertThat(secondElement.name!!.value).isEqualTo("IPAP")

        val firstElement = values[2] as Element
        assertThat(firstElement.name!!.value).isEqualTo("PEEP")

        val fourthElement = values[3] as Element
        assertThat(fourthElement.name!!.value).isEqualTo("MAP - Mean airway pressure / central pressure")

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap:0|magnitude", 101.0),
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap:0|magnitude", 102.0),
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep:0|magnitude", 103.0),
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/map_-_mean_airway_pressure_central_pressure:0|magnitude", 104.0)
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testRuntimeNameConstraintsWT() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ICU -Ventilator device Report.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/cpap")
        assertThat(firstNode.getInput()).isNotNull
        assertThat(firstNode.nodeId).isEqualTo("at0015")

        val second: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep")
        assertThat(second.getInput()).isNotNull
        assertThat(second.nodeId).isEqualTo("at0015")

        val thirdNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap")
        assertThat(thirdNode.getInput()).isNotNull
        assertThat(thirdNode.nodeId).isEqualTo("at0015")

        val fourthNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap")
        assertThat(fourthNode.getInput()).isNotNull
        assertThat(fourthNode.nodeId).isEqualTo("at0015")

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap|magnitude" to 101.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap|unit" to "mbar",
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap|magnitude" to 102.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap|unit" to "mbar",
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep|magnitude" to 103.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep|unit" to "mbar",
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/map_-_mean_airway_pressure_central_pressure|magnitude" to 104.0,
                "ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/map_-_mean_airway_pressure_central_pressure|unit" to "mbar"
            ), context
        )

        val extractor =
            SimplePathValueExtractor("﻿/content[openEHR-EHR-SECTION.adhoc.v1 and name/value='NBP840']/items[openEHR-EHR-OBSERVATION.ventilator_vital_signs.v1 and name/value='NBP840  observtions']/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.ventilator_settings2.v1]/items[at0015]")
        val values = extractor.getValue(composition)
        assertThat(values).hasSize(4)

        val thirdElement = values[0] as Element
        assertThat(thirdElement.name!!.value).isEqualTo("EPAP")

        val secondElement = values[1] as Element
        assertThat(secondElement.name!!.value).isEqualTo("IPAP")

        val firstElement = values[2] as Element
        assertThat(firstElement.name!!.value).isEqualTo("PEEP")

        val fourthElement = values[3] as Element
        assertThat(fourthElement.name!!.value).isEqualTo("MAP - Mean airway pressure / central pressure")

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/epap:0|magnitude", 101.0),
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/ipap:0|magnitude", 102.0),
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peep:0|magnitude", 103.0),
            entry("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/map_-_mean_airway_pressure_central_pressure:0|magnitude", 104.0)
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMissingOpenehrCodedValue() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Diabetes Encounter.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        assertThat(webTemplate).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDvTextListOfValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("vitals/vitals/haemoglobin_a1c/any_event/diagnostic_service")
        assertThat(node.getInput()?.list).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDvTextListOfValuesFixedValue() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("vitals/vitals/body_temperature:0/any_event:0/description_of_thermal_stress")
        assertThat(node.getInput()?.fixed).isTrue
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "vitals/vitals/body_temperature/any_event/temperature|magnitude" to 39.1,
                "vitals/vitals/body_temperature/any_event/temperature|unit" to "°C",
                "vitals/vitals/body_temperature/any_event/body_exposure" to "at0031"
            ),
            context
        )

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/description_of_thermal_stress", "Fixed value"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testProportionFieldType() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Vital Signs.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val composition = getComposition("/convert/compositions/proportion.xml")
        val jsonNode: ObjectNode = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create()) as ObjectNode
        assertThat(
            jsonNode.path("vital_signs")
                .path("indirect_oximetry")
                .path(0).path("spo2")
                .path(0).path("|numerator")
                .floatValue()
        ).isEqualTo(79.21f)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDurationValidation() {
        val webTemplate =
            WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Patient Diagnosis (composition).xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val jsonString = """{
                                "ctx/composer_name":"Dr Louise Jones",
                                "ctx/health_care_facility|id":"9091",
                                "ctx/health_care_facility|name":"St James Hospital, Leeds",
                                "ctx/id_namespace":"NHSEngland",
                                "ctx/id_scheme":"NhsNumber",
                                "ctx/language":"en",
                                "ctx/territory":"GB",
                                "oncology_diagnosis":{
                                   "problem_diagnosis":[
                                      {
                                         "age_at_onset":[
                                            "P3Y6M4DT12H30M5S"
                                         ],
                                         "body_site":[
                                            "Left Breast"
                                         ],
                                         "description":[
                                            "Description 75"
                                         ],
                                         "problem_diagnosis":[
                                            "Breast Cancer"
                                         ]
                                      }
                                   ]
                                }
                         }""".trimIndent()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(jsonString) as ObjectNode, ConversionContext.create().build())
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIdNamespace() {
        val builderContext = WebTemplateBuilderContext("en", listOf("en", "sl"))
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Patient Diagnosis (composition).xml"), builderContext)
        val jsonString = """{
                                "ctx/composer_name":"Dr Louise Jones",
                                "ctx/health_care_facility|id":"9091",
                                "ctx/health_care_facility|name":"St James Hospital, Leeds",
                                "ctx/id_namespace":"NHSEngland",
                                "ctx/id_scheme":"NhsNumber",
                                "ctx/language":"en",
                                "ctx/territory":"GB",
                                "oncology_diagnosis":{
                                   "problem_diagnosis":[
                                      {
                                         "age_at_onset":[
                                            "P3Y6M4DT12H30M5S"
                                         ],
                                         "body_site":[
                                            "Left Breast"
                                         ],
                                         "description":[
                                            "Description 75"
                                         ],
                                         "problem_diagnosis":[
                                            "Breast Cancer"
                                         ]
                                      }
                                   ]
                                }
                         }""".trimIndent()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(jsonString) as ObjectNode, ConversionContext.create().build())
        assertThat(composition?.context?.healthCareFacility?.externalRef?.namespace).isEqualTo("NHSEngland")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOrdinalWithNoOptions() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ZN - APACHE.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        assertThat(webTemplate).isNotNull
    }


    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAnyEventWithIntervalEvent() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Test Template.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "test_composition/blood_pressure/any_event/systolic|magnitude" to 120.0,
                "test_composition/blood_pressure/any_event/systolic|unit" to "mm[Hg]"
            ),
            context
        )
        assertThat(firstComposition).isNotNull

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "test_composition/blood_pressure/a24_hour_average/systolic|magnitude" to 120.0,
                "test_composition/blood_pressure/a24_hour_average/systolic|unit" to "mm[Hg]"
            ),
            context
        )
        assertThat(secondComposition).isNotNull
    }


    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPatientSummary() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Patient Summary.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode =
            webTemplate.findWebTemplateNode("ppop_national_patient_summary/a1._allergies_and_other_adverse_reactions_section/adverse_reaction/a1.7_8_allergen")
        assertThat(node.getInput()?.listOpen).isTrue
    }


    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testPartialDate() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N5.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing:0/testing:0/partial_date")
        assertThat(node.getInput()?.validation?.pattern).isEqualTo("yyyy-mm-??")

        val context = ConversionContext.create().withTerritory("SI").withLanguage("en").withComposerName("Test").build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(mapOf("test_encounter/testing:0/testing:0/partial_date" to "2016-01"), context)
        assertThat(webTemplate.convertFromRawToFlat(firstComposition!!, FromRawConversion.create())["test_encounter/testing:0/testing:0/partial_date"]).isEqualTo(
            YearMonth.of(
                2016,
                1
            )
        )

        val secondComposition: Composition? =
            webTemplate.convertFromFlatToRaw(mapOf("test_encounter/testing:0/testing:0/partial_date" to java.time.LocalDate.of(2016, 1, 1)), context)
        assertThat(webTemplate.convertFromRawToFlat(secondComposition!!, FromRawConversion.create())["test_encounter/testing:0/testing:0/partial_date"]).isEqualTo(
            java.time.LocalDate.of(2016, 1, 1)
        )
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testInvalidPartialDate() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N5.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing:0/testing:0/partial_date")
        assertThat(node.getInput()?.validation?.pattern).isEqualTo("yyyy-mm-??")

        val context = ConversionContext.create().withTerritory("SI").withLanguage("en").withComposerName("Test").build()
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(mapOf("test_encounter/testing:0/testing:0/partial_date" to "2016-13"), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Error processing value \"2016-13\" for pattern \"yyyy-mm-??\" (path: test_encounter/testing:0/testing:0/partial_date).")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondInvalidPartialDate() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N5.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing:0/testing:0/partial_date")
        assertThat(node.getInput()?.validation?.pattern).isEqualTo("yyyy-mm-??")

        val context = ConversionContext.create().withTerritory("SI").withLanguage("en").withComposerName("Test").build()
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(mapOf("test_encounter/testing:0/testing:0/partial_date" to "z2016-12"), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Error processing value \"z2016-12\" for pattern \"yyyy-mm-??\" (path: test_encounter/testing:0/testing:0/partial_date).")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testPartialDateXX() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N6.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing:0/testing:0/partial_date")
        assertThat(node.getInput()?.validation?.pattern).isEqualTo("yyyy-??-XX")

        val context = ConversionContext.create().withTerritory("SI").withLanguage("en").withComposerName("Test").build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(mapOf("test_encounter/testing:0/testing:0/partial_date" to "2016-01"), context)
        assertThat(webTemplate.convertFromRawToFlat(firstComposition!!, FromRawConversion.create())["test_encounter/testing:0/testing:0/partial_date"]).isEqualTo(
            YearMonth.of(
                2016,
                1
            )
        )

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(mapOf("test_encounter/testing:0/testing:0/partial_date" to "2016-12-01"), context)
        assertThat(
            webTemplate.convertFromRawToFlat(
                secondComposition!!,
                FromRawConversion.create()
            )["test_encounter/testing:0/testing:0/partial_date"]
        ).isEqualTo(YearMonth.of(2016, 12))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testInvalidPartialDateXX1() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N6.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))

        val context = ConversionContext.create().withTerritory("SI").withLanguage("en").withComposerName("Test").build()
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(mapOf("test_encounter/testing:0/testing:0/partial_date" to "2016-13"), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Error processing value \"2016-13\" for pattern \"yyyy-??-XX\" (path: test_encounter/testing:0/testing:0/partial_date).")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testMissingValue() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/clinical-summary-events2.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))

        val context = ConversionContext.create().withTerritory("SI").withLanguage("en").withComposerName("Test").build()

        val composition = getComposition("/convert/compositions/clinical-summary-events.xml")
        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())

        val node: WebTemplateNode =
            webTemplate.findWebTemplateNode("clinical_summary_events/laboratory_exams/laboratory_exams_results/pathology_test_result:1/any_event:1/result_group/result/result_value/quantity_value")
        assertThat(node).isNotNull

        assertThat(flatMap["clinical_summary_events/laboratory_exams/laboratory_exams_results/pathology_test_result:1/any_event:1/result_group/result/result_value/quantity_value|magnitude"]).isNotNull

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, context)
        val pathValueExtractor =
            NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-SECTION.adhoc_ubr.v1,'Laboratory exams']/items[openEHR-EHR-SECTION.adhoc_ubr.v1,'Laboratory exams results']/items[openEHR-EHR-OBSERVATION.pathology_test-ubr.v1, 'Pathology Test Result #2']/data[at0001]/events[at0002, 'Any event #2']/data[at0003]/items[at0095,'Result Group']/items[at0096,'Result']/items[at0078]")
        val values = pathValueExtractor.getValue(secondComposition)
        assertThat(values[0]).isInstanceOf(Element::class.java)
        val element = values[0] as Element
        assertThat(element.name!!.value).isEqualTo("Result Value")
        assertThat(element.value).isInstanceOf(DvQuantity::class.java)
        val quantity = element.value as DvQuantity?
        assertThat(quantity!!.magnitude).isEqualTo(7.6)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIntervalEventWidth() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Liver Donor.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val jsonString = getJson("/convert/compositions/Liver Donor.json")
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(jsonString) as ObjectNode, ConversionContext.create().build())
        assertThat(composition).isNotNull

    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testStructuredNoPipes() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Liver Recipient Information.xml"), WebTemplateBuilderContext("en", listOf("en")))
        val jsonString = getJson("/convert/compositions/Liver Recipient Information.json")
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(jsonString) as ObjectNode, ConversionContext.create().build())
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testTermMappings() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals term mapping.opt"), WebTemplateBuilderContext("en", listOf("en")))
        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("composer")
            .addTermBindingTerminology("*")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "vitals/vitals/body_temperature/any_event/temperature|magnitude" to 39.1,
                "vitals/vitals/body_temperature/any_event/temperature|unit" to "°C",
                "vitals/vitals/body_temperature/any_event/symptoms" to "at0.64",
                "vitals/vitals/body_temperature/any_event/body_exposure" to "at0031"
            ),
            context
        )

        val pathValueExtractor =
            NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Vitals']/items[openEHR-EHR-OBSERVATION.body_temperature-zn.v1]/data[at0002]/events[at0003]/data[at0001]/items[at0.63]/value")
        val value = pathValueExtractor.getValue(composition)
        assertThat(value).hasSize(1)
        assertThat(value[0]).isInstanceOf(DvCodedText::class.java)

        val codedText = value[0] as DvCodedText
        assertThat(codedText.mappings).extracting("match").contains("=")
        assertThat(codedText.mappings).extracting("target").extracting("terminologyId").extracting("value").contains("LNC205")
        assertThat(codedText.mappings).extracting("target").extracting("codeString").contains("1111")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAdl20() {
        val builderContext = WebTemplateBuilderContext("en", listOf("en", "sl"))
        assertThat(WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Diabetes Check-up.opt"), builderContext))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testServerError() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Adverse Reaction List.v1.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val values: Map<String, Any> = mapOf(
            Pair("ctx/language", "en"),
            Pair("ctx/territory", "GB"),
            Pair("adverse_reaction_list/composer|name", "Dominic Slatford"),
            Pair("adverse_reaction_list/allergies_and_adverse_reactions/adverse_reaction_risk:0/causative_agent", "Nuts"),
            Pair("adverse_reaction_list/allergies_and_adverse_reactions/adverse_reaction_risk:0/status|value", "E.40"),
            Pair("adverse_reaction_list/allergies_and_adverse_reactions/adverse_reaction_risk:0/status/defining_code", "Likely"),
            Pair("adverse_reaction_list/allergies_and_adverse_reactions/adverse_reaction_risk:0/reaction_details/manifestation:0", "Somthing might happen"),
            Pair("adverse_reaction_list/allergies_and_adverse_reactions/adverse_reaction_risk:0/reaction_details/record_provenance/information_source", "Patient"),
            Pair("adverse_reaction_list/allergies_and_adverse_reactions/adverse_reaction_risk:0/last_updated", "2018-02-13T11:52:41.8090137+00:00")
        )

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(values, ConversionContext.create().build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageContaining("Invalid code 'Likely' (path: adverse_reaction_list/allergies_and_adverse_reactions/adverse_reaction_risk:0/status/defining_code).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCytologyIssue() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Cytology Report.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: ObjectNode = getObjectMapper().readValue(getJson("/convert/compositions/cytology.json"), ObjectNode::class.java)
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, ConversionContext.create().build())
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testHistoryFixedOffset() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Apgar_1.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: ObjectNode = getObjectMapper().readValue(getJson("/convert/compositions/apgar_composition.json"), ObjectNode::class.java)
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, ConversionContext.create().build())
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testHistoryFixedOffsetInvalidContent() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Apgar_1.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: ObjectNode = getObjectMapper().readValue(getJson("/convert/compositions/apgar_composition_with_invalidtimes.json"), ObjectNode::class.java)
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, ConversionContext.create().build())
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDurationDefaultValues() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Temperature.opt"), WebTemplateBuilderContext("en", listOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("temperature/body_temperature/any_event/duration")
        assertThat(node.getInput("year")?.defaultValue).isNull()
        assertThat(node.getInput("month")?.defaultValue).isNull()
        assertThat(node.getInput("day")?.defaultValue).isNull()
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun templateWithTypedRmType() {
        val webTemplate = WebTemplateBuilder.build(getTemplate("/convert/templates/KDS_Laborbericht.opt"), WebTemplateBuilderContext("en"))!!
        val aqlPathToOffendingNode =
            "/content[openEHR-EHR-OBSERVATION.laboratory_test_result.v1,'Laborbefund']/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.specimen.v1,'Probenmaterial']/items[at0015]/value"
        val node = webTemplate.findWebTemplateNodeByAqlPath(aqlPathToOffendingNode)
        assertThat(node).isNotNull
        assertThat(node.rmType).isEqualTo("DV_DATE_TIME")
        assertThat(node.children).isEmpty()
    }

    @Throws(JAXBException::class, IOException::class)
    private fun buildAndExport(templateName: String, prefix: String) {
        buildAndExport(templateName, prefix, "sl", setOf("sl", "en"))
    }
}
