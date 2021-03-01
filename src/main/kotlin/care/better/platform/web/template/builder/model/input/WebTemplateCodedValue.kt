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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder("value", "label", "localizedLabels", "localizedDescriptions", "termBindings", "validation")
open class WebTemplateCodedValue(value: String, label: String?) : CodedValue(value, label) {

    companion object {
        @JvmStatic
        fun toCodedValue(): (Any) -> WebTemplateCodedValue = { WebTemplateCodedValue(it.toString(), it.toString()) }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val localizedLabels: MutableMap<String, String> = mutableMapOf()

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val localizedDescriptions: MutableMap<String, String> = mutableMapOf()

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val termBindings: MutableMap<String, WebTemplateBindingCodedValue> = LinkedHashMap(1)

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var validation: WebTemplateValidation? = null

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other == null || javaClass != other.javaClass -> false
            else -> label == (other as WebTemplateCodedValue).label && value == other.value
        }

    override fun hashCode(): Int = Objects.hash(value, label)


}
