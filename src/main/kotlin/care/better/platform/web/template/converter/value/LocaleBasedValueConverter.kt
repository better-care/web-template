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
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import care.better.platform.web.template.date.partial.PartialDate
import care.better.platform.web.template.date.partial.PartialDateTime
import care.better.platform.web.template.date.partial.PartialTime
import com.sun.xml.internal.ws.wsdl.writer.document.Part
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException
import java.time.*
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.time.temporal.TemporalAccessor
import java.util.*

/**
 * @author Primoz Delopst
 * @author Bostjan Lah
 * @since 3.1.0
 *
 * Implementation of [ValueConverter] that converts decimal and temporal values to strings and vice-versa using the locale.
 */
class LocaleBasedValueConverter(private val locale: Locale) : ValueConverter {

    companion object {
        private const val NOW = "now"

        private val DATE_PARSER = ISODateTimeFormat.dateOptionalTimeParser()
        private val TIME_PARSER = ISODateTimeFormat.timeParser()

        private val INSTANCE: LocaleBasedValueConverter = LocaleBasedValueConverter(Locale.getDefault())

        fun getInstance(): LocaleBasedValueConverter = INSTANCE
    }


    private val jsr310DefaultFormatter = java.time.format.DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
    private val jsr310DateFormatter = java.time.format.DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val jsr310TimeFormatter = java.time.format.DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)

    private val dateTimeFormatters: MutableList<DateTimeFormatter> = mutableListOf()
    private val dateFormatters: MutableList<DateTimeFormatter> = mutableListOf()
    private val timeFormatters: MutableList<DateTimeFormatter> = mutableListOf()
    private val offsetTimeFormatters: MutableList<java.time.format.DateTimeFormatter> = mutableListOf()

    init {
        dateTimeFormatters.add(getFormatter(locale, "MM"))
        addFormatters(dateTimeFormatters, locale, listOf("FF", "LL", "SS", "F-", "L-", "M-", "S-"))
        dateTimeFormatters.add(ISODateTimeFormat.dateTime().withOffsetParsed())
        dateTimeFormatters.add(ISODateTimeFormat.basicDateTime().withOffsetParsed())
        dateTimeFormatters.add(ISODateTimeFormat.dateOptionalTimeParser().withOffsetParsed())
        dateTimeFormatters.add(ISODateTimeFormat.basicDateTimeNoMillis().withOffsetParsed())
        dateTimeFormatters.add(ISODateTimeFormat.time().withOffsetParsed())
        dateTimeFormatters.add(ISODateTimeFormat.basicTime().withOffsetParsed())
        addFormatters(dateFormatters, locale, listOf("M-", "F-", "L-", "S-"))
        dateFormatters.add(DATE_PARSER)
        addFormatters(timeFormatters, locale, listOf("-M", "-F", "-L", "-S"))
        timeFormatters.add(TIME_PARSER)
        offsetTimeFormatters.add(java.time.format.DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
        offsetTimeFormatters.add(java.time.format.DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale))
        offsetTimeFormatters.add(java.time.format.DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL).withLocale(locale))
        offsetTimeFormatters.add(java.time.format.DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG).withLocale(locale))
    }

    private fun addFormatters(formatters: MutableList<DateTimeFormatter>, locale: Locale, styles: List<String>) {
        styles.forEach { formatters.add(getFormatter(locale, it)) }
    }

    private fun getFormatter(locale: Locale, dateStyle: String): DateTimeFormatter =
        DateTimeFormat.forPattern(DateTimeFormat.patternForStyle(dateStyle, locale)).withLocale(locale).withOffsetParsed()


    override fun parseDateTime(value: String, strict: Boolean): OffsetDateTime =
        when {
            NOW.equals(value, ignoreCase = true) -> OffsetDateTime.now()
            else -> {
                try {
                    OffsetDateTime.parse(value, java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                } catch (ignored: DateTimeException) {
                    convertToOffsetDateTimeLocalized(value) ?: throw ConversionException("Unable to convert value to DateTime: $value")
                }
            }
        }

    override fun parseDate(value: String, strict: Boolean): LocalDate =
        when {
            NOW.equals(value, ignoreCase = true) -> LocalDate.now()
            else -> {
                try {
                    LocalDate.parse(value, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (ignored: DateTimeException) {
                    convertToLocalDateLocalized(value) ?: throw ConversionException("Unable to convert value to LocalDate: $value")
                }
            }
        }

    private fun convertToOffsetDateTimeLocalized(value: String): OffsetDateTime? {
        for (format in dateTimeFormatters) {
            try {
                return WebTemplateConversionUtils.convert(format.parseDateTime(value))
            } catch (ignored: IllegalArgumentException) {
            }
        }
        return null
    }

    private fun convertToLocalDateLocalized(value: String): LocalDate? {
        for (format in dateFormatters) {
            try {
                return WebTemplateConversionUtils.convert(format.parseLocalDate(value))
            } catch (ignored: IllegalArgumentException) {
            }
        }
        return null
    }

    private fun convertToLocalTimeLocalized(value: String): LocalTime? {
        for (format in timeFormatters) {
            try {
                return WebTemplateConversionUtils.convert(format.parseLocalTime(value))
            } catch (ignored: IllegalArgumentException) {
            }
        }
        return null
    }

    private fun convertToOffsetTimeLocalized(value: String): OffsetTime? {
        try {
            return OffsetTime.parse(value)
        } catch (ignored: DateTimeParseException) {
        }
        for (format in offsetTimeFormatters) {
            try {
                return format.parse(value) { temporal: TemporalAccessor -> OffsetTime.from(temporal) }
            } catch (ignored: DateTimeParseException) {
            }
        }
        return null
    }

    override fun parsePartialDate(value: String, pattern: String): PartialDate = PartialDate.from(value, pattern)

    override fun parsePartialDate(value: String): PartialDate = PartialDate.from(value)

    override fun parseTime(value: String): LocalTime =
        when {
            NOW.equals(value, ignoreCase = true) -> LocalTime.now()
            else -> {
                try {
                    LocalTime.parse(value, java.time.format.DateTimeFormatter.ISO_LOCAL_TIME)
                } catch (ignored: DateTimeException) {
                    convertToLocalTimeLocalized(value) ?: throw ConversionException("Unable to convert value to LocalTime: $value")
                }
            }

        }

    override fun parseOffsetTime(value: String): OffsetTime =
        if (NOW.equals(value, ignoreCase = true))
            OffsetTime.now()
        else
            convertToOffsetTimeLocalized(value) ?: throw  ConversionException("Unable to convert value to LocalTime: $value")

    override fun parsePartialDateTime(value: String): PartialDateTime = PartialDateTime.from(value)

    override fun parsePartialDateTime(value: String, pattern: String): PartialDateTime = PartialDateTime.from(value, pattern)

    override fun parsePartialTime(value: String): PartialTime = PartialTime.from(value)

    override fun parsePartialTime(value: String, pattern: String): PartialTime = PartialTime.from(value, pattern)

    override fun formatDateTime(dateTime: OffsetDateTime): String = jsr310DefaultFormatter.format(dateTime)

    override fun formatPartialDateTime(partialDateTime: PartialDateTime): String = partialDateTime.format()

    override fun formatDate(date: LocalDate): String = jsr310DateFormatter.format(date)

    override fun formatPartialDate(date: PartialDate): String = date.format()

    override fun formatPartialTime(time: PartialTime): String = time.format()

    override fun formatTime(time: LocalTime): String = jsr310TimeFormatter.format(time)

    override fun formatOffsetTime(time: OffsetTime): String = jsr310TimeFormatter.format(time)

    override fun parseDouble(value: String): Double =
        try {
            with(NumberFormat.getInstance(locale) as DecimalFormat) {
                this.parse(value).toDouble()
            }
        } catch (e: ParseException) {
            throw ConversionException("Invalid decimal value: $value", e)
        }

    override fun formatDouble(value: Double): String = with(NumberFormat.getInstance(locale) as DecimalFormat) { this.format(value) }
}
