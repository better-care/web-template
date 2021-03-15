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

import care.better.openehr.rm.RmObject
import care.better.platform.path.NameAndNodeMatchingPathValueExtractor
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.joda.JodaModule
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.converter.value.SimpleValueConverter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Action
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.datastructures.Cluster
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class RmObjectTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateActionFlat() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/OPENeP - Medication Administration.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val actionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/medication-administration-action-flat.json"), object : TypeReference<Map<String, Any>>() {})
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/medication-administration-composition-flat.json"), object : TypeReference<Map<String, Any>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ACTION.medication.v1]")
        val compositionAction = extractor.getValue(composition).iterator().next() as Action

        test(
            webTemplate.convertFromFlatToRaw(actionMap, ConversionContext.createForAqlPath("/content[openEHR-EHR-ACTION.medication.v1]").build()),
            compositionAction)

        test(
            webTemplate.convertFromFlatToRaw(actionMap, ConversionContext.createForWebTemplatePath("medication_administration/medication_management").build()),
            compositionAction)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateActionStructured() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/OPENeP - Medication Administration.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val structuredAction: ObjectNode = mapper.readValue(getJson("/convert/compositions/rmobject/medication-administration-action-structured.json"), ObjectNode::class.java)
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/medication-administration-composition-flat.json"), object : TypeReference<Map<String, Any>>(){})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())

        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ACTION.medication.v1]")
        val compositionAction = extractor.getValue(composition).iterator().next() as Action

        test(
            webTemplate.convertFromStructuredToRaw(structuredAction, ConversionContext.createForAqlPath("/content[openEHR-EHR-ACTION.medication.v1]").build()),
            compositionAction)
        test(
            webTemplate.convertFromStructuredToRaw(structuredAction, ConversionContext.createForWebTemplatePath("medication_administration/medication_management").build()),
            compositionAction)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateObservationFlat() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val observationMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-observation-flat.json"), object : TypeReference<Map<String, Any>>() {})
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-composition-flat.json"), object : TypeReference<Map<String, Any>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())
        val extractor = NameAndNodeMatchingPathValueExtractor("content[openEHR-EHR-OBSERVATION.lab_test.v1]")
        val compositionObservation = extractor.getValue(composition).iterator().next() as Observation

        val observationFromAqlPath = webTemplate.convertFromFlatToRaw<Observation>(
            observationMap,
            ConversionContext.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]").build())

        val observationFromWebTemplatePath = webTemplate.convertFromFlatToRaw<Observation>(
            observationMap,
            ConversionContext.createForWebTemplatePath("laboratory_report/laboratory_test_result").build())

        test(
            observationFromAqlPath,
            compositionObservation)
        test(
            observationFromWebTemplatePath,
            compositionObservation)

        val observationNode = webTemplate.findWebTemplateNode("laboratory_report/laboratory_test_result")
        
        val retrievedObservationMapFromAqlPath = webTemplate.convertFromRawToFlat(
            observationFromAqlPath!!,
            FromRawConversion.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]"))

        assertThat(retrievedObservationMapFromAqlPath.all { it.key.startsWith(observationNode.jsonId) }).isTrue()

        val retrievedObservationMapFromWebTemplatePath = webTemplate.convertFromRawToFlat(
            observationFromWebTemplatePath!!,
            FromRawConversion.createForWebTemplatePath("laboratory_report/laboratory_test_result"))

        assertThat(retrievedObservationMapFromWebTemplatePath.all { it.key.startsWith(observationNode.jsonId) }).isTrue()

        val retrievedObservationJsonNodeFromAqlPath = webTemplate.convertFromRawToStructured(
            observationFromAqlPath,
            FromRawConversion.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]"))

        assertThat(retrievedObservationJsonNodeFromAqlPath!!.has(observationNode.jsonId)).isTrue()

        val retrievedObservationJsonNodeFromWebTemplatePath = webTemplate.convertFromRawToStructured(
            observationFromWebTemplatePath,
            FromRawConversion.createForWebTemplatePath("laboratory_report/laboratory_test_result"))

        assertThat(retrievedObservationJsonNodeFromWebTemplatePath!!.has(observationNode.jsonId)).isTrue()

    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateObservationStructured() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val structuredObservation: ObjectNode = mapper.readValue(getJson("/convert/compositions/rmobject/lab-observation-structured.json"), ObjectNode::class.java)
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-composition-flat.json"), object : TypeReference<Map<String, Any>>() {})
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())

        val extractor = NameAndNodeMatchingPathValueExtractor("content[openEHR-EHR-OBSERVATION.lab_test.v1]")
        val compositionObservation = extractor.getValue(composition).iterator().next() as Observation

        test(
            webTemplate.convertFromStructuredToRaw(structuredObservation, ConversionContext.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]").build()),
            compositionObservation)
        test(
            webTemplate.convertFromStructuredToRaw(structuredObservation, ConversionContext.createForWebTemplatePath("laboratory_report/laboratory_test_result").build()),
            compositionObservation)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateObservationFlatFromRoot() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = ObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val observationMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-observation-flat-prefixed.json"), object : TypeReference<Map<String, Any>>() {})
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-composition-flat.json"), object : TypeReference<Map<String, Any>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())

        val extractor = NameAndNodeMatchingPathValueExtractor("content[openEHR-EHR-OBSERVATION.lab_test.v1]")
        val compositionObservation = extractor.getValue(composition).iterator().next() as Observation

        test(
            webTemplate.convertFromFlatToRaw(observationMap, ConversionContext.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]").build()),
            compositionObservation)
        test(
            webTemplate.convertFromFlatToRaw(observationMap, ConversionContext.createForWebTemplatePath("laboratory_report/laboratory_test_result").build()),
            compositionObservation)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateObservationStructuredFromRoot() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val observationMap: ObjectNode = mapper.readValue(getJson("/convert/compositions/rmobject/lab-observation-structured-prefixed.json"), ObjectNode::class.java)
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-composition-flat.json"), object : TypeReference<Map<String, Any>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())

        val extractor = NameAndNodeMatchingPathValueExtractor("content[openEHR-EHR-OBSERVATION.lab_test.v1]")
        val compositionObservation = extractor.getValue(composition).iterator().next() as Observation

        test(
            webTemplate.convertFromStructuredToRaw(observationMap, ConversionContext.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]").build()),
            compositionObservation)
        test(
            webTemplate.convertFromStructuredToRaw(observationMap, ConversionContext.createForWebTemplatePath("laboratory_report/laboratory_test_result").build()),
            compositionObservation)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateClusterFlat() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val clusterMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-cluster-flat.json"), object : TypeReference<Map<String, Any>>() {})
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-composition-flat.json"), object : TypeReference<Map<String, Any>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())

        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-OBSERVATION.lab_test.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0095]/items[at0096]")
        val compositionCluster = extractor.getValue(composition).iterator().next() as Cluster

        test(
            webTemplate.convertFromFlatToRaw(
                clusterMap,
                ConversionContext.createForAqlPath( "/content[openEHR-EHR-OBSERVATION.lab_test.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0095]/items[at0096]").build()),
            compositionCluster)
        test(
            webTemplate.convertFromFlatToRaw(
                clusterMap,
                ConversionContext.createForWebTemplatePath("laboratory_report/laboratory_test_result/any_event/result_group/result").build()),
            compositionCluster)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateClusterStructured() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val clusterMap: ObjectNode = mapper.readValue(getJson("/convert/compositions/rmobject/lab-cluster-structured.json"), ObjectNode::class.java)
        val compositionMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-composition-flat.json"), object : TypeReference<Map<String, Any>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())

        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-OBSERVATION.lab_test.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0095]/items[at0096]")
        val compositionCluster = extractor.getValue(composition).iterator().next() as Cluster

        test(
            webTemplate.convertFromStructuredToRaw(
                clusterMap,
                ConversionContext.createForAqlPath( "/content[openEHR-EHR-OBSERVATION.lab_test.v1]/data[at0001]/events[at0002]/data[at0003]/items[at0095]/items[at0096]").build()),
            compositionCluster)
        test(
            webTemplate.convertFromStructuredToRaw(
                clusterMap,
                ConversionContext.createForWebTemplatePath("laboratory_report/laboratory_test_result/any_event/result_group/result").build()),
            compositionCluster)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateObservationCollection() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val observationMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-observation-multi-flat.json"), object : TypeReference<Map<String, Any>>() {})
        assertThatThrownBy {
            webTemplate.convertFromFlatToRaw<Observation>(
                observationMap,
                ConversionContext.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]").build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Expecting to convert single RM object, but multiple were provided.")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreateObservationPrefixedCollection() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/LAB - Laboratory Test Report.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val observationMap: Map<String, Any> = mapper.readValue(getJson("/convert/compositions/rmobject/lab-observation-multi-flat-prefixed.json"), object : TypeReference<Map<String, Any>>() {})
        assertThatThrownBy {
            webTemplate.convertFromFlatToRaw<Observation>(
                observationMap,
                ConversionContext.createForAqlPath("content[openEHR-EHR-OBSERVATION.lab_test.v1]").build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Expecting to convert single RM object, but multiple were provided. (path: laboratory_test_result).")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFlatToStructuredConversion() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/rmobject/NEWS2.opt"), builderContext)

        val compositionMap: MutableMap<String, Any> = linkedMapOf()
        compositionMap["news2_uk/news2_score/total_score"] = 8
        compositionMap["news2_uk/news2_score/total_score|normal_status"] = "N"

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionMap, ConversionContext.create().build())

        val jsonNode: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(jsonNode!!.path("news2_uk").path("news2_score").path(0).path("total_score").path(0)[""].asLong()).isEqualTo(8L)
        assertThat(jsonNode.path("news2_uk").path("news2_score").path(0).path("total_score").path(0)["|normal_status"].asText()).isEqualTo("N")
    }

    private fun <R : RmObject?> test(first: R, second: R) {
        assertThat(first).isNotNull
        assertThat(second).isNotNull
        assertThat(first).usingRecursiveComparison().isEqualTo(second)
    }
}
