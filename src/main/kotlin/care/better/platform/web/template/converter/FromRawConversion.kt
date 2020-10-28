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

package care.better.platform.web.template.converter

import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import care.better.platform.web.template.converter.value.SimpleValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Set of instructions used to convert the RM object from RAW format to the STRUCTURED or FLAT format.
 */
class FromRawConversion private constructor(
        val aqlPath: String? = null,
        val webTemplatePath: String? = null,
        val valueConverter: ValueConverter) {

    companion object {
        /**
         * Creates a new instance of [FromRawConversion].
         *
         * @return [FromRawConversion]
         */
        fun create(): FromRawConversion =
            FromRawConversion(valueConverter = SimpleValueConverter)

        /**
         * Creates a new instance of [FromRawConversion] for [ValueConverter].
         *
         * @param valueConverter [ValueConverter]
         * @return [FromRawConversion]
         */
        fun create(valueConverter: ValueConverter): FromRawConversion =
            FromRawConversion(valueConverter = valueConverter)

        /**
         * Creates a new instance of [FromRawConversion] for [Locale].
         *
         * @param locale [Locale]
         * @return [FromRawConversion]
         */
        fun create(locale: Locale) =
            FromRawConversion(valueConverter = LocaleBasedValueConverter(locale))

        /**
         * Creates a new instance of [FromRawConversion] for the AQL path.
         *
         * @return [FromRawConversion]
         */
        fun createForAqlPath(aqlPath: String) =
            FromRawConversion(aqlPath = aqlPath, valueConverter = SimpleValueConverter)

        /**
         * Creates a new instance of [FromRawConversion] for the AQL path and [ValueConverter].
         *
         * @param valueConverter [ValueConverter]
         * @return [FromRawConversion]
         */
        fun createForAqlPath(aqlPath: String, valueConverter: ValueConverter) =
            FromRawConversion(aqlPath = aqlPath, valueConverter = valueConverter)

        /**
         * Creates a new instance of [FromRawConversion] for the AQL path and [Locale].
         *
         * @param locale [Locale]
         * @return [FromRawConversion]
         */
        fun crateForAqlPath(aqlPath: String, locale: Locale) =
            FromRawConversion(aqlPath = aqlPath, valueConverter = LocaleBasedValueConverter(locale))

        /**
         * Creates a new instance of [FromRawConversion] for the web template path.
         *
         * @return [FromRawConversion]
         */
        fun createForWebTemplatePath(webTemplatePath: String) =
            FromRawConversion(webTemplatePath = webTemplatePath, valueConverter = SimpleValueConverter)

        /**
         * Creates a new instance of [FromRawConversion] for the web template path and [ValueConverter].
         *
         * @param valueConverter [ValueConverter]
         * @return [FromRawConversion]
         */
        fun createForWebTemplatePath(webTemplatePath: String, valueConverter: ValueConverter) =
            FromRawConversion(webTemplatePath = webTemplatePath, valueConverter = valueConverter)

        /**
         * Creates a new instance of [FromRawConversion] for the web template path and [Locale].
         *
         * @param locale [Locale]
         * @return [FromRawConversion]
         */
        fun crateForWebTemplatePath(webTemplatePath: String, locale: Locale) =
            FromRawConversion(webTemplatePath = webTemplatePath, valueConverter = LocaleBasedValueConverter(locale))
    }
}
