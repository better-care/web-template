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
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.mapper.WebTemplateObjectMapper
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.CodedValue
import care.better.platform.web.template.builder.model.input.CodedValueWithDescription
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DescriptionTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCodesAndDescriptions() {
        val template = getTemplate("/convert/templates/MSE - Initial Medication Safety Report.opt")
        val context = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)

        val codes: List<CodedValueWithDescription> = webTemplate.getCodesWithDescription("initial_medication_safety_report/context/event_participant/participant_clinical_role", "sl")
        assertThat(codes[0].label).isNotEmpty
        assertThat(codes[0].description).isNotEmpty
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondCodesAndDescriptions() {
        val template = getTemplate("/convert/templates/Vital Signs.xml")
        val context = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)

        val codes: List<CodedValueWithDescription> = webTemplate.getCodesWithDescription("vital_signs/body_temperature/any_event/body_exposure", "sl")
        assertThat(codes[0].label).isNotEmpty
        assertThat(codes[0].description).isNotEmpty

        val englishCodes: List<CodedValueWithDescription> = webTemplate.getCodesWithDescription("vital_signs/body_temperature/any_event/body_exposure", "en")
        assertThat(englishCodes[0].label).isEqualTo("Naked")
        assertThat(englishCodes[0].description).isEqualTo("No clothing, bedding or covering")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCodesAndDescriptionsDips() {
        val template = getTemplate("/convert/templates/BNA_Test_CodeSetAndICD10.opt")
        val context = WebTemplateBuilderContext("nb", ImmutableList.of("nb"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("test_icd10_and_codeset/problem_diagnose/asa_fysisk_status_klassifikasjon/asa_pasient_status")

        val codedValue: WebTemplateCodedValue = node.getInput()!!.list[0]
        assertThat(codedValue.label).isNotEmpty
        assertThat(codedValue.value).isNotEmpty
        assertThat(codedValue.localizedDescriptions).isNotEmpty
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSerialization() {
        val codedValue = CodedValue("value", "label")
        val jsonString = WebTemplateObjectMapper.getWriter(false).writeValueAsString(codedValue)
        val jsonNode = WebTemplateObjectMapper.readTree(jsonString)
        assertThat(jsonNode.path("description").isMissingNode).isTrue

        val codedValueWithDescription: CodedValue = CodedValueWithDescription("value", "label", "descr")
        val jsonStringWithDescription = WebTemplateObjectMapper.getWriter(false).writeValueAsString(codedValueWithDescription)
        val jsonNodeWithDescription = WebTemplateObjectMapper.readTree(jsonStringWithDescription)
        assertThat(jsonNodeWithDescription.path("description").isMissingNode).isFalse
    }
}
