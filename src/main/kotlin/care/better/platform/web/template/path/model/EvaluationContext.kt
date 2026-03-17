/* Copyright 2026 Better Ltd (www.better.care)
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

package care.better.platform.web.template.path.model

import care.better.platform.web.template.converter.FromRawConversion
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.value.SimpleValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Context for path expression evaluation
 *
 * @constructor Creates a new instance of [EvaluationContext]
 * @param webTemplatePath Web template path (e.g. `blood_pressure/any_event`)
 * @param aqlPath AQL path (e.g. `/content[openEHR-EHR-OBSERVATION.blood_pressure.v1]`)
 * @param valueConverter [ValueConverter] used for RAW conversion
 * @param objectMapper [ObjectMapper] used for RAW conversion
 */
data class EvaluationContext internal constructor(
    val webTemplatePath: String? = null,
    val aqlPath: String? = null,
    val valueConverter: ValueConverter? = null,
    val objectMapper: ObjectMapper? = null) {

    /**
     * Creates a [FromRawConversion] from this [EvaluationContext].
     *
     * @return [FromRawConversion]
     */
    internal fun toFromRawConversion(): FromRawConversion =
        when {
            aqlPath != null -> FromRawConversion.createForAqlPath(aqlPath, valueConverter ?: SimpleValueConverter, objectMapper ?: ConversionObjectMapper)
            webTemplatePath != null -> FromRawConversion.createForWebTemplatePath(webTemplatePath, valueConverter ?: SimpleValueConverter, objectMapper ?: ConversionObjectMapper)
            else -> FromRawConversion.create(valueConverter ?: SimpleValueConverter, objectMapper ?: ConversionObjectMapper)
        }

    companion object {
        /**
         * Creates an [EvaluationContext] with a web template path.
         *
         * @param webTemplatePath WebTemplate path
         * @param valueConverter [ValueConverter]
         * @param objectMapper [ObjectMapper]
         * @return [EvaluationContext]
         */
        @JvmStatic
        @JvmOverloads
        fun ofWebTemplatePath(webTemplatePath: String, valueConverter: ValueConverter? = null, objectMapper: ObjectMapper? = null): EvaluationContext =
            EvaluationContext(webTemplatePath = webTemplatePath, valueConverter = valueConverter, objectMapper = objectMapper)

        /**
         * Creates an [EvaluationContext] with an AQL path.
         *
         * @param aqlPath AQL path
         * @param valueConverter [ValueConverter]
         * @param objectMapper [ObjectMapper]
         * @return [EvaluationContext]
         */
        @JvmStatic
        @JvmOverloads
        fun ofAqlPath(aqlPath: String, valueConverter: ValueConverter? = null, objectMapper: ObjectMapper? = null): EvaluationContext =
            EvaluationContext(aqlPath = aqlPath, valueConverter = valueConverter, objectMapper = objectMapper)
    }
}