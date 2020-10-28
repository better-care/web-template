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

package care.better.platform.web.template.converter.raw.special

import care.better.platform.web.template.converter.exceptions.ConversionException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton that provides [SpecialCaseRmObjectHandler] instance based on the attribute name.
 */
internal object SpecialCaseRmObjectHandlerProvider {

    private val specialCaseRmNodeHandlers: Map<String, SpecialCaseRmObjectHandler> =
        mapOf(
            Pair(HistoryOriginHandler.specialAttribute, HistoryOriginHandler),
            Pair(HistoryDataOriginHandler.specialAttribute, HistoryDataOriginHandler),
            Pair(HistoryStateOriginHandler.specialAttribute, HistoryStateOriginHandler),
            Pair(DvIntervalLowerIncludedHandler.specialAttribute, DvIntervalLowerIncludedHandler),
            Pair(DvIntervalUpperIncludedHandler.specialAttribute, DvIntervalUpperIncludedHandler),
            Pair(DvIntervalLowerUnboundedHandler.specialAttribute, DvIntervalLowerUnboundedHandler),
            Pair(DvIntervalUpperUnboundedHandler.specialAttribute, DvIntervalUpperUnboundedHandler))

    /**
     * Checks if [SpecialCaseRmObjectHandler] for the attribute exists.
     *
     * @param specialCaseAttribute Attribute
     * @return [Boolean] indicating if [SpecialCaseRmObjectHandler] for the attribute exists
     */
    fun isSpecialCaseAttribute(specialCaseAttribute: String): Boolean = specialCaseRmNodeHandlers[specialCaseAttribute] != null

    /**
     * Provides [SpecialCaseRmObjectHandler] instance based on the attribute name.
     *
     * @param specialCaseAttribute Attribute
     * @return [SpecialCaseRmObjectHandler]
     * @throws [ConversionException] if [SpecialCaseRmObjectHandler] is not found
     */
    fun provide(specialCaseAttribute: String): SpecialCaseRmObjectHandler =
        specialCaseRmNodeHandlers[specialCaseAttribute]
            ?: throw ConversionException("Special case RM object handler for $specialCaseAttribute attribute not found.")
}
