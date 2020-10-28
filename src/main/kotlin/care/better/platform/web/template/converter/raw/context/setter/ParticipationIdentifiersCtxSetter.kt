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
import org.openehr.rm.datatypes.DvIdentifier
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [CtxSetter] that sets the participation identifiers to the [ConversionContext.Builder].
 */
internal object ParticipationIdentifiersCtxSetter : CtxSetter {

    private val ATTRIBUTE_DELIMITER = Pattern.compile("::", Pattern.LITERAL)
    private val MULTIPLE_DELIMITER = Pattern.compile(";", Pattern.LITERAL)

    @Suppress("UNCHECKED_CAST")
    override fun set(builder: ConversionContext.Builder, valueConverter: ValueConverter, value: Any) {
        when (value) {
            is List<*> -> {
                value.forEachIndexed { index, element ->
                    when (element) {
                        is List<*> -> addIdentifiers(builder, element, index)
                        is String -> addIdentifiers(builder, element, index)
                        else -> addIdentifiers(builder, listOf(element as Map<String, String>), index)
                    }
                }
            }
            is Map<*, *> -> addIdentifiers(builder, listOf(value as Map<String, String>), 0)
            else -> addIdentifiers(builder, value as String, 0)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addIdentifiers(builder: ConversionContext.Builder, identifiers: List<*>, index: Int) {
        with(mutableListOf<DvIdentifier>()) {
            builder.addParticipationIdentifiers(this, index)

            if (identifiers.size == 1 && identifiers[0] is String) {
                addIdentifiers(identifiers[0] as String, this)
            } else {
                identifiers.forEachIndexed { identifierIndex, identifier ->
                    this.add(identifierIndex, DvIdentifier().apply {
                        this.id = (identifier as Map<String, String>)["|id"]
                        this.type = identifier["|type"]
                        this.assigner = identifier["|assigner"]
                        this.issuer = identifier["|issuer"]
                    })
                }
            }
        }
    }

    private fun addIdentifiers(builder: ConversionContext.Builder, value: String, index: Int) {
        with(mutableListOf<DvIdentifier>()) {
            builder.addParticipationIdentifiers(this, index)
            addIdentifiers(value, this)

        }
    }

    private fun addIdentifiers(value: String, identifiers: MutableList<DvIdentifier>) {
        MULTIPLE_DELIMITER.split(value).forEachIndexed { attributeIndex, identifier ->
            val identifierParts: List<String> = ATTRIBUTE_DELIMITER.split(identifier).toList()
            if (identifierParts.size != 4) {
                throw ConversionException("Invalid DV_IDENTIFIER value: $value, should be issuer::assigner::id::type!")
            }
            identifiers.add(attributeIndex, DvIdentifier().apply {
                this.issuer = identifierParts[0]
                this.assigner = identifierParts[1]
                this.id = identifierParts[2]
                this.type = identifierParts[3]
            })
        }
    }

}
