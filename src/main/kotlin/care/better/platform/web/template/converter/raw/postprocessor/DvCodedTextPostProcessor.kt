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
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.openehr.rm.datatypes.DvCodedText
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [PostProcessor] that post-processes [DvCodedText].
 */
internal object DvCodedTextPostProcessor : PostProcessor<DvCodedText> {
    private val supportedClass = DvCodedText::class.java

    private val SUBFIELD_PATTERN = Pattern.compile("(.*?)(" + Pattern.quote("|") + "(.+))?$")

    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: DvCodedText, webTemplatePath: WebTemplatePath?) {
        if (instance.value == null && instance.definingCode != null) {
            val wtPath = webTemplatePath.toString()
            val matcher = SUBFIELD_PATTERN.matcher(wtPath)
            throw ConversionException("Missing DvCodedText.value at ${if (matcher.matches()) matcher.group(1) else wtPath}!")
        }
        DvTextPostProcessor.postProcess(conversionContext, amNode, instance, webTemplatePath)
    }

    override fun getType(): Class<*> = supportedClass

    override fun getOrder(): Int = Integer.MAX_VALUE - 9
}
