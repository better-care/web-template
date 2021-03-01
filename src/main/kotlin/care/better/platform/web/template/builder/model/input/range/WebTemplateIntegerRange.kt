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
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.openehr.base.foundationtypes.IntervalOfInteger
import java.util.*

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@JsonPropertyOrder("min", "max")
class WebTemplateIntegerRange(var min: Int?, @JsonIgnore var max: Int?) : WebTemplateRange<Int?> {

    constructor(interval: IntervalOfInteger?) : this(AmUtils.getMin(interval), AmUtils.getMax(interval))

    @JsonIgnore
    override fun isEmpty(): Boolean = min == null && max == null

    @JsonIgnore
    override fun isFixed(): Boolean = min != null && min == max

    @JsonIgnore
    override fun getMinimal(): Int? = min

    @JsonIgnore
    override fun getMaximal(): Int? = max

    @JsonProperty("max")
    fun getJsonMax(): Int = max ?: Integer.valueOf(-1)

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other !is WebTemplateIntegerRange -> false
            else -> min == other.min && max == other.max
        }

    override fun hashCode(): Int = Objects.hash(min, max)
}
