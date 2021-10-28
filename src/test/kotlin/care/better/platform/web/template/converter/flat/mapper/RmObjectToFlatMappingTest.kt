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

package care.better.platform.web.template.converter.flat.mapper

import care.better.platform.template.AmNode
import care.better.platform.time.temporal.OpenEhrLocalTime
import care.better.platform.time.temporal.OpenEhrOffsetDateTime
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.datatypes.DvDate
import org.openehr.rm.datatypes.DvDateTime
import org.openehr.rm.datatypes.DvTime
import java.time.*
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class RmObjectToFlatMappingTest {
    private val valueConverter: ValueConverter = LocaleBasedValueConverter(Locale("sl_SI"))

    @Test
    fun testDateTime() {
        val flatConversionContext = FlatMappingContext()
        val dateTime = ZonedDateTime.of(2012, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toOffsetDateTime()
        val actualDateTime = OpenEhrOffsetDateTime.of(2012, 1, 1, 0, 0, 0, null, ZoneId.systemDefault().rules.getOffset(dateTime.toInstant()))

        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_DATE_TIME"), "DV_DATE_TIME", "/"), valueConverter, DvDateTime.create(dateTime), "id", flatConversionContext)
        assertThat<Any>(flatConversionContext.get()["id"]).isEqualTo(actualDateTime)
    }

    @Test
    fun testDate() {
        val flatConversionContext = FlatMappingContext()
        val localDate = LocalDate.of(2012, 1, 1)
        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_DATE"), "DV_DATE", "/"), valueConverter, DvDate.create(localDate), "id", flatConversionContext)
        assertThat(flatConversionContext.get()).contains(entry("id", localDate))
    }

    @Test
    fun testDateAsDateTime() {
        val dvDate = DvDate()
        dvDate.value = "2013-01-01T00:00:00.000+01:00"
        val flatConversionContext = FlatMappingContext()
        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_DATE"), "DV_DATE", "/"), valueConverter, dvDate, "id", flatConversionContext)
        assertThat(flatConversionContext.get()).contains(entry("id", LocalDate.of(2013, 1, 1)))
    }

    @Test
    fun testSecondDateAsDateTime() {
        val dvDate = DvDate()
        dvDate.value = "2013-01-01T00:00:00.000+01:00"
        val flatConversionContext = FlatMappingContext()
        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_DATE"), "DV_DATE", "/"), valueConverter, dvDate, "id", flatConversionContext)
        assertThat(flatConversionContext.get()).contains(entry("id", LocalDate.of(2013, 1, 1)))
    }

    @Test
    fun testTime() {
        val flatConversionContext = FlatMappingContext()
        val localTime = LocalTime.of(13, 37)
        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_TIME"), "DV_TIME", "/"), valueConverter, DvTime.create(localTime), "id", flatConversionContext)
        assertThat(flatConversionContext.get()).contains(entry("id", OpenEhrLocalTime.of(13, 37, 0)))
    }

    @Test
    fun testTimeAsDateTime() {
        val time = DvTime()
        time.value = "2013-1-1T13:37:00.000"
        val flatConversionContext = FlatMappingContext()
        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_TIME"), "DV_TIME", "/"), valueConverter, time, "id", flatConversionContext)
        assertThat(flatConversionContext.get()).contains(entry("id", LocalTime.of(13, 37, 0)))
    }

    @Test
    fun testSecondTimeAsDateTime() {
        val time = DvTime().apply { this.value = "2013-1-1T13:37:00.000+01:00" }
        val flatConversionContext = FlatMappingContext()
        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_TIME"), "DV_TIME", "/"), valueConverter, time, "id", flatConversionContext)
        assertThat(flatConversionContext.get()).contains(entry("id", OffsetTime.of(13, 37, 0, 0, ZoneOffset.ofHours(1))))
    }

    @Test
    fun testThirdTimeAsDateTime() {
        val time = DvTime().apply { this.value = "13:37:00.000+01:00" }
        val flatConversionContext = FlatMappingContext()
        RmObjectToFlatMapperDelegator.delegate(WebTemplateNode(AmNode(null, "DV_TIME"), "DV_TIME", "/"), valueConverter, time, "id", flatConversionContext)
        assertThat(flatConversionContext.get()).contains(entry("id", OffsetTime.of(13, 37, 0, 0, ZoneOffset.ofHours(1))))
    }
}
