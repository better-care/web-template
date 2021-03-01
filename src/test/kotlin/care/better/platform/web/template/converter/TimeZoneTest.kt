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

package care.better.platform.web.template.converter

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.SimpleValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class TimeZoneTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTimeZone() {
        val templateName = "/convert/templates/Demo Vitals.opt"
        val template = getTemplate(templateName)
        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("composer")
            .withValueConvert(TimeZoneTestValueConverter)
            .build()

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("ctx/time", "2015-01-01T10:00:00.000+05:00")
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", "37.7")
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition?.context?.startTime?.value).isEqualTo("2015-01-01T10:00:00+05:00")
    }

    private object TimeZoneTestValueConverter : ValueConverter by SimpleValueConverter {
        override fun parseDateTime(value: String, strict: Boolean): OffsetDateTime {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        }
    }
}
