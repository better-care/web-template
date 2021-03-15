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

import org.openehr.rm.datatypes.DvDateTime
import java.util.*

/**
 * @author Primoz Delopst
 * @author Bostjan Lah
 * @since 3.1.0
 *
 * This class implements partial date and times.
 */
class PartialDateTime(val partialDate: PartialDate, val partialTime: PartialTime?) {

    init {
        require(!(partialDate.day == null && partialTime != null)) { "Invalid partial datetime (date is partial so time is not allowed)!" }
    }

    companion object {
        /**
         * Converts [DvDateTime] to a [PartialDateTime].
         *
         * @param dvDateTime [DvDateTime]
         * @return [PartialDateTime]
         */
        @JvmStatic
        fun from(dvDateTime: DvDateTime): PartialDateTime = from(dvDateTime.value!!)

        /**
         * Parses a [String] to a [PartialDateTime].
         *
         * @param value [String] value
         * @return [PartialDateTime]
         */
        @JvmStatic
        fun from(value: String): PartialDateTime =
            with(value.indexOf('T')) {
                if (this != -1)
                    PartialDateTime(PartialDate.from(value.substring(0, this)), PartialTime.from(value.substring(this + 1)))
                else
                    PartialDateTime(PartialDate.from(value), null)
            }

        /**
         * Parses a [String] to a [PartialDateTime] matching it against the constraint pattern from the archetype.
         *
         * @param value [String] value
         * @param pattern Pattern
         * @return [PartialTime]
         */
        @JvmStatic
        fun from(value: String, pattern: String): PartialDateTime =
            with(value.indexOf('T')) {
                if (this != -1) {
                    val patternParts = pattern.split("T")
                    PartialDateTime(
                        PartialDate.from(value.substring(0, this), patternParts[0]),
                        if (patternParts.size == 2)
                            PartialTime.from(value.substring(this + 1), patternParts[1])
                        else
                            PartialTime.from(value.substring(this + 1)))
                } else {
                    PartialDateTime(PartialDate.from(value, pattern), null)
                }
            }

    }

    fun format(): String = "${partialDate.format()}${if (partialTime == null) "" else "T${partialTime.format()}"}"

    fun format(pattern: String) = with(pattern) {
        val patternParts = pattern.split("T")
        "${partialDate.format(patternParts[0])}${if (partialTime == null) "" else "T${if (patternParts.size == 2) partialTime.format(patternParts[1]) else partialTime.format()}"}"
    }

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other !is PartialDateTime -> false
            else -> partialDate == other.partialDate && partialTime == other.partialTime
        }

    override fun hashCode(): Int = Objects.hash(partialDate, partialTime)
}
