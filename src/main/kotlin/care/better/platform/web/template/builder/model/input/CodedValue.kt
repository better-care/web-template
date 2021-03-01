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

package care.better.platform.web.template.builder.model.input

import org.apache.commons.lang3.builder.ToStringBuilder

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Coded value: a value with a label (such as for example an option in a select html element).
 */
open class CodedValue(val value: String, open val label: String?) {

    override fun toString(): String {
        return ToStringBuilder(this)
            .append("value", value)
            .append("label", label)
            .toString()
    }
}
