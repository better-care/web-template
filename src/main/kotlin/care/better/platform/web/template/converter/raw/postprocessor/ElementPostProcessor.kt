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

package care.better.platform.web.template.converter.raw.postprocessor

import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.constant.WebTemplateConstants.NULL_FLAVOURS_GROUP_NAME
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [PostProcessor] that post-processes [Element].
 */
internal object ElementPostProcessor : LocatablePostProcessor<Element>() {
    private val supportedClass = Element::class.java

    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: Element, webTemplatePath: WebTemplatePath?) {
        instance.nullFlavour?.also { nullFlavour ->
            if (nullFlavour.value == null && nullFlavour.definingCode?.codeString != null) {
                instance.nullFlavour = DvCodedText.createFromOpenEhrTerminology(NULL_FLAVOURS_GROUP_NAME, nullFlavour.definingCode?.codeString!!)
            } else if (nullFlavour.value != null && nullFlavour.definingCode?.codeString == null) {
                DvCodedText.createFromOpenEhrTerminology(NULL_FLAVOURS_GROUP_NAME, nullFlavour.value!!).also { nullFlavour.definingCode = it.definingCode }
            }

            nullFlavour.definingCode?.also {
                if (it.terminologyId == null || !it.terminologyId?.value.equals("openehr")) {
                    it.terminologyId = TerminologyId().apply { this.value = "openehr" }
                }
            }
        }
    }

    override fun getType(): Class<*> = supportedClass
}
