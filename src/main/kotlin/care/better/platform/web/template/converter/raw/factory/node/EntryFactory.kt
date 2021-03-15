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
import care.better.platform.web.template.converter.raw.extensions.createFromAmNode
import care.better.platform.web.template.converter.raw.extensions.createPartyIdentified
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartyRelated
import org.openehr.rm.common.PartySelf
import org.openehr.rm.composition.Entry
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [LocatableFactory] that creates a new instance of [Entry].
 *
 * @constructor Creates a new instance of [EntryFactory]
 */
internal abstract class EntryFactory<T : Entry> : LocatableFactory<T>() {
    override fun createLocatable(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T =
        createEntry(conversionContext, amNode, webTemplatePath).apply {

            conversionContext.language?.also { this.language = CodePhrase.createLanguagePhrase(it) }
            conversionContext.encoding?.also { this.encoding = CodePhrase.createEncodingPhrase(it) }

            val subjectAmNode = amNode?.let { AmUtils.getAmNode(it, "subject") }

            if (subjectAmNode != null && "PARTY_PROXY" != subjectAmNode.rmType)
                this.subject = when (subjectAmNode.rmType) {
                    "PARTY_IDENTIFIED" -> PartyIdentified()
                    "PARTY_RELATED" -> PartyRelated().apply {
                        AmUtils.getAmNode(subjectAmNode, "relationship")?.also { this.relationship = DvCodedText.createFromAmNode(it) }
                    }
                    else -> PartySelf()
                }
            else
                this.subject = conversionContext.subject


            if (conversionContext.entryProvider != null)
                this.provider = conversionContext.entryProvider
            else if (conversionContext.providerName != null)
                this.provider = PartyIdentified.createPartyIdentified(
                    conversionContext.providerName,
                    conversionContext.providerId,
                    conversionContext.idScheme,
                    conversionContext.idNamespace)

            this.otherParticipations.addAll(conversionContext.getParticipationList())
        }

    protected abstract fun createEntry(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T
}
