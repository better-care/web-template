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
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ChoiceTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testChoiceDvQuantityAndDvInterval() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order-choice.opt"), builderContext)
        val values: Map<String, Any> = mapOf(
            Pair("ctx/language", "en"),
            Pair("ctx/territory", "IE"),
            Pair("ctx/composer_name", "John"),
            Pair("medication_order/medication_detail/medication_instruction:0/narrative", "Take as prescribed!"),
            Pair("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R0"),
            Pair("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            Pair("medication_order/medication_detail/medication_instruction:0/order:0/structured_dose/quantity/interval_of_quantity_value/lower|magnitude", 72.36),
            Pair("medication_order/medication_detail/medication_instruction:0/order:0/structured_dose/quantity/interval_of_quantity_value/lower|unit", "1"),
            Pair("medication_order/medication_detail/medication_instruction:0/order:0/structured_dose/quantity/interval_of_quantity_value/upper|magnitude", 84.34),
            Pair("medication_order/medication_detail/medication_instruction:0/order:0/structured_dose/quantity/interval_of_quantity_value/upper|unit", "1"))

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLabsOceanTD() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/LAB - Laboratory Test Report.opt"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_report/laboratory_test_result/any_event/result_group/result/result_value")
        assertThat(node.children).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLabsAD() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/LAB - Laboratory Test Report-2.opt"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("laboratory_report/laboratory_test_result/any_event/result_group/result/result_value")
        assertThat(node.children).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAny() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/constraintedv3.xml"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/body_weight/any_event_yes/any")
        assertThat(node.children).isNotEmpty
        assertThat(node.children.firstOrNull { "date_time_value" == it.jsonId }?.rmType ?: "xxx").isEqualTo("DV_DATE_TIME")
    }
}
