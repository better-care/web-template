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

package care.better.platform.web.template.converter.raw.factory.node

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import org.openehr.am.aom.CCodePhrase
import org.openehr.base.basetypes.ArchetypeId
import org.openehr.rm.common.Archetyped
import org.openehr.rm.common.Locatable
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [RmObjectNodeFactory] that creates a new instance of [Locatable].
 *
 * @constructor Creates a new instance of [LocatableFactory]
 */
internal abstract class LocatableFactory<T : Locatable> : RmObjectNodeFactory<T> {
    override fun create(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T =
        createLocatable(conversionContext, amNode, webTemplatePath).apply {
            if (amNode != null) {
                val archetypeNodeId = amNode.archetypeNodeId
                this.archetypeNodeId = archetypeNodeId
                if (archetypeNodeId != null && !archetypeNodeId.startsWith("at")) {
                    this.archetypeDetails = createArchetypeDetails(archetypeNodeId)
                }
                this.name = createLocatableName(amNode, webTemplatePath)
            }
        }

    protected abstract fun createLocatable(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T

    private fun createArchetypeDetails(archetypeNodeId: String) =
        Archetyped().apply {
            this.archetypeId = ArchetypeId().apply { this.value = archetypeNodeId }
        }

    private fun createLocatableName(amNode: AmNode, webTemplatePath: WebTemplatePath?): DvText {
        if (webTemplatePath?.parent != null) {
            val nameCodePhrase: CCodePhrase? = AmUtils.getNameCodePhrase(amNode)
            if (nameCodePhrase != null) {
                val codeList = nameCodePhrase.codeList
                if (nameCodePhrase.terminologyId != null && codeList.isNotEmpty()) {
                    val matchingCode = findMatchingCode(amNode, codeList, webTemplatePath.key) ?: codeList[0]
                    return DvCodedText.create(nameCodePhrase.terminologyId?.value!!, matchingCode, AmUtils.findTermText(amNode, matchingCode)!!)
                }

            }
        }
        return DvText().apply { this.value = amNode.name }
    }

    private fun findMatchingCode(amNode: AmNode, codeList: List<String>, key: String): String? =
        codeList.firstOrNull { Objects.equals(WebTemplateConversionUtils.getWebTemplatePathSegmentForName(AmUtils.findTermText(amNode, it)!!), key) }


    protected fun setFallbackName(locatable: T, name: String) {
        val locatableName = locatable.name ?: DvText().also { locatable.name = it }
        if (locatableName.value == null) {
            locatableName.value = name
        }
    }
}
