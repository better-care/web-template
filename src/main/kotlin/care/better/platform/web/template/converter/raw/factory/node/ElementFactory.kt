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
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvText
import org.openehr.rm.datatypes.TermMapping

/**
 * @author Primoz Delopst
 * @since 4.0.0
 *
 * Singleton instance of [LocatableFactory] that creates a new instance of [Element].
 */
internal object ElementFactory : LocatableFactory<Element>() {
    override fun createLocatable(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): Element = Element()

    override fun addTermBindings(dvText: DvText, conversionContext: ConversionContext, amNode: AmNode) {
        amNode.archetypeNodeId?.also { archetypeNodeId ->
            dvText.mappings.addAll(
                    amNode.getTermBindings(archetypeNodeId)
                        .filterKeys { conversionContext.termBindingTerminologies.contains("*") || conversionContext.termBindingTerminologies.contains(it) }
                        .map { TermMapping("=", null, CodePhrase(TerminologyId(it.key), WebTemplateConversionUtils.extractTerminologyCode(it.value), null)) }
            )
        }
    }
}
