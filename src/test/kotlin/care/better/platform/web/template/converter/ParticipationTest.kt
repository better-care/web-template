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
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openehr.rm.common.Participation
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ParticipationTest : AbstractWebTemplateTest() {

    val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("sl"))

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipations() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|name" to "Janez Novak",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|id" to "999",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|function" to "performer",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|mode" to "216",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:0" to "A",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:0" to "I",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:0" to "T",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:0" to "1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:1" to "A1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:1" to "I1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:1" to "T1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:1" to "2",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|name" to "Marija Medved",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|id" to "998",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|function" to "watcher",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|mode" to "videoconferencing",
                "vitals/vitals/haemoglobin_a1c/_link:0|meaning" to "none",
                "vitals/vitals/haemoglobin_a1c/_link:0|type" to "href",
                "vitals/vitals/haemoglobin_a1c/_link:0|target" to "http://www.sun.com",
                "vitals/vitals/haemoglobin_a1c/_link:1|meaning" to "serious",
                "vitals/vitals/haemoglobin_a1c/_link:1|type" to "url",
                "vitals/vitals/haemoglobin_a1c/_link:1|target" to "http://www.ehrscape.com"
            ),
            ConversionContext.create().build()
        )

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
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "http://www.ehrscape.com")
        )

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
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|name" to "Janez Novak",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|id" to "999",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|function" to "performer",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|mode" to "216",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner" to "A",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer" to "I",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type" to "T",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id" to "1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|name" to "Marija Medved",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|id" to "998",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|function" to "watcher",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|mode" to "videoconferencing",
                "vitals/vitals/haemoglobin_a1c/_link:0|meaning" to "none",
                "vitals/vitals/haemoglobin_a1c/_link:0|type" to "href",
                "vitals/vitals/haemoglobin_a1c/_link:0|target" to "http://www.sun.com",
                "vitals/vitals/haemoglobin_a1c/_link:1|meaning" to "serious",
                "vitals/vitals/haemoglobin_a1c/_link:1|type" to "url",
                "vitals/vitals/haemoglobin_a1c/_link:1|target" to "http://www.ehrscape.com"
            ),
            ConversionContext.create().build()
        )

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
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "http://www.ehrscape.com")
        )

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
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|name" to "Janez Novak",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|function" to "performer",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|mode" to "216",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:0" to "A1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:0" to "I1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:0" to "T1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:0" to "id1",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_assigner:1" to "A2",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_issuer:1" to "I2",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_type:1" to "T2",
                "vitals/vitals/haemoglobin_a1c/_other_participation:0|identifiers_id:1" to "id2",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|name" to "Marija Medved",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|id" to "998",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|function" to "watcher",
                "vitals/vitals/haemoglobin_a1c/_other_participation:1|mode" to "videoconferencing",
                "vitals/vitals/haemoglobin_a1c/_link:0|meaning" to "none",
                "vitals/vitals/haemoglobin_a1c/_link:0|type" to "href",
                "vitals/vitals/haemoglobin_a1c/_link:0|target" to "http://www.sun.com",
                "vitals/vitals/haemoglobin_a1c/_link:1|meaning" to "serious",
                "vitals/vitals/haemoglobin_a1c/_link:1|type" to "url",
                "vitals/vitals/haemoglobin_a1c/_link:1|target" to "http://www.ehrscape.com"
            ),
            ConversionContext.create().build()
        )

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
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "http://www.ehrscape.com")
        )

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
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/context/_participations:0|name" to "Janez Novak",
                "vitals/context/_participations:0|id" to "999",
                "vitals/context/_participations:0|function" to "performer",
                "vitals/context/_participations:0|mode" to "216",
                "vitals/context/_participations:1|name" to "Marija Medved",
                "vitals/context/_participations:1|id" to "998",
                "vitals/context/_participations:1|function" to "watcher",
                "vitals/context/_participations:1|mode" to "videoconferencing"
            ),
            ConversionContext.create().build()
        )

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
            entry("vitals/context/_participation:0|name", "Janez Novak"),
            entry("vitals/context/_participation:0|id", "999"),
            entry("vitals/context/_participation:0|mode", "face-to-face communication"),
            entry("vitals/context/_participation:0|function", "performer"),
            entry("vitals/context/_participation:1|name", "Marija Medved"),
            entry("vitals/context/_participation:1|id", "998"),
            entry("vitals/context/_participation:1|mode", "videoconferencing"),
            entry("vitals/context/_participation:1|function", "watcher")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsOnContextFromCtx() {
        assertThatThrownBy {
            webTemplate.convertFromFlatToRaw<Composition>(
                mapOf(
                    "ctx/language" to "sl",
                    "ctx/territory" to "SI",
                    "ctx/id_scheme" to "ispek",
                    "ctx/id_namespace" to "ispek",
                    "ctx/composer_name" to "George Orwell",
                    "ctx/participation_name" to "Named Participant",
                    "ctx/participation_function" to "District Nurse",
                    "ctx/participation_id" to "998877",
                    "ctx/participation_mode" to "videoconferencing"
                ),
                ConversionContext.create().build()
            )
        }
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
            ConversionContext.create().build()
        )

        assertThat(composition).isNull()
    }


    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsOnContextFromCtxOverride() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "ctx/participation_name" to "Named Participant",
                "ctx/participation_function" to "District Nurse",
                "ctx/participation_id" to "998877",
                "ctx/participation_mode" to "videoconferencing",
                "vitals/context/_participations:0|name" to "Janez Novak",
                "vitals/context/_participations:0|id" to "999",
                "vitals/context/_participations:0|function" to "performer",
                "vitals/context/_participations:0|mode" to "216"
            ),
            ConversionContext.create().build()
        )

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/context/_participation:0|name", "Janez Novak"),
            entry("vitals/context/_participation:0|id", "999"),
            entry("vitals/context/_participation:0|mode", "face-to-face communication"),
            entry("vitals/context/_participation:0|function", "performer")
        )

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsSchemeAndNamespace() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/context/_participations:0|name" to "Janez Novak",
                "vitals/context/_participations:0|id" to "999",
                "vitals/context/_participations:0|id_scheme" to "scheme",
                "vitals/context/_participations:0|id_namespace" to "ns",
                "vitals/context/_participations:0|function" to "performer",
                "vitals/context/_participations:0|mode" to "216"
            ),
            ConversionContext.create().build()
        )

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/context/_participation:0|name", "Janez Novak"),
            entry("vitals/context/_participation:0|id", "999"),
            entry("vitals/context/_participation:0|id_scheme", "scheme"),
            entry("vitals/context/_participation:0|id_namespace", "ns"),
            entry("vitals/context/_participation:0|mode", "face-to-face communication"),
            entry("vitals/context/_participation:0|function", "performer")
        )

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsScheme() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/context/_participations:0|name" to "Janez Novak",
                "vitals/context/_participations:0|id" to "999",
                "vitals/context/_participations:0|id_scheme" to "scheme",
                "vitals/context/_participations:0|function" to "performer",
                "vitals/context/_participations:0|mode" to "216"
            ),
            ConversionContext.create().build()
        )

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/context/_participation:0|name", "Janez Novak"),
            entry("vitals/context/_participation:0|id", "999"),
            entry("vitals/context/_participation:0|id_scheme", "scheme"),
            entry("vitals/context/_participation:0|id_namespace", "ispek"),
            entry("vitals/context/_participation:0|mode", "face-to-face communication"),
            entry("vitals/context/_participation:0|function", "performer")
        )

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParticipationsNamespace() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/context/_participations:0|name" to "Janez Novak",
                "vitals/context/_participations:0|id" to "999",
                "vitals/context/_participations:0|id_namespace" to "ns",
                "vitals/context/_participations:0|function" to "performer",
                "vitals/context/_participations:0|mode" to "216"
            ),
            ConversionContext.create().build()
        )

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/context/_participation:0|name", "Janez Novak"),
            entry("vitals/context/_participation:0|id", "999"),
            entry("vitals/context/_participation:0|id_scheme", "ispek"),
            entry("vitals/context/_participation:0|id_namespace", "ns"),
            entry("vitals/context/_participation:0|mode", "face-to-face communication"),
            entry("vitals/context/_participation:0|function", "performer")
        )

        assertThat(flatMap.entries.filter { it.key.startsWith("vitals/context/_participation") }.count()).isEqualTo(6L)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIdentifiersInCtx() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "ctx/participation_name" to "Named Participant",
                "ctx/participation_function" to "District Nurse",
                "ctx/participation_id" to "998877",
                "ctx/participation_mode" to "videoconferencing",
                "ctx/participation_identifiers" to "I1::A1::ID1::T1;I2::A2::ID2::T2",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_link:0|meaning" to "none",
                "vitals/vitals/haemoglobin_a1c/_link:0|type" to "href",
                "vitals/vitals/haemoglobin_a1c/_link:0|target" to "http://www.sun.com",
                "vitals/vitals/haemoglobin_a1c/_link:1|meaning" to "serious",
                "vitals/vitals/haemoglobin_a1c/_link:1|type" to "url",
                "vitals/vitals/haemoglobin_a1c/_link:1|target" to "http://www.ehrscape.com"
            ),
            ConversionContext.create().build()
        )

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
            entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:0|identifiers_id:1", "ID2")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIdentifiersInCtxMulti() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "ctx/participation_name:0" to "Named Participant",
                "ctx/participation_function:0" to "District Nurse",
                "ctx/participation_id:0" to "998877",
                "ctx/participation_mode:0" to "videoconferencing",
                "ctx/participation_identifiers:0" to "I1::A1::ID1::T1;I2::A2::ID2::T2",
                "ctx/participation_name:1" to "Named Participant 2",
                "ctx/participation_function:1" to "District Nurse 2",
                "ctx/participation_id:1" to "9988772",
                "ctx/participation_mode:1" to "videoconferencing",
                "ctx/participation_identifiers:1" to "II1::AA1::IID1::TT1;II2::AA2::IID2::TT2",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_link:0|meaning" to "none",
                "vitals/vitals/haemoglobin_a1c/_link:0|type" to "href",
                "vitals/vitals/haemoglobin_a1c/_link:0|target" to "http://www.sun.com",
                "vitals/vitals/haemoglobin_a1c/_link:1|meaning" to "serious",
                "vitals/vitals/haemoglobin_a1c/_link:1|type" to "url",
                "vitals/vitals/haemoglobin_a1c/_link:1|target" to "http://www.ehrscape.com"
            ),
            ConversionContext.create().build()
        )

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
            entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_id:1", "IID2")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondIdentifiersInCtxMulti() {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "ctx/participation_name:0" to "Named Participant",
                "ctx/participation_function:0" to "District Nurse",
                "ctx/participation_id:0" to "998877",
                "ctx/participation_mode:0" to "videoconferencing",
                "ctx/participation_identifiers:0|issuer:0" to "I1",
                "ctx/participation_identifiers:0|assigner:0" to "A1",
                "ctx/participation_identifiers:0|id:0" to "ID1",
                "ctx/participation_identifiers:0|type:0" to "T1",
                "ctx/participation_identifiers:0|issuer:1" to "I2",
                "ctx/participation_identifiers:0|assigner:1" to "A2",
                "ctx/participation_identifiers:0|id:1" to "ID2",
                "ctx/participation_identifiers:0|type:1" to "T2",
                "ctx/participation_name:1" to "Named Participant 2",
                "ctx/participation_function:1" to "District Nurse 2",
                "ctx/participation_id:1" to "9988772",
                "ctx/participation_mode:1" to "videoconferencing",
                "ctx/participation_identifiers:1" to "II1::AA1::IID1::TT1;II2::AA2::IID2::TT2",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_link:0|meaning" to "none",
                "vitals/vitals/haemoglobin_a1c/_link:0|type" to "href",
                "vitals/vitals/haemoglobin_a1c/_link:0|target" to "http://www.sun.com",
                "vitals/vitals/haemoglobin_a1c/_link:1|meaning" to "serious",
                "vitals/vitals/haemoglobin_a1c/_link:1|type" to "url",
                "vitals/vitals/haemoglobin_a1c/_link:1|target" to "http://www.ehrscape.com"
            ),
            ConversionContext.create().build()
        )

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
            entry("vitals/vitals/haemoglobin_a1c:0/_other_participation:1|identifiers_id:1", "IID2")
        )
    }

    @Test
    fun testParticipationFunctions() {
        val growthChartWebTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/Lifecare_Care_Template_Growth_Chart.xml"),
            WebTemplateBuilderContext("en")
        )

        val flatMap: Map<String, Any?> = getObjectMapper().readValue(
            getJson("/convert/compositions/Lifecare_Care_Template_Growth_Chart.json"),
            object : TypeReference<Map<String, Any?>>() {})

        val composition: Composition? = growthChartWebTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())

        assertThat(composition).isNotNull

        assertThat(NameAndNodeMatchingPathValueExtractor("/context/participations").getValue(composition).map { (it as Participation).function?.value })
            .containsOnly("COMPOSER", "MAIN_PROVIDER")

    }
}
