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

package care.better.platform.web.template.build.input

import care.better.platform.template.AmNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.input.*
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.junit.jupiter.api.Test
import org.openehr.am.aom.*
import org.openehr.base.foundationtypes.*
import java.io.File
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class InputTypeSerializationTestTest {

    private val objectWriter: ObjectWriter = with(ObjectMapper()) {
        this.configure(SerializationFeature.INDENT_OUTPUT, true)
        this.writerWithDefaultPrettyPrinter()
    }

    private val webTemplateBuilderContext: WebTemplateBuilderContext = WebTemplateBuilderContext("en")

    @Throws(IOException::class)
    private fun write(input: WebTemplateInput, extra: Int) {
        objectWriter.writeValue(File.createTempFile("${input.type}${extra}", null), input)
    }

    @Test
    @Throws(IOException::class)
    fun testBoolean() {
        val inputBuilder = BooleanWebTemplateInputBuilder
        val node = AmNode(null, "DV_BOOLEAN")
        write(inputBuilder.build(node, CBoolean(), webTemplateBuilderContext), 0)
    }

    @Test
    @Throws(IOException::class)
    fun testDate() {
        val inputBuilder = DateWebTemplateInputBuilder
        val node = AmNode(null, "DV_DATE")
        val range = IntervalOfDate().apply {
            this.lower = ISODateTimeFormat.date().print(DateTime.now().minusYears(30))
            this.upper = ISODateTimeFormat.date().print(DateTime.now().minusYears(20))
        }
        val cDate = CDate().apply {
            this.range = range
            this.pattern = "yyyy-??-??"
        }
        write(inputBuilder.build(node, cDate, webTemplateBuilderContext), 0)
    }

    @Test
    @Throws(IOException::class)
    fun testDateTime() {
        val inputBuilder = DateTimeWebTemplateInputBuilder
        val node = AmNode(null, "DV_DATE_TIME")

        val range = IntervalOfDateTime().apply {
            this.lower = ISODateTimeFormat.dateTime().print(DateTime.now().minusYears(30))
            this.upper = ISODateTimeFormat.dateTime().print(DateTime.now().minusYears(20))
        }
        val cDateTime = CDateTime().apply {
            this.range = range
        }
        write(inputBuilder.build(node, cDateTime, webTemplateBuilderContext), 0)
    }

    @Test
    @Throws(IOException::class)
    fun testDecimal() {
        val inputBuilder = DecimalWebTemplateInputBuilder
        val node = AmNode(null as AmNode?, "REAL")
        val range = IntervalOfReal().apply {
            this.lower = -1.1f
            this.lowerIncluded = true
            this.upper = 2.7f
            this.upperIncluded = false
        }
        val cReal = CReal().apply {
            this.range = range
        }
        write(inputBuilder.build(node, cReal, webTemplateBuilderContext), 0)
    }

    @Test
    @Throws(IOException::class)
    fun testDuration() {
        val inputBuilder = DurationWebTemplateInputBuilder
        val node = AmNode(null as AmNode?, "DURATION")
        val intervalOfDuration = IntervalOfDuration().apply {
            this.lower = "P0Y"
            this.upper = "P100Y"
        }
        val cDuration = CDuration().apply {
            this.pattern = "PYM"
            this.range = intervalOfDuration
        }
        write(inputBuilder.build(node, cDuration, webTemplateBuilderContext), 0)
    }

    @Test
    @Throws(IOException::class)
    fun testInteger() {
        val inputBuilder = DurationWebTemplateInputBuilder
        val node = AmNode(null as AmNode?, "INTEGER")
        val intervalOfDuration = IntervalOfDuration().apply {
            this.lower = "P0Y"
            this.upper = "P100Y"
        }
        val cDuration = CDuration().apply {
            this.pattern = "PYM"
            this.range = intervalOfDuration
        }
        write(inputBuilder.build(node, cDuration, webTemplateBuilderContext), 0)
    }

    @Test
    @Throws(IOException::class)
    fun testQuantity() {
        val inputBuilder = QuantityWebTemplateInputBuilder
        val node = AmNode(null as AmNode?, "RANGE")
        val firstRange = IntervalOfReal().apply {
            this.lower = -3.0f
            this.lowerIncluded = true
            this.upper = 5.0f
            this.upperIncluded = false
        }

        val firstIntervalOfInteger = IntervalOfInteger().apply {
            this.lower = 1
            this.upper = 2
        }
        val firstItem = CQuantityItem().apply {
            this.magnitude = firstRange
            this.precision = firstIntervalOfInteger
            this.units = "cm"

        }
        val secondRange = IntervalOfReal().apply {
            this.lower = -1.5f
            this.lowerIncluded = true
            this.upper = 2.0f
            this.upperIncluded = false
        }
        val secondIntervalOfInteger = IntervalOfInteger().apply {
            this.lower = 1
            this.upper = 2
        }
        val secondItem = CQuantityItem().apply {
            this.magnitude = secondRange
            this.precision = secondIntervalOfInteger
            this.units = "in"
        }
        val quantity = CDvQuantity().apply {
            this.list.add(firstItem)
            this.list.add(secondItem)
        }
        write(inputBuilder.build(node, quantity, webTemplateBuilderContext), 0)
    }
}
