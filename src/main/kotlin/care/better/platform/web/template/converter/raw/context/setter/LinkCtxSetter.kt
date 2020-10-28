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

package care.better.platform.web.template.converter.raw.context.setter

import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.common.Link
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvEhrUri
import org.openehr.rm.datatypes.DvText
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [CtxSetter] that sets the links to the [ConversionContext.Builder].
 */
internal object LinkCtxSetter : CtxSetter {
    private val DELIMITER_PATTERN = Pattern.compile("::", Pattern.LITERAL)
    private val DELIMITER_REGEX = DELIMITER_PATTERN.toRegex()

    @Suppress("UNCHECKED_CAST")
    override fun set(builder: ConversionContext.Builder, valueConverter: ValueConverter, value: Any) {
        if (value is Map<*, *>) {
            addLinks(builder, value as Map<String, String>, 0)
        } else {
            (value as List<Map<String, String>>).forEachIndexed { listIndex, link ->
                addLinks(builder, link, listIndex)
            }
        }
    }

    private fun addLinks(builder: ConversionContext.Builder, link: Map<String, String>, index: Int) {
        builder.addLink(
            Link().apply {
                link["|meaning"]?.also { this.meaning = getDvTextOrCodedText(it) }
                link["|type"]?.also { this.type = getDvTextOrCodedText(it) }
                link["|target"]?.also { this.target = DvEhrUri().apply { this.value = it } }
            },
            index)
    }

    private fun getDvTextOrCodedText(valueString: String): DvText =
        with(DELIMITER_PATTERN.split(valueString)) {
            when (this.size) {
                1 -> DvText(this[0])
                2 -> DvCodedText().apply {
                    this.value = this@with[1]
                    this.definingCode = CodePhrase().apply { this.codeString = this@with[0] }
                }
                3 -> DvCodedText.create(this[0], this[1], this[2])
                else -> throw ConversionException("Wrong format of DV_CODED_TEXT string: $valueString.")
            }
        }
}
