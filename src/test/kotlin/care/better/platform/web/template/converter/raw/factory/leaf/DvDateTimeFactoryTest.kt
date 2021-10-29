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
import care.better.platform.time.temporal.OpenEhrOffsetDateTime
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
import org.openehr.am.aom.CDateTime
import org.openehr.am.aom.CPrimitiveObject
import org.openehr.rm.datatypes.DvDateTime
import java.time.*
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.stream.Stream

/**
 * @author Matic Ribic
 */
class DvDateTimeFactoryTest : AbstractWebTemplateTest() {

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
        fun providePatternDateTimeAndExpectedResult(): Stream<Arguments> = Stream.of(
                // UTC datetime pattern, datetime in timezone
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17:35-0400", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17:35-04", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17:35.654789-04:00", "2021-08-06T23:17:35-04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01:17:35+0400", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01:17:35+04", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35Z", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2013-1-1T01:00:17.000Z", "2013-01-01T01:00:17Z", CONVERSION_EXCEPTION),

                // UTC datetime pattern, local datetime
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01:17:35", "2021-08-06T01:17:35+02:00", CONVERSION_EXCEPTION),

                // UTC datetime pattern, partial datetime in timezone
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17-04:00", "2021-08-06T23:17:00-04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01:17+04:00", "2021-08-06T01:17:00+04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01+04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23:17Z", "2021-08-06T23:17:00Z", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T23Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // UTC datetime pattern, local partial datetime
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01:17", "2021-08-06T01:17:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T01", "2021-08-06T01:00:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06T", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08-06", "2021-08-06T00:00:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021-08", "2021-08-01T00:00:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SSZ", "2021", "2021-01-01T00:00:00+01:00", CONVERSION_EXCEPTION),

                // local datetime pattern, datetime in timezone
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00"),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00"),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35Z", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23:17:35.000+04:00", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35+04:00"),

                // local datetime pattern, local datetime
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T01:17:35", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23:17:35.654", "2021-08-06T23:17:35+02:00", CONVERSION_EXCEPTION),

                // local datetime pattern, partial datetime in timezone
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23:17-04:00", "2021-08-06T23:17:00-04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T01:17+04:00", "2021-08-06T01:17:00+04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T01+04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23:17Z", "2021-08-06T23:17:00Z", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T23Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // local datetime pattern, local partial datetime
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T01:17", "2021-08-06T01:17:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T01", "2021-08-06T01:00:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06T", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08-06", "2021-08-06T00:00:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021-08", "2021-08-01T00:00:00+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS", "2021", "2021-01-01T00:00:00+01:00", CONVERSION_EXCEPTION),

                // optional seconds pattern, datetime in timezone
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00"),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00", "2021-08-06T01:17:35+04:00"),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35Z", CONVERSION_EXCEPTION),

                // optional seconds pattern, local datetime
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T01:17:35", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),

                // optional seconds pattern, partial datetime in timezone
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T23:17-04:00", "2021-08-07T05:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T23-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T01:17+04:00", "2021-08-05T23:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T01+04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T23:17Z", "2021-08-07T01:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T23Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // optional seconds pattern, local partial datetime
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T01:17", "2021-08-06T01:17", "2021-08-06T01:17"),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T01", "2021-08-06T01:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06T", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08-06", "2021-08-06T00:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021-08", "2021-08-01T00:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:??", "2021", "2021-01-01T00:00", CONVERSION_EXCEPTION),

                // not allowed seconds pattern, datetime in timezone
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T23:17:35-04:00", "2021-08-07T05:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T01:17:35+04:00", "2021-08-05T23:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T23:17:35Z", "2021-08-07T01:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T23:17:35.654Z", "2021-08-07T01:17", CONVERSION_EXCEPTION),

                // not allowed seconds pattern, local datetime
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T01:17:35", "2021-08-06T01:17", CONVERSION_EXCEPTION),

                // not allowed seconds pattern, partial datetime in timezone
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T23:17-04:00", "2021-08-07T05:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T23-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T01:17+04:00", "2021-08-05T23:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T01+04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T23:17Z", "2021-08-07T01:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T23Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // not allowed seconds pattern, local partial datetime
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T01:17", "2021-08-06T01:17", "2021-08-06T01:17"),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T01", "2021-08-06T01:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06T", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08-06", "2021-08-06T00:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021-08", "2021-08-01T00:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:XX", "2021", "2021-01-01T00:00", CONVERSION_EXCEPTION),

                // optional hour and minute and not allowed seconds pattern, datetime in timezone
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T23:17:35-04:00", "2021-08-07T05:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T01:17:35+04:00", "2021-08-05T23:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T23:17:35Z", "2021-08-07T01:17", CONVERSION_EXCEPTION),

                // optional hour and minute and not allowed seconds pattern, local datetime
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T01:17:35", "2021-08-06T01:17", CONVERSION_EXCEPTION),

                // optional hour and minute and not allowed seconds pattern, partial datetime in timezone
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T23:17-04:00", "2021-08-07T05:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T23-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T01:17+04:00", "2021-08-05T23:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T01+04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T23:17Z", "2021-08-07T01:17", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T23Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // optional hour and minute and not allowed seconds pattern, local partial datetime
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T01:17", "2021-08-06T01:17", "2021-08-06T01:17"),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T01", "2021-08-06T01", "2021-08-06T01"),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06T", "CONVERSION_EXCEPTION", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-mm-ddT??:??:XX", "2021-08", "2021-08-01", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddT??:??:XX", "2021", "2021-01-01", CONVERSION_EXCEPTION),

                // case insensitive pattern
                args("yyyy-mm-ddthh:mm:ss", "2021-08-06T01:17:35", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),
                args("YYYY-MM-DDTHH:MM:SS", "2021-08-06T01:17:35", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),
                args("yyyy-mm-ddthh:mm:xx", "2021-08-06T01:17", "2021-08-06T01:17", "2021-08-06T01:17"),
                args("YYYY-MM-DDTHH:MM:XX", "2021-08-06T01:17", "2021-08-06T01:17", "2021-08-06T01:17"),

                // not allowed pattern
                args("yyyy-XX-XXTXX:XX:XX", "2021-08-06T01:17:35", "2021", CONVERSION_EXCEPTION),
                args("yyyy-XX-XXTXX:XX:XX", "2021-08-06T01:17", "2021", CONVERSION_EXCEPTION),
                args("yyyy-XX-XXTXX:XX:XX", "2021-08-06T01", "2021", CONVERSION_EXCEPTION),
                args("yyyy-XX-XXTXX:XX:XX", "2021-08-06", "2021", CONVERSION_EXCEPTION),
                args("yyyy-XX-XXTXX:XX:XX", "2021-08", "2021", CONVERSION_EXCEPTION),
                args("yyyy-XX-XXTXX:XX:XX", "2021", "2021", "2021"),
                args("yyyy-MM-ddTXX:XX:XX", "2021-08-06T01", "2021-08-06", CONVERSION_EXCEPTION),
                args("yyyy-MM-ddTXX:XX:XX", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-MM-ddTHH:XX:XX", "2021-08-06T01", "2021-08-06T01", "2021-08-06T01"),

                // optional pattern
                args("yyyy-??-??T??:??:??", "2021-08-06T01:17:35", "2021-08-06T01:17:35+02:00", "2021-08-06T01:17:35+02:00"),
                args("yyyy-??-??T??:??:??", "2021-08-06T01:17:35.654789", "2021-08-06T01:17:35+02:00", CONVERSION_EXCEPTION),
                args("yyyy-??-??T??:??:??", "2021-08-06T01:17:35,654789", "2021-08-06T01:17:35+02:00", CONVERSION_EXCEPTION),
                args("yyyy-??-??T??:??:??", "2021-08-06T01:17", "2021-08-06T01:17", "2021-08-06T01:17"),
                args("yyyy-??-??T??:??:??", "2021-08-06T01", "2021-08-06T01", "2021-08-06T01"),
                args("yyyy-??-??T??:??:??", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-??-??T??:??:??", "2021-08", "2021-08", "2021-08"),
                args("yyyy-??-??T??:??:??", "2021", "2021", "2021"),
                args("yyyy-MM-??T??:??:??", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-MM-??T??:??:??", "2021-08", "2021-08", "2021-08"),
                args("yyyy-MM-ddT??:??:??", "2021-08-06T01", "2021-08-06T01", "2021-08-06T01"),
                args("yyyy-MM-ddT??:??:??", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-MM-ddTHH:??:??", "2021-08-06T01:17", "2021-08-06T01:17", "2021-08-06T01:17"),
                args("yyyy-MM-ddTHH:??:??", "2021-08-06T01", "2021-08-06T01", "2021-08-06T01"),
                args("yyyy-??-??T??:??:??.???Z", "2021-08-06T01:17:35.654Z", "2021-08-06T01:17:35.654Z", "2021-08-06T01:17:35.654Z"),
                args("yyyy-??-??T??:??:??.???Z", "2021-08-06T01:17:35.654789Z", "2021-08-06T01:17:35.654789Z", "2021-08-06T01:17:35.654789Z"),
                args("yyyy-??-??T??:??:??.???", "2021-08-06T01:17:35.654", "2021-08-06T01:17:35.654+02:00", "2021-08-06T01:17:35.654+02:00"),
                args("yyyy-??-??T??:??:??.???", "2021-08-06T01:17:35.654789", "2021-08-06T01:17:35.654789+02:00", "2021-08-06T01:17:35.654789+02:00"),

                // milliseconds pattern
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35.000-04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35,654-04:00", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.654789-04:00", "2021-08-06T23:17:35.654789-04:00", "2021-08-06T23:17:35.654789-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35.000+04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35.000Z", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.000Z", "2021-08-06T23:17:35.000Z", "2021-08-06T23:17:35.000Z"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.65Z", "2021-08-06T23:17:35.650Z", "2021-08-06T23:17:35.650Z"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.654321Z", "2021-08-06T23:17:35.654321Z", "2021-08-06T23:17:35.654321Z"),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35", "2021-08-06T23:17:35.000+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSSZ", "2021-08-06T23:17:35.654", "2021-08-06T23:17:35.654+02:00", CONVERSION_EXCEPTION),

                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35.000-04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35.000+04:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00"),
                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35.000Z", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z"),
                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35", "2021-08-06T23:17:35.000+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.SSS", "2021-08-06T23:17:35.654", "2021-08-06T23:17:35.654+02:00", "2021-08-06T23:17:35.654+02:00"),

                // optional milliseconds pattern
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35+04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z"),
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35", "2021-08-06T23:17:35+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM:SS.???Z", "2021-08-06T23:17:35.654", "2021-08-06T23:17:35.654+02:00", CONVERSION_EXCEPTION),

                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00", "2021-08-06T23:17:35-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00", "2021-08-06T23:17:35.654-04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35+04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00", "2021-08-06T23:17:35.654+04:00"),
                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z"),
                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35", "2021-08-06T23:17:35+02:00", "2021-08-06T23:17:35+02:00"),
                args("yyyy-mm-ddTHH:MM:SS.???", "2021-08-06T23:17:35.654", "2021-08-06T23:17:35.654+02:00", "2021-08-06T23:17:35.654+02:00"),

                // partial patterns with only required fields, datetime in timezone
                args("yyyy-mm-ddTHH:MMZ", "2021-08-06T23:17-04:00", "2021-08-06T23:17-04:00", "2021-08-06T23:17-04:00"),
                args("yyyy-mm-ddTHH:MMZ", "2021-08-06T23:17Z", "2021-08-06T23:17Z", "2021-08-06T23:17Z"),
                args("yyyy-mm-ddTHH:MMZ", "2021-08-06T23:17", "2021-08-06T23:17+02:00", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH:MM", "2021-08-06T23:17-04:00", "2021-08-06T23:17-04:00", "2021-08-06T23:17-04:00"),
                args("yyyy-mm-ddTHH:MM", "2021-08-06T23:17Z", "2021-08-06T23:17Z", "2021-08-06T23:17Z"),
                args("yyyy-mm-ddTHH:MM", "2021-08-06T23:17", "2021-08-06T23:17", "2021-08-06T23:17"),

                args("yyyy-mm-ddTHHZ", "2021-08-06T23:17-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHHZ", "2021-08-06T23-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHHZ", "2021-08-06T23", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH", "2021-08-06T23:17-04:00", "2021-08-07T05", CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH", "2021-08-06T23-04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-ddTHH", "2021-08-06T23", "2021-08-06T23", "2021-08-06T23"),

                args("yyyy-mm-ddZ", "2021-08-06Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06", "2021-08-06", "2021-08-06"),
                args("yyyy-mm-dd", "2021-08-06T23:17:35-04:00", "2021-08-07", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06T13:17:35-04:00", "2021-08-06", CONVERSION_EXCEPTION),
                args("yyyy-mm-dd", "2021-08-06T00:17:35+04:00", "2021-08-05", CONVERSION_EXCEPTION),

                args("yyyy-mm", "2021-08", "2021-08", "2021-08"),
                args("yyyy-mm", "2021-08-31T23:17:35-04:00", "2021-09", CONVERSION_EXCEPTION),
                args("yyyy-mm", "2021-08-06T13:17:35-04:00", "2021-08", CONVERSION_EXCEPTION),
                args("yyyy-mm", "2021-08-01T00:17:35+04:00", "2021-07", CONVERSION_EXCEPTION),

                args("yyyy", "2021", "2021", "2021"),
                args("yyyy", "2021-08", "2021", CONVERSION_EXCEPTION),
                args("yyyy", "2021-12-31T23:17:35-04:00", "2022", CONVERSION_EXCEPTION),
                args("yyyy", "2021-08-06T13:17:35-04:00", "2021", CONVERSION_EXCEPTION),
                args("yyyy", "2021-01-01T00:17:35+04:00", "2020", CONVERSION_EXCEPTION),

                // compact pattern
                args("yyyymmddTHHMMSSZ", "2021-08-06T01:17:35Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyymmddTHHMMSSZ", "20210806T011735", "20210806T011735+0200", CONVERSION_EXCEPTION),

                args("yyyymmddTHHMMSS", "2021-08-06T01:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyymmddTHHMMSS", "20210806T011735", "20210806T011735+0200", "20210806T011735+0200"),

                args("yyyy-mm-ddTHH:MM:SSZ", "20210806T011735", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                args("yyyymmddTHHMM??", "2021-08-06T01:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyymmddTHHMM??", "20210806T011735", "20210806T011735+0200", "20210806T011735+0200"),
                args("yyyymmddTHHMM??", "20210806T0117", "20210806T0117", "20210806T0117"),

                args("yyyymm??T??????", "2021-08-06T01", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyymm??T??????", "20210806T01", "20210806T01", "20210806T01"),

                args("yyyymmddTHHMMXX", "2021-08-06T01:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyymmddTHHMMXX", "20210806T0117", "20210806T0117", "20210806T0117"),

                args("yyyymmXXTXXXXXX", "2021-08", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyymmXXTXXXXXX", "202108", "202108", "202108"),

                // invalid separator position
                args("YYYY-MM-DDTHH:MM:SS", "2021-08-06T01:17:", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("YYYY-MM-DDTHH:MM:SS", "2021-08-06T01:", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("YYYY-MM-DDTHH:MM:SS", "2021-08-", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("YYYY-MM-DDTHH:MM:SS", "2021-", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-??-??T??:??:??", "2021-08-06T01:17:", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-??-??T??:??:??", "2021-08-06T01:", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-??-??T??:??:??", "2021-08-", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("yyyy-??-??T??:??:??", "2021-", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // undefined pattern
                args("", "2021-08-06T23:17:35.000Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args("", "2021-08-06T23:17:35.65Z", "2021-08-06T23:17:35.65Z", "2021-08-06T23:17:35.65Z"),
                args("", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z"),
                args("", "2021-08-06T23:17:35.654321Z", "2021-08-06T23:17:35.654321Z", "2021-08-06T23:17:35.654321Z"),
                args("", "2021-08-06T23:17:35.000+04:00", "2021-08-06T23:17:35+04:00", "2021-08-06T23:17:35+04:00"),
                args(null, "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z", "2021-08-06T23:17:35.654Z"),
                args("", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args(null, "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z", "2021-08-06T23:17:35Z"),
                args("", "2021-08-06T23:17:35.654", "2021-08-06T23:17:35.654+02:00", "2021-08-06T23:17:35.654+02:00"),
                args("", "2021-08-06T23:17:35", "2021-08-06T23:17:35+02:00", "2021-08-06T23:17:35+02:00"),
                args("", "2021-08-06T23:17Z", "2021-08-06T23:17Z", "2021-08-06T23:17Z"),
                args("", "2021-08-06T04:03:02Z", "2021-08-06T04:03:02Z", "2021-08-06T04:03:02Z"),
                args("", "2021-08-06T23Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "2021-08-06", "2021-08-06", "2021-08-06"),

                args("", "23:17:35.654+04:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "23:17:35.654Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args(null, "23:17:35.654Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "23:17:35Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "23:17:35.654", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "23:17:35", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "23:17Z", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "23:17", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),

                // localized date
                args("", "06/08/2021", "2021-08-06T00:00:00+02:00", "2021-08-06T00:00:00+02:00", Locale("en", "IE")),
                args("", "6/8/2021", "2021-08-06T00:00:00+02:00", "2021-08-06T00:00:00+02:00", Locale("en", "IE")),
                args("", "6/8/2021 4:03", "2021-08-06T04:03:00+02:00", "2021-08-06T04:03:00+02:00", Locale("en", "IE")),
                args("", "06.08.2021", "2021-08-06T00:00:00+02:00", "2021-08-06T00:00:00+02:00"),
                args("", "06.08.2021 04:03:02", "2021-08-06T04:03:02+02:00", "2021-08-06T04:03:02+02:00"),
                args("", "06.08.2021 4:03:02+02:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "08.2021", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
                args("", "6.8.2021", "2021-08-06T00:00:00+02:00", "2021-08-06T00:00:00+02:00"),
                args("", "06.08.2021 4:03:02", "2021-08-06T04:03:02+02:00", "2021-08-06T04:03:02+02:00"),
                args("", "6.8.2021 4:3:2", "2021-08-06T04:03:02+02:00", "2021-08-06T04:03:02+02:00"),
                args("", "6.8.2021 04:03:02", "2021-08-06T04:03:02+02:00", "2021-08-06T04:03:02+02:00"),
                args("yyyy-mm-ddTHH:mm:SS", "06.08.2021", "2021-08-06T00:00:00+02:00", "2021-08-06T00:00:00+02:00"),
                args("yyyy-mm-ddTHH:mm:SS", "06.08.2021 04:03:02", "2021-08-06T04:03:02+02:00", "2021-08-06T04:03:02+02:00"),
                args("yyyy-mm-ddTHH:mm:SS", "06.08.2021 4:03:02+02:00", CONVERSION_EXCEPTION, CONVERSION_EXCEPTION),
        )

        @JvmStatic
        @Suppress("unused")
        fun providePatternAndExpectedResultTypeForNow(): Stream<Arguments> = Stream.of(
                argsForNow("", OffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddTHH:mm:SS.SSSZ", OffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddTHH:mm:SS.???", OffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddTHH:mm:SSZ", OpenEhrOffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddTHH:mm:SS", OpenEhrOffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddTHH:mm:??", OpenEhrOffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddTHH:mm", OpenEhrOffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddT??:??:??", OpenEhrOffsetDateTime::class.java),
                argsForNow("yyyy-mm-ddTXX:XX:XX", LocalDate::class.java),
                argsForNow("yyyy-mm-dd", LocalDate::class.java),
                argsForNow("yyyy-mm-??", LocalDate::class.java),
                argsForNow("yyyy-mm-XX", YearMonth::class.java),
                argsForNow("yyyy-mm", YearMonth::class.java),
                argsForNow("yyyy-??", YearMonth::class.java),
                argsForNow("yyyy", Year::class.java),
                argsForNow("yyyy-??-??T??:??:??", OpenEhrOffsetDateTime::class.java),
                argsForNow("yyyy-XX-XXTXX:XX:XX", Year::class.java),
        )

        private fun args(pattern: String?, dateTime: String, resultInLenientMode: String, resultInStrictMode: String, locale: Locale? = null) =
            Arguments.of(pattern, dateTime, resultInLenientMode, resultInStrictMode, locale ?: defaultLocale)

        private fun <T : TemporalAccessor> argsForNow(pattern: String?, resultType: Class<T>) = Arguments.of(pattern, resultType)
    }

    @ParameterizedTest
    @MethodSource("providePatternDateTimeAndExpectedResult")
    fun handleDateTimeFieldInLenientMode(
            pattern: String?,
            dateTime: String,
            expectedResultInLenientMode: String,
            expectedResultInStrictMode: String,
            locale: Locale) {
        handleDateTimeField(pattern, dateTime, expectedResultInLenientMode, false, locale)
    }

    @ParameterizedTest
    @MethodSource("providePatternDateTimeAndExpectedResult")
    fun handleDateTimeFieldInStrictMode(
            pattern: String?,
            dateTime: String,
            expectedResultInLenientMode: String,
            expectedResultInStrictMode: String,
            locale: Locale) {
        handleDateTimeField(pattern, dateTime, expectedResultInStrictMode, true, locale)
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
        val pattern = "yyyy-MM-ddTHH:mm:SS.SSS"

        val value = handleField(now, pattern, false)
        assertThat(value).isNotNull

        val dateTime = OpenEhrDateTimeFormatter.ofPattern(pattern).parseDateTime(value!!)
        assertThat(dateTime).isInstanceOf(OffsetDateTime::class.java)
    }

    @Test
    fun compareCalculatedNowWithActualNow() {
        val pattern = "yyyy-MM-ddTHH:mm:SS.SSS"
        val nowBefore = OffsetDateTime.now()
        val value = handleField("now", pattern, false)
        val nowAfter = OffsetDateTime.now()

        assertThat(value).isNotNull
        val dateTime = OpenEhrDateTimeFormatter.ofPattern(pattern).parseDateTime(value!!)
        assertThat(dateTime).isInstanceOf(OffsetDateTime::class.java)

        assertThat(Duration.between(nowBefore, (dateTime as OffsetDateTime)).isNegative).isFalse
        assertThat(Duration.between(dateTime, nowAfter).isNegative).isFalse
    }

    private fun handleNow(pattern: String?, resultType: Class<out Temporal>, strictMode: Boolean) {
        val value = handleField("now", pattern, strictMode)
        assertThat(value).isNotNull

        val dateTime = OpenEhrDateTimeFormatter.ofPattern("").parseDateTime(value!!)
        assertThat(dateTime).isInstanceOf(resultType)
    }

    private fun handleDateTimeField(pattern: String?, dateTime: String, expectedResult: String, strictMode: Boolean, locale: Locale) {
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
        val dvDateTime = DvDateTime()
        var contextBuilder = ConversionContext.create().withLocale(locale).withLanguage(locale.language).withComposerName("Test")
        if (strictMode) contextBuilder = contextBuilder.withStrictMode()
        val conversionContext = contextBuilder.build()

        DvDateTimeFactory.handleField(
                conversionContext,
                getAmNode(pattern),
                AttributeDto.ofBlank(),
                dvDateTime,
                getTextNode(dateTime),
                WebTemplatePath.forBlankPath())
        return dvDateTime.value
    }

    private fun getAmNode(pattern: String?): AmNode {
        val cPrimitiveObject = CPrimitiveObject()
        cPrimitiveObject.rmTypeName = "DATE_TIME"
        val cDateTime = CDateTime()
        cDateTime.pattern = pattern
        cPrimitiveObject.item = cDateTime
        val amNode = AmNode(null, "ignored")
        amNode.attributes["value"] = AmAttribute(null, listOf(AmNode(cPrimitiveObject, null)))
        return amNode
    }

    private fun getTextNode(dateTime: String) = getObjectMapper().createObjectNode().textNode(dateTime)
}
