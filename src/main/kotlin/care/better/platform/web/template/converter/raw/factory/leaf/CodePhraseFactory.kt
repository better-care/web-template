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

package care.better.platform.web.template.converter.raw.factory.leaf

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.am.aom.CCodePhrase
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.datatypes.CodePhrase

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [CodePhrase].
 */
internal object CodePhraseFactory : RmObjectLeafNodeFactory<CodePhrase>() {

    override fun createInstance(attributes: Set<AttributeDto>): CodePhrase = CodePhrase()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: CodePhrase,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        if (attribute.attribute.isBlank() || attribute.attribute == "code") {
            val textValue = jsonNode.asText()
            findConstraint(amNode)?.also {
                if (it.codeList.isNotEmpty() && !it.codeList.contains(textValue)) {
                    throw ConversionException("Invalid code '$textValue'", webTemplatePath.toString())
                }
            }

            rmObject.codeString = textValue
            true
        } else if (attribute.attribute == "terminology") {
            rmObject.terminologyId ?: TerminologyId().also { rmObject.terminologyId = it }.apply { this.value = jsonNode.asText() }
            true
        } else if (attribute.attribute == "preferred_term") {
            rmObject.preferredTerm = jsonNode.asText()
            true
        } else {
            false
        }

    override fun removeDependentValues(map: MutableMap<AttributeDto, JsonNode>): Boolean {
        val codeNode = map[AttributeDto.forAttribute("code")]
        if (codeNode != null) {
            val codeAttribute = AttributeDto.forAttribute(codeNode.asText())
            if (map[codeAttribute] != null) {
                map.remove(codeAttribute)
                return true
            }
        }
        return false
    }

    /**
     * Returns [CCodePhrase] for [AmNode] defining code attribute.
     *
     * @param amNode [AmNode]
     * @return [CCodePhrase]
     */
    private fun findConstraint(amNode: AmNode): CCodePhrase? =
        if (amNode.cObject is CCodePhrase)
            amNode.cObject as CCodePhrase
        else
            AmUtils.getCObjectItem(amNode, CCodePhrase::class.java, "defining_code")
}
