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

package care.better.platform.web.template.converter.value

import care.better.platform.web.template.converter.exceptions.ConversionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.time.OffsetTime
import java.time.ZoneOffset

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class SimpleValueConverterTest {
    @Test
    fun testInvalidDateFormat() {
        assertThatThrownBy { SimpleValueConverter.parseDateTime("15.11.2016 00:00:00") }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageStartingWith("Unable to convert value to datetime")
    }

    @Test
    fun testDateFormat() {
        assertThat(SimpleValueConverter.parseDateTime("2016-11-15T00:00:00+02:00")).isNotNull()
    }

    @Test
    fun testTimeFormat() {
        assertThat(SimpleValueConverter.parseTime("2016-11-15T00:00:00+02:00")).isEqualTo(LocalTime.of(0, 0, 0))
    }

    @Test
    fun testTimeFormatNoDate() {
        assertThat(SimpleValueConverter.parseTime("12:11:10+02:00")).isEqualTo(LocalTime.of(12, 11, 10))
        assertThat(SimpleValueConverter.parseTime("12:11:10")).isEqualTo(LocalTime.of(12, 11, 10))
    }

    @Test
    fun testOffsetTimeFormat() {
        assertThat(SimpleValueConverter.parseOffsetTime("2016-11-15T00:00:00+02:00"))
            .isEqualTo(OffsetTime.of(0, 0, 0, 0, ZoneOffset.ofHours(2)))
    }

    @Test
    fun testOffsetTimeFormatNoDate() {
        assertThat(SimpleValueConverter.parseOffsetTime("12:11:10+02:00"))
            .isEqualTo(OffsetTime.of(12, 11, 10, 0, ZoneOffset.ofHours(2)))
    }

    @Test
    fun testOffsetTimeFormatNoDateNoZone() {
        assertThatThrownBy { SimpleValueConverter.parseOffsetTime("12:11:10") }.isInstanceOf(ConversionException::class.java)
    }
}
