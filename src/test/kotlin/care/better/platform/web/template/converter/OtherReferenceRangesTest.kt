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
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class OtherReferenceRangesTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOtherReferenceRangesSingle() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/IDCR_-_Laboratory_Test_Report(1).json"), object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/lower|magnitude", 2.5),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/lower|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/upper|magnitude", 6.6),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/upper|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/meaning", "too high"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/lower|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/lower|magnitude", 6.6),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/upper|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/upper|magnitude", 15.1))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOtherReferenceRangesInvalidAttribute() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/IDCR_-_Laboratory_Test_Report(2).json"), object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(compositionFlatMap, ConversionContext.create().build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageContaining("DV_QUANTITY has no attribute  (path: laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0).")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOtherReferenceRangesMulti() {
        val compositionFlatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/IDCR_-_Laboratory_Test_Report(3).json"), object : TypeReference<Map<String, Any?>>() {})

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/lower|magnitude", 2.5),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/lower|unit", "mmol/l"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/upper|magnitude", 6.6),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/upper|unit", "mmol/l"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/meaning", "too high"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/lower|unit", "mmol/l"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/lower|magnitude", 6.6),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/upper|unit", "mmol/l"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_other_reference_ranges:0/upper|magnitude", 15.1),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:0/meaning|code", "X"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:0/meaning|terminology", "mine"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:0/meaning|value", "too high"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:0/lower|unit", "mmol/l"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:0/lower|magnitude", 110.0),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:1/meaning|code", "Y"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:1/meaning|terminology", "mine"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:1/meaning|value", "much too high"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:1/lower|unit", "mmol/l"),
                entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_other_reference_ranges:1/lower|magnitude", 150.0))
    }
}
