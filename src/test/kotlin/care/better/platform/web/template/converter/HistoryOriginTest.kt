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

import care.better.platform.time.temporal.OpenEhrOffsetDateTime
import care.better.platform.utils.JSR310ConversionUtils
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import jakarta.xml.bind.JAXBException
import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import java.io.IOException
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * @author Primoz Delopst
 * @author Tilen Zabkar
 * @since 3.1.0
 */
class HistoryOriginTest : AbstractWebTemplateTest() {

    private val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
        getTemplate("/convert/templates/Demo Vitals.opt"),
        WebTemplateBuilderContext("sl")
    )

    private val time1 = ZonedDateTime.of(2025, 1, 1, 1, 1, 1, 0, ZoneId.systemDefault()).toOffsetDateTime()
    private val time2 = ZonedDateTime.of(2025, 1, 1, 1, 1, 2, 0, ZoneId.systemDefault()).toOffsetDateTime()
    private val time1Formatted = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(time1)

    private companion object {
        const val START_TIME_KEY = "vitals/context/start_time"
        const val HISTORY_ORIGIN_KEY = "vitals/vitals/haemoglobin_a1c:0/history_origin"
        const val HISTORY_ORIGIN_INPUT_KEY = "vitals/vitals/haemoglobin_a1c/history_origin"
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testHistoryOrigin() {
        val composition = createComposition(time1)

        val section = composition.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(JSR310ConversionUtils.toOffsetDateTime(observation.data!!.origin!!).compareTo(time1)).isEqualTo(0)
    }

    @Test
    fun testHistoryOriginReturnedWhenDifferentFromStartTimeFlat() {
        val composition = createComposition(time1, time2)
        val flatResult = webTemplate.convertFromRawToFlat(composition)

        assertFlatResultHasOrigin(flatResult, time1)
    }

    @Test
    fun testHistoryOriginReturnedWhenEqualsStartTimeFlat() {
        val composition = createComposition(time1, time1)
        val flatResult = webTemplate.convertFromRawToFlat(composition)

        assertFlatResultHasOrigin(flatResult, time1)
    }

    @Test
    fun testHistoryOriginReturnedWhenDifferentFromStartTimeFlatFormatted() {
        val composition = createComposition(time1, time2)
        val flatFormattedResult = webTemplate.convertFormattedFromRawToFlat(composition)

        assertFlatFormattedResultHasOrigin(flatFormattedResult, time1Formatted)
    }

    @Test
    fun testHistoryOriginReturnedWhenEqualsStartTimeFlatFormatted() {
        val composition = createComposition(time1, time1)
        val flatFormattedResult = webTemplate.convertFormattedFromRawToFlat(composition)

        assertFlatFormattedResultHasOrigin(flatFormattedResult, time1Formatted)
    }

    @Test
    fun testHistoryOriginReturnedWhenDifferentFromStartTimeStructured() {
        val composition = createComposition(time1, time2)
        val structuredResult = webTemplate.convertFromRawToStructured(composition)

        assertStructuredResultHasOrigin(structuredResult, time1)
    }

    @Test
    fun testHistoryOriginReturnedWhenEqualsStartTimeStructured() {
        val composition = createComposition(time1, time1)
        val structuredResult = webTemplate.convertFromRawToStructured(composition)

        assertStructuredResultHasOrigin(structuredResult, time1)
    }

    @Test
    fun testHistoryOriginReturnedWhenDifferentFromStartTimeStructuredFormatted() {
        val composition = createComposition(time1, time2)
        val structuredFormattedResult = webTemplate.convertFormattedFromRawToStructured(composition)

        assertStructuredResultHasOrigin(structuredFormattedResult, time1)
    }

    @Test
    fun testHistoryOriginReturnedWhenEqualsStartTimeStructuredFormatted() {
        val composition = createComposition(time1, time1)
        val structuredFormattedResult = webTemplate.convertFormattedFromRawToStructured(composition)

        assertStructuredResultHasOrigin(structuredFormattedResult, time1)
    }

    @Test
    fun testHistoryOriginEmittedWhenNotGivenFlat() {
        val composition = createComposition()
        val flatResult = webTemplate.convertFromRawToFlat(composition)

        assertFlatResultHasOrigin(flatResult)
    }

    @Test
    fun testHistoryOriginEmittedWhenNotGivenFlatFormatted() {
        val composition = createComposition()
        val flatFormattedResult = webTemplate.convertFormattedFromRawToFlat(composition)

        assertFlatFormattedResultHasOrigin(flatFormattedResult)
    }

    @Test
    fun testHistoryOriginEmittedWhenNotGivenStructured() {
        val composition = createComposition()
        val structuredResult = webTemplate.convertFromRawToStructured(composition)

        assertStructuredResultHasOrigin(structuredResult)
    }

    @Test
    fun testHistoryOriginEmittedWhenNotGivenStructuredFormatted() {
        val composition = createComposition()
        val structuredFormattedResult = webTemplate.convertFormattedFromRawToStructured(composition)

        assertStructuredResultHasOrigin(structuredFormattedResult)
    }

    private fun createComposition (
        historyOrigin: OffsetDateTime? = null,
        startTime: OffsetDateTime? = null
    ): Composition {
        val flatInput = buildMap {
            put("ctx/language", "sl")
            put("ctx/territory", "SI")
            put("ctx/composer_name", "Composer")
            put("ctx/id_scheme", "ispek")
            put("ctx/id_namespace", "ispek")
            put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
            put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
            historyOrigin?.let { put(HISTORY_ORIGIN_INPUT_KEY, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it)) }
            startTime?.let { put(START_TIME_KEY, DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(it)) }
        }

        return webTemplate.convertFromFlatToRaw(flatInput, ConversionContext.create().build())!!
    }

    private fun assertFlatResultHasOrigin(
        flatResult: Map<String, Any>,
        expectedOrigin: OffsetDateTime? = null
    ) {
        assertThat(flatResult).containsKey(HISTORY_ORIGIN_KEY)

        expectedOrigin?.let {
            val storedOrigin = flatResult[HISTORY_ORIGIN_KEY] as? OpenEhrOffsetDateTime
            assertThat(storedOrigin?.dateTime).isEqualTo(expectedOrigin)
        }
    }

    private fun assertFlatFormattedResultHasOrigin(
        flatFormattedResult: Map<String, String>,
        expectedOriginFormatted: String? = null
    ) {
        assertThat(flatFormattedResult).containsKey(HISTORY_ORIGIN_KEY)

        expectedOriginFormatted?.let {
            val storedOrigin = flatFormattedResult[HISTORY_ORIGIN_KEY]
            assertThat(storedOrigin).isEqualTo(expectedOriginFormatted)
        }
    }

    private fun assertStructuredResultHasOrigin(
        structuredResult: JsonNode?,
        expectedOrigin: OffsetDateTime? = null
    ) {
        assertThat(structuredResult).isNotNull()
        assertThat(structuredResult!!.has("vitals")).isTrue()

        val vitals = structuredResult.get("vitals")
        assertThat(vitals.has("vitals")).isTrue()

        val vitalsArray = vitals.get("vitals")
        assertThat(vitalsArray.isArray).isTrue()
        assertThat(vitalsArray.size()).isGreaterThan(0)

        val firstVitals = vitalsArray.get(0)
        assertThat(firstVitals.has("haemoglobin_a1c")).isTrue()

        val haemoglobinArray = firstVitals.get("haemoglobin_a1c")
        assertThat(haemoglobinArray.isArray).isTrue()
        assertThat(haemoglobinArray.size()).isEqualTo(1)

        val haemoglobin = haemoglobinArray.get(0)
        assertThat(haemoglobin.has("history_origin")).isTrue()

        expectedOrigin?.let {
            val historyOrigin = haemoglobin.get("history_origin")
            assertThat(historyOrigin).isNotNull()
            assertThat(historyOrigin.isArray).isTrue()
            assertThat(historyOrigin.size()).isEqualTo(1)
            assertThat(historyOrigin.get(0).asText()).isEqualTo(expectedOrigin.toString())
        }
    }
}
