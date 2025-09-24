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
import care.better.platform.time.temporal.OpenEhrOffsetTime
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
import org.openehr.am.aom.CPrimitiveObject
import org.openehr.am.aom.CTime
import org.openehr.rm.datatypes.DvTime
import java.time.Duration
import java.time.OffsetTime
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.stream.Stream

/**
 * @author Matic Ribic
 */
class DvTimeFactoryTest : AbstractWebTemplateTest() {

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
        fun providePatternTimeAndExpectedResult(): Stream<Arguments> = Stream.of(
                // full time pattern
                args("HH:MM:SSZ", "23:17:35-04:00", "23:17:35-04:00", "23:17:35-04:00"),
                args("HH:MM:SSZ", "23:17:35-0400", "23:17:35-04:00", "23:17:35-04:00"),
                args("HH:MM:SSZ", "23:17:35-04", "23:17:35-04:00", "23:17:35-04:00"),
                args("HH:MM:SSZ", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:MM:SSZ", "23:17Z", "23:17:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SSZ", "23Z", "23:00:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SSZ", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SSZ", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SSZ", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS", "23:17:35-04:00", "23:17:35-04:00", "23:17:35-04:00"),
                args("HH:MM:SS", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:MM:SS", "23:17Z", "23:17:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "23Z", "23:00:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "23:17:35", "23:17:35", "23:17:35"),
                args("HH:MM:SS", "23:17", "23:17:00", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "23", "23:00:00", CONVERSION_EXCEPTION),

                // hour, minute pattern
                args("HH:MMZ", "23:17:35-04:00", "23:17-04:00", CONVERSION_EXCEPTION),
                args("HH:MMZ", "23:17:35Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:MMZ", "23:17Z", "23:17Z", "23:17Z"),
                args("HH:MMZ", "23Z", "23:00Z", CONVERSION_EXCEPTION),
                args("HH:MMZ", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MMZ", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MMZ", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM", "23:17:35-04:00", "23:17-04:00", CONVERSION_EXCEPTION),
                args("HH:MM", "23:17:35Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:MM", "23:17Z", "23:17Z", "23:17Z"),
                args("HH:MM", "23Z", "23:00Z", CONVERSION_EXCEPTION),
                args("HH:MM", "23:17:35", "23:17", CONVERSION_EXCEPTION),
                args("HH:MM", "23:17", "23:17", "23:17"),
                args("HH:MM", "23", "23:00", CONVERSION_EXCEPTION),

                // hour pattern
                args("HHZ", "12:17:35Z", "12Z", CONVERSION_EXCEPTION),
                args("HHZ", "23:17:35-04:00", "23-04:00", CONVERSION_EXCEPTION),
                args("HHZ", "23:17:35Z", "23Z", CONVERSION_EXCEPTION),
                args("HHZ", "23:17Z", "23Z", CONVERSION_EXCEPTION),
                args("HHZ", "23Z", "23Z", "23Z"),
                args("HHZ", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HHZ", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HHZ", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HHZ", "12+02:00", "12+02:00", "12+02:00"),
                args("HHZ", "23:17:35+0200", "23+02:00", CONVERSION_EXCEPTION),
                args("HH", "23:17:35-04:00", "23-04:00", CONVERSION_EXCEPTION),
                args("HH", "23:17:35Z", "23Z", CONVERSION_EXCEPTION),
                args("HH", "23:17Z", "23Z", CONVERSION_EXCEPTION),
                args("HH", "23Z", "23Z", "23Z"),
                args("HH", "23:17:35", "23", CONVERSION_EXCEPTION),
                args("HH", "23:17", "23", CONVERSION_EXCEPTION),
                args("HH", "23", "23", "23"),

                // milliseconds pattern
                args("HH:MM:SS.SSSZ", "23:17:35.653-04:00", "23:17:35.653-04:00", "23:17:35.653-04:00"),
                args("HH:MM:SS.SSSZ", "23:17:35.653Z", "23:17:35.653Z", "23:17:35.653Z"),
                args("HH:MM:SS.SSSZ", "23:17:35Z", "23:17:35.000Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSSZ", "23:17Z", "23:17:00.000Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSSZ", "23Z", "23:00:00.000Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSSZ", "23:17:35.653", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSSZ", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSSZ", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSSZ", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSS", "23:17:35.653-04:00", "23:17:35.653-04:00", "23:17:35.653-04:00"),
                args("HH:MM:SS.SSS", "23:17:35.653Z", "23:17:35.653Z", "23:17:35.653Z"),
                args("HH:MM:SS.SSS", "23:17:35Z", "23:17:35.000Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSS", "23:17Z", "23:17:00.000Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSS", "23Z", "23:00:00.000Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSS", "23:17:35.653", "23:17:35.653", "23:17:35.653"),
                args("HH:MM:SS.SSS", "23:17:35,653", "23:17:35.653", "23:17:35.653"),
                args("HH:MM:SS.SSS", "23:17:35", "23:17:35.000", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSS", "23:17", "23:17:00.000", CONVERSION_EXCEPTION),
                args("HH:MM:SS.SSS", "23", "23:00:00.000", CONVERSION_EXCEPTION),

                // optional milliseconds pattern
                args("HH:MM:SS.???Z", "23:17:35.653-04:00", "23:17:35.653-04:00", "23:17:35.653-04:00"),
                args("HH:MM:SS.???Z", "23:17:35.653Z", "23:17:35.653Z", "23:17:35.653Z"),
                args("HH:MM:SS.???Z", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:MM:SS.???Z", "23:17Z", "23:17:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.???Z", "23Z", "23:00:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.???Z", "23:17:35.653", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.???Z", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.???Z", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.???Z", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS.???", "23:17:35.653-04:00", "23:17:35.653-04:00", "23:17:35.653-04:00"),
                args("HH:MM:SS.???", "23:17:35.653Z", "23:17:35.653Z", "23:17:35.653Z"),
                args("HH:MM:SS.???", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:MM:SS.???", "23:17Z", "23:17:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.???", "23Z", "23:00:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS.???", "23:17:35.653", "23:17:35.653", "23:17:35.653"),
                args("HH:MM:SS.???", "23:17:35", "23:17:35", "23:17:35"),
                args("HH:MM:SS.???", "23:17", "23:17:00", CONVERSION_EXCEPTION),
                args("HH:MM:SS.???", "23", "23:00:00", CONVERSION_EXCEPTION),

                // optional second pattern
                args("HH:MM:??Z", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:MM:??Z", "23:17Z", "23:17Z", "23:17Z"),
                args("HH:MM:??Z", "23Z", "23:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:??Z", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:??Z", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:??Z", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:??", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:MM:??", "23:17Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:MM:??", "23Z", "23:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:??", "23:17:35", "23:17:35", "23:17:35"),
                args("HH:MM:??", "23:17", "23:17", "23:17"),
                args("HH:MM:??", "23", "23:00", CONVERSION_EXCEPTION),

                // optional minute and second pattern
                args("HH:??:??Z", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:??:??Z", "23:17Z", "23:17Z", "23:17Z"),
                args("HH:??:??Z", "23Z", "23Z", "23Z"),
                args("HH:??:??Z", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:??:??Z", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:??:??Z", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:??:??", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("HH:??:??", "23:17Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:??:??", "23Z", "23Z", CONVERSION_EXCEPTION),
                args("HH:??:??", "23:17:35", "23:17:35", "23:17:35"),
                args("HH:??:??", "23:17", "23:17", "23:17"),
                args("HH:??:??", "23", "23", "23"),

                // not allowed second pattern
                args("HH:MM:xxZ", "23:17:35Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:MM:xxZ", "23:17Z", "23:17Z", "23:17Z"),
                args("HH:MM:xxZ", "23Z", "23:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:xxZ", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:xxZ", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:xxZ", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:xx", "23:17:35Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:MM:xx", "23:17Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:MM:xx", "23Z", "23:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:xx", "23:17:35", "23:17", CONVERSION_EXCEPTION),
                args("HH:MM:xx", "23:17", "23:17", "23:17"),
                args("HH:MM:xx", "23", "23:00", CONVERSION_EXCEPTION),

                // not allowed second with optional minute pattern
                args("HH:??:xxZ", "23:17:35Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:??:xxZ", "23:17Z", "23:17Z", "23:17Z"),
                args("HH:??:xxZ", "23Z", "23Z", "23Z"),
                args("HH:??:xxZ", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:??:xxZ", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:??:xxZ", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:??:xx", "23:17:35Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:??:xx", "23:17Z", "23:17Z", CONVERSION_EXCEPTION),
                args("HH:??:xx", "23Z", "23Z", CONVERSION_EXCEPTION),
                args("HH:??:xx", "23:17:35", "23:17", CONVERSION_EXCEPTION),
                args("HH:??:xx", "23:17", "23:17", "23:17"),
                args("HH:??:xx", "23", "23", "23"),

                // not allowed minute and second pattern
                args("HH:xx:xxZ", "23:17:35Z", "23Z", CONVERSION_EXCEPTION),
                args("HH:xx:xxZ", "23:17Z", "23Z", CONVERSION_EXCEPTION),
                args("HH:xx:xxZ", "23Z", "23Z", "23Z"),
                args("HH:xx:xxZ", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:xx:xxZ", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:xx:xxZ", "23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:xx:xx", "23:17:35Z", "23Z", CONVERSION_EXCEPTION),
                args("HH:xx:xx", "23:17Z", "23Z", CONVERSION_EXCEPTION),
                args("HH:xx:xx", "23Z", "23Z", CONVERSION_EXCEPTION),
                args("HH:xx:xx", "23:17:35", "23", CONVERSION_EXCEPTION),
                args("HH:xx:xx", "23:17", "23", CONVERSION_EXCEPTION),
                args("HH:xx:xx", "23", "23", "23"),

                // case insensitive pattern
                args("HH:MM:SS", "23:17:35", "23:17:35", "23:17:35"),
                args("HH:MM:SS", "23:17:35", "23:17:35", "23:17:35"),
                args("HH:MM:xx", "23:17:35", "23:17", CONVERSION_EXCEPTION),
                args("HH:MM:XX", "23:17:35", "23:17", CONVERSION_EXCEPTION),

                // compact pattern
                args("HHMMSS", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HHMMSS", "231735", "231735", "231735"),

                args("HH:MM:SS", "231735", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                args("HH??XX", "231735", "2317", CONVERSION_EXCEPTION),
                args("HH??XX", "2317", "2317", "2317"),

                // date with time
                args("HH:MM:SSZ", "2021-08-06T23:17:35-04:00", "23:17:35-04:00", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "2021-08-06T23:17:35-04:00", "23:17:35-04:00", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "2021-08-06T01:17:35+04:00", "01:17:35+04:00", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "2021-08-06T23:17:35.654Z", "23:17:35Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "2021-08-06T23:17:35Z", "23:17:35Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "2021-08-06T23Z", "23:00:00Z", CONVERSION_EXCEPTION),
                args("HH:MM:SS", "2021-08-06TZ", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS", "2021-08-06Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // invalid separator position
                args("HH:MM:SS", "23:17:", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS", "23:", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // undefined pattern
                args("", "2021-08-06T23:17:35.654Z", "23:17:35.654Z", CONVERSION_EXCEPTION),
                args("", "23:17:35.654+04:00", "23:17:35.654+04:00", "23:17:35.654+04:00"),
                args("", "23:17:35.654Z", "23:17:35.654Z", "23:17:35.654Z"),
                args(null, "23:17:35.654Z", "23:17:35.654Z", "23:17:35.654Z"),
                args("", "23:17:35Z", "23:17:35Z", "23:17:35Z"),
                args("", "23:17:35.654", "23:17:35.654", "23:17:35.654"),
                args("", "23:17:35", "23:17:35", "23:17:35"),
                args("", "23:17Z", "23:17Z", "23:17Z"),
                args("", "23:17", "23:17", "23:17"),
                args("", "04:03:02+02:00", "04:03:02+02:00", "04:03:02+02:00"),

                // localized date
                args("", "02:03:04 Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "02:03:04 +02:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS", "02:03:04 Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("HH:MM:SS", "02:03:04 +02:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
        )

        @JvmStatic
        @Suppress("unused")
        fun providePatternAndExpectedResultTypeForNow(): Stream<Arguments> = Stream.of(
                argsForNow("", OffsetTime::class.java),
                argsForNow("HH:mm:SS.SSSZ", OffsetTime::class.java),
                argsForNow("HH:mm:SS.???", OffsetTime::class.java),
                argsForNow("HH:mm:SSZ", OpenEhrOffsetTime::class.java),
                argsForNow("HH:mm:SS", OpenEhrOffsetTime::class.java),
                argsForNow("HH:mm:??", OpenEhrOffsetTime::class.java),
                argsForNow("HH:mm", OpenEhrOffsetTime::class.java),
                argsForNow("HH:??:??", OpenEhrOffsetTime::class.java),
        )

        private fun args(pattern: String?, Time: String, resultInLenientMode: String, resultInStrictMode: String) =
                Arguments.of(pattern, Time, resultInLenientMode, resultInStrictMode)

        private fun <T : TemporalAccessor> argsForNow(pattern: String?, resultType: Class<T>) = Arguments.of(pattern, resultType)
    }

    @ParameterizedTest
    @MethodSource("providePatternTimeAndExpectedResult")
    fun handleTimeFieldInLenientMode(pattern: String?, Time: String, expectedResultInLenientMode: String, expectedResultInStrictMode: String) {
        handleTimeField(pattern, Time, expectedResultInLenientMode, false)
    }

    @ParameterizedTest
    @MethodSource("providePatternTimeAndExpectedResult")
    fun handleTimeFieldInStrictMode(pattern: String?, Time: String, expectedResultInLenientMode: String, expectedResultInStrictMode: String) {
        handleTimeField(pattern, Time, expectedResultInStrictMode, true)
    }


    @ParameterizedTest
    @MethodSource("providePatternAndExpectedResultTypeForNow")
    fun handleNowInLenientMode(pattern: String?, resultType: Class<out Temporal>) {
        handleNow(pattern, resultType, false)
    }

    @ParameterizedTest
    @MethodSource("providePatternAndExpectedResultTypeForNow")
    fun handleNowInStrictMode(pattern: String?, resultType: Class<out Temporal>,) {
        handleNow(pattern, resultType, true)
    }


    @ParameterizedTest
    @ValueSource(strings = ["now", "NOW", "nOw", "NoW"])
    fun handleNowCaseInsensitive(now: String) {
        val pattern = "HH:MM:SS.SSS"

        val value = handleField(now, pattern, false)
        assertThat(value).isNotNull

        val time = OpenEhrDateTimeFormatter.ofPattern(pattern).parseTime(value!!)
        assertThat(time).isInstanceOf(OffsetTime::class.java)
    }

    @Test
    fun compareCalculatedNowWithActualNow() {
        val pattern = "HH:MM:SS.SSS"
        val nowBefore = OpenEhrOffsetTime.now()
        val value = handleField("now", pattern, false)
        val nowAfter = OpenEhrOffsetTime.now()

        assertThat(value).isNotNull
        val time = OpenEhrDateTimeFormatter.ofPattern(pattern).parseTime(value!!)
        assertThat(time).isInstanceOf(OffsetTime::class.java)

        val durationFromBefore = Duration.between(nowBefore, (time as OffsetTime)).abs()
        assertThat(durationFromBefore.toMillis()).isLessThanOrEqualTo(500)

        val durationFromAfter = Duration.between(time, nowAfter).abs()
        assertThat(durationFromAfter.toMillis()).isLessThanOrEqualTo(500)
    }

    private fun handleNow(pattern: String?, resultType: Class<out Temporal>, strictMode: Boolean) {
        val value = handleField("now", pattern, strictMode)
        assertThat(value).isNotNull

        val time = OpenEhrDateTimeFormatter.ofPattern("").parseTime(value!!)
        assertThat(time).isInstanceOf(resultType)
    }

    private fun handleTimeField(pattern: String?, Time: String, expectedResult: String, strictMode: Boolean) {
        if (expectedResult == CONVERSION_EXCEPTION) {
            assertThatThrownBy {
                handleField(Time, pattern, strictMode)
            }.describedAs("Parsing \"$Time\" using pattern \"$pattern\" in ${if (strictMode) "strict" else "lenient"} mode.").isInstanceOf(ConversionException::class.java)
        } else {
            val actualResult = handleField(Time, pattern, strictMode)
            assertThat(actualResult).describedAs("Parsing \"$Time\" using pattern \"$pattern\" in ${if(strictMode) "strict" else "lenient"} mode.").isEqualTo(expectedResult)
        }
    }

    private fun handleField(Time: String, pattern: String?, strictMode: Boolean): String? {
        val dvTime = DvTime()
        var contextBuilder = ConversionContext.create().withLocale(defaultLocale).withLanguage(defaultLocale.language).withComposerName("Test")
        if (strictMode) contextBuilder = contextBuilder.withStrictMode()
        val conversionContext = contextBuilder.build()

        DvTimeFactory.handleField(conversionContext, getAmNode(pattern), AttributeDto.ofBlank(), dvTime, getTextNode(Time), WebTemplatePath.forBlankPath())
        return dvTime.value
    }

    private fun getAmNode(pattern: String?): AmNode {
        val cPrimitiveObject = CPrimitiveObject()
        cPrimitiveObject.rmTypeName = "TIME"
        val cTime = CTime()
        cTime.pattern = pattern
        cPrimitiveObject.item = cTime
        val amNode = AmNode(null, "ignored")
        amNode.attributes["value"] = AmAttribute(null, listOf(AmNode(cPrimitiveObject, null)))
        return amNode
    }

    private fun getTextNode(Time: String) = getObjectMapper().createObjectNode().textNode(Time)
}
