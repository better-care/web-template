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

package care.better.platform.web.template.converter.flat.context

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Context used when mapping RM object in RAW format to the RM object in FLAT format.
 */
abstract class AbstractFlatMappingContext<T> {
    private val flatMap: LinkedHashMap<String, T> = linkedMapOf()

    operator fun set(key: String, value: T?) {
        if (value != null) {
            flatMap[key] = value
        }
    }

    fun getIterator() = flatMap.entries.iterator()

    fun remove(key: String) {
        flatMap.remove(key)
    }

    fun getMutable(): Map<String, T> = flatMap

    fun get(): Map<String, T> = flatMap.toMap()
}

/**
 * Instance of [AbstractFlatMappingContext] that converts the RM object in RAW format to the RM object in FLAT format.
 *
 * @constructor Creates a new instance of [FlatMappingContext]
 */
class FlatMappingContext : AbstractFlatMappingContext<Any>()


/**
 * Instance of [AbstractFlatMappingContext] that converts the RM object in RAW format to the RM object in FLAT format with formatted values.
 *
 * @constructor Creates a new instance of [FormattedFlatMappingContext]
 */
class FormattedFlatMappingContext : AbstractFlatMappingContext<String>()

