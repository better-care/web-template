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

import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import care.better.platform.web.template.converter.value.SimpleValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.ObjectMapper
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
        val valueConverter: ValueConverter,
        val objectMapper: ObjectMapper) {

    companion object {
        /**
         * Creates a new instance of [FromRawConversion] for [ValueConverter] and [ObjectMapper].
         *
         * @param valueConverter [ValueConverter]
         * @param objectMapper [ObjectMapper]
         * @return [FromRawConversion]
         */
        @JvmOverloads
        @JvmStatic
        fun create(valueConverter: ValueConverter = SimpleValueConverter, objectMapper: ObjectMapper = ConversionObjectMapper): FromRawConversion =
            FromRawConversion(valueConverter = valueConverter, objectMapper = objectMapper)

        /**
         * Creates a new instance of [FromRawConversion] for [Locale].
         *
         * @param locale [Locale]
         * @return [FromRawConversion]
         */
        @JvmOverloads
        @JvmStatic
        fun create(locale: Locale, objectMapper: ObjectMapper = ConversionObjectMapper) =
            FromRawConversion(valueConverter = LocaleBasedValueConverter(locale), objectMapper = objectMapper)

        /**
         * Creates a new instance of [FromRawConversion] for the AQL path.
         *
         * @return [FromRawConversion]
         */
        @JvmOverloads
        @JvmStatic
        fun createForAqlPath(aqlPath: String, valueConverter: ValueConverter = SimpleValueConverter, objectMapper: ObjectMapper = ConversionObjectMapper) =
            FromRawConversion(aqlPath = aqlPath, valueConverter = valueConverter, objectMapper = objectMapper)

        /**
         * Creates a new instance of [FromRawConversion] for the AQL path and [Locale].
         *
         * @param locale [Locale]
         * @return [FromRawConversion]
         */
        @JvmOverloads
        @JvmStatic
        fun createForAqlPath(aqlPath: String, locale: Locale, objectMapper: ObjectMapper = ConversionObjectMapper) =
            FromRawConversion(aqlPath = aqlPath, valueConverter = LocaleBasedValueConverter(locale), objectMapper = objectMapper)

        /**
         * Creates a new instance of [FromRawConversion] for the web template path.
         *
         * @return [FromRawConversion]
         */
        @JvmOverloads
        @JvmStatic
        fun createForWebTemplatePath(
                webTemplatePath: String,
                valueConverter: ValueConverter = SimpleValueConverter,
                objectMapper: ObjectMapper = ConversionObjectMapper) =
            FromRawConversion(webTemplatePath = webTemplatePath, valueConverter = valueConverter, objectMapper = objectMapper)

        /**
         * Creates a new instance of [FromRawConversion] for the web template path and [Locale].
         *
         * @param locale [Locale]
         * @return [FromRawConversion]
         */
        @JvmOverloads
        @JvmStatic
        fun crateForWebTemplatePath(webTemplatePath: String, locale: Locale, objectMapper: ObjectMapper = ConversionObjectMapper) =
            FromRawConversion(webTemplatePath = webTemplatePath, valueConverter = LocaleBasedValueConverter(locale), objectMapper = objectMapper)
    }
}
