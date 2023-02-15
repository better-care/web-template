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
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.converter.exceptions.ConversionException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openehr.rm.common.Participation
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ParticipationTest : AbstractWebTemplateTest() {

    val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"),  WebTemplateBuilderContext("sl"))

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipations() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|name", "Janez Novak")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|id", "999")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|function", "performer")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|mode", "216")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:0", "A")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:0", "I")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:0", "T")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:0", "1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:1", "A1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:1", "I1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:1", "T1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:1", "2")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|name", "Marija Medved")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|id", "998")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|function", "watcher")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|mode", "videoconferencing")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|meaning", "none")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|type", "href")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|target", "http://www.sun.com")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|meaning", "serious")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|type", "url")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|target", "http://www.ehrscape.com")
                        .build(),
                ConversionContext.create().build())

        val flatMap: MutableMap<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|name", "Janez Novak"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id", "999"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|mode", "face-to-face communication"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|function", "performer"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|name", "Marija Medved"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id", "998"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|function", "watcher"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:0", "A"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:0", "I"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:0", "T"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:0", "1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:1", "A1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:1", "I1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:1", "T1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:1", "2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|meaning", "none"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|type", "href"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|target", "http://www.sun.com"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|meaning", "serious"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|type", "url"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "http://www.ehrscape.com"))

        flatMap["ctx/language"] = "sl"
        flatMap["ctx/territory"] = "SI"
        flatMap["ctx/id_scheme"] = "ispek"
        flatMap["ctx/id_namespace"] = "ispek"
        flatMap["ctx/composer_name"] = "George Orwell"

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        val section = secondComposition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.otherParticipations).hasSize(2)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsSingleIdentifier() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|name", "Janez Novak")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|id", "999")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|function", "performer")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|mode", "216")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner", "A")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer", "I")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type", "T")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id", "1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|name", "Marija Medved")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|id", "998")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|function", "watcher")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|mode", "videoconferencing")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|meaning", "none")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|type", "href")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|target", "http://www.sun.com")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|meaning", "serious")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|type", "url")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|target", "http://www.ehrscape.com")
                        .build(),
                ConversionContext.create().build())

        val flatMap: MutableMap<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|name", "Janez Novak"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id", "999"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|mode", "face-to-face communication"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|function", "performer"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|name", "Marija Medved"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id", "998"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|function", "watcher"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:0", "A"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:0", "I"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:0", "T"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:0", "1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|meaning", "none"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|type", "href"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|target", "http://www.sun.com"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|meaning", "serious"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|type", "url"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "http://www.ehrscape.com"))

        flatMap["ctx/language"] = "sl"
        flatMap["ctx/territory"] = "SI"
        flatMap["ctx/id_scheme"] = "ispek"
        flatMap["ctx/id_namespace"] = "ispek"
        flatMap["ctx/composer_name"] = "George Orwell"
        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        val section = secondComposition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.otherParticipations).hasSize(2)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsMultipleIdentifierNoId() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|name", "Janez Novak")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|function", "performer")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|mode", "216")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:0", "A1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:0", "I1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:0", "T1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:0", "id1")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:1", "A2")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:1", "I2")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:1", "T2")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:1", "id2")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|name", "Marija Medved")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|id", "998")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|function", "watcher")
                        .put("vitals/vitals/haemoglobin_a1c/_other_participation:1|mode", "videoconferencing")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|meaning", "none")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|type", "href")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|target", "http://www.sun.com")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|meaning", "serious")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|type", "url")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|target", "http://www.ehrscape.com")
                        .build(),
                ConversionContext.create().build())

        val flatMap: MutableMap<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|name", "Janez Novak"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|mode", "face-to-face communication"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|function", "performer"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|name", "Marija Medved"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|function", "watcher"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:0", "A1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:0", "I1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:0", "T1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:0", "id1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:1", "A2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:1", "I2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:1", "T2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:1", "id2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|meaning", "none"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|type", "href"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:0|target", "http://www.sun.com"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|meaning", "serious"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|type", "url"),
                entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "http://www.ehrscape.com"))

        flatMap["ctx/language"] = "sl"
        flatMap["ctx/territory"] = "SI"
        flatMap["ctx/id_scheme"] = "ispek"
        flatMap["ctx/id_namespace"] = "ispek"
        flatMap["ctx/composer_name"] = "George Orwell"

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        val section = secondComposition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.otherParticipations).hasSize(2)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsOnContext() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/context/_participations:0|name", "Janez Novak")
                        .put("vitals/context/_participations:0|id", "999")
                        .put("vitals/context/_participations:0|function", "performer")
                        .put("vitals/context/_participations:0|mode", "216")
                        .put("vitals/context/_participations:1|name", "Marija Medved")
                        .put("vitals/context/_participations:1|id", "998")
                        .put("vitals/context/_participations:1|function", "watcher")
                        .put("vitals/context/_participations:1|mode", "videoconferencing")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
                entry("vitals/context/_participation:0|name", "Janez Novak"),
                entry("vitals/context/_participation:0|id", "999"),
                entry("vitals/context/_participation:0|mode", "face-to-face communication"),
                entry("vitals/context/_participation:0|function", "performer"),
                entry("vitals/context/_participation:1|name", "Marija Medved"),
                entry("vitals/context/_participation:1|id", "998"),
                entry("vitals/context/_participation:1|mode", "videoconferencing"),
                entry("vitals/context/_participation:1|function", "watcher"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsOnContextFromCtx() {
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("ctx/participation_name", "Named Participant")
                        .put("ctx/participation_function", "District Nurse")
                        .put("ctx/participation_id", "998877")
                        .put("ctx/participation_mode", "videoconferencing")
                        .build(),
                ConversionContext.create().build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("COMPOSITION has no attribute vitals.")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsOnContextFromCtxAndRoot() {
        val structuredComposition = """
            {
                "ctx/language": "sl",
                "ctx/territory": "SI",
                "ctx/id_scheme": "ispek",
                "ctx/id_namespace": "ispek",
                "ctx/composer_name": "George Orwell",
                "ctx/participation_name": "Named Participant",
                "ctx/participation_function": "District Nurse",
                "ctx/participation_id": "998877",
                "ctx/participation_mode": "videoconferencing",
                "vitals": {}
            }
        """

        val composition = webTemplate.convertFromStructuredToRaw<Composition>(
            getObjectMapper().readTree(structuredComposition) as ObjectNode,
            ConversionContext.create().build())

        assertThat(composition).isNull()
    }


        @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsOnContextFromCtxOverride() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("ctx/participation_name", "Named Participant")
                        .put("ctx/participation_function", "District Nurse")
                        .put("ctx/participation_id", "998877")
                        .put("ctx/participation_mode", "videoconferencing")
                        .put("vitals/context/_participations:0|name", "Janez Novak")
                        .put("vitals/context/_participations:0|id", "999")
                        .put("vitals/context/_participations:0|function", "performer")
                        .put("vitals/context/_participations:0|mode", "216")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("vitals/context/_participation:0|name", "Janez Novak"),
                entry("vitals/context/_participation:0|id", "999"),
                entry("vitals/context/_participation:0|mode", "face-to-face communication"),
                entry("vitals/context/_participation:0|function", "performer"))

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsSchemeAndNamespace() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/context/_participations:0|name", "Janez Novak")
                        .put("vitals/context/_participations:0|id", "999")
                        .put("vitals/context/_participations:0|id_scheme", "scheme")
                        .put("vitals/context/_participations:0|id_namespace", "ns")
                        .put("vitals/context/_participations:0|function", "performer")
                        .put("vitals/context/_participations:0|mode", "216")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("vitals/context/_participation:0|name", "Janez Novak"),
                entry("vitals/context/_participation:0|id", "999"),
                entry("vitals/context/_participation:0|id_scheme", "scheme"),
                entry("vitals/context/_participation:0|id_namespace", "ns"),
                entry("vitals/context/_participation:0|mode", "face-to-face communication"),
                entry("vitals/context/_participation:0|function", "performer"))

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsScheme() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/context/_participations:0|name", "Janez Novak")
                        .put("vitals/context/_participations:0|id", "999")
                        .put("vitals/context/_participations:0|id_scheme", "scheme")
                        .put("vitals/context/_participations:0|function", "performer")
                        .put("vitals/context/_participations:0|mode", "216")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("vitals/context/_participation:0|name", "Janez Novak"),
                entry("vitals/context/_participation:0|id", "999"),
                entry("vitals/context/_participation:0|id_scheme", "scheme"),
                entry("vitals/context/_participation:0|id_namespace", "ispek"),
                entry("vitals/context/_participation:0|mode", "face-to-face communication"),
                entry("vitals/context/_participation:0|function", "performer"))

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsNamespace() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/context/_participations:0|name", "Janez Novak")
                        .put("vitals/context/_participations:0|id", "999")
                        .put("vitals/context/_participations:0|id_namespace", "ns")
                        .put("vitals/context/_participations:0|function", "performer")
                        .put("vitals/context/_participations:0|mode", "216")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("vitals/context/_participation:0|name", "Janez Novak"),
                entry("vitals/context/_participation:0|id", "999"),
                entry("vitals/context/_participation:0|id_scheme", "ispek"),
                entry("vitals/context/_participation:0|id_namespace", "ns"),
                entry("vitals/context/_participation:0|mode", "face-to-face communication"),
                entry("vitals/context/_participation:0|function", "performer"))

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIdentifiersInCtx() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("ctx/participation_name", "Named Participant")
                        .put("ctx/participation_function", "District Nurse")
                        .put("ctx/participation_id", "998877")
                        .put("ctx/participation_mode", "videoconferencing")
                        .put("ctx/participation_identifiers", "I1::A1::ID1::T1;I2::A2::ID2::T2")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|meaning", "none")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|type", "href")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|target", "http://www.sun.com")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|meaning", "serious")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|type", "url")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|target", "http://www.ehrscape.com")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|name", "Named Participant"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id", "998877"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id_scheme", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id_namespace", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|function", "District Nurse"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:0", "A1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:0", "I1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:0", "T1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:0", "ID1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:1", "A2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:1", "I2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:1", "T2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:1", "ID2"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIdentifiersInCtxMulti() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("ctx/participation_name:0", "Named Participant")
                        .put("ctx/participation_function:0", "District Nurse")
                        .put("ctx/participation_id:0", "998877")
                        .put("ctx/participation_mode:0", "videoconferencing")
                        .put("ctx/participation_identifiers:0", "I1::A1::ID1::T1;I2::A2::ID2::T2")
                        .put("ctx/participation_name:1", "Named Participant 2")
                        .put("ctx/participation_function:1", "District Nurse 2")
                        .put("ctx/participation_id:1", "9988772")
                        .put("ctx/participation_mode:1", "videoconferencing")
                        .put("ctx/participation_identifiers:1", "II1::AA1::IID1::TT1;II2::AA2::IID2::TT2")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|meaning", "none")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|type", "href")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|target", "http://www.sun.com")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|meaning", "serious")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|type", "url")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|target", "http://www.ehrscape.com")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|name", "Named Participant"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id", "998877"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id_scheme", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id_namespace", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|function", "District Nurse"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:0", "A1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:0", "I1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:0", "T1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:0", "ID1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:1", "A2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:1", "I2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:1", "T2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:1", "ID2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|name", "Named Participant 2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id", "9988772"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id_scheme", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id_namespace", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|function", "District Nurse 2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_assigner:0", "AA1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_issuer:0", "II1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_type:0", "TT1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_id:0", "IID1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_assigner:1", "AA2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_issuer:1", "II2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_type:1", "TT2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_id:1", "IID2"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondIdentifiersInCtxMulti() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("ctx/participation_name:0", "Named Participant")
                        .put("ctx/participation_function:0", "District Nurse")
                        .put("ctx/participation_id:0", "998877")
                        .put("ctx/participation_mode:0", "videoconferencing")
                        .put("ctx/participation_identifiers:0|issuer:0", "I1")
                        .put("ctx/participation_identifiers:0|assigner:0", "A1")
                        .put("ctx/participation_identifiers:0|id:0", "ID1")
                        .put("ctx/participation_identifiers:0|type:0", "T1")
                        .put("ctx/participation_identifiers:0|issuer:1", "I2")
                        .put("ctx/participation_identifiers:0|assigner:1", "A2")
                        .put("ctx/participation_identifiers:0|id:1", "ID2")
                        .put("ctx/participation_identifiers:0|type:1", "T2")
                        .put("ctx/participation_name:1", "Named Participant 2")
                        .put("ctx/participation_function:1", "District Nurse 2")
                        .put("ctx/participation_id:1", "9988772")
                        .put("ctx/participation_mode:1", "videoconferencing")
                        .put("ctx/participation_identifiers:1", "II1::AA1::IID1::TT1;II2::AA2::IID2::TT2")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|meaning", "none")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|type", "href")
                        .put("vitals/vitals/haemoglobin_a1c/_link:0|target", "http://www.sun.com")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|meaning", "serious")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|type", "url")
                        .put("vitals/vitals/haemoglobin_a1c/_link:1|target", "http://www.ehrscape.com")
                        .build(),
                ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|name", "Named Participant"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id", "998877"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id_scheme", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|id_namespace", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|function", "District Nurse"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:0", "A1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:0", "I1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:0", "T1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:0", "ID1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_assigner:1", "A2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_issuer:1", "I2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_type:1", "T2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:1", "ID2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|name", "Named Participant 2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id", "9988772"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id_scheme", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|id_namespace", "ispek"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|mode", "videoconferencing"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|function", "District Nurse 2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_assigner:0", "AA1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_issuer:0", "II1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_type:0", "TT1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_id:0", "IID1"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_assigner:1", "AA2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_issuer:1", "II2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_type:1", "TT2"),
                entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_id:1", "IID2"))
    }

    @Test
    fun testParticipationFunctions() {
        val growthChartWebTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
                getTemplate("/convert/templates/Lifecare_Care_Template_Growth_Chart.xml"),
                WebTemplateBuilderContext("en"))

        val flatMap: Map<String, Any?> = getObjectMapper().readValue(
                getJson("/convert/compositions/Lifecare_Care_Template_Growth_Chart.json"),
                object : TypeReference<Map<String, Any?>>() {})

        val composition: Composition? = growthChartWebTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())

        assertThat(composition).isNotNull

        assertThat(NameAndNodeMatchingPathValueExtractor("/context/participations").getValue(composition).map { (it as Participation).function?.value })
            .containsOnly("COMPOSER", "MAIN_PROVIDER")

    }
}
