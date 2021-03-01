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

package care.better.platform.web.template.builder.model.input.range

import care.better.platform.template.AmUtils
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.openehr.base.foundationtypes.IntervalOfInteger
import java.util.*

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@JsonPropertyOrder("minOp", "min", "maxOp", "max")
class WebTemplateValidationIntegerRange(val min: Int?, val minOp: String?, val max: Int?, val maxOp: String?) : WebTemplateRange<Int?> {
    companion object {
        @JvmStatic
        private fun getMax(interval: IntervalOfInteger?): Int? =
            if (interval != null && !interval.isUpperUnbounded() && interval.upper != null)
                if (java.lang.Boolean.FALSE == interval.isUpperIncluded()) interval.upper!! - 1 else interval.upper
            else
                null
    }

    constructor(min: Int?, max: Int?) : this(
        min,
        if (min != null) ">=" else null,
        max,
        if (max != null) "<=" else null)

    constructor(interval: IntervalOfInteger?) : this(
        AmUtils.getMin(interval),
        if (AmUtils.getMin(interval) != null) ">=" else null,
        AmUtils.getMax(interval),
        if (getMax(interval) != null) "<=" else null)

    @JsonIgnore
    override fun isEmpty(): Boolean = min == null && max == null

    @JsonIgnore
    override fun isFixed(): Boolean = min != null && min == max

    @JsonIgnore
    override fun getMinimal(): Int? = min

    @JsonIgnore
    override fun getMaximal(): Int? = max

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other !is WebTemplateValidationIntegerRange -> false
            else -> minOp == other.minOp && min == other.min && maxOp == other.maxOp && max == other.max
        }

    override fun hashCode(): Int = Objects.hash(minOp, min, maxOp, max)
}
