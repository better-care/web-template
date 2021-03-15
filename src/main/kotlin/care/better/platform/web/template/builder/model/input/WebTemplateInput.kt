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

import care.better.platform.web.template.builder.model.WebTemplateInputType
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder("suffix", "type")
class WebTemplateInput @JvmOverloads constructor(val type: WebTemplateInputType, val suffix: String? = null) {
    val list: MutableList<WebTemplateCodedValue> = mutableListOf()
    var listOpen: Boolean? = null
    var validation: WebTemplateValidation? = null

    @JsonIgnore
    var fixed = false
    var defaultValue: Any? = null
    var terminology: String? = null
    val otherTerminologies: MutableSet<String> = hashSetOf()


    @JsonIgnore
    fun isExternalTerminology(): Boolean = terminology != null

    override fun equals(other: Any?): Boolean =
        when {
            this === other -> true
            other !is WebTemplateInput -> false
            else -> fixed == other.fixed &&
                    suffix == other.suffix && type == other.type &&
                    list == other.list &&
                    listOpen == other.listOpen &&
                    validation == other.validation &&
                    defaultValue == other.defaultValue &&
                    terminology == other.terminology &&
                    otherTerminologies == other.otherTerminologies
        }

    override fun hashCode(): Int = Objects.hash(suffix, type, list, listOpen, validation, fixed, defaultValue, terminology, otherTerminologies)

}
