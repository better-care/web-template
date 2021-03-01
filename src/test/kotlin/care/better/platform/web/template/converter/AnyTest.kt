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
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.DataValue
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvQuantity
import org.openehr.rm.datatypes.DvText
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class AnyTest : AbstractWebTemplateTest() {

    private val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/any_element.opt"), WebTemplateBuilderContext("en"))
    private val context: ConversionContext = ConversionContext.create().build()

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAnyElementQuantity() {
        val values = getInitMap()
        values["encounter/test_any/any_element/quantity_value|magnitude"] = 300
        values["encounter/test_any/any_element/quantity_value|unit"] = "mm"

        val composition = getComposition(values)
        val quantity = validateAndGetElementValue(composition, DvQuantity::class.java)!!
        assertThat(quantity.magnitude).isEqualTo(300.0)
        assertThat(quantity.units).isEqualTo("mm")

        val formatted: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(formatted).contains(Assertions.entry("encounter/test_any:0/any_element/quantity_value|magnitude", "300.0"))
        assertThat(formatted).contains(Assertions.entry("encounter/test_any:0/any_element/quantity_value|unit", "mm"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAnyElementText() {
        val values = getInitMap()
        values["encounter/test_any/any_element/text_value"] = "Hello world!"

        val composition = getComposition(values)
        val text = validateAndGetElementValue(composition, DvText::class.java)!!
        assertThat(text.value).isEqualTo("Hello world!")

        val formatted: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(formatted).contains(Assertions.entry("encounter/test_any:0/any_element/text_value", "Hello world!"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAnyElementCodedText() {
        val values = getInitMap()
        values["encounter/test_any/any_element/coded_text_value|value"] = "Hello world!"
        values["encounter/test_any/any_element/coded_text_value|code"] = "HW"
        values["encounter/test_any/any_element/coded_text_value|terminology"] = "mine"

        val composition = getComposition(values)
        val text = validateAndGetElementValue(composition, DvCodedText::class.java)!!
        assertThat(text.value).isEqualTo("Hello world!")
        assertThat(text.definingCode!!.codeString).isEqualTo("HW")
        assertThat(text.definingCode!!.terminologyId!!.value).isEqualTo("mine")

        val formatted: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(formatted).contains(Assertions.entry("encounter/test_any:0/any_element/coded_text_value|value", "Hello world!"))
        assertThat(formatted).contains(Assertions.entry("encounter/test_any:0/any_element/coded_text_value|code", "HW"))
        assertThat(formatted).contains(Assertions.entry("encounter/test_any:0/any_element/coded_text_value|terminology", "mine"))
    }

    private fun getInitMap(): MutableMap<String, Any> {
        val values: MutableMap<String, Any> = mutableMapOf()
        values["ctx/language"] = "en"
        values["ctx/territory"] = "IE"
        values["ctx/composer_name"] = "John"
        return values
    }

    private fun getComposition(values: Map<String, Any>): Composition = webTemplate.convertFromFlatToRaw(values, context)!!

    @Suppress("UNCHECKED_CAST")
    private fun <T : DataValue?> validateAndGetElementValue(composition: Composition, clazz: Class<T>): T? {
        val observation = composition.content[0] as Observation
        val itemTree = observation.data!!.events[0].data as ItemTree?
        val element = itemTree!!.items[0] as Element
        val value = element.value
        assertThat(value).isInstanceOf(clazz)
        return value as T?
    }
}
