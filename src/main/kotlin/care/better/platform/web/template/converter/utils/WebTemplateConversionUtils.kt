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

package care.better.platform.web.template.converter.utils

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.web.template.builder.model.input.range.WebTemplateDecimalRange
import care.better.platform.web.template.builder.utils.CodePhraseUtils
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.openehr.am.aom.ArchetypeTerm
import org.openehr.base.foundationtypes.IntervalOfReal
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Set of utility functions used during the RM object conversion.
 */
object WebTemplateConversionUtils {

    private val ID_INVALID_CHARACTERS = Pattern.compile("[^\\p{IsAlphabetic}0-9_.-]")
    private val MULTIPLE_UNDERSCORE = Pattern.compile("_{2,}")

    /**
     * Creates the web template path segment (without index and attribute) from from name.
     *
     * @param name Name
     * @return Web template path segment key (without index and attribute)
     */
    @JvmStatic
    internal fun getWebTemplatePathSegmentForName(name: String): String =
        ID_INVALID_CHARACTERS.matcher(name).replaceAll("_").lowercase()
            .let { MULTIPLE_UNDERSCORE.matcher(it).replaceAll("_") }
            .removePrefix("_").removeSuffix("_")

    /**
     * Converts [LocalTime] to [java.time.LocalTime].
     *
     * @param localTime [LocalTime]
     * @return [java.time.LocalTime]
     */
    @JvmStatic
    internal fun convert(localTime: LocalTime): java.time.LocalTime =
        java.time.LocalTime.of(localTime.hourOfDay, localTime.minuteOfHour, localTime.secondOfMinute, localTime.millisOfSecond * 1000000)


    /**
     * Converts [DateTime] to [java.time.LocalTime].
     *
     * @param dateTime [DateTime]
     * @return [java.time.LocalTime]
     */
    @JvmStatic
    internal fun convertTime(dateTime: DateTime): java.time.LocalTime =
        java.time.LocalTime.of(dateTime.hourOfDay, dateTime.minuteOfHour, dateTime.secondOfMinute, dateTime.millisOfSecond * 1000000)

    /**
     * Converts [DateTime] to [java.time.OffsetTime].
     *
     * @param dateTime [DateTime]
     * @return [java.time.OffsetTime]
     */
    @JvmStatic
    internal fun convertOffsetTime(dateTime: DateTime): OffsetTime =
        OffsetTime.of(
                dateTime.hourOfDay,
                dateTime.minuteOfHour,
                dateTime.secondOfMinute,
                dateTime.millisOfSecond * 1000000,
                ZoneOffset.ofTotalSeconds(dateTime.zone.toTimeZone().rawOffset / 1000))

    /**
     * Converts [LocalDate] to [java.time.LocalDate].
     *
     * @param localDate [LocalDate]
     * @return [java.time.LocalDate]
     */
    @JvmStatic
    internal fun convert(localDate: LocalDate): java.time.LocalDate =
        java.time.LocalDate.of(localDate.year, localDate.monthOfYear, localDate.dayOfMonth)

    /**
     * Converts [DateTime] to [java.time.OffsetDateTime].
     *
     * @param dateTime [DateTime]
     * @return [java.time.OffsetDateTime]
     */
    @JvmStatic
    internal fun convert(dateTime: DateTime): OffsetDateTime = OffsetDateTime.ofInstant(dateTime.toDate().toInstant(), ZoneId.of(dateTime.zone.id))

    @JvmStatic
    fun getTermText(amNode: AmNode, terminologyId: String?, codeString: String?, language: String?): String? =
        if ("openehr" == terminologyId) {
            CodePhraseUtils.getOpenEhrTerminologyText(codeString!!, language)
        } else {
            val term = if (amNode.termDefinitions.containsKey(language))
                AmUtils.findTerm(amNode.termDefinitions[language] ?: emptyList(), codeString, "text")
            else
                null

            term ?: AmUtils.findTerm(amNode.terms, codeString, "text") ?: findTemplateTerminologyTerm(amNode.terms, terminologyId, codeString)
        }

    private fun findTemplateTerminologyTerm(terms: List<ArchetypeTerm>, terminologyId: String?, codeString: String?): String? =
        if (terminologyId != null && codeString != null)
            AmUtils.findTerm(terms, "$terminologyId::$codeString", "text")
        else
            null

    @JvmStatic
    internal fun getFixedValue(interval: IntervalOfReal?): Float? =
        interval?.let {
            val range = WebTemplateDecimalRange(interval)
            if (range.isFixed())
                range.min
            else
                null
        }

    private val encodedTerminologyRegex = Regex("\\[.+?::([^]]+)]")

    @JvmStatic
    internal fun extractTerminologyCode(encodedTerminologyString: String): String =
        encodedTerminologyRegex.matchEntire(encodedTerminologyString)?.let { it.groupValues[1] } ?: encodedTerminologyString
}
