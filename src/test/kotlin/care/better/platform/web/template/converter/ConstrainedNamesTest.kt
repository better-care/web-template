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
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ConstrainedNamesTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testAnnotationLocalization() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ZN - Fluid balance record.opt"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("fluid_balance_record/fluid_intake/food")
        assertThat(node.localizedNames).isNotEmpty
        assertThat(node.localizedNames["sl"]).isEqualTo("Hrana")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testConstrainedNames() {
        val template = getTemplate("/convert/templates/ICU - Ventilator device Report3.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en")))

        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings")
        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_settings")
        assertThat(firstNode.name).isEqualTo("Ventilator findings")
        assertThat(firstNode.localizedNames["en"]).isEqualTo("Ventilator findings")
        assertThat(secondNode.name).isEqualTo("Ventilator settings")
        assertThat(secondNode.localizedNames["en"]).isEqualTo("Ventilator settings")

        val thirdNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peak_airway_pressure_p_peak")
        val fourthNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/plateau_airway_pressure_p_plateau")
        assertThat(thirdNode.localizedNames["en"]).isEqualTo("Peak airway pressure (P peak)")
        assertThat(fourthNode.localizedNames["en"]).isEqualTo("Plateau airway pressure (P plateau)")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testRelaxedNamesConversion() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_specialist_examination.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru", ImmutableList.of("ru")))

        val element = getComposition("/convert/compositions/compositionWithRelaxedNames.xml")

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(element, FromRawConversion.create())
        assertThat(retrieve).containsKeys(
            "осмотр_специалиста/диагноз/сопутствующие_заболевания/сопутствующее_заболевание:0/код_по_мкб_10|code",
            "осмотр_специалиста/диагноз/сопутствующие_заболевания/сопутствующее_заболевание:1/код_по_мкб_10|code")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testNamesWithTerminologies() {
        val template = getTemplate("/convert/templates/Laboratory report.xml")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en")))

        val composition = getComposition("/convert/compositions/namesExtTerminology.xml")

        val values: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(values).contains(
            Assertions.entry("report/laboratory_test_result:0/_name|value", "LipidPanel"),
            Assertions.entry("report/laboratory_test_result:0/_name|code", "3219"),
            Assertions.entry("report/laboratory_test_result:0/_name|terminology", "LOINC"),
            Assertions.entry("report/laboratory_test_result:0/any_event:0/laboratory_test_analyte:0/_name|value", "HDL"),
            Assertions.entry("report/laboratory_test_result:0/any_event:0/laboratory_test_analyte:0/_name|code", "3218"),
            Assertions.entry("report/laboratory_test_result:0/any_event:0/laboratory_test_analyte:0/_name|terminology", "LOINC"))
    }
}
