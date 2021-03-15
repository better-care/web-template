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

import care.better.platform.web.template.converter.constant.WebTemplateConstants.PARTICIPATION_MODE_GROUP_NAME
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [CtxSetter] that sets the participation mode to the [ConversionContext.Builder].
 */
internal object ParticipationModeCtxSetter : CtxSetter {
    @Suppress("UNCHECKED_CAST")
    override fun set(builder: ConversionContext.Builder, valueConverter: ValueConverter, value: Any) {
        val modes = if (value is List<*>) value as List<String> else listOf(value.toString())
        modes.forEachIndexed { index, mode ->
            try {
                DvCodedText.createFromOpenEhrTerminology(PARTICIPATION_MODE_GROUP_NAME, mode)
            } catch (e: ConversionException) {
                throw ConversionException("Unknown participation mode: '$mode'")
            }
            builder.addParticipationMode(mode, index)
        }
    }
}
