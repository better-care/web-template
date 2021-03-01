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
import com.google.common.collect.ImmutableSet
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.exception.UnknownPathBuilderException
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.CodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class LocalizationTest : AbstractWebTemplateTest() {
    private val path = "perinatal_history/perinatal_history/maternal_pregnancy/significant_family_history/family_issue"

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testPerinatal() {
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/MED - Perinatal history Summary.opt"),
            WebTemplateBuilderContext("sl", ImmutableList.of("en", "ru")))

        assertThat(webTemplate.getLabel(path)).isEqualTo("Družinska anamneza")
        assertThat(webTemplate.getLabel(path, "en")).isEqualTo("Family issue")

        val codes: List<CodedValue> = webTemplate.getCodes(path)
        assertThat(codes).extracting("label")
            .containsExactly("Ni posebnosti", "Dvojčki v družini", "Prirojene anomalije", "Hipertenzija v družini", "Sladkorna bolezen v družini")
        assertThat(webTemplate.getCodes(path, "en")).extracting("label")
            .containsExactly("No significant family history", "Twins in the family", "Congenital anomaly", "Hypertension", "Diabetes mellitus")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testEmptyDefaultLanguage() {
        val template = getTemplate("/convert/templates/MED - Perinatal history Summary.opt")
        val context = WebTemplateBuilderContext("", ImmutableList.of("en", "ru"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate.defaultLanguage).isEqualTo("")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testInvalidPath() {
        val template = getTemplate("/convert/templates/MED - Perinatal history Summary.opt")
        val context = WebTemplateBuilderContext("sl", ImmutableList.of("en", "ru"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThatThrownBy { webTemplate.getLabel("perinatal_history/11perinatal_history/maternal_pregnancy/significant_family_history/family_issue") }
            .isInstanceOf(UnknownPathBuilderException::class.java)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCodes() {
        val template = getTemplate("/convert/templates/MSE - Initial Medication Safety Report.opt")
        val context = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        val codes: List<CodedValue> = webTemplate.getCodes("initial_medication_safety_report/context/event_participant/participant_clinical_role", "sl")
        assertThat(codes[0].label).isNotEmpty
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecoondCodes() {
        val template = getTemplate("/convert/templates/ICU - Ventilator device Report.opt")
        val context = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/sle_5000/sle_5000_observations/ventilator_settings/inspiratory_time_t_i")
        assertThat(node.localizedNames).contains(entry("sl", "Inspiratorni čas (T i)"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testLabels() {
        val template = getTemplate("/convert/templates/MSE - Adverse Drug Reaction Report.opt")
        val context = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate.getLabel("adverse_drug_reaction_report/context/event_participant", "en")).isEqualTo("Event participant")
        assertThat(
            webTemplate.getLabel(
                "adverse_drug_reaction_report/context/event_participant/participant_clinical_role",
                "en")).isEqualTo("Participant clinical role")
        assertThat(
            webTemplate.getLabel(
                "adverse_drug_reaction_report/context/event_participant/participant_event_role",
                "en")).isEqualTo("Participant event role")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testInvalidNode() {
        val template = getTemplate("/convert/templates/MSE - Adverse Drug Reaction Report.opt")
        val context = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThatThrownBy { webTemplate.getLabel("xyz", "en") }.isInstanceOf(UnknownPathBuilderException::class.java)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testAnnotations() {
        val template = getTemplate("/convert/templates/ZN - Vital Functions Encounter-1.xml")
        val node: WebTemplateNode =
            WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))).findWebTemplateNode("vital_functions/vital_signs")
        assertThat(node.localizedNames).containsOnly(entry("en", "Vital signs"), entry("sl", "Ocena vitalnih funkcij"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testAnnotationsV2() {
        val template = getTemplate("/convert/templates/Basic Assessment.opt")
        val firstNode: WebTemplateNode =
            WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))).findWebTemplateNode("basic_assessment")
        assertThat(firstNode.localizedNames).containsOnly(entry("en", "Basic Assessment"), entry("sl", ""))

        val secondNode: WebTemplateNode =
            WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "fr"))).findWebTemplateNode("basic_assessment")
        assertThat(secondNode.localizedNames).containsOnly(entry("en", "Basic Assessment"), entry("fr", "Évaluation de base"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testUnits() {
        val template = getTemplate("/convert/templates/older/Demo Vitals.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("vitals/vitals/body_temperature:0/any_event:0/temperature")
        val codedValues: List<WebTemplateCodedValue> = node.inputs[1].list
        assertThat(codedValues).hasSize(2)
        assertThat(codedValues[0].localizedLabels["en"]).isEqualTo("enLBLdegC")
        assertThat(codedValues[0].localizedLabels["sl"]).isEqualTo("slLBLdegC")
        assertThat(codedValues[1].localizedLabels["en"]).isEqualTo("enLBLdegF")
        assertThat(codedValues[1].localizedLabels["sl"]).isEqualTo("slLBLdegF")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondUnits() {
        val template = getTemplate("/convert/templates/Unit Localisation.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "de")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("unit_localisation/body_temperature:0/any_event:0/temperature")
        val codedValues: List<WebTemplateCodedValue> = node.inputs[1].list
        assertThat(codedValues).hasSize(2)
        assertThat(codedValues[0].localizedLabels["en"]).isEqualTo("en degC")
        assertThat(codedValues[0].localizedLabels["de"]).isEqualTo("de degC")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testConstrainedNameLocalization() {
        val template = getTemplate("/convert/templates/DRP Report - new.opt")
        val webTemplateEN: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))
        val nodeEN: WebTemplateNode = webTemplateEN.findWebTemplateNode("drp_report/context/sender")
        assertThat(nodeEN.localizedName).isEqualTo("Sender")
        assertThat(nodeEN.localizedNames).contains(entry("en", "Sender"), entry("sl", "Pošiljatelj"))

        val webTemplateSL: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl", ImmutableList.of("en", "sl")))
        val nodeSL: WebTemplateNode = webTemplateSL.findWebTemplateNode("drp_report/context/sender")
        assertThat(nodeSL.localizedName).isEqualTo("Pošiljatelj")
        assertThat(nodeSL.localizedNames).contains(entry("en", "Sender"), entry("sl", "Pošiljatelj"))

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext(languages = setOf("en", "sl")))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("drp_report/context/sender")
        assertThat(node.localizedName).isEqualTo("Sender")
        assertThat(node.localizedNames).contains(entry("en", "Sender"), entry("sl", "Pošiljatelj"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testAnnotationsFromAD() {
        val template = getTemplate("/convert/templates/Headache.opt")
        val webTemplateEN: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "sl", "de")))
        val node: WebTemplateNode = webTemplateEN.findWebTemplateNode("headache/headache")
        assertThat(node.localizedName).isEqualTo("Headache")
        assertThat(node.localizedNames).contains(entry("sl", "Slovenski prevod"), entry("de", "Deutsch"))

        val secondNode: WebTemplateNode = webTemplateEN.findWebTemplateNode("headache/ena")
        assertThat(secondNode.localizedName).isEqualTo("Ena")
        assertThat(secondNode.localizedNames).contains(entry("sl", "Ena slovensko"), entry("de", "Eins"))

        val thirdNode: WebTemplateNode = webTemplateEN.findWebTemplateNode("headache/dva")
        assertThat(thirdNode.localizedName).isEqualTo("Dva")
        assertThat(thirdNode.localizedNames).contains(entry("sl", "Dva slovensko"), entry("de", "Zwei"))
    }
}
