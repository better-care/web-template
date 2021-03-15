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
import care.better.platform.web.template.date.partial.PartialDate
import care.better.platform.web.template.date.partial.PartialDateTime
import care.better.platform.web.template.date.partial.PartialTime
import org.openehr.rm.datatypes.DvDate
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime

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
     * Parses a [String] value to a [PartialDate].
     *
     * @param value [String] value
     * @param pattern Pattern in 'YYYY-MM-??' or 'YYYY-??-XX' format
     * @return [PartialDate]
     */
    fun parsePartialDate(value: String, pattern: String): PartialDate

    /**
     * Parses a [String] value to a [PartialDate].
     *
     * @param value String value
     * @return [PartialDate]
     */
    fun parsePartialDate(value: String): PartialDate

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
     * Parses a [String] value to a [PartialDateTime].
     *
     * @param value [String] value
     * @return [PartialDateTime]
     */
    fun parsePartialDateTime(value: String): PartialDateTime

    /**
     * Parses a [String] value to a [PartialDateTime].
     *
     * @param value [String] value
     * @param pattern Pattern
     * @return [PartialDateTime]
     */
    fun parsePartialDateTime(value: String, pattern: String): PartialDateTime

    /**
     * Parses a [String] value to a [PartialTime].
     *
     * @param value [String] value
     * @return [PartialTime]
     */
    fun parsePartialTime(value: String): PartialTime

    /**
     * Parses a [String] value to a [PartialTime].
     *
     * @param value [String] value
     * @param pattern Pattern
     * @return [PartialTime]
     */
    fun parsePartialTime(value: String, pattern: String): PartialTime

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
     * Formats a [PartialDate] to a [String].
     *
     * @param date [PartialDate] value
     * @return [PartialDate] formatted as [String]
     */
    fun formatPartialDate(date: PartialDate): String


    /**
     * Formats a [PartialTime] to a [String].
     *
     * @param time [PartialTime] value
     * @return [PartialTime] formatted as [String]
     */
    fun formatPartialTime(time: PartialTime): String

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
     * Formats a [PartialDateTime] as [String].
     *
     * @param partialDateTime [PartialDateTime] value
     * @return [PartialDateTime] formatted as [String]
     */
    fun formatPartialDateTime(partialDateTime: PartialDateTime): String

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
