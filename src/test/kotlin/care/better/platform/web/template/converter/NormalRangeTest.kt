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
class NormalRangeTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testNormalRanges() {

        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR_-_Laboratory_Test_Report.v0.opt"), builderContext)

        val flatMap: Map<String, Any?> = getObjectMapper().readValue(
            getJson("/convert/compositions/IDCR_-_Laboratory_Test_Report(Range).json"),
            object : TypeReference<Map<String, Any?>>() {})


        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/lower|magnitude", 2.5),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/lower|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/upper|magnitude", 6.6),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:0/result_value/_normal_range/upper|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_normal_range/lower|magnitude", 80.0),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_normal_range/lower|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_normal_range/upper|magnitude", 110.0),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:1/result_value/_normal_range/upper|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:2/result_value/_normal_range/lower|magnitude", 133.0),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:2/result_value/_normal_range/lower|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:2/result_value/_normal_range/upper|magnitude", 146.0),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:2/result_value/_normal_range/upper|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:3/result_value/_normal_range/lower|magnitude", 3.5),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:3/result_value/_normal_range/lower|unit", "mmol/l"),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:3/result_value/_normal_range/upper|magnitude", 5.3),
            entry("laboratory_test_report/laboratory_test:0/laboratory_test_panel/laboratory_result:3/result_value/_normal_range/upper|unit", "mmol/l"))
    }
}
