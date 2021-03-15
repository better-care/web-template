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

import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.structured.exceptions.PathFormatException
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Data class that represents the web template path child-parent structure without attributes.
 */
data class WebTemplatePath(val key: String, val parent: WebTemplatePath? = null, val index: Int? = null) {

    companion object {
        @JvmStatic
        fun forBlankPath(): WebTemplatePath = WebTemplatePath("", null, null)
    }

    /**
     * Returns [String] representation of this [WebTemplatePath] recursively.
     *
     * @return [String] representation of this [WebTemplatePath]
     */
    override fun toString(): String {
        if (parent == null) {
            return getWebTemplatePathSegmentString()
        }
        return "$parent${with(getWebTemplatePathSegmentString()) { if (this.startsWith("|")) this else "/$this" }}"
    }

    fun copy(amNode: AmNode, index: Int? = null): WebTemplatePath =
        if (amNode.occurrences.upper != null) {
            if (1 == amNode.occurrences.upper) {
                this.copy(index = null)
            } else {
                if (index != null) {
                    this.copy(index = index)
                } else {
                    this
                }
            }
        } else {
            if (index != null) {
                this.copy(index = index)
            } else {
                this
            }
        }


    /**
     * Returns [String] representation of this [WebTemplatePath].
     *
     * @return [String] representation of this [WebTemplatePath]
     */
    private fun getWebTemplatePathSegmentString() = "$key${if (index == null) "" else ":$index"}"

    /**
     * Creates new instance of [WebTemplatePath] with this [WebTemplatePath] parent and null index.
     *
     * @param key Web template path segment key
     * @return [WebTemplatePath]
     */
    operator fun plus(key: String): WebTemplatePath = WebTemplatePath(key, this, null)
}

/**
 * Data class that represents the web template path patent-child structure without attributes.
 */
data class ReversedWebTemplatePath(val key: String, val child: ReversedWebTemplatePath? = null, val index: Int? = null) {
    companion object {
        private val segmentSeparatorPattern = Pattern.compile("/", Pattern.LITERAL)

        /**
         * Creates [ReversedWebTemplatePath] from web template path.
         *
         * @param webTemplatePath Web template path
         * @return [ReversedWebTemplatePath]
         */
        @JvmStatic
        fun fromString(webTemplatePath: String): ReversedWebTemplatePath =
            convertRecursive(webTemplatePath.split(segmentSeparatorPattern).toList(), 0)!!

        /**
         * Recursively converts the web template path [String] to the [ReversedWebTemplatePath].
         *
         * @param webTemplatePathSegments [List] of web template path segments
         * @param index Index of the current segment in the list
         * @return [ReversedWebTemplatePath]
         */
        private fun convertRecursive(webTemplatePathSegments: List<String>, index: Int): ReversedWebTemplatePath? =
            if (index >= webTemplatePathSegments.size) {
                null
            } else {
                val webTemplatePathSegment = convertRecursive(webTemplatePathSegments, index + 1)
                with(WebTemplatePathSegment.fromString(webTemplatePathSegments[index])) {
                    ReversedWebTemplatePath(this.key, webTemplatePathSegment, this.index)

                }
            }
    }

    /**
     * Returns the web template path without indexes.
     *
     * @return web template path without indexes
     */
    fun getId(): String {
        return "${key}${if (child == null) "" else "/${child.getId()}"}"
    }

    /**
     * Returns [String] representation of this [ReversedWebTemplatePath] recursively.
     *
     * @return [String] representation of this [ReversedWebTemplatePath]
     */
    override fun toString(): String {
        if (child == null) {
            return getWebTemplatePathSegmentString()
        }
        return "${getWebTemplatePathSegmentString()}${with(child.toString()) { { if (this.startsWith("|")) this else "/$this" } }}"
    }

    /**
     * Returns [String] representation of this [ReversedWebTemplatePath].
     *
     * @return [String] representation of this [ReversedWebTemplatePath]
     */
    private fun getWebTemplatePathSegmentString() = "$key${if (index == null) "" else ":$index"}"
}


/**
 * Data class that represents the web template path segment.
 */
data class WebTemplatePathSegment(val key: String, val index: Int?, val attribute: String?, val attributeIndex: Int?) {
    companion object {
        private val attributeSeparatorPattern = Pattern.compile("|", Pattern.LITERAL)
        private val indexSeparatorPattern = Pattern.compile(":", Pattern.LITERAL)


        /**
         * Creates [WebTemplatePathSegment] from the web template path segment.
         *
         * @param webTemplatePathSegment Web template path segment
         * @return [WebTemplatePathSegment]
         */
        @JvmStatic
        fun fromString(webTemplatePathSegment: String): WebTemplatePathSegment {
            try {
                val (keyString, attributeString) = webTemplatePathSegment.split(attributeSeparatorPattern).toList().padWithNulls(2)

                val (key, index) = (keyString ?: throw PathFormatException(webTemplatePathSegment)).split(indexSeparatorPattern).toList().padWithNulls(2)

                return if (attributeString == null) {
                    WebTemplatePathSegment(key ?: throw PathFormatException(webTemplatePathSegment), index?.toInt(), null, null)
                } else {
                    val (attribute, attributeIndex) = attributeString.split(indexSeparatorPattern).toList().padWithNulls(2)

                    WebTemplatePathSegment(
                        key ?: throw PathFormatException(webTemplatePathSegment),
                        index?.toInt(),
                        attribute?.let { "|$it" },
                        attributeIndex?.toInt())
                }
            } catch (ex: Exception) {
                throw PathFormatException(webTemplatePathSegment)
            }
        }

        private fun <E> List<E>.padWithNulls(limit: Int): List<E?> {
            val result: MutableList<E?> = this.toMutableList()

            while (result.size < limit) {
                result.add(null)
            }

            return result
        }
    }
}
