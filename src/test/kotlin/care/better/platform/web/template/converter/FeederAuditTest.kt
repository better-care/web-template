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
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.common.FeederAudit
import org.openehr.rm.common.Locatable
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Evaluation
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.DvMultimedia
import org.openehr.rm.datatypes.DvParsable
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class FeederAuditTest : AbstractWebTemplateTest() {

    private val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
        getTemplate("/convert/templates/older/Demo Vitals.xml"),
        WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))

    @Test
    fun testWtToComposition() {
        val composition = buildComposition()
        validateFeederAudit(composition)
    }

    @Test
    fun testWtToCompositionDeep() {
        val composition = buildDeepComposition()
        assertThat(composition).isNotNull
    }

    @Test
    fun testWtToCompositionMultimedia() {
        val composition = buildCompositionWithMultiMedia()
        assertThat(composition).isNotNull

        val feederAudit = getFeederAudit(composition)
        assertThat(feederAudit!!.originalContent).isInstanceOf(DvMultimedia::class.java)

        val originalContent = feederAudit.originalContent as DvMultimedia?
        assertThat(originalContent!!.mediaType!!.codeString).isEqualTo("text/html")
        assertThat(originalContent.uri!!.value).isEqualTo("http://www.marand.com")

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/body_temperature:0/_feeder_audit/original_content_multimedia", "http://www.marand.com"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/original_content_multimedia|mediatype", "text/html"))
    }

    @Test
    fun testCompositionToWtDeep() {
        val composition = buildDeepComposition()
        assertThat(composition).isNotNull

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|id", "123"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|name", "Testing Doctor"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|id_scheme", "seq"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|id_namespace", "kzz"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit|time", "2017-01-31T00:00:00Z")
        )
    }

    @Test
    fun testCompositionToWt() {
        val composition = buildComposition()
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/body_temperature:0/_feeder_audit/original_content", "Hello world!"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/original_content|formalism", "text/plain"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0|assigner", "assigner1"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0|issuer", "issuer1"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0", "id1"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0|type", "PERSON"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1|assigner", "assigner2"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1|issuer", "issuer2"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1", "id2"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1|type", "PERSON"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0|assigner", "assigner1"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0|issuer", "issuer1"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0", "id1"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0|type", "PERSON"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1|assigner", "assigner2"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1|issuer", "issuer2"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1", "id2"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1|type", "PERSON"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit|system_id", "orig"),
            entry("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit|version_id", "vvv"))
    }

    @Test
    @Throws(IOException::class)
    fun testStructuredWithFeederAudit() {
        val jsonNode = getObjectMapper().readTree(getJson("/convert/compositions/DemoVitalsCompositionFeederAudit.json")) as ObjectNode

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(jsonNode, ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/_feeder_audit/original_content", "{\"hello\": \"world\"}"),
            entry("vitals/_feeder_audit/original_content|formalism", "application/json"),
            entry("vitals/_feeder_audit/originating_system_audit|system_id", "mine")
        )
    }

    @Test
    @Throws(IOException::class)
    fun testStructuredWithGenericFields() {
        val jsonNode = getObjectMapper().readTree(getJson("/convert/compositions/DemoVitalsCompositionGenericFields.json")) as ObjectNode

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(jsonNode, ConversionContext.create().build())
        assertThat(composition!!.feederAudit).isNotNull
        assertThat(composition.feederAudit!!.originatingSystemAudit).isNotNull
        assertThat(composition.feederAudit!!.originatingSystemAudit!!.systemId).isEqualTo("FormRenderer")

        val structuredComposition: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(structuredComposition!!.path("ctx").path("generic_fields").isObject).isTrue
        assertThat(structuredComposition.path("ctx").path("generic_fields").path("field1").isArray).isTrue
        assertThat(structuredComposition.path("ctx").path("generic_fields").path("field1").path(0).textValue()).isEqualTo("val1")
        assertThat(structuredComposition.path("ctx").path("generic_fields").path("field1").path(1).textValue()).isEqualTo("val2")
        assertThat(structuredComposition.path("ctx").path("generic_fields").path("field2").path(0).textValue()).isEqualTo("val3")
        assertThat(structuredComposition.path("ctx").path("generic_fields").path("field2").path(1).textValue()).isEqualTo("val4")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testFeederAuditBroken() {
        val flatMap = getObjectMapper().readValue(getJson("/convert/compositions/gel_data.json"), object : TypeReference<Map<String, Any>>(){})
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/GEL Cancer diagnosis input.opt"), WebTemplateBuilderContext("en"))

        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        assertThat(composition).isNotNull

        val pathValueExtractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-EVALUATION.problem_diagnosis.v1]")
        val values = pathValueExtractor.getValue(composition)
        assertThat(values).isNotEmpty

        val value = values[0] as Locatable
        assertThat(value.feederAudit).isNotNull
        assertThat(value.feederAudit!!.originatingSystemAudit!!.systemId).isEqualTo("infoflex")
        assertThat(value.feederAudit!!.originatingSystemAudit!!.time!!.value).isEqualTo("2018-01-01T03:00Z")
    }

    @Test
    fun testFeederAuditOnElement() {
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Clinical course.opt"), WebTemplateBuilderContext("en"))
        val flatMap = getObjectMapper().readValue(getJson("/convert/compositions/clinical_course.json"), object : TypeReference<Map<String, Any>>(){})

        val builderContext = ConversionContext.create().withLanguage("en").withTerritory("SI").build()
        val composition: Composition? =  webTemplate.convertFromFlatToRaw(flatMap, builderContext)
        assertThat(composition).isNotNull

        val webTemplateNode = webTemplate.findWebTemplateNode("clinical_course/meap/assessment_a/clinical_synopsis")
        val extractor = NameAndNodeMatchingPathValueExtractor(webTemplateNode.path)
        val evaluation: Evaluation =  extractor.getValue(composition)[0] as Evaluation
        assertThat(((evaluation.data as ItemTree).items[0] as Element).feederAudit?.originatingSystemAudit?.systemId).isEqualTo("386053000")
    }

    private fun buildDeepComposition(): Composition {
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|id", "123")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|name", "Testing Doctor")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|id_scheme", "seq")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit/location|id_namespace", "kzz")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit|time", "2017-01-31T00:00:00Z")
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", 34.1)
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C")
            .put("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|code", "at0.65")
            .put("vitals/vitals/body_temperature:0/any_event:0/body_exposure|code", "at0033")
            .put("vitals/vitals/body_temperature:0/any_event:0/description_of_thermal_stress", "Description of thermal stress 73")
            .build()

        val builderContext = ConversionContext.create().withLanguage("en").withTerritory("SI").build()
        return webTemplate.convertFromFlatToRaw(values, builderContext)!!
    }

    private fun buildCompositionWithMultiMedia(): Composition {
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("vitals/vitals/body_temperature:0/_feeder_audit/original_content_multimedia|url", "http://www.marand.com")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/original_content_multimedia|mediatype", "text/html")
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", 34.1)
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C")
            .put("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|code", "at0.65")
            .put("vitals/vitals/body_temperature:0/any_event:0/body_exposure|code", "at0033")
            .put("vitals/vitals/body_temperature:0/any_event:0/description_of_thermal_stress", "Description of thermal stress 73")
            .build()

        val builderContext = ConversionContext.create().withLanguage("en").withTerritory("SI").build()
        return webTemplate.convertFromFlatToRaw(values, builderContext)!!
    }

    private fun buildComposition(): Composition {
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("vitals/vitals/body_temperature:0/_feeder_audit/original_content", "Hello world!")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/original_content|formalism", "text/plain")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0|assigner", "assigner1")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0|issuer", "issuer1")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0|id", "id1")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:0|type", "PERSON")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1|assigner", "assigner2")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1|issuer", "issuer2")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1|id", "id2")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_item_id:1|type", "PERSON")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0|assigner", "assigner1")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0|issuer", "issuer1")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0|id", "id1")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:0|type", "PERSON")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1|assigner", "assigner2")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1|issuer", "issuer2")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1|id", "id2")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/feeder_system_item_id:1|type", "PERSON")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit|system_id", "orig")
            .put("vitals/vitals/body_temperature:0/_feeder_audit/originating_system_audit|version_id", "vvv")
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", 34.1)
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C")
            .put("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|code", "at0.65")
            .put("vitals/vitals/body_temperature:0/any_event:0/body_exposure|code", "at0033")
            .put("vitals/vitals/body_temperature:0/any_event:0/description_of_thermal_stress", "Description of thermal stress 73")
            .build()

        val builderContext = ConversionContext.create().withLanguage("en").withTerritory("SI").build()
        return webTemplate.convertFromFlatToRaw(values, builderContext)!!
    }

    private fun validateFeederAudit(composition: Composition) {
        assertThat(composition).isNotNull

        val feederAudit = getFeederAudit(composition)
        assertThat(feederAudit).isNotNull
        assertThat(feederAudit!!.originalContent).isInstanceOf(DvParsable::class.java)

        val originalContent = feederAudit.originalContent as DvParsable?
        assertThat(originalContent!!.value).isEqualTo("Hello world!")
        assertThat(originalContent.formalism).isEqualTo("text/plain")
        assertThat(feederAudit.originatingSystemItemIds).hasSize(2)
        assertThat(feederAudit.originatingSystemItemIds).extracting("assigner").containsExactly("assigner1", "assigner2")
        assertThat(feederAudit.originatingSystemItemIds).extracting("issuer").containsExactly("issuer1", "issuer2")
        assertThat(feederAudit.originatingSystemItemIds).extracting("id").containsExactly("id1", "id2")
        assertThat(feederAudit.originatingSystemItemIds).extracting("type").containsExactly("PERSON", "PERSON")
        assertThat(feederAudit.feederSystemItemIds).hasSize(2)
        assertThat(feederAudit.feederSystemItemIds).extracting("assigner").containsExactly("assigner1", "assigner2")
        assertThat(feederAudit.feederSystemItemIds).extracting("issuer").containsExactly("issuer1", "issuer2")
        assertThat(feederAudit.feederSystemItemIds).extracting("id").containsExactly("id1", "id2")
        assertThat(feederAudit.feederSystemItemIds).extracting("type").containsExactly("PERSON", "PERSON")
        assertThat(feederAudit.originatingSystemAudit!!.systemId).isEqualTo("orig")
        assertThat(feederAudit.originatingSystemAudit!!.versionId).isEqualTo("vvv")
    }

    private fun getFeederAudit(composition: Composition): FeederAudit? {
        val section = composition.content[0] as Section
        val observation = section.items[0] as Observation
        return observation.feederAudit
    }
}
