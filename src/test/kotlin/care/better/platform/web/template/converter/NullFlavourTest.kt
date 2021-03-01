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

import care.better.platform.template.AmNode
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.flat.mapper.ElementToFlatMapper
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.SimpleValueConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvParsable
import org.openehr.rm.datatypes.DvText
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class NullFlavourTest : AbstractWebTemplateTest() {
    @Test
    fun testNullFlavorElementToFLatMap() {
        val flatConversionContext = FlatMappingContext()
        val element = Element().apply { this.nullFlavour = DvCodedText.create("openehr", "271", "no information") }

        ElementToFlatMapper.map(WebTemplateNode(AmNode(null, "ELEMENT"), "ELEMENT", "/"), SimpleValueConverter, element, "id", flatConversionContext)

        assertThat(flatConversionContext.get()).hasSize(3)
        assertThat(flatConversionContext.get()).contains(entry("id/_null_flavour|code", "271"))
        assertThat(flatConversionContext.get()).contains(entry("id/_null_flavour|value", "no information"))
        assertThat(flatConversionContext.get()).contains(entry("id/_null_flavour|terminology", "openehr"))
    }

    @Test
    fun testNullFlavorElementToFormattedFlatMap() {
        val formattedFlatConversionContext = FormattedFlatMappingContext()
        val element = Element().apply { this.nullFlavour = DvCodedText.create("openehr", "272", "masked") }
        ElementToFlatMapper.mapFormatted(WebTemplateNode(AmNode(null, "ELEMENT"),"ELEMENT", "/"), SimpleValueConverter, element, "id", formattedFlatConversionContext)
        assertThat(formattedFlatConversionContext.get()).hasSize(3)

        assertThat(formattedFlatConversionContext.get()).hasSize(3)
        assertThat(formattedFlatConversionContext.get()).contains(entry("id/_null_flavour|code", "272"))
        assertThat(formattedFlatConversionContext.get()).contains(entry("id/_null_flavour|value", "masked"))
        assertThat(formattedFlatConversionContext.get()).contains(entry("id/_null_flavour|terminology", "openehr"))
    }

    @Test
    @Throws(IOException::class)
    fun testNullFlavorJsonRetrieve() {
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")));

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(
                getObjectMapper().readTree(getJson("/convert/compositions/Demo Vitals Null Flavour(1).json")) as ObjectNode,
                context)
        assertThat(composition).isNotNull
        val section = composition!!.content[0] as Section
        val observation = section.items[0] as Observation
        val event = observation.data!!.events[0]
        val dvTextElelemt = (event.data as ItemTree?)!!.items[0] as Element

        dvTextElelemt.nullFlavour = DvCodedText.create("openehr", "273", "not applicable")
        dvTextElelemt.value = null

        val structuredNode: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(structuredNode!!.isNull).isFalse
        assertThat(structuredNode["vitals"]["vitals"].isArray).isTrue
        assertThat(structuredNode["vitals"]["vitals"][0]["haemoglobin_a1c"].isArray).isTrue
        assertThat(structuredNode["vitals"]["vitals"][0]["haemoglobin_a1c"][0]["any_event"][0]["test_name"].isArray).isTrue
        assertThat(structuredNode["vitals"]["vitals"][0]["haemoglobin_a1c"][0]["any_event"][0]["test_name"][0].isObject).isTrue
        assertThat(structuredNode["vitals"]["vitals"][0]["haemoglobin_a1c"][0]["any_event"][0]["test_name"][0].get("_null_flavour").isArray()).isTrue()
        assertThat(structuredNode["vitals"]["vitals"][0]["haemoglobin_a1c"][0]["any_event"][0]["test_name"][0].get("_null_flavour").get(0).get("|code").asText()).isEqualTo("273")
        assertThat(structuredNode["vitals"]["vitals"][0]["haemoglobin_a1c"][0]["any_event"][0]["test_name"][0].get("_null_flavour").get(0).get("|value").asText()).isEqualTo("not applicable")
        assertThat(structuredNode["vitals"]["vitals"][0]["haemoglobin_a1c"][0]["any_event"][0]["test_name"][0].get("_null_flavour").get(0).get("|terminology").asText()).isEqualTo("openehr")
    }

    @Test
    @Throws(IOException::class)
    fun testNullFlavorPlainRetrieve() {
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")));

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(
                getObjectMapper().readTree(getJson("/convert/compositions/Demo Vitals Null Flavour(2).json")) as ObjectNode,
                context)
        assertThat(composition).isNotNull
        val section = composition!!.content[0] as Section
        val observation = section.items[0] as Observation
        val event = observation.data!!.events[0]
        val dvTextElement = (event.data as ItemTree?)!!.items[0] as Element

        dvTextElement.nullFlavour = DvCodedText.create("openehr", "273", "not applicable")
        dvTextElement.value = null

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_null_flavour|code", "273"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_null_flavour|value", "not applicable"))
        assertThat(flatMap).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_name/_null_flavour|terminology", "openehr"))
    }

    @Test
    @Throws(IOException::class)
    fun testNullFlavorJsonBuild() {
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")));

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(
                getObjectMapper().readTree(getJson("/convert/compositions/Demo Vitals Null Flavour(3).json")) as ObjectNode,
                context)
        assertThat(composition).isNotNull
        val section = composition!!.content[0] as Section
        val observation = section.items[0] as Observation
        val event = observation.data!!.events[0]
        val dvTextElement = (event.data as ItemTree?)!!.items[0] as Element

        val nullFlavor = dvTextElement.nullFlavour

        assertThat(nullFlavor).isNotNull
        assertThat(nullFlavor?.definingCode?.codeString).isEqualTo("273")
        assertThat(nullFlavor?.definingCode?.terminologyId?.value).isEqualTo("openehr")
        assertThat(nullFlavor?.value).isEqualTo("not applicable")
        assertThat(dvTextElement.value).isNull()

        val secondObservation = section.items[1] as Observation
        val secondEvent = secondObservation.data!!.events[0]
        val magElement = (secondEvent.data as ItemTree?)!!.items[0] as Element
        val secondNullFlavour = magElement.nullFlavour

        assertThat(secondNullFlavour).isNotNull
        assertThat(secondNullFlavour?.definingCode?.codeString).isEqualTo("272")
        assertThat(secondNullFlavour?.definingCode?.terminologyId?.value).isEqualTo("openehr")
        assertThat(secondNullFlavour?.value).isEqualTo("masked")
    }

    @Test
    @Throws(IOException::class)
    fun testNullFlavorMapBuild() {
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")));
        val flatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/Demo Vitals Null Flavour(4).json"), object : TypeReference<Map<String, Any?>>() {})
        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, context)
        assertThat(composition).isNotNull
        val section = composition!!.content[0] as Section
        val observation = section.items[1] as Observation
        val event = observation.data!!.events[0]
        val dvCodedText = (event.data as ItemTree?)!!.items[1] as Element
        val nullFlavor = dvCodedText.nullFlavour


        assertThat(dvCodedText.value).isNotNull
        assertThat(nullFlavor).isNotNull
        assertThat(nullFlavor?.definingCode?.codeString).isEqualTo("271")
        assertThat(nullFlavor?.definingCode?.terminologyId?.value).isEqualTo("openehr")
        assertThat(nullFlavor?.value).isEqualTo("no information")
    }


    @Test
    @Throws(IOException::class)
    fun testNullFlavorMissingTerminology() {
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")));

        val flatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/Demo Vitals Null Flavour(5).json"), object : TypeReference<Map<String, Any?>>() {})
        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, context)
        assertThat(composition).isNotNull
        val section = composition!!.content[0] as Section
        val observation = section.items[1] as Observation
        val event = observation.data!!.events[0]
        val dvCodedText = (event.data as ItemTree?)!!.items[1] as Element
        val nullFlavor = dvCodedText.nullFlavour

        assertThat(nullFlavor).isNotNull
        assertThat(nullFlavor?.definingCode?.codeString).isEqualTo("271")
        assertThat(nullFlavor?.definingCode?.terminologyId?.value).isEqualTo("openehr")
        assertThat(nullFlavor?.value).isEqualTo("no information")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testNullFlavorDirectValueToJson() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")));

        val composition = getDemoVitalsComposition(webTemplate, true)

        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(node).isNotNull
        val dvTextNode = node!!.path("vitals")
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

        val nullFlavour: JsonNode = dvTextNode.path(0).path("_null_flavour")
        assertThat(nullFlavour.isArray).isTrue
        assertThat(nullFlavour.size()).isEqualTo(1)
        assertThat(nullFlavour.path(0).path("|code").asText()).isEqualTo("271")
        assertThat(nullFlavour.path(0).path("|value").asText()).isEqualTo("no information")
        assertThat(nullFlavour.path(0).path("|terminology").asText()).isEqualTo("openehr")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testNullFlavorDirectValueFromJson() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")));

        val composition = getDemoVitalsComposition(webTemplate, true)
        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(node).isNotNull
        val dvTextNode = node!!.path("vitals")
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

        val nullFlavour: JsonNode = dvTextNode.path(0).path("_null_flavour")
        assertThat(nullFlavour.isArray).isTrue
        assertThat(nullFlavour.size()).isEqualTo(1)
        assertThat(nullFlavour.path(0).path("|code").asText()).isEqualTo("271")
        assertThat(nullFlavour.path(0).path("|value").asText()).isEqualTo("no information")
        assertThat(nullFlavour.path(0).path("|terminology").asText()).isEqualTo("openehr")

        val json = getObjectMapper().writeValueAsString(node)
        val readedNode = getObjectMapper().readTree(json)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("test_composer").build()

        val secondComposition: Composition? = webTemplate.convertFromStructuredToRaw(readedNode as ObjectNode, context)
        val section = secondComposition!!.content[0] as Section
        val observation = section.items[1] as Observation
        val stateItem = observation.data!!.events[0].state as ItemTree?
        val stateElement = stateItem!!.items[0] as Element
        val nullFlavourCt = stateElement.nullFlavour

        assertThat(nullFlavourCt).isNotNull
        assertThat(nullFlavourCt?.definingCode?.codeString).isEqualTo("271")
        assertThat(nullFlavourCt?.definingCode?.terminologyId?.value).isEqualTo("openehr")
        assertThat(nullFlavourCt?.value).isEqualTo("no information")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSimpleTest() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/composer_name", "Composer")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|code", "253")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|code", "253"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|value", "unknown"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|terminology", "openehr"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondSimpleTest() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/composer_name", "Composer")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|value", "no information")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|code", "271"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|value", "no information"))
        assertThat(flatMap).contains(entry("vitals/vitals/body_temperature:0/any_event:0/temperature/_null_flavour|terminology", "openehr"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testNullFlavourBroken() {
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/clinical-summary-events.opt"),  WebTemplateBuilderContext("en"))
        val context = ConversionContext.create()
                .withActivityTimingProvider { _ -> DvParsable("R1", "timing")  }
                .withInstructionNarrativeProvider { _ -> DvText("narrative") }
                .build()

        val flatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/Demo Vitals Null Flavour(6).json"), object : TypeReference<Map<String, Any?>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, context)

        assertThat(composition).isNotNull
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Throws(IOException::class)
    private fun getDemoVitalsComposition(webTemplate: WebTemplate, nullFlavor: Boolean): Composition {
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("test_composer").build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(
                getObjectMapper().readTree(getJson("/convert/compositions/DemoVitalsComposition.json")) as ObjectNode,
                context)
        val section = composition!!.content[0] as Section
        val observation = section.items[1] as Observation
        val stateItem = observation.data!!.events[0].state as ItemTree?
        val stateEllement = stateItem!!.items[0] as Element
        stateEllement.value = DvText("Null flavor test")

        if (nullFlavor) {
            stateEllement.nullFlavour = DvCodedText.createWithOpenEHRTerminology("271", "no information")
            stateEllement.value = null
        }
        return composition
    }
}
