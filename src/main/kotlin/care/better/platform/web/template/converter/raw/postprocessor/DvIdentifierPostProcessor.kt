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
import org.openehr.rm.datatypes.DvIdentifier

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [PostProcessor] that post-processes [DvIdentifier].
 */
internal object DvIdentifierPostProcessor : PostProcessor<DvIdentifier> {
    private val supportedClass = DvIdentifier::class.java


    override fun postProcess(
            conversionContext: ConversionContext,
            amNode: AmNode?,
            instance: DvIdentifier,
            webTemplatePath: WebTemplatePath?) {
        if (instance.issuer == null) {
            instance.issuer = conversionContext.identifierIssuer
        }
        if (instance.assigner == null) {
            instance.assigner = conversionContext.identifierAssigner
        }
        if (instance.type == null) {
            instance.type = conversionContext.identifierType
        }
    }

    override fun getType(): Class<*> = supportedClass
}
