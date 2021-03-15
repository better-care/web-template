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
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ISM_TRANSITION_GROUP_NAME
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import org.openehr.rm.composition.Action
import org.openehr.rm.composition.IsmTransition
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvDateTime

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [EntryPostProcessor] that post-processes [Action].
 */
internal object ActionPostProcessor : EntryPostProcessor<Action>() {
    private val supportedClass = Action::class.java

    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: Action, webTemplatePath: WebTemplatePath?) {
        super.postProcess(conversionContext, amNode, instance, webTemplatePath)

        if (instance.time == null) {
            instance.time = DvDateTime.create(conversionContext.actionTime ?: conversionContext.time)
        }
        if (instance.ismTransition == null) {
            instance.ismTransition = IsmTransition().apply {
                this.currentState = DvCodedText.createFromOpenEhrTerminology(ISM_TRANSITION_GROUP_NAME, conversionContext.ismTransitionCurrentState)
            }
        }
    }

    override fun getType(): Class<*> = supportedClass
}
