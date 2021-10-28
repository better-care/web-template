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

import care.better.platform.utils.JSR310ConversionUtils
import org.openehr.rm.datatypes.DvDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.temporal.TemporalAccessor

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Interface used to convert between decimal and temporal values to strings and vice-versa.
 */
interface ValueConverter {
    /**
     * Parses a [String] value to a [LocalDate].
     *
     * @param value [String] value
     * @return [LocalDate]
     */
    fun parseDate(value: String): LocalDate = parseDate(value, false)

    /**
     * Parses a [String] value to a [LocalDate].
     *
     * @param value [String] value
     * @param strict [Boolean] indicating if only a full date time is allowed
     * @return [LocalDate]
     */
    fun parseDate(value: String, strict: Boolean): LocalDate

    /**
     * Parses a [String] value to a [LocalTime].
     *
     * @param value [String] value
     * @return [LocalTime]
     */
    fun parseTime(value: String): LocalTime

    /**
     * Parses a [String] value to a [LocalTime].
     *
     * @param value [String] value
     * @return [LocalTime]
     */
    fun parseOffsetTime(value: String): OffsetTime

    /**
     * Parses a [String] value to a [OffsetDateTime].
     *
     * @param value [String] value
     * @return [OffsetDateTime]
     */
    fun parseDateTime(value: String): OffsetDateTime = parseDateTime(value, false)

    /**
     * Parses a [String] value to a [OffsetDateTime].
     *
     * @param value  [String] value
     * @param strict [Boolean] indicating if only a full date time is allowed
     * @return [OffsetDateTime]
     */
    fun parseDateTime(value: String, strict: Boolean): OffsetDateTime

    /**
     * Parses a [String] value to a [TemporalAccessor] by OpenEHR specification.
     *
     * @param value [String] value
     * @param pattern Pattern
     * @param strict [Boolean] indicating if a date has to strictly fit to pattern
     * @return [TemporalAccessor]
     */
    fun parseOpenEhrDate(value: String, pattern: String, strict: Boolean): TemporalAccessor

    /**
     * Parses a [String] value to a [TemporalAccessor] by OpenEHR specification.
     *
     * @param value [String] value
     * @param pattern Pattern
     * @param strict [Boolean] indicating if a date has to strictly fit to pattern
     * @return [TemporalAccessor]
     */
    fun parseOpenEhrDateTime(value: String, pattern: String, strict: Boolean): TemporalAccessor

    /**
     * Parses a [String] value to a [TemporalAccessor] by OpenEHR specification.
     *
     * @param value [String] value
     * @param pattern Pattern
     * @param strict [Boolean] indicating if a date has to strictly fit to pattern
     * @return [TemporalAccessor]
     */
    fun parseOpenEhrTime(value: String, pattern: String, strict: Boolean): TemporalAccessor

    /**
     * Formats a [TemporalAccessor] as [String].
     *
     * @param temporal [TemporalAccessor] value
     * @param pattern Pattern
     * @param strict [Boolean] indicating if a date has to strictly fit to pattern
     * @return [TemporalAccessor] formatted as [String]
     */
    fun formatOpenEhrTemporal(temporal: TemporalAccessor, pattern: String, strict: Boolean): String

    /**
     * Formats a [LocalDate] as [String].
     *
     * @param date [LocalDate] value
     * @return [LocalDate] formatted as [String]
     */
    fun formatDate(date: LocalDate): String

    /**
     * Formats a [DvDate] as [String].
     *
     * @param date [DvDate]
     * @return [LocalDate] formatted as [String]
     */
    fun formatDate(date: DvDate): String = formatDate(JSR310ConversionUtils.toLocalDate(date))

    /**
     * Formats a [LocalTime] as [String].
     *
     * @param time [LocalTime] value
     * @return [LocalTime] formatted as [String]
     */
    fun formatTime(time: LocalTime): String

    /**
     * Formats a [OffsetTime] as [String].
     *
     * @param time [OffsetTime] value
     * @return [OffsetTime] formatted as [String]
     */
    fun formatOffsetTime(time: OffsetTime): String

    /**
     * Formats a [OffsetDateTime] as [String].
     *
     * @param dateTime [OffsetDateTime] value
     * @return [OffsetDateTime] formatted as [String]
     */
    fun formatDateTime(dateTime: OffsetDateTime): String

    /**
     * Parses a [String] value to a [Double].
     *
     * @param value [String] value
     * @return [Double] value
     */
    fun parseDouble(value: String): Double

    /**
     * Formats a [Double] as a [String].
     *
     * @param value [Double] value
     * @return [Double] formatted as [String]
     */
    fun formatDouble(value: Double): String
}
