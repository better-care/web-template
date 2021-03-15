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

package care.better.platform.web.template.builder.context

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.builder.path.NoopPathFilter
import care.better.platform.web.template.builder.path.PathFilter

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Context for used when building [WebTemplate].
 *
 * @constructor Creates a new instance of [WebTemplateBuilderContext]
 * @param defaultLanguage Default language
 * @param languages [Collection] of other possible languages
 * @param isAddDescriptions [Boolean] indicating if descriptions will be added or not
 * @param filter [PathFilter]
 */
data class WebTemplateBuilderContext @JvmOverloads constructor(
        val defaultLanguage: String? = null,
        val languages: Collection<String> = emptyList(),
        val contextLanguage: String? = null,
        val isAddDescriptions: Boolean = true,
        val filter: PathFilter = NoopPathFilter)
