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

package care.better.platform.web.template.builder.utils

import care.better.platform.utils.JodaConversionUtils
import care.better.platform.web.template.builder.model.input.WebTemplateDurationField
import com.google.common.base.Splitter
import org.joda.time.Period
import org.openehr.am.aom.CDuration
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
object DurationUtils {

    @JvmStatic
    fun getAllowedFields(pattern: String): Set<WebTemplateDurationField> {
        val iterator: Iterator<String> = Splitter.on("T").split(pattern).iterator()
        val allowedFields: MutableSet<WebTemplateDurationField> = EnumSet.noneOf(WebTemplateDurationField::class.java)
        if (iterator.hasNext()) {
            val ymdw = iterator.next()
            if (ymdw.contains("Y")) {
                allowedFields.add(WebTemplateDurationField.YEAR)
            }
            if (ymdw.contains("M")) {
                allowedFields.add(WebTemplateDurationField.MONTH)
            }
            if (ymdw.contains("D")) {
                allowedFields.add(WebTemplateDurationField.DAY)
            }
            if (ymdw.contains("W")) {
                allowedFields.add(WebTemplateDurationField.WEEK)
            }
        }
        if (iterator.hasNext()) {
            val hms = iterator.next()
            if (hms.contains("H")) {
                allowedFields.add(WebTemplateDurationField.HOUR)
            }
            if (hms.contains("M")) {
                allowedFields.add(WebTemplateDurationField.MINUTE)
            }
            if (hms.contains("S")) {
                allowedFields.add(WebTemplateDurationField.SECOND)
            }
        }
        return allowedFields
    }

    @JvmStatic
    fun getMin(item: CDuration): Period = if (item.range?.lower != null) JodaConversionUtils.toPeriod(item.range?.lower!!) else Period.ZERO

    @JvmStatic
    fun getMax(item: CDuration): Period = if (item.range?.upper != null) JodaConversionUtils.toPeriod(item.range?.upper!!) else Period.ZERO

    @JvmStatic
    fun getAssumedValue(item: CDuration): Period = if (item.assumedValue != null) JodaConversionUtils.toPeriod(item.assumedValue!!) else Period.ZERO

}
