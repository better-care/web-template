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

package care.better.platform.web.template.builder.model

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils.attributeNameOf
import care.better.platform.web.template.builder.archetype.ArchetypePredicateProvider
import care.better.platform.web.template.builder.model.input.WebTemplateBindingCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.range.WebTemplateIntegerRange
import com.fasterxml.jackson.annotation.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@JsonPropertyOrder(
    "id",
    "name",
    "localizedName",
    "rmType",
    "nodeId",
    "occurences",
    "cardinalities",
    "dependsOn",
    "localizedNames",
    "localizedDescriptions",
    "annotations",
    "path",
    "proportionTypes",
    "inputs",
    "children")
@JsonInclude(JsonInclude.Include.NON_NULL)
class WebTemplateNode(@JsonIgnore val amNode: AmNode, var rmType: String, @field:JsonProperty("aqlPath") var path: String) {

    var name: String? = null
    var localizedName: String? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val localizedNames: MutableMap<String, String?> = mutableMapOf()

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val localizedDescriptions: MutableMap<String, String?> = mutableMapOf()

    @JsonIgnore
    lateinit var id: String

    @JsonProperty("id")
    lateinit var jsonId: String

    @JsonIgnore
    var alternativeId: String? = null

    @JsonIgnore
    var alternativeJsonId: String? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var nodeId: String? = null

    @JsonIgnore
    var nameCodeString: String? = null
    var inContext: Boolean? = null

    @JsonUnwrapped
    var occurences: WebTemplateIntegerRange? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var cardinalities: MutableList<WebTemplateCardinality>? = null
    var dependsOn: MutableList<String>? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val children: MutableList<WebTemplateNode> = mutableListOf()

    @JsonIgnore
    val chain: MutableList<AmNode> = ArrayList()

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val annotations: MutableMap<String, String> = HashMap()

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val termBindings: MutableMap<String, WebTemplateBindingCodedValue> = LinkedHashMap(1)

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val proportionTypes: MutableSet<String> = hashSetOf()

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val inputs: MutableList<WebTemplateInput> = mutableListOf()

    @JsonIgnore
    fun getSubPath(index: Int, archetypePredicateProvider: ArchetypePredicateProvider): String =
        with(StringBuilder()) {
            var parent = chain[0].parent
            for ((i, node) in chain.withIndex()) {
                val attribute = attributeNameOf(parent!!, node)
                this.append(getPath(attribute, node, if (i == chain.size - 1) index else -1, archetypePredicateProvider))
                parent = node
                if ("ELEMENT" == node.rmType) {
                    break
                }
            }
            this.toString()
        }

    private fun getPath(attributeName: String?, amNode: AmNode, index: Int, archetypePredicateProvider: ArchetypePredicateProvider): String =
        if (attributeName == null)
            ""
        else
            "/${attributeName}${archetypePredicateProvider.getPredicate(amNode, index)}"

    @JsonIgnore
    fun getInput(): WebTemplateInput? = if (inputs.isEmpty()) null else inputs[0]

    @JsonIgnore
    fun getInput(suffix: String): WebTemplateInput? = inputs.firstOrNull { suffix == it.suffix }

    fun setInput(input: WebTemplateInput) {
        if (inputs.isEmpty())
            inputs.add(input)
        else
            inputs[0] = input
    }

    @JsonIgnore
    fun isRepeating(): Boolean = occurences != null && (occurences?.max == null || (occurences?.max ?: 0) > 1)

    @JsonIgnore
    fun hasInput(): Boolean = inputs.isNotEmpty()

    @JsonIgnore
    fun jsonIdMatches(id: String): Boolean = id == jsonId || id == alternativeJsonId

    @JsonIgnore
    fun isJsonIdInitialized(): Boolean = this::jsonId.isInitialized

    override fun toString(): String = "$rmType[${jsonId}:${name ?: ""}]"
}
