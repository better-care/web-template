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
import care.better.platform.template.AmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ISM_TRANSITION_GROUP_NAME
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import org.openehr.am.aom.CCodePhrase
import org.openehr.rm.composition.IsmTransition
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [PostProcessor] that post-processes [IsmTransition].
 */
internal object IsmTransitionPostProcessor : PostProcessor<IsmTransition> {
    private val supportedClass = IsmTransition::class.java

    override fun postProcess(
            conversionContext: ConversionContext,
            amNode: AmNode?,
            instance: IsmTransition,
            webTemplatePath: WebTemplatePath?) {

        if (instance.careflowStep != null && instance.currentState == null && instance.transition == null) {
            amNode?.parent?.attributes?.get("ism_transition")?.also { attribute ->
                attribute.children.firstOrNull { it.nodeId == instance.careflowStep?.definingCode?.codeString }?.also {
                    val currentStateCCodePhrase = AmUtils.getCObjectItem(it, CCodePhrase::class.java, "current_state", "defining_code")
                    if (currentStateCCodePhrase != null && currentStateCCodePhrase.codeList.isNotEmpty()) {
                        val codedText: DvCodedText = toDvCodedText(conversionContext, amNode, currentStateCCodePhrase)
                        instance.currentState = codedText
                    }
                    val transitionCCodePhrase = AmUtils.getCObjectItem(it, CCodePhrase::class.java, "transition", "defining_code")
                    if (transitionCCodePhrase != null && transitionCCodePhrase.codeList.size == 1) {
                        val codedText: DvCodedText = toDvCodedText(conversionContext, amNode, transitionCCodePhrase)
                        instance.transition = codedText
                    }
                }
            }
        } else if (instance.currentState == null) {
            instance.currentState = DvCodedText.createFromOpenEhrTerminology(ISM_TRANSITION_GROUP_NAME, conversionContext.ismTransitionCurrentState)
        }
    }

    /**
     * Constructs [DvCodedText] from [ConversionContext], [AmNode] and [currentStateCCodePhrase].
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param currentStateCCodePhrase [CCodePhrase]
     * @return [DvCodedText]
     */
    private fun toDvCodedText(conversionContext: ConversionContext, amNode: AmNode, currentStateCCodePhrase: CCodePhrase): DvCodedText {
        val terminologyId = currentStateCCodePhrase.terminologyId?.value!!
        val code = currentStateCCodePhrase.codeList[0]
        return DvCodedText.create(
            terminologyId,
            currentStateCCodePhrase.codeList[0],
            WebTemplateConversionUtils.getTermText(amNode, terminologyId, code, conversionContext.language)!!)
    }

    override fun getType(): Class<*> = supportedClass
}
