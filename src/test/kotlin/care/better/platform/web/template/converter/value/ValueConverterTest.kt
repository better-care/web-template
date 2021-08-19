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
import java.time.*
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ValueConverterTest {
    @Test
    fun testRussian() {
        val valueConverter: ValueConverter = LocaleBasedValueConverter(Locale("ru", "RU"))
        assertThat(valueConverter.parseDouble("1000,6")).isEqualTo(1000.6)
        assertThat(valueConverter.parseDate("13.12.2013")).isEqualTo(LocalDate.of(2013, 12, 13))
        assertThat(valueConverter.parseTime("14:50")).isEqualTo(LocalTime.of(14, 50, 0))
        assertThat(valueConverter.parseOffsetTime("14:50+02:00")).isEqualTo(OffsetTime.of(14, 50, 0, 0, ZoneOffset.ofHours(2)))
        assertThat(valueConverter.parseDateTime("13.12.2013, 14:50"))
            .isEqualTo(ZonedDateTime.of(2013, 12, 13, 14, 50, 0, 0, ZoneId.systemDefault()).toOffsetDateTime())
    }

    @Test
    fun testGerman() {
        val valueConverter: ValueConverter = LocaleBasedValueConverter(Locale("de", "DE"))
        assertThat(valueConverter.parseDouble("1000,6")).isEqualTo(1000.6)
        assertThat(valueConverter.parseDate("13. Dezember 2013")).isEqualTo(LocalDate.of(2013, 12, 13))
        assertThat(valueConverter.parseDate("Freitag, 13. Dezember 2013")).isEqualTo(LocalDate.of(2013, 12, 13))
        assertThat(valueConverter.parseTime("14:50")).isEqualTo(LocalTime.of(14, 50, 0))
        assertThat(valueConverter.parseOffsetTime("14:50+02:00")).isEqualTo(OffsetTime.of(14, 50, 0, 0, ZoneOffset.ofHours(2)))
        assertThat(valueConverter.parseDateTime("13.12.2013, 14:50"))
            .isEqualTo(ZonedDateTime.of(2013, 12, 13, 14, 50, 0, 0, ZoneId.systemDefault()).toOffsetDateTime())
    }

    @Test
    fun testFail() {
        assertThatThrownBy { LocaleBasedValueConverter(Locale("de", "DE")).parseDouble("abc") }
            .isInstanceOf(ConversionException::class.java)
        assertThatThrownBy { SimpleValueConverter.parseDouble("abc") }.isInstanceOf(ConversionException::class.java)
    }

    @Test
    fun testSimple() {
        assertThat(SimpleValueConverter.parseDouble("1000.6")).isEqualTo(1000.6)
        assertThat(SimpleValueConverter.parseDateTime("2010-01-01T01:01:01.000Z"))
            .isEqualTo(OffsetDateTime.of(2010, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC))
        assertThat(SimpleValueConverter.formatDouble(1000.6)).isEqualTo("1000.6")
        assertThat(SimpleValueConverter.formatDateTime(OffsetDateTime.of(2010, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC)))
            .isEqualTo("2010-01-01T01:01:01Z")
    }

    @Test
    fun testTimeZones() {
        assertThat(SimpleValueConverter.parseDateTime("2010-01-01T01:01:01.000Z").offset).isEqualTo(ZoneOffset.UTC)
        assertThat(SimpleValueConverter.parseDateTime("2010-01-01T01:01:01.000+02:00").offset).isEqualTo(ZoneOffset.ofHours(2))
    }
}
