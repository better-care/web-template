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
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class WebTemplateContentTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCategoryInWT() {
        val webTemplate: WebTemplate = getWebTemplate("/convert/templates/older/Demo Vitals.opt")
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("/category")
        assertThat(node).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCategoryReturned() {
        val webTemplate = getWebTemplate("/convert/templates/older/Demo Vitals.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.Builder<String, Any?>()
                .put("vitals/vitals/body_temperature/any_event/temperature|magnitude", 39.1)
                .put("vitals/vitals/body_temperature/any_event/temperature|unit", "°C")
                .put("vitals/vitals/body_temperature/any_event/body_exposure", "at0031")
                .build(),
            context)

        assertThat(composition).isNotNull

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(Assertions.entry("vitals/category|code", "433"))
        assertThat(flatMap).contains(Assertions.entry("vitals/category|terminology", "openehr"))
        assertThat(flatMap).contains(Assertions.entry("vitals/category|value", "event"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testLanguageAndTerritoryInWT() {
        val webTemplate = getWebTemplate("/convert/templates/older/Demo Vitals.opt")
        val languageNode: WebTemplateNode = webTemplate.findWebTemplateNode("/language")
        assertThat(languageNode).isNotNull
        val territoryNode: WebTemplateNode = webTemplate.findWebTemplateNode("/territory")
        assertThat(territoryNode).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testLanguageAndTerritoryReturned() {
        val webTemplate = getWebTemplate("/convert/templates/older/Demo Vitals.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.Builder<String, Any?>()
                .put("vitals/vitals/body_temperature/any_event/temperature|magnitude", 39.1)
                .put("vitals/vitals/body_temperature/any_event/temperature|unit", "°C")
                .put("vitals/vitals/body_temperature/any_event/body_exposure", "at0031")
                .build(),
            context)
        assertThat(composition).isNotNull

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(Assertions.entry("vitals/language|code", "sl"))
        assertThat(flatMap).contains(Assertions.entry("vitals/territory|code", "SI"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testContextEndTimeReturned() {
        val webTemplate = getWebTemplate("/convert/templates/older/Demo Vitals.opt")
        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("composer")
            .withTime(OffsetDateTime.of(2017, 11, 1, 1, 30, 0, 0, ZoneOffset.UTC))
            .withEndTime(OffsetDateTime.of(2017, 12, 1, 1, 30, 0, 0, ZoneOffset.UTC))
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.Builder<String, Any>()
                .put("vitals/vitals/body_temperature/any_event/temperature|magnitude", 39.1)
                .put("vitals/vitals/body_temperature/any_event/temperature|unit", "°C")
                .put("vitals/vitals/body_temperature/any_event/body_exposure", "at0031")
                .build(),
            context)
        assertThat(composition).isNotNull

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(Assertions.entry("vitals/context/start_time", "2017-11-01T01:30:00Z"))
        assertThat(flatMap).contains(Assertions.entry("vitals/context/_end_time", "2017-12-01T01:30:00Z"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testContextLocationReturned() {
        val webTemplate = getWebTemplate("/convert/templates/older/Demo Vitals.opt")
        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("composer")
            .withTime(OffsetDateTime.of(2017, 11, 1, 1, 30, 0, 0, ZoneOffset.UTC))
            .withEndTime(OffsetDateTime.of(2017, 12, 1, 1, 30, 0, 0, ZoneOffset.UTC))
            .withLocation("I am here!")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.Builder<String, Any>()
                .put("vitals/vitals/body_temperature/any_event/temperature|magnitude", 39.1)
                .put("vitals/vitals/body_temperature/any_event/temperature|unit", "°C")
                .put("vitals/vitals/body_temperature/any_event/body_exposure", "at0031")
                .build(),
            context)
        assertThat(composition).isNotNull

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(Assertions.entry("vitals/context/start_time", "2017-11-01T01:30:00Z"))
        assertThat(flatMap).contains(Assertions.entry("vitals/context/_end_time", "2017-12-01T01:30:00Z"))
        assertThat(flatMap).contains(Assertions.entry("vitals/context/_location", "I am here!"))
    }
}
