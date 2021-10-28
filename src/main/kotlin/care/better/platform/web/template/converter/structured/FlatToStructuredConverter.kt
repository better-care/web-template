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

package care.better.platform.web.template.converter.structured

import care.better.openehr.rm.RmObject
import care.better.platform.time.format.OpenEhrDateTimeFormatter
import care.better.platform.time.temporal.*
import care.better.platform.web.template.converter.WebTemplatePathSegment
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.mapper.isEmptyInDepth
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.SimpleValueConverter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.format.ISOPeriodFormat
import java.math.BigDecimal
import java.math.BigInteger
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Converts the RM object in FLAT format to the RM object in STRUCTURED format.
 */
class FlatToStructuredConverter(private val objectMapper: ObjectMapper) : (Map<String, Any?>) -> JsonNode, (Map<String, Any?>, ConversionContext) -> JsonNode {

    companion object {
        private val segmentSeparatorPattern = Pattern.compile("/", Pattern.LITERAL)
        private const val ctx = "ctx"

        private val INSTANCE: FlatToStructuredConverter = FlatToStructuredConverter(ConversionObjectMapper)

        @JvmStatic
        fun getInstance(): FlatToStructuredConverter = INSTANCE
    }

    /**
     *  Converts the RM object in FLAT format to the RM object in STRUCTURED format.
     *
     *  @param map RM object in FLAT format
     *  @return RM object in STRUCTURED format
     */
    override fun invoke(map: Map<String, Any?>): JsonNode = map(map, false)

    /**
     *  Converts the RM object in FLAT format to the RM object in STRUCTURED format.
     *
     *  @param map RM object in FLAT format
     *  @param conversionContext [ConversionContext]
     *  @return RM object in STRUCTURED format
     */
    override fun invoke(map: Map<String, Any?>, conversionContext: ConversionContext): JsonNode = map(map, conversionContext.strictMode)

    private fun map(map: Map<String, Any?>, strict: Boolean = false): JsonNode = objectMapper.createObjectNode().apply {
        convertToStructured(map.asSequence().filter { strict || isNotEmpty(it.value) }.map { convertEntry(it.key, it.value) }.toList(), this, 0)
    }

    /**
     * Checks if object is not empty.
     *
     * @param value Object
     * @return [Boolean] indicating if object is not empty
     */
    private fun isNotEmpty(value: Any?): Boolean =
        when {
            value == null -> false
            value is String && value.isBlank() -> false
            value is List<*> && value.isEmpty() -> false
            value is Map<*, *> && value.isEmpty() -> false
            value is JsonNode -> !value.isEmptyInDepth()
            else -> true
        }

