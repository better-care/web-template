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

import care.better.platform.time.format.OpenEhrDateTimeFormatter
import care.better.platform.utils.DateTimeConversionUtils
import care.better.platform.web.template.converter.exceptions.ConversionException
import org.openehr.rm.datatypes.DvDate
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

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

    override fun parseOpenEhrDate(value: String, pattern: String, strict: Boolean): TemporalAccessor =
        if (NOW.equals(value, ignoreCase = true)) {
            OpenEhrDateTimeFormatter.ofPattern(pattern, false).parseDate(LocalDate.now().toString())
        } else {
            try {
                OpenEhrDateTimeFormatter.ofPattern(pattern, strict).parseDate(value)
            } catch (ignored: DateTimeException) {
                throw  ConversionException("Unable to convert value to OpenEhr Date: $value")
            }
        }

    override fun parseOpenEhrDateTime(value: String, pattern: String, strict: Boolean): TemporalAccessor =
        if (NOW.equals(value, ignoreCase = true)) {
            OpenEhrDateTimeFormatter.ofPattern(pattern, false).parseDateTime(OffsetDateTime.now().toString())
        } else {
            try {
                OpenEhrDateTimeFormatter.ofPattern(pattern, strict).parseDateTime(value)
            } catch (ignored: DateTimeException) {
                throw  ConversionException("Unable to convert value to OpenEhr DateTime: $value")
            }
        }

    override fun parseOpenEhrTime(value: String, pattern: String, strict: Boolean): TemporalAccessor =
        if (NOW.equals(value, ignoreCase = true)) {
            OpenEhrDateTimeFormatter.ofPattern(pattern, false).parseTime(OffsetTime.now().toString())
        } else {
            try {
                OpenEhrDateTimeFormatter.ofPattern(pattern, strict).parseTime(value)
            } catch (ignored: DateTimeException) {
                throw  ConversionException("Unable to convert value to OpenEhr Time: $value")
            }
        }

    override fun formatOpenEhrTemporal(temporal: TemporalAccessor, pattern: String, strict: Boolean): String =
        try {
            OpenEhrDateTimeFormatter.ofPattern(pattern, strict).format(temporal)
        } catch (ignored: DateTimeException) {
            throw  ConversionException("Unable to format OpenEhr Date/Time/DateTime: $temporal")
        }

    override fun formatDate(date: LocalDate): String = DateTimeFormatter.ISO_LOCAL_DATE.format(date)

    override fun formatDate(date: DvDate): String = date.value!!

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
