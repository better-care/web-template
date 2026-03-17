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

package care.better.platform.web.template.path.exception

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Exception thrown when a web template path expression is invalid or evaluation fails.
 *
 * @constructor Creates a new instance of [WebTemplatePathExpressionException]
 * @param message Exception message
 */
class WebTemplatePathExpressionException(message: String) : RuntimeException(message) {
    companion object {
        @Suppress("unused")
        private const val serialVersionUID: Long = 1L
    }
}
