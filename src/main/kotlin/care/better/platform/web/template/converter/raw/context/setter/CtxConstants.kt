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

import care.better.platform.web.template.converter.constant.WebTemplateConstants
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.node.ObjectNode
import org.joda.time.DateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Enum of all possible ctx attributes.
 *
 * @constructor Creates a new instance of [CtxSetter]
 * @param attributeName Name of the [ObjectNode] entry key
 * @param ctxSetter [CtxSetter] that sets the appropriate value to the [ConversionContext.Builder]
 */
@Suppress("unused")
internal enum class CtxConstants(val attributeName: String, val ctxSetter: CtxSetter) {
    ID_NAMESPACE(
        WebTemplateConstants.ID_NAMESPACE,
        CtxSetter { builder, _, value -> builder.withIdNamespace(value.toString()) }),
    ID_SCHEMA(
        WebTemplateConstants.ID_SCHEMA,
        CtxSetter { builder, _, value -> builder.withIdScheme(value.toString()) }),
    ID_SCHEME(
        WebTemplateConstants.ID_SCHEME,
        CtxSetter { builder, _, value -> builder.withIdScheme(value.toString()) }),
    COMPOSER_NAME(
        WebTemplateConstants.COMPOSER_NAME,
        CtxSetter { builder, _, value -> builder.withComposerName(value.toString()) }),
    COMPOSER_ID(
        WebTemplateConstants.COMPOSER_ID,
        CtxSetter { builder, _, value -> builder.withComposerId(value.toString()) }),
    COMPOSER_SELF(
        WebTemplateConstants.COMPOSER_SELF,
        CtxSetter { builder, _, value -> builder.withComposerSelf(value.toString().toBoolean()) }),
    CATEGORY(
        WebTemplateConstants.CATEGORY,
        CtxSetter { builder, _, value -> builder.withCategory(value.toString()) }),
    LANGUAGE(
        WebTemplateConstants.LANGUAGE,
        CtxSetter { builder, _, value -> builder.withLanguage(value.toString()) }),
    TERRITORY(
        WebTemplateConstants.TERRITORY,
        CtxSetter { builder, _, value -> builder.withTerritory(value.toString()) }),
    TIME(
        WebTemplateConstants.TIME,
        CtxSetter { builder, valueConverter, value -> builder.withTime(convertToOffsetDateTime(valueConverter, value)) }),
    END_TIME(
        WebTemplateConstants.END_TIME,
        CtxSetter { builder, valueConverter, value -> builder.withEndTime(convertToOffsetDateTime(valueConverter, value)) }),
    SETTING(
        WebTemplateConstants.SETTING,
        CtxSetter { builder, _, value -> builder.withSetting(value.toString()) }),
    HISTORY_ORIGIN(
        WebTemplateConstants.HISTORY_ORIGIN,
        CtxSetter { builder, valueConverter, value -> builder.withHistoryOrigin(convertToOffsetDateTime(valueConverter, value)) }),
    PROVIDER_NAME(
        WebTemplateConstants.PROVIDER_NAME,
        CtxSetter { builder, _, value -> builder.withProviderName(value.toString()) }),
    PROVIDER_ID(
        WebTemplateConstants.PROVIDER_ID,
        CtxSetter { builder, _, value -> builder.withProviderId(value.toString()) }),
    PARTICIPATION_NAME(
        WebTemplateConstants.PARTICIPATION_NAME,
        CtxSetter { builder, _, value -> setStringList(value) { index, element -> builder.addParticipationName(element, index) } }),
    PARTICIPATION_ID(
        WebTemplateConstants.PARTICIPATION_ID,
        CtxSetter { builder, _, value -> setStringList(value) { index, element -> builder.addParticipationId(element, index) } }),
    PARTICIPATION_FUNCTION(
        WebTemplateConstants.PARTICIPATION_FUNCTION,
        CtxSetter { builder, _, value -> setStringList(value) { index, element -> builder.addParticipationFunction(element, index) } }),
    PARTICIPATION_MODE(
        WebTemplateConstants.PARTICIPATION_MODE,
        ParticipationModeCtxSetter),
    PARTICIPATION_IDENTIFIERS(
        WebTemplateConstants.PARTICIPATION_IDENTIFIERS,
        ParticipationIdentifiersCtxSetter),
    ACTION_TIME(
        WebTemplateConstants.ACTION_TIME,
        CtxSetter { builder, valueConverter, value -> builder.withActionTime(convertToOffsetDateTime(valueConverter, value)) }),
    LOCATION(
        WebTemplateConstants.LOCATION,
        CtxSetter { builder, _, value -> builder.withLocation(value.toString()) }),
    ACTION_ISM_TRANSITION_CURRENT_STATE(
        WebTemplateConstants.ACTION_ISM_TRANSITION_CURRENT_STATE,
        CtxSetter { builder, _, value -> builder.withIsmTransitionCurrentState(value.toString()) }),
    INSTRUCTION_NARRATIVE(
        WebTemplateConstants.INSTRUCTION_NARRATIVE,
        CtxSetter { builder, _, value -> builder.withInstructionNarrative(value.toString()) }),
    ACTIVITY_TIMING(
        WebTemplateConstants.ACTIVITY_TIMING,
        CtxSetter { builder, _, value -> builder.withActivityTiming(value.toString()) }),
    HEALTH_CARE_FACILITY(
        WebTemplateConstants.HEALTH_CARE_FACILITY,
        HealthCareFacilityCtxSetter),
    HEALTHCARE_FACILITY(
        WebTemplateConstants.HEALTHCARE_FACILITY,
        HealthCareFacilityCtxSetter),
    WORK_FLOW_ID(
        WebTemplateConstants.WORK_FLOW_ID,
        WorkFlowIdCtxSetter),
    LINK(
        WebTemplateConstants.LINK,
        LinkCtxSetter);

    companion object {
        private val attributeNameMap: Map<String, CtxConstants> = values().associateBy { it.attributeName }

        /**
         * Returns [CtxConstants] for the attribute name.
         *
         * @param name Name of the attribute
         */
        @JvmStatic
        fun forAttributeName(name: String): CtxConstants? =
            if (name.startsWith("_"))
                attributeNameMap[name.substring(1, name.length)]
            else
                attributeNameMap[name]
    }
}

/**
 * Converts value to [OffsetDateTime].
 *
 * @param valueConverter [ValueConverter]
 * @param value [Any]
 * @return [OffsetDateTime]
 */
internal fun convertToOffsetDateTime(valueConverter: ValueConverter, value: Any): OffsetDateTime {
    return when (value) {
        is DateTime -> OffsetDateTime.ofInstant(value.toDate().toInstant(), ZoneId.of(value.zone.id))
        is ZonedDateTime -> value.toOffsetDateTime()
        is OffsetDateTime -> value
        else -> valueConverter.parseDateTime(value.toString())
    }
}

/**
 * Converts value to [List] of [String] and invokes the setter.
 *
 * @param value Value to convert and set
 * @param setter Consumer that accepts the index of element in the list and the element itself and set them to the [ConversionContext.Builder]
 */
@Suppress("UNCHECKED_CAST")
internal fun setStringList(value: Any, setter: (Int, String) -> Unit) {
    if (value is List<*>) {
        value.forEachIndexed { index, element ->
            setter.invoke(index, element.toString())
        }
    } else {
        setter.invoke(0, value.toString())
    }
}
