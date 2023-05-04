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

package care.better.platform.web.template.converter.exceptions

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Exception that is thrown during RM object conversion.
 */
@Suppress("USELESS_CAST")
class ConversionException : RuntimeException {

    private var path: String? = null

    companion object {
        /**
         * Formats the exception message.
         *
         * @param message Exception message
         * @param path Web template or AQL path
         * @return Formatted exception message
         */
        @JvmStatic
        private fun formatMessage(message: String, path: String): String = "$message (path: $path)."
    }

    /**
     * Creates a new instance of [ConversionException].
     *
     * @param message Exception message
     */
    constructor(message: String) : super(message)

    /**
     * Creates a new instance of [ConversionException].
     *
     * @param message Exception message
     * @param cause [Throwable]
     */
    constructor(message: String, cause: Throwable) : super(message, cause)

    /**
     * Creates a new instance of [ConversionException].
     *
     * @param cause [Throwable]
     */
    constructor(cause: Throwable) : super(cause)

    /**
     * Creates a new instance of [ConversionException].
     *
     * @param message Exception message
     * @param path Web template or AQL path
     */
    constructor(message: String, path: String) : this(formatMessage(message, path)) {
        this.path = path
    }

    /**
     * Creates a new instance of [ConversionException].
     *
     * @param message Exception message
     * @param cause [Throwable]
     * @param path Web template or AQL path
     */
    constructor(message: String, cause: Throwable, path: String) : this(formatMessage(message, path), cause) {
        this.path = path
    }

    /**
     * Returns web template or AQL path.
     *
     * @return Web template or AQL path.
     */
    fun getPath() = path

    fun copyWithPath(path: String) =
        if (cause != null && message != null)
            ConversionException(message as String, cause as Throwable, path)
        else if (message != null)
            ConversionException(message as String, path)
        else
            ConversionException(path)
}
