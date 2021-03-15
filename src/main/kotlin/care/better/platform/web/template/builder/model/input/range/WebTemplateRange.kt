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
import com.fasterxml.jackson.annotation.JsonInclude
import org.openehr.base.foundationtypes.Interval

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
interface WebTemplateRange<T> {
    fun isEmpty(): Boolean
    fun isFixed(): Boolean
    fun getMinimal(): T?
    fun getMaximal(): T?

    companion object {
        @JsonIgnore
        @JvmStatic
        fun getMinOp(interval: Interval): String = if (false == interval.isLowerIncluded()) ">" else ">="

        @JsonIgnore
        @JvmStatic
        fun getMaxOp(interval: Interval): String = if (false == interval.isUpperIncluded()) "<" else "<="

        @JsonIgnore
        @JvmStatic
        fun isFixed(range: WebTemplateRange<*>, minOp: String?, maxOp: String?): Boolean =
            range.getMinimal() == range.getMaximal() && ">=" == minOp && "<=" == maxOp
    }
}