    /**
     *  Converts the RM object in FLAT format to the RM object in STRUCTURED format.
     *
     *  @param entries [List] of [EntryDto]
     *  @param node [JsonNode]
     */
    private fun convertToStructured(entries: List<EntryDto>, node: JsonNode, depth: Int) {
        if (entries.isEmpty()) {
            return
        }

        val currentNode: ObjectNode = if (node.isObject) node as ObjectNode else (node as ArrayNode).addObject()
        entries.groupBy { it.name }.asSequence().forEach { entry ->
            when {
                entry.key == ctx -> convertCtx(entry.value, currentNode)
                entry.value.all { it.child == null } -> {
                    if (entry.value.size == 1 && (entry.key.startsWith("|") || entry.key.isBlank())) {
                        currentNode.replace(entry.key, get(entry.value.iterator().next().value))
                    } else {
                        currentNode.putArray(entry.key).apply {
                            entry.value.asSequence().sortedBy { it.order }.forEach { add(it.value, this) }
                        }
                    }
                }
                else -> {
                    if (depth == 0) {
                        if(entry.value.distinctBy { it.order }.count() > 1) {
                            throw ConversionException("Expecting to convert single RM object, but multiple were provided.")
                        }
                        currentNode.putObject(entry.key).apply {
                            convertToStructured(entry.value.mapNotNull { value -> value.child }, this, depth + 1)
                        }
                    } else {
                        currentNode.putArray(entry.key).apply {
                            entry.value.groupBy { it.order }.asSequence().sortedBy { it.key }.forEach {
                                convertToStructured(convertEntries(it.value).mapNotNull { value -> value.child }, this, depth + 1)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts [List] of [EntryDto] if necessary.
     *
     * NOTE: This handles the cases when one of the value is passed without the attribute. For example:
     *
     * "request_histopathology/service_request:0/current_activity:0/timing": "R1"
     * "request_histopathology/service_request:0/current_activity:0/timing|formalism": "timing"
     *
     * @param entries [List] of [EntryDto]
     * @return Converted [List] of [EntryDto]
     */
    private fun convertEntries(entries: List<EntryDto>): List<EntryDto> =
            entries.map {
                if (it.child == null)
                    EntryDto(it.name, it.order, EntryDto("", 0, null, it.value), null)
                else
                    it
            }

    /**
     *  Converts ctx in FLAT format to ctx in STRUCTURED format.
     *
     *  @param entries [List] of [EntryDto]
     *  @param node [JsonNode]
     */
    private fun convertCtx(entries: List<EntryDto>, node: JsonNode) {
        if (entries.isEmpty()) {
            return
        }

        val currentNode: ObjectNode = if (node.isObject) node as ObjectNode else (node as ArrayNode).addObject()
        entries.groupBy { it.name }.asSequence().forEach { entry ->
            when {
                entry.value.all { it.child == null } -> {
                    if (entry.value.size == 1) {
                        entry.value[0].also {
                            if (it.value is Map<*, *> || it.value is List<*>)
                                currentNode.set<JsonNode>(entry.key, objectMapper.convertValue(it.value, JsonNode::class.java))
                            else
                                put(entry.key, it.value, currentNode)
                        }
                    } else {
                        currentNode.putArray(entry.key).apply {
                            entry.value.asSequence().sortedBy { it.order }.forEach { add(it.value, this) }
                        }
                    }
                }
                else -> {
                    currentNode.putArray(entry.key).apply {
                        if (entry.key.contains("participation_identifiers")) { //Only array of array case in FLAT format
                            entry.value.groupBy { it.order }.asSequence().sortedBy { it.key }.forEach {
                                this.addArray().apply {
                                    if (it.value.size == 1 && it.value[0].value is String) {
                                        this.add(TextNode.valueOf(it.value[0].value as String))
                                    } else {
                                        handleParticipationIdentifiersObjects(it.value.mapNotNull { value -> value.child }, this)
                                    }
                                }
                            }
                        } else {
                            entry.value.groupBy { it.order }.asSequence().sortedBy { it.key }.forEach {
                                convertCtx(it.value.mapNotNull { value -> value.child }, this)
                            }
                        }
                    }

                    (currentNode.get(entry.key) as ArrayNode).also {
                        if (it.size() == 1) {
                            val firstNode = it.get(0)
                            currentNode.replace(entry.key, firstNode)
                        }
                    }
                }
            }
        }
    }

    private fun handleParticipationIdentifiersObjects(entries: List<EntryDto>, arrayNode: ArrayNode) {
            arrayNode.apply {
                val groups = entries.groupBy { it.order }
                groups.asSequence().sortedBy { it.key }.forEach { entry ->
                    arrayNode.addObject().apply {
                        entry.value.groupBy { it.name }.forEach {
                            put(it.key, it.value.iterator().next().value, this)
                        }
                    }
                }
            }
    }

    private fun get(value: Any?): JsonNode =
        when (value) {
            null -> ConversionObjectMapper.nullNode()
            is Int -> IntNode.valueOf(value)
            is Long -> LongNode.valueOf(value)
            is Float -> FloatNode.valueOf(value)
            is Short -> ShortNode.valueOf(value)
            is Double -> DoubleNode.valueOf(value)
            is String -> TextNode.valueOf(value)
            is BigInteger -> BigIntegerNode.valueOf(value)
            is BigDecimal -> DecimalNode.valueOf(value)
            is Boolean -> BooleanNode.valueOf(value)
            is ByteArray -> BinaryNode.valueOf(value)
            is DateTime -> TextNode.valueOf(convertDateTime(value))
            is OffsetDateTime -> TextNode.valueOf(convertOffsetDateTime(value))
            is ZonedDateTime -> TextNode.valueOf(convertZonedDateTime(value))
            is Year -> TextNode.valueOf(convertYear(value))
            is YearMonth -> TextNode.valueOf(convertYearMonth(value))
            is LocalDateTime -> TextNode.valueOf(convertLocalDateTime(value))
            is LocalDate -> TextNode.valueOf(convertLocalDate(value))
            is org.joda.time.LocalDate -> TextNode.valueOf(convertLocalDate(value))
            is LocalTime -> TextNode.valueOf(convertLocalTime(value))
            is org.joda.time.LocalTime -> TextNode.valueOf(convertLocalTime(value))
            is OffsetTime -> TextNode.valueOf(convertOffsetTime(value))
            is OpenEhrOffsetDateTime -> TextNode.valueOf(convertOpenEhrOffsetDateTime(value))
            is OpenEhrLocalDateTime -> TextNode.valueOf(convertOpenEhrLocalDateTime(value))
            is OpenEhrLocalDate -> TextNode.valueOf(convertOpenEhrLocalDate(value))
            is OpenEhrOffsetTime -> TextNode.valueOf(convertOpenEhrOffsetTime(value))
            is OpenEhrLocalTime -> TextNode.valueOf(convertOpenEhrLocalTime(value))
            is Period -> TextNode.valueOf(ISOPeriodFormat.standard().print(value))
            is RmObject -> ConversionObjectMapper.createObjectNode().apply { this.replace("|raw", ConversionObjectMapper.valueToTree(value)) }
            else -> throw ConversionException("${value::class.java.name} is not supported!")
        }


    /**
     * Sets the value of a [ObjectNode] field.
     *
     * @param key JSON attribute key
     * @param value JSON value
     * @param objectNode [ObjectNode]
     */
    private fun put(key: String, value: Any?, objectNode: ObjectNode) {
        objectNode.replace(key, get(value))
    }

    /**
     * Adds the value at the end of the [ArrayNode]
     *
     * @param value JSON value
     * @param arrayNode [ArrayNode]
     */
    private fun add(value: Any?, arrayNode: ArrayNode) {
        arrayNode.add(get(value))
    }

    private fun convertDateTime(dateTime: DateTime): String =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.ofInstant(dateTime.toDate().toInstant(), ZoneId.of(dateTime.zone.id)))

    private fun convertOffsetDateTime(offsetDateTime: OffsetDateTime): String =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime)

    private fun convertZonedDateTime(zonedDateTime: ZonedDateTime): String =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(zonedDateTime.toOffsetDateTime())

    private fun convertLocalDate(localDate: LocalDate): String =
            DateTimeFormatter.ISO_LOCAL_DATE.format(localDate)

    private fun convertYear(year: Year): String = year.value.toString()

    private fun convertYearMonth(yearMonth: YearMonth): String = OpenEhrDateTimeFormatter.ofPattern("yyyy-MM").format(yearMonth)

    private fun convertLocalDateTime(localDateTime: LocalDateTime): String =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime)

    private fun convertLocalDate(localDate: org.joda.time.LocalDate): String =
            DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.of(localDate.year, localDate.monthOfYear, localDate.dayOfMonth))

    private fun convertLocalTime(localTime: LocalTime): String =
            DateTimeFormatter.ISO_LOCAL_TIME.format(localTime)

    private fun convertLocalTime(localTime: org.joda.time.LocalTime): String =
            DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.of(
                    localTime.hourOfDay,
                    localTime.minuteOfHour,
                    localTime.secondOfMinute,
                    localTime.millisOfSecond * 1000000))

    private fun convertOffsetTime(offsetTime: OffsetTime): String = DateTimeFormatter.ISO_OFFSET_TIME.format(offsetTime)

    private fun convertOpenEhrOffsetDateTime(offsetDateTime: OpenEhrOffsetDateTime): String =
        SimpleValueConverter.formatOpenEhrTemporal(offsetDateTime, "YYYY-??-??T??:??:??.???Z", true)

    private fun convertOpenEhrLocalDateTime(localDateTime: OpenEhrLocalDateTime): String =
        SimpleValueConverter.formatOpenEhrTemporal(localDateTime, "YYYY-??-??T??:??:??.???", true)

    private fun convertOpenEhrLocalDate(localDate: OpenEhrLocalDate): String =
        SimpleValueConverter.formatOpenEhrTemporal(localDate, "YYYY-??-??", true)

    private fun convertOpenEhrOffsetTime(offsetTime: OpenEhrOffsetTime): String =
        SimpleValueConverter.formatOpenEhrTemporal(offsetTime, "HH:??:??.???Z", true)

    private fun convertOpenEhrLocalTime(localTime: OpenEhrLocalTime): String =
        SimpleValueConverter.formatOpenEhrTemporal(localTime, "HH:??:??.???", true)

    /**
     * Converts the single FLAT format entry to the [EntryDto].
     *
     * @param key FLAT format entry key
     * @param value FLAT format entry value
     * @return [EntryDto]
     */
    private fun convertEntry(key: String, value: Any?): EntryDto = convertEntryRecursive(key.split(segmentSeparatorPattern).toList(), 0, value)!!

    /**
     * Converts the single FLAT format entry to the [EntryDto] recursively.
     * Note that the FLAT format entry key is already split to the key segments.
     *
     * @param segments FLAT format entry key segments
     * @param index Current index of the element in the segments [List]
     * @param value FLAT format entry value
     * @return [EntryDto]
     */
    private fun convertEntryRecursive(segments: List<String>, index: Int, value: Any?): EntryDto? =
            if (index >= segments.size) {
                null
            } else {
                val segment = convertEntryRecursive(segments, index + 1, value)
                convertKeySegment(segments[index], segment, if (segment == null) value else null)
            }

    /**
     * Converts the single FLAT format entry key segment.
     *
     * @param segment FLAT format entry key segment
     * @param child Child of the current element
     * @param value FLAT format entry value
     * @return [EntryDto]
     */
    private fun convertKeySegment(segment: String, child: EntryDto?, value: Any?): EntryDto {
        val (key, index, attribute, attributeIndex) = WebTemplatePathSegment.fromString(segment)
        return EntryDto(key, index ?: 0, if (attribute == null) child else EntryDto(attribute, attributeIndex ?: 0, child, value), value)
    }

    /**
     * Class representing the single FLAT format entry.
     *
     * @constructor Creates a new instance of [EntryDto]
     * @param name FLAT format entry name
     * @param order FLAT format entry order
     * @param child FLAT format entry child
     * @param value FLAT format entry value
     */
    private data class EntryDto(val name: String, val order: Int, val child: EntryDto?, val value: Any?)
}
