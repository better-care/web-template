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

import care.better.platform.path.NameAndNodeMatchingPathValueExtractor
import care.better.platform.path.PathValueExtractor
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DatePatternTest : AbstractWebTemplateTest() {

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPatterns() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/zzz-dates.xml"), builderContext)

        assertThat(webTemplate.findWebTemplateNode("encounter/date_possibilities/any_event/date-any").getInput()!!.validation).isNull()
        assertThat(webTemplate.findWebTemplateNode("encounter/date_possibilities/any_event/date-full").getInput()!!.validation?.pattern).isEqualTo("yyyy-mm-dd")
        assertThat(webTemplate.findWebTemplateNode("encounter/date_possibilities/any_event/date-partial").getInput()!!.validation?.pattern).isEqualTo("yyyy-??-XX")
        assertThat(webTemplate.findWebTemplateNode("encounter/date_possibilities/any_event/date-partialwithmonth").getInput()!!.validation?.pattern).isEqualTo("yyyy-mm-??")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPatternsContentAny() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/zzz-dates.xml"), builderContext)
        val extractorAny: PathValueExtractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-OBSERVATION.date_possibilities.v0]/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/value")
        val attributeName = "date-any"

        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02-01", "2019-02-01")
        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02", "2019-02")
        assertValueMatches(webTemplate, extractorAny, attributeName, "2019", "2019")

        assertThatThrownBy { buildComposition(webTemplate, getValues(attributeName, "2019-17")) }.isInstanceOf(ConversionException::class.java)
        assertThatThrownBy { buildComposition(webTemplate, getValues(attributeName, "2019-12-33")) }.isInstanceOf(ConversionException::class.java)
        assertThatThrownBy { buildComposition(webTemplate, getValues(attributeName, "2019-12-")) }.isInstanceOf(ConversionException::class.java)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPatternsContentFull() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/zzz-dates.xml"), builderContext)
        val extractorAny: PathValueExtractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-OBSERVATION.date_possibilities.v0]/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/value")
        val attributeName = "date-full"

        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02-01", "2019-02-01")
        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02", "2019-02-01")
        assertThatThrownBy { buildComposition(webTemplate, getValues(attributeName, "2019-02-33")) }.isInstanceOf(ConversionException::class.java)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPatternsContentPartial() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/zzz-dates.xml"), builderContext)
        val extractorAny: PathValueExtractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-OBSERVATION.date_possibilities.v0]/data[at0001]/events[at0002]/data[at0003]/items[at0006]/value/value")
        val attributeName = "date-partial"

        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02-01", "2019-02")
        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02", "2019-02")
        assertValueMatches(webTemplate, extractorAny, attributeName, "2019", "2019")
        assertThatThrownBy { buildComposition(webTemplate, getValues(attributeName, "2019-17")) }.isInstanceOf(ConversionException::class.java)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPatternsContentPartialWithMonth() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/zzz-dates.xml"), builderContext)
        val extractorAny: PathValueExtractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-OBSERVATION.date_possibilities.v0]/data[at0001]/events[at0002]/data[at0003]/items[at0007]/value/value")
        val attributeName = "date-partialwithmonth"

        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02-01", "2019-02-01")
        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-02", "2019-02")
        assertValueMatches(webTemplate, extractorAny, attributeName, "2019-01", "2019-01")
        assertThatThrownBy { buildComposition(webTemplate, getValues(attributeName, "2019-17")) }.isInstanceOf(ConversionException::class.java)
    }

    private fun assertValueMatches(
            webTemplate: WebTemplate,
            extractorAny: PathValueExtractor,
            attributeName: String,
            incomingValue: String,
            compositionValue: String) {
        val composition = buildComposition(webTemplate, getValues(attributeName, incomingValue))
        assertThat(extractorAny.getValue(composition)).hasSize(1).containsExactly(compositionValue)
        val retrieve: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(retrieve["encounter/date_possibilities:0/any_event:0/$attributeName"]).isEqualTo(compositionValue)
    }

    private fun getValues(attributeName: String, value: String): Map<String, Any> {
        val values: MutableMap<String, Any> = HashMap()
        values["ctx/language"] = "en"
        values["ctx/territory"] = "SI"
        values["encounter/date_possibilities:0/any_event:0/$attributeName"] = value
        return values
    }

    private fun buildComposition(webTemplate: WebTemplate, values: Map<String, Any>): Composition {
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())
        assertThat(composition).isNotNull
        return composition!!
    }
}
