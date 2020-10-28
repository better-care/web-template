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

package care.better.platform.web.template.date.partial

import org.joda.time.LocalDate
import org.openehr.rm.datatypes.DvDate
import java.io.Serializable
import java.util.*
import java.util.regex.Pattern

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * This class implements partial dates - i.e. dates with only a year's value or dates with a year's and a month's value.
 */
class PartialDate @JvmOverloads constructor(val year: Int = 0, val month: Int?, val day: Int?) : Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L

        private val VALUE_PATTERN = Pattern.compile("([0-9]{4})(-([0-9]{1,2}))?(-([0-9]{1,2}))?")
        private val REGEX_PATTERN = Pattern.compile("([^-]+)-([^-]+)-([^-]+)")
        private const val PATTERN_EXCEPTION_FORMAT = "Invalid value '%s' for partial date pattern '%s'!"

        /**
         * Converts [DvDate] to a [PartialDate].
         *
         * @param dvDate [DvDate]
         * @return [PartialDate]
         */
        @JvmStatic
        fun from(dvDate: DvDate): PartialDate = from(dvDate.value!!)

        /**
         * Parses a [String] to a [PartialDate].
         *
         * @param value [String] value
         * @return [PartialDate]
         */
        @JvmStatic
        fun from(value: String): PartialDate =
            try {
                val matcher = VALUE_PATTERN.matcher(value)
                if (matcher.matches()) {
                    when {
                        matcher.group(5) != null -> {
                            PartialDate(matcher.group(1).toInt(), Integer.valueOf(matcher.group(3)), Integer.valueOf(matcher.group(5)))
                        }
                        matcher.group(3) != null -> PartialDate(matcher.group(1).toInt(), Integer.valueOf(matcher.group(3)))
                        else -> PartialDate(matcher.group(1).toInt())
                    }
                } else {
                    throw IllegalArgumentException("Invalid partial date value: $value!")
                }
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Invalid partial date value: $value!", e)
            }

        /**
         * Parses a [String] to a [PartialDate] matching it against the constraint pattern from the archetype.
         *
         * @param value [String] value
         * @param pattern Pattern
         * @return [PartialDate]
         */
        @JvmStatic
        fun from(value: String, pattern: String): PartialDate =
            with(PartialTemporalPattern(REGEX_PATTERN, pattern, "Invalid partial date pattern: %s!")) {
                this.from(value, VALUE_PATTERN, String.format(PATTERN_EXCEPTION_FORMAT, value, pattern)).let {
                    PartialDate(it[0]!!, it[1], it[2])
                }
            }
    }

    init {
        require(!(day != null && month == null)) { "Invalid partial date (day set but month not set)!" }
        if (day != null)
            LocalDate(year, month!!, day)
        else if (month != null)
            LocalDate(year, month, 1)
    }

    /**
     * Creates a new instance of [PartialDate].
     *
     * @param year  Year
     * @param month Month (may be null)
     */
    constructor(year: Int, month: Int?) : this(year, month, null)

    /**
     * Creates a new instance of [PartialDate].
     *
     * @param year Year
     */
    constructor(year: Int) : this(year, null)

    /**
     * Formats [PartialDate] according to the archetype constraint pattern.
     *
     * @param pattern Archetype constraint pattern (none or YYYY-MM-?? or YYYY-??-XX)
     * @return Formatted [PartialDate] [String]
     */
    fun format(pattern: String): String =
        with(PartialTemporalPattern(REGEX_PATTERN, pattern, "Invalid partial date pattern: %s!")) {
            this.format(
                year,
                month,
                day,
                "%04d-%02d-%02d",
                "%04d-%02d",
                "%04d",
                String.format(PATTERN_EXCEPTION_FORMAT, toString(), pattern))
        }

    /**
     * Formats [PartialDate]
     *
     * @return Formatted [PartialDate] [String]
     */
    fun format(): String =
        when {
            day != null -> String.format("%04d-%02d-%02d", year, month, day)
            month != null -> String.format("%04d-%02d", year, month)
            else -> String.format("%04d", year)
        }

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other == null || javaClass != other.javaClass -> false
            else -> {
                year == (other as PartialDate).year && month == other.month && day == other.day
            }
        }

    override fun hashCode(): Int = Objects.hash(year, month, day)

    override fun toString(): String =
        when {
            month == null -> year.toString()
            day == null -> "$year-$month"
            else -> "$year-$month-$day"
        }
}
