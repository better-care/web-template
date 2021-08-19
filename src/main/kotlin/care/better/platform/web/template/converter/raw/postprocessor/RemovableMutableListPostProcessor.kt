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

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.isEmpty

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [PostProcessor] that post-processes [MutableList] and removed empty elements.
 */
internal object RemovableMutableListPostProcessor : AbstractMutableListPostProcessor() {
    override fun mustRemove(element: Any, conversionContext: ConversionContext): Boolean =
        if (element is RmObject)
            element.isEmpty(conversionContext.strictMode)
        else
            false

}
