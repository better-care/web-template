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
        "id_namespace",
        CtxSetter { builder, _, value -> builder.withIdNamespace(value.toString()) }),
    ID_SCHEMA(
        "id_schema",
        CtxSetter { builder, _, value -> builder.withIdScheme(value.toString()) }),
    ID_SCHEME(
        "id_scheme",
        CtxSetter { builder, _, value -> builder.withIdScheme(value.toString()) }),
    COMPOSER_NAME(
        "composer_name",
        CtxSetter { builder, _, value -> builder.withComposerName(value.toString()) }),
    COMPOSER_ID(
        "composer_id",
        CtxSetter { builder, _, value -> builder.withComposerId(value.toString()) }),
    COMPOSER_SELF(
        "composer_self",
        CtxSetter { builder, _, value -> builder.withComposerSelf(value.toString().toBoolean()) }),
    CATEGORY(
        "category",
        CtxSetter { builder, _, value -> builder.withCategory(value.toString()) }),
    LANGUAGE(
        "language",
        CtxSetter { builder, _, value -> builder.withLanguage(value.toString()) }),
    TERRITORY(
        "territory",
        CtxSetter { builder, _, value -> builder.withTerritory(value.toString()) }),
    TIME(
        "time",
        CtxSetter { builder, valueConverter, value -> builder.withTime(convertToOffsetDateTime(valueConverter, value)) }),
    END_TIME(
        "end_time",
        CtxSetter { builder, valueConverter, value -> builder.withEndTime(convertToOffsetDateTime(valueConverter, value)) }),
    SETTING(
        "setting",
        CtxSetter { builder, _, value -> builder.withSetting(value.toString()) }),
    HISTORY_ORIGIN(
        "history_origin",
        CtxSetter { builder, valueConverter, value -> builder.withHistoryOrigin(convertToOffsetDateTime(valueConverter, value)) }),
    PROVIDER_NAME(
        "provider_name",
        CtxSetter { builder, _, value -> builder.withProviderName(value.toString()) }),
    PROVIDER_ID(
        "provider_id",
        CtxSetter { builder, _, value -> builder.withProviderId(value.toString()) }),
    PARTICIPATION_NAME(
        "participation_name",
        CtxSetter { builder, _, value -> setStringList(value) { index, element -> builder.addParticipationName(element, index) } }),
    PARTICIPATION_ID(
        "participation_id",
        CtxSetter { builder, _, value -> setStringList(value) { index, element -> builder.addParticipationId(element, index) } }),
    PARTICIPATION_FUNCTION(
        "participation_function",
        CtxSetter { builder, _, value -> setStringList(value) { index, element -> builder.addParticipationFunction(element, index) } }),
    PARTICIPATION_MODE(
        "participation_mode",
        ParticipationModeCtxSetter),
    PARTICIPATION_IDENTIFIERS(
        "participation_identifiers",
        ParticipationIdentifiersCtxSetter),
    ACTION_TIME(
        "action_time",
        CtxSetter { builder, valueConverter, value -> builder.withActionTime(convertToOffsetDateTime(valueConverter, value)) }),
    LOCATION(
        "location",
        CtxSetter { builder, _, value -> builder.withLocation(value.toString()) }),
    ACTION_ISM_TRANSITION_CURRENT_STATE(
        "action_ism_transition_current_state",
        CtxSetter { builder, _, value -> builder.withIsmTransitionCurrentState(value.toString()) }),
    INSTRUCTION_NARRATIVE(
        "instruction_narrative",
        CtxSetter { builder, _, value -> builder.withInstructionNarrative(value.toString()) }),
    ACTIVITY_TIMING(
        "activity_timing",
        CtxSetter { builder, _, value -> builder.withActivityTiming(value.toString()) }),
    HEALTH_CARE_FACILITY(
        "health_care_facility",
        HealthCareFacilityCtxSetter),
    HEALTHCARE_FACILITY(
        "healthcare_facility",
        HealthCareFacilityCtxSetter),
    WORK_FLOW_ID(
        "work_flow_id",
        WorkFlowIdCtxSetter),
    LINK(
        "link",
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
