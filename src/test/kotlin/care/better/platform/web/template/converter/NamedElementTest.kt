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
import com.fasterxml.jackson.core.type.TypeReference
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
class NamedElementTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCustomNames() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/IDCR_-_Laboratory_Test_Report(NamedElement-1).json"), object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_name|value", "Urea"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_name|code", "365755003"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_name|terminology", "SNOMED-CT"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_name|value", "Creatinine"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_name|code", "70901006"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_name|terminology", "SNOMED-CT"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:2/result_value/_name|value", "Sodium"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:2/result_value/_name|code", "365761000"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:2/result_value/_name|terminology", "SNOMED-CT"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:3/result_value/_name|value", "Potassium"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:3/result_value/_name|code", "365760004"))
        assertThat(flatMap).contains(entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:3/result_value/_name|terminology", "SNOMED-CT"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCustomNamesAsDvText() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/IDCR_-_Laboratory_Test_Report(NamedElement-2).json"), object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_name", "Urea"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_name", "New name"))

        assertThat(flatMap.keys).doesNotContain(
                "laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_name|code",
                "laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_name|terminology",
                "laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_name|code",
                "laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_name|terminology")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testRegularNames() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/IDCR_-_Laboratory_Test_Report(NamedElement-3).json"), object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_name|value")).isFalse
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value|magnitude")).isTrue
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_name|value")).isFalse
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value|magnitude")).isTrue
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:1/laboratory_test_panel/laboratory_result:0/result_value/_name|value")).isFalse
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:1/laboratory_test_panel/laboratory_result:0/result_value|magnitude")).isTrue
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:1/laboratory_test_panel/laboratory_result:1/result_value/_name|value")).isFalse
        assertThat(flatMap.containsKey("laboratory_test_report/laboratory_test:1/laboratory_test_panel/laboratory_result:1/result_value|magnitude")).isTrue

        assertThat(flatMap).doesNotContain(
                entry("laboratory_test_report/laboratory_test:1/laboratory_test_panel/laboratory_result:1/result_value/_name|value", "Potassium"),
                entry("laboratory_test_report/laboratory_test:1/laboratory_test_panel/laboratory_result:1/result_value/_name|code", "365760004"),
                entry("laboratory_test_report/laboratory_test:1/laboratory_test_panel/laboratory_result:1/result_value/_name|terminology", "SNOMED-CT"))

        assertThat(flatMap.keys.any { it.contains("/_name") }).isFalse
    }
}
