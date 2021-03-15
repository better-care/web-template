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

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [RmObjectNodeFactory] that creates a new instance of [RmObject] using the provided factory supplier.
 *
 * @constructor Creates a new instance of [RmObjectInstanceFactory]
 * @param factory Supplier that creates a new instance of [RmObject]
 */
internal class RmObjectInstanceFactory<T : RmObject>(private val factory: () -> T) : RmObjectNodeFactory<T> {
    override fun create(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T = factory.invoke()
}
