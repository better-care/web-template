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

package care.better.platform.web.template.converter.raw.factory.leaf

import care.better.platform.template.AmAttribute
import care.better.platform.template.AmNode
import care.better.platform.time.format.OpenEhrDateTimeFormatter
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.openehr.am.aom.CDate
import org.openehr.am.aom.CPrimitiveObject
import org.openehr.rm.datatypes.DvDate
import java.time.LocalDate
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.stream.Stream

/**
 * @author Matic Ribic
 */
class DvDateFactoryTest : AbstractWebTemplateTest() {

    companion object {
        private lateinit var defaultTimeZone: TimeZone
        private var defaultLocale = Locale.GERMANY
        private const val CONVERSION_EXCEPTION = "CONVERSION_EXCEPTION"

        @BeforeAll
        @JvmStatic
        @Suppress("unused")
        internal fun init() {
            defaultTimeZone = TimeZone.getDefault()
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Ljubljana"))
        }

        @AfterAll
        @JvmStatic
        @Suppress("unused")
        internal fun tearDown() {
            TimeZone.setDefault(defaultTimeZone)
        }

        @JvmStatic
        @Suppress("unused")
        fun providePatternDateAndExpectedResult(): Stream<Arguments> = Stream.of(
                // full date pattern
                args("yyyy-mm-dd", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-mm-dd", "2021-08", "2021-08-01", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021", "2021-01-01", CONVERSION_EXCEPTION),

                // year, month pattern
                args("yyyy-mm", "2021-08-06", "2021-08", CONVERSION_EXCEPTION),
                args("yyyy-mm", "2021-08", "2021-08", "2021-08"),
                args("yyyy-mm", "2021", "2021-01", CONVERSION_EXCEPTION),

                // year pattern
                args("yyyy", "2021-08-06", "2021", CONVERSION_EXCEPTION),
                args("yyyy", "2021-08", "2021", CONVERSION_EXCEPTION),
                args("yyyy", "2021", "2021", "2021"),

                // optional day pattern
                args("yyyy-mm-??", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-mm-??", "2021-08", "2021-08", "2021-08"),
                args("yyyy-mm-??", "2021", "2021-01", CONVERSION_EXCEPTION),

                // optional month and day pattern
                args("yyyy-??-??", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-??-??", "2021-08", "2021-08", "2021-08"),
                args("yyyy-??-??", "2021", "2021", "2021"),

                // not allowed day pattern
                args("yyyy-mm-xx", "2021-08-06", "2021-08", CONVERSION_EXCEPTION),
                args("yyyy-mm-xx", "2021-08", "2021-08", "2021-08"),
                args("yyyy-mm-xx", "2021", "2021-01", CONVERSION_EXCEPTION),

                // not allowed day with optional month pattern
                args("yyyy-??-xx", "2021-08-06", "2021-08", CONVERSION_EXCEPTION),
                args("yyyy-??-xx", "2021-08", "2021-08", "2021-08"),
                args("yyyy-??-xx", "2021", "2021", "2021"),

                // not allowed month and day pattern
                args("yyyy-xx-xx", "2021-08-06", "2021", CONVERSION_EXCEPTION),
                args("yyyy-xx-xx", "2021-08", "2021", CONVERSION_EXCEPTION),
                args("yyyy-xx-xx", "2021", "2021", "2021"),

                // case insensitive pattern
                args("yyyy-mm-dd", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("YYYY-MM-DD", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-mm-xx", "2021-08-06", "2021-08", CONVERSION_EXCEPTION),
                args("YYYY-MM-XX", "2021-08-06", "2021-08", CONVERSION_EXCEPTION),

                // compact pattern
                args("yyyymmdd", "2021-08-06", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyymmdd", "20210806", "20210806", "20210806"),

                args("yyyy-mm-dd", "20210806T011735", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "20210806", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                args("yyyy??XX", "2021-08", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy??XX", "202108", "202108", "202108"),

                // date with time
                args("yyyy-mm-dd", "2021-08-06T23:17:35-04:00", "2021-08-06", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06T01:17:35+04:00", "2021-08-06", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06T23:17:35.654Z", "2021-08-06", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06T23:17:35Z", "2021-08-06", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06T23Z", "2021-08-06", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06TZ", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06T", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // invalid separator position
                args("yyyy-mm-dd", "2021-08-", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // undefined pattern
                args("", "2021-08-06T23:17:35.654Z", "2021-08-06", CONVERSION_EXCEPTION),
                args("", "2021-08-06T23:17:35Z", "2021-08-06", CONVERSION_EXCEPTION),
                args("", "2021-08-06T23Z", "2021-08-06", CONVERSION_EXCEPTION),
                args("", "2021-08-06", "2021-08-06", "2021-08-06"),
                args(null, "2021-08-06", "2021-08-06", "2021-08-06"),
                args("", "2021-08-06", "2021-08-06", "2021-08-06"),
                args(null, "2021-08", "2021-08", "2021-08"),
                args("", "2021", "2021", "2021"),
                args(null, "2021", "2021", "2021"),

                // undefined pattern
                args("", "2021-08-06T23:17:35.654Z", "2021-08-06", CONVERSION_EXCEPTION),
                args("", "2021-08-06", "2021-08-06", "2021-08-06"),
                args(null, "2021-08-06", "2021-08-06", "2021-08-06"),
                args("", "2021-08", "2021-08", "2021-08"),
                args("", "2021", "2021", "2021"),

                // localized date
                // commented/disabled because of http://openjdk.java.net/jeps/252
//                args("", "8/6/2021", "2021-08-06", "2021-08-06", Locale("en_IE")),
//                args("", "08/06/2021", "2021-08-06", "2021-08-06", Locale("en_IE")),
                args("", "06.08.2021", "2021-08-06", "2021-08-06"),
                args("", "6.8.2021", "2021-08-06", "2021-08-06"),
                args("yyyy-mm-dd", "06.08.2021", "2021-08-06", "2021-08-06"),
                args("yyyy-mm-dd", "6.8.2021", "2021-08-06", "2021-08-06"),
        )

        @JvmStatic
        @Suppress("unused")
        fun providePatternAndExpectedResultTypeForNow(): Stream<Arguments> = Stream.of(
                argsForNow("", LocalDate::class.java),
                argsForNow("yyyy-mm-dd", LocalDate::class.java),
                argsForNow("yyyy-mm-??", LocalDate::class.java),
                argsForNow("yyyy-mm-XX", YearMonth::class.java),
                argsForNow("yyyy-mm", YearMonth::class.java),
                argsForNow("yyyy-??", YearMonth::class.java),
                argsForNow("yyyy", Year::class.java),
                argsForNow("yyyy-??-??", LocalDate::class.java),
                argsForNow("yyyy-XX-XX", Year::class.java),
        )

        private fun args(pattern: String?, dateTime: String, resultInLenientMode: String, resultInStrictMode: String, locale: Locale? = null) =
            Arguments.of(pattern, dateTime, resultInLenientMode, resultInStrictMode, locale ?: defaultLocale)

        private fun <T : TemporalAccessor> argsForNow(pattern: String?, resultType: Class<T>) = Arguments.of(pattern, resultType)
    }

    @ParameterizedTest
    @MethodSource("providePatternDateAndExpectedResult")
    fun handleDateFieldInLenientMode(
            pattern: String?,
            dateTime: String,
            expectedResultInLenientMode: String,
            expectedResultInStrictMode: String,
            locale: Locale) {
        handleDateField(pattern, dateTime, expectedResultInLenientMode, false, locale)
    }

    @ParameterizedTest
    @MethodSource("providePatternDateAndExpectedResult")
    fun handleDateFieldInStrictMode(
            pattern: String?,
            dateTime: String,
            expectedResultInLenientMode: String,
            expectedResultInStrictMode: String,
            locale: Locale) {
        handleDateField(pattern, dateTime, expectedResultInStrictMode, true, locale)
    }

    @ParameterizedTest
    @MethodSource("providePatternAndExpectedResultTypeForNow")
    fun handleNowInLenientMode(pattern: String?, resultType: Class<out Temporal>) {
        handleNow(pattern, resultType, false)
    }

    @ParameterizedTest
    @MethodSource("providePatternAndExpectedResultTypeForNow")
    fun handleNowInStrictMode(pattern: String?, resultType: Class<out Temporal>) {
        handleNow(pattern, resultType, true)
    }

    @ParameterizedTest
    @ValueSource(strings = ["now", "NOW", "nOw", "NoW"])
    fun handleNowCaseInsensitive(now: String) {
        val pattern = "yyyy-MM-dd"

        val value = handleField(now, pattern, false)
        assertThat(value).isNotNull

        val date = OpenEhrDateTimeFormatter.ofPattern(pattern).parseDateTime(value!!)
        assertThat(date).isInstanceOf(LocalDate::class.java)
    }

    @Test
    fun compareCalculatedNowWithActualNow() {
        val pattern = "yyyy-MM-dd"
        val nowBefore = LocalDate.now()
        val value = handleField("now", pattern, false)
        val nowAfter = LocalDate.now()

        assertThat(value).isNotNull
        val date = OpenEhrDateTimeFormatter.ofPattern(pattern).parseDateTime(value!!)
        assertThat(date).isInstanceOf(LocalDate::class.java)

        assertThat(Period.between(nowBefore, (date as LocalDate)).isNegative).isFalse
        assertThat(Period.between(date, nowAfter).isNegative).isFalse
    }

    private fun handleNow(pattern: String?, resultType: Class<out Temporal>, strictMode: Boolean) {
        val value = handleField("now", pattern, strictMode)
        assertThat(value).isNotNull

        val date = OpenEhrDateTimeFormatter.ofPattern("").parseDate(value!!)
        assertThat(date).isInstanceOf(resultType)
    }

    private fun handleDateField(pattern: String?, dateTime: String, expectedResult: String, strictMode: Boolean, locale: Locale) {
        if (expectedResult == CONVERSION_EXCEPTION) {
            assertThatThrownBy {
                handleField(dateTime, pattern, strictMode, locale)
            }.describedAs("Parsing \"$dateTime\" using pattern \"$pattern\" in ${if (strictMode) "strict" else "lenient"} mode.")
                .isInstanceOf(ConversionException::class.java)
        } else {
            val actualResult = handleField(dateTime, pattern, strictMode, locale)
            assertThat(actualResult).describedAs("Parsing \"$dateTime\" using pattern \"$pattern\" in ${if (strictMode) "strict" else "lenient"} mode.")
                .isEqualTo(expectedResult)
        }
    }

    private fun handleField(dateTime: String, pattern: String?, strictMode: Boolean, locale: Locale = defaultLocale): String? {
        val dvDate = DvDate()
        var contextBuilder = ConversionContext.create().withLocale(locale).withLanguage(locale.language).withComposerName("Test")
        if (strictMode) contextBuilder = contextBuilder.withStrictMode()
        val conversionContext = contextBuilder.build()

        DvDateFactory.handleField(conversionContext, getAmNode(pattern), AttributeDto.ofBlank(), dvDate, getTextNode(dateTime), WebTemplatePath.forBlankPath())
        return dvDate.value
    }

    private fun getAmNode(pattern: String?): AmNode {
        val cPrimitiveObject = CPrimitiveObject()
        cPrimitiveObject.rmTypeName = "DATE"
        val cDate = CDate()
        cDate.pattern = pattern
        cPrimitiveObject.item = cDate
        val amNode = AmNode(null, "ignored")
        amNode.attributes["value"] = AmAttribute(null, listOf(AmNode(cPrimitiveObject, null)))
        return amNode
    }

    private fun getTextNode(dateTime: String) = getObjectMapper().createObjectNode().textNode(dateTime)
}
