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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.openehr.base.foundationtypes.IntervalOfDate
import org.openehr.base.foundationtypes.IntervalOfDateTime
import org.openehr.base.foundationtypes.IntervalOfDuration
import org.openehr.base.foundationtypes.IntervalOfTime
import java.util.*

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@JsonPropertyOrder("min", "minOp", "max", "maxOp")
class WebTemplateTemporalRange(val min: String?, val minOp: String?, val max: String?, val maxOp: String?) : WebTemplateRange<String> {

    constructor(interval: IntervalOfTime?) : this(
        if (interval != null && !interval.isLowerUnbounded()) interval.lower else null,
        if (interval != null && !interval.isLowerUnbounded()) WebTemplateRange.getMinOp(interval) else null,
        if (interval != null && !interval.isUpperUnbounded()) interval.upper else null,
        if (interval != null && !interval.isUpperUnbounded()) WebTemplateRange.getMaxOp(interval) else null)

    constructor(interval: IntervalOfDateTime?) : this(
        if (interval != null && !interval.isLowerUnbounded()) interval.lower else null,
        if (interval != null && !interval.isLowerUnbounded()) WebTemplateRange.getMinOp(interval) else null,
        if (interval != null && !interval.isUpperUnbounded()) interval.upper else null,
        if (interval != null && !interval.isUpperUnbounded()) WebTemplateRange.getMaxOp(interval) else null)

    constructor(interval: IntervalOfDate?) : this(
        if (interval != null && !interval.isLowerUnbounded()) interval.lower else null,
        if (interval != null && !interval.isLowerUnbounded()) WebTemplateRange.getMinOp(interval) else null,
        if (interval != null && !interval.isUpperUnbounded()) interval.upper else null,
        if (interval != null && !interval.isUpperUnbounded()) WebTemplateRange.getMaxOp(interval) else null)

    constructor(interval: IntervalOfDuration?) : this(
        if (interval != null && !interval.isLowerUnbounded()) interval.lower else null,
        if (interval != null && !interval.isLowerUnbounded()) WebTemplateRange.getMinOp(interval) else null,
        if (interval != null && !interval.isUpperUnbounded()) interval.upper else null,
        if (interval != null && !interval.isUpperUnbounded()) WebTemplateRange.getMaxOp(interval) else null)

    @JsonIgnore
    override fun isEmpty(): Boolean = min == null && max == null

    @JsonIgnore
    override fun isFixed(): Boolean = WebTemplateRange.isFixed(this, minOp, maxOp)

    @JsonIgnore
    override fun getMinimal(): String? = min

    @JsonIgnore
    override fun getMaximal(): String? = max

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other !is WebTemplateTemporalRange -> false
            else -> min == other.min && minOp == other.minOp && max == other.max && maxOp == other.maxOp
        }

    override fun hashCode(): Int = Objects.hash(min, minOp, max, maxOp)
}
