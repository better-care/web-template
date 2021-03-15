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

import care.better.platform.utils.DateTimeConversionUtils
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.date.partial.PartialDate
import care.better.platform.web.template.date.partial.PartialDateTime
import care.better.platform.web.template.date.partial.PartialTime
import org.openehr.rm.datatypes.DvDate
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * @author Primoz Delopst
 * @author Bostjan Lah
 * @since 3.1.0
 *
 * Singleton instance of [ValueConverter] that converts decimal and temporal values to strings and vice-versa without the locale.
 */
object SimpleValueConverter : ValueConverter {
    private const val NOW = "now"


    override fun parseDateTime(value: String, strict: Boolean): OffsetDateTime =
        when {
            NOW.equals(value, ignoreCase = true) -> OffsetDateTime.now()
            else -> {
                try {
                    DateTimeConversionUtils.toOffsetDateTime(value, strict)
                } catch (ex: DateTimeException) {
                    throw ConversionException("Unable to convert value to datetime: $value", ex)
                }
            }
        }

    override fun parseDate(value: String, strict: Boolean): LocalDate =
        when {
            NOW.equals(value, ignoreCase = true) -> LocalDate.now()
            else -> {
                try {
                    DateTimeConversionUtils.toLocalDate(value, strict)
                } catch (ex: DateTimeException) {
                    throw ConversionException("Unable to convert value to date: $value", ex)
                }
            }
        }

    override fun parsePartialDate(value: String, pattern: String): PartialDate = PartialDate.from(value, pattern)

    override fun parsePartialDate(value: String): PartialDate = PartialDate.from(value)

    override fun parseTime(value: String): LocalTime =
        when {
            NOW.equals(value, ignoreCase = true) -> LocalTime.now()
            else -> {
                try {
                    DateTimeConversionUtils.toLocalTime(value)
                } catch (ex: DateTimeException) {
                    throw ConversionException("Unable to convert value to time: $value", ex)
                }
            }
        }

    override fun parseOffsetTime(value: String): OffsetTime =
        when {
            NOW.equals(value, ignoreCase = true) -> OffsetTime.now()
            else -> {
                try {
                    DateTimeConversionUtils.toOffsetTime(value, true)
                } catch (ex: DateTimeException) {
                    throw ConversionException("Unable to convert value to time: $value", ex)
                }
            }
        }

    override fun formatDateTime(dateTime: OffsetDateTime): String = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime)

    override fun formatPartialDateTime(partialDateTime: PartialDateTime): String = partialDateTime.format()

    override fun parsePartialDateTime(value: String, pattern: String): PartialDateTime = PartialDateTime.from(value, pattern)

    override fun parsePartialDateTime(value: String): PartialDateTime = PartialDateTime.from(value)

    override fun parsePartialTime(value: String): PartialTime = PartialTime.from(value)

    override fun parsePartialTime(value: String, pattern: String): PartialTime = PartialTime.from(value, pattern)

    override fun formatDate(date: LocalDate): String = DateTimeFormatter.ISO_LOCAL_DATE.format(date)

    override fun formatDate(date: DvDate): String = date.value!!

    override fun formatPartialDate(date: PartialDate): String = date.format()

    override fun formatPartialTime(time: PartialTime): String = time.format()

    override fun formatTime(time: LocalTime): String = DateTimeFormatter.ISO_LOCAL_TIME.format(time)

    override fun formatOffsetTime(time: OffsetTime): String = DateTimeFormatter.ISO_OFFSET_TIME.format(time)

    override fun parseDouble(value: String): Double =
        try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            throw ConversionException("Invalid decimal value: $value", e)
        }

    override fun formatDouble(value: Double): String = value.toString()
}
