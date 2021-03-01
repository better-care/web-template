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

package care.better.platform.web.template.builder.input

import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.builder.model.input.WebTemplateValidation
import care.better.platform.web.template.builder.model.input.range.WebTemplateTemporalRange
import org.apache.commons.lang3.StringUtils

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
abstract class TemporalWebTemplateInputBuilder<T> : WebTemplateInputBuilder<T> {
    open fun build(type: WebTemplateInputType, range: WebTemplateTemporalRange, pattern: String?): WebTemplateInput {
        val input = WebTemplateInput(type)

        if (!range.isEmpty()) {
            input.validation = WebTemplateValidation().apply { this.range = range }
        }
        if (StringUtils.isNotBlank(pattern)) {
            if (input.validation == null) {
                input.validation = WebTemplateValidation()
            }
            val validation = input.validation ?: WebTemplateValidation()
            validation.pattern = pattern
        }
        input.fixed = range.isFixed()
        return input
    }

    open fun build(type: WebTemplateInputType): WebTemplateInput {
        return WebTemplateInput(type)
    }
}
