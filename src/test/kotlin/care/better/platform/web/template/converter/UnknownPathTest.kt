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

import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 */
class UnknownPathTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOnRoot() {
        val template = getTemplate("/convert/templates/OTE Demo.xml")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))

        val values: Map<String, Any> = mapOf(
                "ehrId" to "d41bd303-3e97-45ad-82d2-e64ce7390f59",
                "subjectId" to "42",
                "ctx/language" to "en",
                "ctx/territory" to "SI",
                "ote_demo/blood_pressure/any_event:0/systolic|magnitude" to 99.0,
                "ote_demo/blood_pressure/any_event:0/systolic|unit" to "mm[Hg]",
                "ote_demo/blood_pressure/any_event:0/diastolic|magnitude" to 32.0,
                "ote_demo/blood_pressure/any_event:0/diastolic|unit" to "mm[Hg]")

        val context = ConversionContext.create()
            .withLanguage("en")
            .withTerritory("GB")
            .withComposerName("composer")
            .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(values, context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageStartingWith("COMPOSITION has no attribute ehrId.")

    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOnChild() {
        val template = getTemplate("/convert/templates/OTE Demo.xml")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))

        val values: Map<String, Any> = mapOf(
                "ctx/language" to "en",
                "ctx/territory" to "SI",
                "ote_demo/blood_pressure/any_event:0/xyz|magnitude" to 99.0,
                "ote_demo/blood_pressure/any_event:0/xyz|unit" to "mm[Hg]",
                "ote_demo/blood_pressure/any_event:0/diastolic|magnitude" to 32.0,
                "ote_demo/blood_pressure/any_event:0/diastolic|unit" to "mm[Hg]")

        val context = ConversionContext.create()
            .withLanguage("en")
            .withTerritory("GB")
            .withComposerName("composer")
            .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(values, context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageStartingWith("EVENT has no attribute xyz (path: ote_demo/blood_pressure/any_event:0/xyz).")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOnLeaf() {
        val template = getTemplate("/convert/templates/OTE Demo.xml")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))

        val values: Map<String, Any> = mapOf(
                "ctx/language" to "en",
                "ctx/territory" to "SI",
                "ote_demo/blood_pressure/any_event:0/systolic|xyz" to 99.0,
                "ote_demo/blood_pressure/any_event:0/systolic|unit" to "mm[Hg]",
                "ote_demo/blood_pressure/any_event:0/diastolic|magnitude" to 32.0,
                "ote_demo/blood_pressure/any_event:0/diastolic|unit" to "mm[Hg]")

        val context = ConversionContext.create()
            .withLanguage("en")
            .withTerritory("GB")
            .withComposerName("composer")
            .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(values, context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageStartingWith("DV_QUANTITY has no attribute |xyz (path: ote_demo/blood_pressure/any_event:0/systolic|xyz).")
    }
}