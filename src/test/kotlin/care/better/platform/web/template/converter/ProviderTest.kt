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

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ProviderTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testEntryProvider() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_provider|name" to "Marija Medved",
                "vitals/vitals/haemoglobin_a1c/_provider|id" to "998"
            ),
            ConversionContext.create().build()
        )

        val flatMap: MutableMap<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/_provider|name", "Marija Medved"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider|id", "998")
        )

        flatMap["ctx/language"] = "sl"
        flatMap["ctx/territory"] = "SI"
        flatMap["ctx/id_scheme"] = "ispek"
        flatMap["ctx/id_namespace"] = "ispek"
        flatMap["ctx/composer_name"] = "George Orwell"

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        val section = secondComposition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.provider!!.externalRef!!.id!!.value).isEqualTo("998")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testEntryProviderWithIdentifiers() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_provider|name" to "Marija Medved",
                "vitals/vitals/haemoglobin_a1c/_provider|id" to "998",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:0" to "1",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:0|type" to "person",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:0|assigner" to "nhs",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:0|issuer" to "nhs",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:1" to "123",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:1|type" to "person",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:1|assigner" to "uk",
                "vitals/vitals/haemoglobin_a1c/_provider/_identifier:1|issuer" to "uk"
            ),
            ConversionContext.create().build()
        )

        val flatMap: MutableMap<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/_provider|name", "Marija Medved"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider|id", "998"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:0", "1"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:0|type", "person"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:0|assigner", "nhs"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:0|issuer", "nhs"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:1", "123"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:1|type", "person"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:1|assigner", "uk"),
            entry("vitals/vitals/haemoglobin_a1c:0/_provider/_identifier:1|issuer", "uk")
        )

        flatMap["ctx/language"] = "sl"
        flatMap["ctx/territory"] = "SI"
        flatMap["ctx/id_scheme"] = "ispek"
        flatMap["ctx/id_namespace"] = "ispek"
        flatMap["ctx/composer_name"] = "George Orwell"

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        val section = secondComposition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.provider!!.externalRef!!.id!!.value).isEqualTo("998")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testHealthCareFacility() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/context/_health_care_facility|name" to "Hospital",
                "vitals/context/_health_care_facility/_identifier:0" to "17",
                "vitals/context/_health_care_facility/_identifier:0|assigner" to "uk",
                "vitals/context/_health_care_facility/_identifier:0|issuer" to "uk",
                "vitals/context/_health_care_facility/_identifier:0|type" to "ESTABLISHMENT",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            ConversionContext.create().build()
        )

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/context/_health_care_facility|name", "Hospital"),
            entry("vitals/context/_health_care_facility/_identifier:0", "17"),
            entry("vitals/context/_health_care_facility/_identifier:0|type", "ESTABLISHMENT"),
            entry("vitals/context/_health_care_facility/_identifier:0|assigner", "uk"),
            entry("vitals/context/_health_care_facility/_identifier:0|issuer", "uk")
        )
    }
}
