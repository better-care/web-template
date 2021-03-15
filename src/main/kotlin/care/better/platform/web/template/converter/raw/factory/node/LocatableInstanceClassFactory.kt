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
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.openehr.rm.common.Locatable

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [LocatableFactory] that creates a new instance of [Locatable] using reflection.
 * This is useful for classes with unknown generic types.
 *
 * @constructor Creates a new instance of [LocatableInstanceClassFactory]
 * @param rmClass [Locatable] [Class] for which a new instance will be created
 */
internal open class LocatableInstanceClassFactory<T : Locatable>(private val rmClass: Class<T>) : LocatableFactory<T>() {
    override fun createLocatable(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): T =
        try {
            rmClass.getConstructor().newInstance()
        } catch (e: Exception) {
            throw ConversionException(e)
        }
}
