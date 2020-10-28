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

import org.joda.time.LocalTime
import org.openehr.rm.datatypes.DvTime
import java.io.Serializable
import java.util.*
import java.util.regex.Pattern

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * This class implements partial times - i.e. times with only an hour's value or times with hour's and a minute's value.
 */
class PartialTime @JvmOverloads constructor(val hour: Int = 0, val minute: Int? = null, val second: Int? = null) : Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L
        private val VALUE_PATTERN = Pattern.compile("([0-9]{1,2})(:([0-9]{1,2}))?(:([0-9]{1,2}))?")
        private val REGEX_PATTERN = Pattern.compile("([^:]{1,2}):([^:]{1,2}):(.{1,2})")
        private const val PATTERN_EXCEPTION_FORMAT = "Invalid value '%s' for partial time pattern '%s'!"

        /**
         * Converts [DvTime] to a [PartialTime].
         *
         * @param dvTime [DvTime]
         * @return [PartialTime]
         */
        @JvmStatic
        fun from(dvTime: DvTime): PartialTime = from(dvTime.value!!)

        /**
         * Parses a [String] to a [PartialTime].
         *
         * @param value [String] value
         * @return [PartialTime]
         */
        @JvmStatic
        fun from(value: String): PartialTime =
            with(VALUE_PATTERN.matcher(value)) {
                if (this.matches()) {
                    when {
                        this.group(5) != null -> PartialTime(this.group(1).toInt(), Integer.valueOf(this.group(3)), Integer.valueOf(this.group(5)))
                        this.group(3) != null -> PartialTime(this.group(1).toInt(), Integer.valueOf(this.group(3)))
                        else -> PartialTime(this.group(1).toInt())
                    }
                } else {
                    throw IllegalArgumentException("Invalid partial time value: $value!")
                }
            }

        /**
         * Parses a [String] to a [PartialTime] matching it against the constraint pattern from the archetype.
         *
         * @param value [String] value
         * @param pattern Pattern
         * @return [PartialTime]
         */
        fun from(value: String, pattern: String): PartialTime =
            with(PartialTemporalPattern(REGEX_PATTERN, pattern, String.format(PATTERN_EXCEPTION_FORMAT, value, pattern))) {
                this.from(value, VALUE_PATTERN, String.format(PATTERN_EXCEPTION_FORMAT, value, pattern)).let {
                    PartialTime(it[0]!!, it[1], it[2])
                }
            }
    }

    init {
        require(!(second != null && minute == null)) { "Invalid partial time (second set but minute not set)!" }
        if (second != null)
            LocalTime(hour, minute!!, second)
        else if (minute != null)
            LocalTime(hour, minute, 1)
    }

    /**
     * Formats [PartialTime].
     *
     * @param pattern Archetype constraint pattern (none or hh:mm:?? or hh:??:XX)
     * @return Formatted [PartialTime] [String]
     */
    fun format(pattern: String): String =
        with(PartialTemporalPattern(REGEX_PATTERN, pattern, String.format(PATTERN_EXCEPTION_FORMAT, toString(), pattern))) {
            this.format(
                hour,
                minute,
                second,
                "%02d:%02d:%02d",
                "%02d:%02d",
                "%02d",
                String.format(PATTERN_EXCEPTION_FORMAT, toString(), pattern))
        }

    /**
     * Formats [PartialTime] according to the archetype constraint pattern.
     *
     * @return Formatted [PartialTime] [String]
     */
    fun format(): String =
        when {
            second != null -> String.format("%02d:%02d:%02d", hour, minute, second)
            minute != null -> String.format("%02d:%02d", hour, minute)
            else -> String.format("%02d", hour)
        }

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other == null || javaClass != other.javaClass -> false
            else -> hour == (other as PartialTime).hour && minute == other.minute && second == other.second
        }

    override fun hashCode(): Int = Objects.hash(hour, minute, second)

    override fun toString(): String =
        when {
            minute == null -> String.format("%02d", hour)
            second == null -> String.format("%02d:%02d", hour, minute)
            else -> String.format("%02d:%02d:%02d", hour, minute, second)
        }
}
