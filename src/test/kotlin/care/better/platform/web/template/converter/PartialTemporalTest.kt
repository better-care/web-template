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
import care.better.platform.path.PathValueExtractor
import care.better.platform.time.temporal.OpenEhrLocalDateTime
import care.better.platform.time.temporal.OpenEhrLocalTime
import care.better.platform.time.temporal.OpenEhrOffsetDateTime
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class OpenEhrTemporalTest : AbstractWebTemplateTest() {

    private val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/dates.opt"), WebTemplateBuilderContext("en"))

    @Test
    fun testDateTimePatterns() {
        assertThat(webTemplate.findWebTemplateNode("dates/dates/any").getInput()?.validation).isNull()
        assertThat(webTemplate.findWebTemplateNode("dates/dates/date_and_time").getInput()?.validation?.pattern).isEqualTo("yyyy-mm-ddTHH:MM:SS")
        assertThat(webTemplate.findWebTemplateNode("dates/dates/date_and_partial_time").getInput()?.validation?.pattern).isEqualTo("yyyy-mm-ddTHH:??:??")
    }

    @Test
    fun testDatePatterns() {
        assertThat(webTemplate.findWebTemplateNode("dates/dates/any_date").getInput()?.validation).isNull()
        assertThat(webTemplate.findWebTemplateNode("dates/dates/full_date").getInput()?.validation?.pattern).isEqualTo("yyyy-mm-dd")
        assertThat(webTemplate.findWebTemplateNode("dates/dates/partial_date").getInput()?.validation?.pattern).isEqualTo("yyyy-??-XX")
        assertThat(webTemplate.findWebTemplateNode("dates/dates/partial_date_with_month").getInput()?.validation?.pattern).isEqualTo("yyyy-mm-??")
    }

    @Test
    fun testTimePatterns() {
        assertThat(webTemplate.findWebTemplateNode("dates/dates/any_time").getInput()?.validation).isNull()
        assertThat(webTemplate.findWebTemplateNode("dates/dates/full_time").getInput()?.validation?.pattern).isEqualTo("HH:MM:SS")
        assertThat(webTemplate.findWebTemplateNode("dates/dates/partial_time").getInput()?.validation?.pattern).isEqualTo("HH:??:XX")
        assertThat(webTemplate.findWebTemplateNode("dates/dates/partial_time_with_minutes").getInput()?.validation?.pattern).isEqualTo("HH:MM:??")
    }

    @Test
    fun testDateTimeAny() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0002]/value/value")
        val attributeName = "any"
        assertValueMatches(webTemplate, extractor, attributeName, "2019", "2019", Year.of(2019))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02", "2019-02", YearMonth.of(2019, 2))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-01", "2019-02-01", LocalDate.of(2019, 2, 1))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-01T13", "2019-02-01T13", OpenEhrLocalDateTime.of(2019, 2, 1, 13))

        assertValueMatches(
            webTemplate,
            extractor,
            attributeName,
            "2019-02-01T13:10",
            "2019-02-01T13:10",
            OpenEhrLocalDateTime.of(2019, 2, 1, 13, 10))

        val secondOffsetDateTime = ZonedDateTime.of(2019, 2, 1, 13, 10, 20, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(
                webTemplate,
                extractor,
                attributeName,
                "2019-02-01T13:10:20",
                "2019-02-01T13:10:20+01:00",
                OpenEhrOffsetDateTime.of(2019, 2, 1, 13, 10, 20, null, ZoneId.systemDefault().rules.getOffset(secondOffsetDateTime.toInstant())))

        val thirdOffsetDateTime = ZonedDateTime.of(2019, 2, 1, 13, 10, 20, 777000000, ZoneId.systemDefault()).toOffsetDateTime()
        val thirdOffsetDateTimeString = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(thirdOffsetDateTime)
        assertValueMatches(webTemplate, extractor, attributeName, thirdOffsetDateTimeString, thirdOffsetDateTimeString, thirdOffsetDateTime)
    }

    /**
     * When datetime is specified as full in the template we allow only missing seconds, mili/nanoseconds and zone.
     */
    @Test
    fun testDateTimeFull() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0003]/value/value")
        val attributeName = "date_and_time"
        val firstDateTime = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(
                webTemplate,
                extractor,
                attributeName,
                "2019",
                "2019-01-01T00:00:00+01:00",
                firstDateTime)

        val secondDateTime = ZonedDateTime.of(2019, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(
                webTemplate,
                extractor,
                attributeName,
                "2019-02",
                "2019-02-01T00:00:00+01:00",
                secondDateTime)

        assertValueMatches(
                webTemplate,
                extractor,
                attributeName,
                "2019-02-01",
                "2019-02-01T00:00:00+01:00",
                secondDateTime)

        val thirdDateTime = ZonedDateTime.of(2019, 2, 3, 13, 0, 0, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(
                webTemplate,
                extractor,
                attributeName,
                "2019-02-03T13",
                "2019-02-03T13:00:00+01:00",
                thirdDateTime)

        val firstOffsetDateTime = ZonedDateTime.of(2019, 2, 3, 13, 10, 0, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(
            webTemplate,
            extractor,
            attributeName,
            "2019-02-03T13:10",
            "2019-02-03T13:10:00+01:00",
            firstOffsetDateTime)

        val secondOffsetDateTime = ZonedDateTime.of(2019, 2, 3, 13, 10, 20, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(
            webTemplate,
            extractor,
            attributeName,
            "2019-02-03T13:10:20",
            "2019-02-03T13:10:20+01:00",
            secondOffsetDateTime)

        val thirdOffsetDateTime = ZonedDateTime.of(2019, 2, 3, 13, 10, 20, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-03T13:10:20.777+01:00", "2019-02-03T13:10:20+01:00", thirdOffsetDateTime)
    }

    @Test
    fun testDateTimePartial() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0004]/value/value")
        val attributeName = "date_and_partial_time"
        assertValueMatches(webTemplate, extractor, attributeName, "2019", "2019-01-01T00", OpenEhrLocalDateTime.of(2019, 1, 1, 0))

        assertValueMatches(webTemplate, extractor, attributeName, "2019-02", "2019-02-01T00", OpenEhrLocalDateTime.of(2019, 2, 1, 0))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-03", "2019-02-03T00", OpenEhrLocalDateTime.of(2019, 2, 3, 0))

        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-03T13", "2019-02-03T13", OpenEhrLocalDateTime.of(2019, 2, 3, 13))

        assertValueMatches(
            webTemplate,
            extractor,
            attributeName,
            "2019-02-01T13:10",
            "2019-02-01T13:10",
            OpenEhrLocalDateTime.of(2019, 2, 1, 13, 10),
            "2019-02-01T13:10")

        val secondOffsetDateTime = ZonedDateTime.of(2019, 2, 1, 13, 10, 20, 0, ZoneId.systemDefault()).toOffsetDateTime()
        assertValueMatches(
            webTemplate,
            extractor,
            attributeName,
            "2019-02-01T13:10:20",
            "2019-02-01T13:10:20+01:00",
            OpenEhrOffsetDateTime.of(2019, 2, 1, 13, 10, 20, null, ZoneId.systemDefault().rules.getOffset(secondOffsetDateTime.toInstant())))

        val thirdOffsetDateTime =
            OpenEhrOffsetDateTime.of(2019, 2, 1, 13, 10, 20, null, ZoneId.systemDefault().rules.getOffset(secondOffsetDateTime.toInstant()))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-01T13:10:20.777+01:00", "2019-02-01T13:10:20+01:00", thirdOffsetDateTime)
    }

    @Test
    fun testDateAny() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0005]/value/value")
        val attributeName = "any_date"
        assertValueMatches(webTemplate, extractor, attributeName, "2019", "2019", Year.of(2019))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02", "2019-02", YearMonth.of(2019, 2))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-01", "2019-02-01", LocalDate.of(2019, 2, 1))
    }

    @Test
    fun testDateFull() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0006]/value/value")
        val attributeName = "full_date"
        assertValueMatches(webTemplate, extractor, attributeName, "2019", "2019-01-01", LocalDate.of(2019, 1, 1))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02", "2019-02-01", LocalDate.of(2019, 2, 1))

        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-01", "2019-02-01", LocalDate.of(2019, 2, 1))
    }

    @Test
    fun testDatePartial() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0007]/value/value")
        val attributeName = "partial_date"
        assertValueMatches(webTemplate, extractor, attributeName, "2019", "2019", Year.of(2019))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02", "2019-02", YearMonth.of(2019, 2))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-01", "2019-02", YearMonth.of(2019, 2))
    }

    @Test
    fun testDatePartialWithMonth() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0008]/value/value")
        val attributeName = "partial_date_with_month"
        assertValueMatches(webTemplate, extractor, attributeName, "2019", "2019-01", YearMonth.of(2019, 1))

        assertValueMatches(webTemplate, extractor, attributeName, "2019-02", "2019-02", YearMonth.of(2019, 2))
        assertValueMatches(webTemplate, extractor, attributeName, "2019-02-01", "2019-02-01", LocalDate.of(2019, 2, 1))
    }

    @Test
    fun testTimeAny() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0009]/value/value")
        val attributeName = "any_time"
        assertValueMatches(webTemplate, extractor, attributeName, "13:12:11", "13:12:11", OpenEhrLocalTime.of(13, 12, 11))
    }

    @Test
    fun testTimeFull() {
        val extractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.dates.v0]/data[at0001]/items[at0010]/value/value")
        val attributeName = "full_time"
        assertValueMatches(webTemplate, extractor, attributeName, "13:12:11", "13:12:11", OpenEhrLocalTime.of(13, 12, 11))
    }

    private fun assertValueMatches(
            webTemplate: WebTemplate,
            extractor: PathValueExtractor,
            attributeName: String,
            incomingValue: String,
            compositionValue: String) {
        assertValueMatches(webTemplate, extractor, attributeName, incomingValue, compositionValue, compositionValue)
    }

    private fun assertValueMatches(
            webTemplate: WebTemplate,
            extractor: PathValueExtractor,
            attributeName: String,
            incomingValue: String,
            compositionValue: String,
            nativeValue: Any,
            formattedCompositionValue: String = compositionValue) {
        val composition = buildComposition(webTemplate, getValues(attributeName, incomingValue))
        assertThat(extractor.getValue(composition)).hasSize(1).containsExactly(compositionValue)

        val retrieveFormatted: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(retrieveFormatted["dates/dates/$attributeName"]).isEqualTo(formattedCompositionValue)
        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(retrieve["dates/dates/$attributeName"]).isEqualTo(nativeValue)
    }

    private fun getValues(attributeName: String, value: String): Map<String, Any> {
        val values: MutableMap<String, Any> = HashMap()
        values["ctx/language"] = "en"
        values["ctx/territory"] = "SI"
        values["ctx/composer_name"] = "test"
        values["dates/dates/$attributeName"] = value
        return values
    }

    private fun buildComposition(webTemplate: WebTemplate, values: Map<String, Any>): Composition {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())
        assertThat(composition).isNotNull
        return composition!!
    }
}
