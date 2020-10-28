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

package care.better.platform.web.template.converter.raw.context.setter

import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.ValueConverter

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Interface used to set context values to the [ConversionContext.Builder].
 */
internal fun interface CtxSetter {
    /**
     * Set the value to the [ConversionContext.Builder].
     *
     * @param builder [ConversionContext.Builder]
     * @param valueConverter [ValueConverter]
     * @param value Value to set
     */
    fun set(builder: ConversionContext.Builder, valueConverter: ValueConverter, value: Any)
}
