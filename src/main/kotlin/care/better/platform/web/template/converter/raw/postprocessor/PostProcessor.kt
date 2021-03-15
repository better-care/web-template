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

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Interface used to post-process the RM object or [MutableList] of the RM objects in RAW format that were created from STRUCTURED format.
 */
interface PostProcessor<T> {
    /**
     * Post-processes the RM object in RAW format.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [AmNode]
     * @param instance RM object or [MutableList] of RM objects in RAW format
     * @param webTemplatePath Web template path to the RM object or to the [List] of RM objects
     */
    fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: T, webTemplatePath: WebTemplatePath?)

    /**
     * Checks if this [PostProcessor] will accept the RM object or [MutableList] of the RM objects in RAW format.
     *
     * @param instanceClass [Class] of the RM object or [MutableList] of RM objects that will be post processed.
     * @return [Boolean] indicating if this [PostProcessor] can process the RM object or [MutableList] of the RM objects
     */
    @JvmDefault
    fun accept(instanceClass: Class<*>): Boolean = getType().isAssignableFrom(instanceClass)

    /**
     * Returns the RM object or [MutableList] of the RM objects [Class] that can be processed by this [PostProcessor].
     *
     * @return RM object or [MutableList] of RM objects [Class] that can be processed by this [PostProcessor]
     */
    fun getType(): Class<*>

    /**
     * Returns the order of this [PostProcessor].
     * Note that [Integer] MIN_VALUE will be returned by default.
     *
     * @return Order of this [PostProcessor]
     */
    @JvmDefault
    fun getOrder(): Int = Integer.MIN_VALUE
}




