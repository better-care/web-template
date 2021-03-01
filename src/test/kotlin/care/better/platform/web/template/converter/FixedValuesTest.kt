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
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class FixedValuesTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFixed() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ZN - Assessment Scales Encounter2.opt"), builderContext)

        val values: Map<String, String> = mapOf(Pair("assessment_scales/pain_assessment/story/pain/exascerbating_factor/factor", "test"))

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("assessment_scales/pain_assessment/story/pain:0/exascerbating_factor/factor", "test"),
            entry("assessment_scales/pain_assessment/story/pain:0/exascerbating_factor/change|ordinal", 2))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFixedCodedInDvText() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template N1.opt"), builderContext)
        val values: Map<String, String> = mapOf(Pair("test_encounter/testing/testing/name_1", "hello world!"))

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("test_encounter/testing:0/testing:0/testing_dv_text|value", "Hello world"),
            entry("test_encounter/testing:0/testing:0/testing_dv_text|terminology", "LOINC"),
            entry("test_encounter/testing:0/testing:0/testing_dv_text|code", "1234"))
    }
}
