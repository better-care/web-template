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
import care.better.platform.web.template.converter.constant.WebTemplateConstants
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromAmNode
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.PartyRef
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartySelf
import org.openehr.rm.composition.Composition
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [LocatableFactory] that creates a new instance of [Composition].
 */
internal object CompositionFactory : LocatableFactory<Composition>() {
    override fun createLocatable(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): Composition =
        Composition().apply {
            when {
                conversionContext.composer != null -> this.composer = conversionContext.composer
                conversionContext.composerName != null -> {
                    this.composer = PartyIdentified.forName(conversionContext.composerName).apply {
                        if (conversionContext.composerId != null) {
                            this.externalRef = getExternalRef(conversionContext)
                        }
                    }
                }
                conversionContext.composerSelf -> {
                    this.composer = PartySelf().apply {
                        if (conversionContext.composerId != null) {
                            this.externalRef = getExternalRef(conversionContext)
                        }
                    }
                }
            }

            val categoryAmNode = amNode?.let { node -> AmUtils.getAmNode(node, "category")?.also { this.category = DvCodedText.createFromAmNode(it) } }
            val categoryDvCodedText = if (categoryAmNode != null) DvCodedText.createFromAmNode(categoryAmNode) else null

            this.category = when {
                categoryDvCodedText != null -> categoryDvCodedText
                conversionContext.category != null ->
                    DvCodedText.createFromOpenEhrTerminology(WebTemplateConstants.COMPOSITION_CATEGORY_GROUP_NAME, conversionContext.category)
                else -> throw ConversionException("Composition category is not valid: ${conversionContext.category}")
            }

            conversionContext.language?.also { this.language = CodePhrase.createLanguagePhrase(it) }
            conversionContext.territory?.also { this.territory = CodePhrase.createTerritoryPhrase(it) }
        }

    /**
     * Creates and returns [PartyRef] from [ConversionContext].
     *
     * @param conversionContext [ConversionContext]
     * @return [PartyRef]
     */
    private fun getExternalRef(conversionContext: ConversionContext): PartyRef =
        PartyRef().apply {
            this.type = "PERSON"
            this.namespace = conversionContext.idNamespace
            this.id = GenericId().apply {
                this.scheme = conversionContext.idScheme
                this.value = conversionContext.composerId
            }
        }
}
