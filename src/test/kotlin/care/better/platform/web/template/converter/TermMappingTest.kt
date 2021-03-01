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

import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText
import org.openehr.rm.datatypes.TermMapping
import java.io.IOException
import java.util.*
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class TermMappingTest : AbstractWebTemplateTest() {

    private val webTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/older/Demo Vitals.xml"),
            WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTermMappingToObjectMap() {
        val composition = prepareCompositionWithTm()
        val valueMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(valueMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0|match", "="))
        assertThat(valueMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|terminology", "SNOMED-CT"))
        assertThat(valueMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|code", "21794005"))
        assertThat(valueMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1|match", "="))
        assertThat(valueMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|terminology", "RTX"))
        assertThat(valueMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|code", "W.11.7"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSendMapping() {
        val flatComposition: Map<String, String> = mapOf(
                Pair("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|code", "at0.64"),
                Pair("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0|match", "="),
                Pair("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|terminology", "SNOMED-CT"),
                Pair("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|code", "99302"),
                Pair("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1|match", "="),
                Pair("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|terminology", "RTX"),
                Pair("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|code", "XYZ"))

        val builderContext = ConversionContext.create().withComposerName("User").withLanguage("en").withTerritory("CA").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatComposition, builderContext)
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|code", "at0.64"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|terminology", "local"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|value", "Chills / rigor / shivering"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0|match", "="))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|terminology", "SNOMED-CT"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|code", "99302"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1|match", "="))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|terminology", "RTX"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|code", "XYZ"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSendRetrieveMappingOnDvText() {
        val flatComposition: Map<String, String> = mapOf(
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name|code", "117"),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name|terminology", "mine"),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name|value", "Hello world!"),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name/_mapping:0|match", "="),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name/_mapping:0/target|terminology", "SNOMED-CT"),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name/_mapping:0/target|code", "99302"),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name/_mapping:1|match", "="),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name/_mapping:1/target|terminology", "RTX"),
                Pair("vitals/vitals/haemoglobin_a1c/any_event/test_name/_mapping:1/target|code", "XYZ"))

        val builderContext = ConversionContext.create().withComposerName("User").withLanguage("en").withTerritory("CA").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatComposition, builderContext)
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())

        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name|code", "117"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name|terminology", "mine"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name|value", "Hello world!"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_mapping:0|match", "="))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_mapping:0/target|terminology", "SNOMED-CT"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_mapping:0/target|code", "99302"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_mapping:1|match", "="))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_mapping:1/target|terminology", "RTX"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_mapping:1/target|code", "XYZ"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTermMappingToStringMap() {
        val composition = prepareCompositionWithTm()
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create(Locale.US))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0|match", "="))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|terminology", "SNOMED-CT"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:0/target|code", "21794005"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1|match", "="))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|terminology", "RTX"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0/_mapping:1/target|code", "W.11.7"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTermMappingToJson() {
        val composition = prepareCompositionWithTm()
        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(node).isNotNull
        val mappingNode: JsonNode = node!!.path("vitals")
                .path("vitals")
                .path(0)
                .path("body_temperature")
                .path(0)
                .path("any_event")
                .path(0)
                .path("symptoms")
                .path(0)
                .path("_mapping")

        assertThat(mappingNode.isNull).isFalse
        assertThat(mappingNode.isArray).isTrue

        assertThat(mappingNode.size()).isEqualTo(2)
        assertThat(mappingNode.path(0).path("|match").asText()).isEqualTo("=")
        assertThat(mappingNode.path(0).path("target").path(0).path("|terminology").asText()).isEqualTo("SNOMED-CT")
        assertThat(mappingNode.path(0).path("target").path(0).path("|code").asText()).isEqualTo("21794005")
        assertThat(mappingNode.path(1).path("|match").asText()).isEqualTo("=")
        assertThat(mappingNode.path(1).path("target").path(0).path("|terminology").asText()).isEqualTo("RTX")
        assertThat(mappingNode.path(1).path("target").path(0).path("|code").asText()).isEqualTo("W.11.7")

        val dvTextNode = node.path("vitals")
                .path("vitals")
                .path(0)
                .path("body_temperature")
                .path(0)
                .path("any_event")
                .path(0)
                .path("description_of_thermal_stress")

        assertThat(dvTextNode.isArray).isTrue
        assertThat(dvTextNode.size()).isEqualTo(1)
        assertThat(dvTextNode.path(0).isObject).isTrue
        assertThat(dvTextNode.path(0).path("|value").asText()).isEqualTo("Test description of symptoms Modified With Term. Mapping")

        val dvTextMn: JsonNode = dvTextNode.path(0).path("_mapping")

        assertThat(dvTextMn.isArray).isTrue
        assertThat(dvTextMn.path(0).path("|match").asText()).isEqualTo("=")
        assertThat(dvTextMn.path(0).path("target").path(0).isObject).isTrue
        assertThat(dvTextMn.path(0).path("target").path(0).path("|terminology").asText()).isEqualTo("IAXA")
        assertThat(dvTextMn.path(0).path("target").path(0).path("|code").asText()).isEqualTo("99.1")
        assertThat(dvTextMn.path(0).path("purpose").path(0).isObject).isTrue
        assertThat(dvTextMn.path(0).path("purpose").path(0).path("|terminology").asText()).isEqualTo("Purposes")
        assertThat(dvTextMn.path(0).path("purpose").path(0).path("|code").asText()).isEqualTo("p.0.63.1")
        assertThat(dvTextMn.path(0).path("purpose").path(0).path("|value").asText()).isEqualTo("Purpose 1")
    }

    @Test
    @Throws(IOException::class)
    fun testTermMappingFromJson() {
        val composition = prepareCompositionWithTm()
        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(node).isNotNull
        val mappingNode: JsonNode = node!!.path("vitals")
                .path("vitals")
                .path(0)
                .path("body_temperature")
                .path(0)
                .path("any_event")
                .path(0)
                .path("symptoms")
                .path(0)
                .path("_mapping")

        assertThat(mappingNode.isNull).isFalse

        val context = ConversionContext.create().withComposerName("test_composer").withLanguage("sl").withTerritory("SI").build()

        val convertedComposition: Composition? = webTemplate.convertFromStructuredToRaw(node as ObjectNode, context)

        val section = convertedComposition!!.content[0] as Section
        val observation = section.items[1] as Observation
        val item = observation.data!!.events[0].data as ItemTree?
        val element = item!!.items[1] as Element
        val symptoms = element.value as DvCodedText?
        val termMappings: List<TermMapping> = symptoms!!.mappings

        assertThat(termMappings).hasSize(2)

        assertThat(termMappings[0].match).isEqualTo("=")
        assertThat(termMappings[0].target?.codeString).isEqualTo("21794005")
        assertThat(termMappings[0].target?.terminologyId?.value).isEqualTo("SNOMED-CT")
        assertThat(termMappings[0].purpose).isNull()
        assertThat(termMappings[1].match).isEqualTo("=")
        assertThat(termMappings[1].target?.codeString).isEqualTo("W.11.7")
        assertThat(termMappings[1].target?.terminologyId?.value).isEqualTo("RTX")
        assertThat(termMappings[1].purpose).isNull()

        val stateItem = observation.data!!.events[0].state as ItemTree?
        val stateElement = stateItem!!.items[0] as Element
        val description = stateElement.value as DvText?

        val secondTermMappings = description?.mappings ?: emptyList()
        assertThat(secondTermMappings).hasSize(1)

        assertThat(secondTermMappings[0].match).isEqualTo("=")
        assertThat(secondTermMappings[0].target?.codeString).isEqualTo("99.1")
        assertThat(secondTermMappings[0].target?.terminologyId?.value).isEqualTo("IAXA")
        assertThat(secondTermMappings[0].purpose).isNotNull
        val purpose = secondTermMappings[0].purpose
        assertThat(purpose?.definingCode?.codeString).isEqualTo("p.0.63.1")
        assertThat(purpose?.definingCode?.terminologyId?.value).isEqualTo("Purposes")
        assertThat(purpose?.value).isEqualTo("Purpose 1")
    }

    @Test
    @Throws(IOException::class)
    fun testTermMappingFromJsonNoPurposeValue() {
        val composition = prepareCompositionWithTmNoPurposeValue()
        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(node).isNotNull
        val mappingNode: JsonNode = node!!.path("vitals")
                .path("vitals")
                .path(0)
                .path("body_temperature")
                .path(0)
                .path("any_event")
                .path(0)
                .path("symptoms")
                .path(0)
                .path("_mapping")

        assertThat(mappingNode.isNull).isFalse

        val context = ConversionContext.create().withComposerName("test_composer").withLanguage("sl").withTerritory("SI").build()
        assertThatThrownBy { webTemplate.convertFromStructuredToRaw<Composition>(node as ObjectNode, context) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageStartingWith("Missing DvCodedText.value at vitals/vitals/body_temperature:0/any_event:0/description_of_thermal_stress/_mapping:0/purpose!")
    }

    @Test
    @Throws(IOException::class)
    fun testTermMappingFromMap() {
        val composition = prepareCompositionWithTm()
        val map: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())

        val context = ConversionContext.create().withComposerName("test_composer").withLanguage("sl").withTerritory("SI").build()
        val convertedComposition: Composition? = webTemplate.convertFromFlatToRaw(map, context)

        val section = convertedComposition!!.content[0] as Section
        val observation = section.items[1] as Observation
        val item = observation.data!!.events[0].data as ItemTree?
        val element = item!!.items[1] as Element
        val symptoms = element.value as DvCodedText?

        val termMappings: List<TermMapping> = symptoms?.mappings ?: emptyList()
        assertThat(termMappings).hasSize(2)
        assertThat(termMappings[0].match).isEqualTo("=")
        assertThat(termMappings[0].target?.codeString).isEqualTo("21794005")
        assertThat(termMappings[0].target?.terminologyId?.value).isEqualTo("SNOMED-CT")
        assertThat(termMappings[0].purpose).isNull()

        assertThat(termMappings[1].match).isEqualTo("=")
        assertThat(termMappings[1].target?.codeString).isEqualTo("W.11.7")
        assertThat(termMappings[1].target?.terminologyId?.value).isEqualTo("RTX")
        assertThat(termMappings[1].purpose).isNull()

        val stateItem = observation.data!!.events[0].state as ItemTree?
        val stateElement = stateItem!!.items[0] as Element
        val description = stateElement.value as DvText?

        val secondTermMappings = description?.mappings ?: emptyList()
        assertThat(secondTermMappings).hasSize(1)
        assertThat(secondTermMappings[0].match).isEqualTo("=")
        assertThat(secondTermMappings[0].target?.codeString).isEqualTo("99.1")
        assertThat(secondTermMappings[0].target?.terminologyId?.value).isEqualTo("IAXA")
        assertThat(secondTermMappings[0].purpose).isNotNull
        val purpose = secondTermMappings[0].purpose
        assertThat(purpose?.definingCode?.codeString).isEqualTo("p.0.63.1")
        assertThat(purpose?.definingCode?.terminologyId!!.value).isEqualTo("Purposes")
        assertThat(purpose.value).isEqualTo("Purpose 1")
    }

    @Throws(IOException::class)
    private fun getDemoVitalsComposition(): Composition {
        val structuredComposition = getObjectMapper().readValue(getJson("/convert/compositions/DemoVitalsComposition.json"), ObjectNode::class.java)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("test_composer").build()
        return webTemplate.convertFromStructuredToRaw(structuredComposition, context)!!
    }

    private fun toTermMapping(match: String, terminologyId: String, code: String, purpose: DvCodedText? = null): TermMapping =
            TermMapping().apply {
                this.match = match
                this.target = CodePhrase().apply {
                    this.codeString = code
                    this.terminologyId = TerminologyId().apply { this.value = terminologyId }
                }
                this.purpose = purpose
            }

    @Throws(IOException::class)
    private fun prepareCompositionWithTm(): Composition {
        val composition = getDemoVitalsComposition()
        assertThat(composition).isNotNull

        val section = composition.content[0] as Section
        val observation = section.items[1] as Observation
        val item = observation.data?.events?.get(0)?.data as ItemTree?
        val element = item?.items?.get(1) as Element?
        val stateItem = observation.data?.events?.get(0)?.state as ItemTree?
        val stateElement = stateItem?.items?.get(0) as Element?
        val symptoms = element?.value as DvCodedText?


        stateElement?.also {
            it.value = DvText().apply {
                this.value = "Test description of symptoms Modified With Term. Mapping"
                this.mappings.add(toTermMapping("=", "IAXA", "99.1", DvCodedText.create("Purposes", "p.0.63.1", "Purpose 1")))
            }
        }

        assertThat(symptoms).isNotNull
        assertThat(symptoms?.definingCode?.codeString).isEqualTo("at0.64")
        symptoms?.mappings?.add(toTermMapping("=", "SNOMED-CT", "21794005"))
        symptoms?.mappings?.add(toTermMapping("=", "RTX", "W.11.7"))
        return composition
    }

    @Throws(IOException::class)
    private fun prepareCompositionWithTmNoPurposeValue(): Composition {
        val composition = prepareCompositionWithTm()
        assertThat(composition).isNotNull
        val section = composition.content[0] as Section
        val observation = section.items[1] as Observation
        val stateItem = observation.data?.events?.get(0)?.state as ItemTree?
        val stateElement = stateItem?.items?.get(0) as Element?
        val text = stateElement?.value as DvText?
        text?.mappings?.get(0)?.purpose?.also { it.value = null }
        return composition
    }
}
