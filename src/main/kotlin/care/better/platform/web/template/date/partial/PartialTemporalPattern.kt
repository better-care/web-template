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

import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @author Bostjan Lah
 * @since 3.1.0
 *
 * Used to format [PartialDate], [PartialTime] and [PartialDateTime].
 *
 * @constructor Creates a new instance of [PartialDateTime]
 * @param regexPattern Regex pattern
 * @param pattern Pattern
 * @param exceptionFormat Exception format
 */
class PartialTemporalPattern(regexPattern: Pattern, pattern: String, exceptionFormat: String) {

    enum class FieldState {
        MANDATORY, OPTIONAL, FORBIDDEN
    }

    private val fieldStates = arrayOfNulls<FieldState>(3)

    init {
        regexPattern.matcher(pattern).also { matcher ->
            if (matcher.matches() && matcher.groupCount() == 3) {
                for (i in 0 .. 2) {
                    val group = matcher.group(i + 1)
                    when {
                        group.contains("X") -> fieldStates[i] = FieldState.FORBIDDEN
                        group.contains("?") -> fieldStates[i] = FieldState.OPTIONAL
                        else -> fieldStates[i] = FieldState.MANDATORY
                    }
                }
            } else {
                throw IllegalArgumentException(String.format(exceptionFormat, pattern))
            }
        }
    }

    operator fun get(pos: Int): FieldState? = fieldStates[pos]

    fun isMandatory(pos: Int): Boolean = fieldStates[pos] == FieldState.MANDATORY

    fun isForbidden(pos: Int): Boolean = fieldStates[pos] == FieldState.FORBIDDEN

    fun format(field0: Int, field1: Int?, field2: Int?, full: String?, medium: String, small: String, exceptionMessage: String): String =
        when {
            fieldStates[1] == FieldState.FORBIDDEN -> String.format(small, field0)
            fieldStates[2] == FieldState.FORBIDDEN -> format(field0, field1, medium, small, exceptionMessage)
            fieldStates[1] == FieldState.OPTIONAL -> {
                when {
                    field1 == null -> String.format(small, field0)
                    field2 == null -> String.format(medium, field0, field1)
                    else -> String.format(full!!, field0, field1, field2)
                }
            }
            fieldStates[2] == FieldState.OPTIONAL -> {
                when {
                    field1 == null -> throw IllegalArgumentException(exceptionMessage)
                    field2 == null -> String.format(medium, field0, field1)
                    else -> String.format(full!!, field0, field1, field2)
                }
            }
            else -> {
                require(!(field1 == null || field2 == null)) { exceptionMessage }
                String.format(full!!, field0, field1, field2)
            }
        }

    private fun format(field0: Int, field1: Int?, medium: String, small: String, exceptionMessage: String): String =
        if (fieldStates[1] == FieldState.OPTIONAL) {
            if (field1 == null) String.format(small, field0) else String.format(medium, field0, field1)
        } else {
            if (field1 == null) throw IllegalArgumentException(exceptionMessage) else String.format(medium, field0, field1)
        }


    fun from(value: String, valuePattern: Pattern, exceptionMessage: String): Array<Int?> =
        with(valuePattern.matcher(value)) {
            if (this.matches()) {
                when {
                    isMandatory(2) -> {
                        require(!(this.group(3) == null || this.group(5) == null)) { exceptionMessage }
                        arrayOf(Integer.valueOf(this.group(1)), Integer.valueOf(this.group(3)), Integer.valueOf(this.group(5)))
                    }
                    isMandatory(1) -> {
                        requireNotNull(this.group(3)) { exceptionMessage }
                        if (this.group(5) != null && !isForbidden(2)) {
                            arrayOf(Integer.valueOf(this.group(1)), Integer.valueOf(this.group(3)), Integer.valueOf(this.group(5)))
                        } else arrayOf(Integer.valueOf(this.group(1)), Integer.valueOf(this.group(3)), null)
                    }
                    else -> {
                        when {
                            this.group(5) != null && !isForbidden(2) -> {
                                arrayOf(Integer.valueOf(this.group(1)), Integer.valueOf(this.group(3)), Integer.valueOf(this.group(5)))
                            }
                            this.group(3) != null && !isForbidden(1) -> {
                                arrayOf(Integer.valueOf(this.group(1)), Integer.valueOf(this.group(3)), null)
                            }
                            else -> arrayOf(Integer.valueOf(this.group(1)), null, null)

                        }
                    }

                }
            } else {
                throw IllegalArgumentException(exceptionMessage)
            }
        }
}
