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
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.openehr.rm.common.Participation
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [PostProcessor] that post-processes [Participation].
 */
internal object ParticipationPostProcessor : PostProcessor<Participation> {
    private val supportedClass = Participation::class.java

    override fun postProcess(
            conversionContext: ConversionContext,
            amNode: AmNode?,
            instance: Participation,
            webTemplatePath: WebTemplatePath?) {
        if (instance.function == null) {
            instance.function = DvText("unknown")
        }
        if (instance.mode == null) {
            instance.mode = DvCodedText.createWithOpenEHRTerminology("193", "not specified")
        }
    }

    override fun getType(): Class<*> = supportedClass
}
