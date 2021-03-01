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

package care.better.platform.web.template.builder.exception

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
class UnknownPathBuilderException : RuntimeException {
    companion object {
        private const val serialVersionUID: Long = 1L

        /**
         * Formats the exception message.
         *
         * @param message Exception message
         * @param path Web template path
         * @return Formatted exception message
         */
        @JvmStatic
        private fun formatMessage(message: String, path: String): String = "$message (path: $path)."
    }

    constructor(path: String) : super(formatMessage("Invalid path!", path))

    constructor(path: String, segment: String) : super(formatMessage("Unknown segment: $segment!", path))
}
