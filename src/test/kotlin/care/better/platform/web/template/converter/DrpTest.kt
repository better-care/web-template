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
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.mapper.WebTemplateObjectMapper
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.*
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Evaluation
import org.openehr.rm.datastructures.Cluster
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvText
import java.io.File
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DrpTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testInitialMedicationSafety() {
        val template = getTemplate("/convert/templates/MSE - Initial Medication Safety Report.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val compositionFlatMap: Map<String, String> = mapOf(
            "initial_medication_safety_report/context/start_time" to "2013-01-01T10:00:00.000+01:00",
            "initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|code" to "ac001",
            "initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|value" to "Value"
        )
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val evaluation = composition.content[0] as Evaluation
        val tree = evaluation.data as ItemTree?
        val cluster = tree!!.items[0] as Cluster
        val element = cluster.items[0] as Element
        assertThat(element.value).isInstanceOf(DvCodedText::class.java)

        val codedText = element.value as DvCodedText?
        assertThat(codedText!!.definingCode!!.codeString).isEqualTo("ac001")
        assertThat(codedText.value).isEqualTo("Value")

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create(context.locale!!))
        assertThat(flatMap).contains(
            entry("initial_medication_safety_report/medication_safety_event:0/adverse_effect:0/reaction|code", "ac001"),
            entry("initial_medication_safety_report/medication_safety_event:0/adverse_effect:0/reaction|value", "Value")
        )

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSeconfInitialMedicationSafety() {
        val template = getTemplate("/convert/templates/MSE - Initial Medication Safety Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, String> = mapOf(
            "initial_medication_safety_report/medication_safety_event/event_description" to "Just some description !!"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val evaluation = composition.content[0] as Evaluation
        val tree = evaluation.data as ItemTree?
        val element = tree!!.items[0] as Element
        assertThat(element.value).isInstanceOf(DvText::class.java)

        val text = element.value as DvText?
        assertThat(text!!.value).isEqualTo("Just some description !!")

        val map: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create(context.locale!!))
        assertThat(map).contains(
            entry("initial_medication_safety_report/medication_safety_event:0/event_description", "Just some description !!")
        )

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testThirdInitialMedicationSafety() {
        val template = getTemplate("/convert/templates/initial2.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, String> = mapOf(
            "initial_medication_safety_report/context/context_detail/period_of_care_identifier" to "id"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFourthInitialMedicationSafety4() {
        val template = getTemplate("/convert/templates/initial2.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, String> = mapOf(
            "initial_medication_safety_report/context/event_participant/participant_clinical_role" to "at0.0.51",
            "initial_medication_safety_report/context/event_participant/participant_event_role" to "at0.0.60",
            "initial_medication_safety_report/context/event_participant:1/participant_event_role" to "at0.0.64",
            "initial_medication_safety_report/context/event_participant:1/participant_clinical_role" to "at0.0.52",
            "initial_medication_safety_report/context/event_participant:2/participant_event_role" to "at0.0.64",
            "initial_medication_safety_report/context/event_participant:2/participant_clinical_role" to "at0.0.52"
        )
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        composition!!.archetypeDetails!!.templateId = template.templateId

        val otherContext = composition.context!!.otherContext as ItemTree?
        assertThat(otherContext!!.items).hasSize(3)

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testAdverseReaction() {
        val template = getTemplate("/convert/templates/MSE - Adverse Drug Reaction Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, Any> = mapOf(
            "adverse_drug_reaction_report/adverse_drug_reaction/event_type" to "at0250",
            "adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/estimated_cause_of_event" to "at0067",
            "adverse_drug_reaction_report/adverse_drug_reaction/event_timestamp" to DateTime.now(),
            "adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/comment" to "Comment"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testAdverseReactionException() {
        val template = getTemplate("/convert/templates/MSE - Adverse Drug Reaction Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, Any> = mapOf(
            "adverse_drug_reaction_report/adverse_drug_reaction/event_type" to "at0250",
            "adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/estimated_cause_of_event" to "at0067"
        )
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondAdverseReactionException() {
        val template = getTemplate("/convert/templates/adverse3.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, Any> = mapOf(
            "adverse_drug_reaction_report/adverse_drug_reaction/actual_patient_outcome" to "at0053",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention|code" to "Intervention: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention|value" to "Intervention: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention_result|code" to "Result: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention_result|value" to "Result: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention|code" to "Intervention: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention|value" to "Intervention: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention_result|code" to "Result: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention_result|value" to "Result: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention|code" to "Intervention: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention|value" to "Intervention: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention_result|code" to "Result: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention_result|value" to "Result: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/reaction|code" to "Reaction : 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/reaction|value" to "Reaction : 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/severity" to "at0225",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention|code" to "Intervention: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention|value" to "Intervention: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention_result|code" to "Result: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention_result|value" to "Result: 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention|code" to "Intervention: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention|value" to "Intervention: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention_result|code" to "Result: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention_result|value" to "Result: 2",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention|code" to "Intervention: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention|value" to "Intervention: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention_result|code" to "Result: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention_result|value" to "Result: 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/reaction|code" to "Reaction : 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/reaction|value" to "Reaction : 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/severity" to "at0225",
            "adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event:1/comment" to "Reason 1",
            "adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event:1/estimated_cause_of_event" to "at0067",
            "adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/comment" to "Reason 0",
            "adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/estimated_cause_of_event" to "at0066",
            "adverse_drug_reaction_report/adverse_drug_reaction/event_timestamp" to "2012-11-06T17:29:37.360+01:00",
            "adverse_drug_reaction_report/adverse_drug_reaction/event_type" to "at0250",
            "adverse_drug_reaction_report/adverse_drug_reaction/medra_classification|code" to "medra",
            "adverse_drug_reaction_report/adverse_drug_reaction/medra_classification|value" to "medra",
            "adverse_drug_reaction_report/adverse_drug_reaction/patient_outcome_category" to "at0063"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testErrorReport() {
        val template = getTemplate("/convert/templates/MSE - Medication Error Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val dateTime = DateTime()
        val attributes: Map<String, Any> = mapOf(
            "medication_error_report/medication_error/actual_patient_outcome" to "at0053",
            "medication_error_report/medication_error/adverse_effect:1/datetime_of_reaction_onset" to dateTime,
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention|code" to "at0304",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|code" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|value" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:2/intervention|code" to "at0304",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:2/intervention_result|code" to "Result: 2",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:2/intervention_result|value" to "Result: 2",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention|code" to "at0304",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|code" to "Result: 0",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|value" to "Result: 0",
            "medication_error_report/medication_error/adverse_effect:1/liklihood_of_causation" to "at0223",
            "medication_error_report/medication_error/adverse_effect:1/reaction|code" to "Reaction : 1",
            "medication_error_report/medication_error/adverse_effect:1/reaction|value" to "Reaction : 1",
            "medication_error_report/medication_error/adverse_effect:1/severity" to "at0225",
            "medication_error_report/medication_error/adverse_effect/datetime_of_reaction_onset" to dateTime,
            "medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention|code" to "at0304",
            "medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|code" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|value" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention|code" to "at0304",
            "medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|code" to "Result: 2",
            "medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|value" to "Result: 2",
            "medication_error_report/medication_error/adverse_effect/intervention_details/intervention|code" to "at0304",
            "medication_error_report/medication_error/adverse_effect/intervention_details/intervention_result|code" to "Result: 0",
            "medication_error_report/medication_error/adverse_effect/intervention_details/intervention_result|value" to "Result: 0",
            "medication_error_report/medication_error/adverse_effect/liklihood_of_causation" to "at0223",
            "medication_error_report/medication_error/adverse_effect/reaction|code" to "Reaction : 0",
            "medication_error_report/medication_error/adverse_effect/reaction|value" to "Reaction : 0",
            "medication_error_report/medication_error/adverse_effect/severity" to "at0225",
            "medication_error_report/medication_error/cause_of_event:1/comment" to "Reason 1",
            "medication_error_report/medication_error/cause_of_event:1/estimated_cause_of_event" to "at0067",
            "medication_error_report/medication_error/cause_of_event:2/comment" to "Reason 2",
            "medication_error_report/medication_error/cause_of_event:2/estimated_cause_of_event" to "at0068",
            "medication_error_report/medication_error/cause_of_event/comment" to "Reason 0",
            "medication_error_report/medication_error/cause_of_event/estimated_cause_of_event" to "at0067",
            "medication_error_report/medication_error/event_timestamp" to dateTime,
            "medication_error_report/medication_error/medra_classification|code" to "medra",
            "medication_error_report/medication_error/medra_classification|value" to "medra",
            "medication_error_report/medication_error/patient_outcome_category" to "at0063",
            "medication_error_report/medication_error/potential_patient_outcome" to "at0053",
            "medication_error_report/medication_error/safety_event_type" to "at0250"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testValidation() {
        val template = getTemplate("/convert/templates/MSE - Initial Medication Safety Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val attributes: Map<String, Any> = mapOf(
            "initial_medication_safety_report/context/case_identifier" to "10135201",
            "initial_medication_safety_report/context/context_detail/original_location|code" to "44132-H",
            "initial_medication_safety_report/context/context_detail/original_location|value" to "Endo-Hospital",
            "initial_medication_safety_report/context/context_detail/period_of_care_identifier" to "29885508",
            "initial_medication_safety_report/context/event_participant/participant_clinical_role" to "at0.0.53",
            "initial_medication_safety_report/context/event_participant/participant_event_role" to "at0.0.60",
            "initial_medication_safety_report/context/event_participant:1/participant_clinical_role" to "at0.0.87",
            "initial_medication_safety_report/context/event_participant:1/participant_event_role" to "at0.0.63",
            "initial_medication_safety_report/context/status" to "SAVED",
            "initial_medication_safety_report/medication_safety_event/actual_patient_outcome" to "at0057",
            "initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|code" to "10",
            "initial_medication_safety_report/medication_safety_event/adverse_effect/severity" to "at0224",
            "initial_medication_safety_report/medication_safety_event/event_timestamp" to "2013-03-26T09:33:00.000+01:00",
            "initial_medication_safety_report/medication_safety_event/safety_event_type" to "at0252"
        )

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(attributes, context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Missing DvCodedText.value at initial_medication_safety_report/medication_safety_event:0/adverse_effect:0/reaction!")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFixedValue() {
        val template = getTemplate("/convert/templates/MSE - Medication Event Case Summary.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val attributes: Map<String, Any> = mapOf(
            "medication_event_case_summary/case_summary/patient_outcome_category" to "at0064",
            "medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|code" to "R69",
            "medication_event_case_summary/context/case_identifier" to 10118153L,
            "medication_event_case_summary/context/status" to "IN_PROGRESS",
            "medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|value" to "Neznani in neopredeljeni vzroki bolezni (MKB10AM)",
            "medication_event_case_summary/case_summary/actual_patient_outcome" to "at0053",
            "medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|code" to "E68",
            "medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|value" to "E68: Kasne posledice (sekvele) prenahranjenosti (MKB10AM)"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFixedValueTestDouble() {
        val template = getTemplate("/convert/templates/MSE - Medication Event Case Summary.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val attributes: Map<String, Any> = mapOf(
            "medication_event_case_summary/context/report_type" to "at0.0.74",
            "medication_event_case_summary/case_summary/patient_outcome_category" to "at0064",
            "medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|code" to "R69",
            "medication_event_case_summary/context/case_identifier" to 10118153L,
            "medication_event_case_summary/context/status" to "IN_PROGRESS",
            "medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|value" to "Neznani in neopredeljeni vzroki bolezni (MKB10AM)",
            "medication_event_case_summary/case_summary/actual_patient_outcome" to "at0053",
            "medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|code" to "E68",
            "medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|value" to "E68: Kasne posledice (sekvele) prenahranjenosti (MKB10AM)"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)

        val itemTree = composition!!.context!!.otherContext as ItemTree?
        assertThat(itemTree!!.items).hasSize(3)

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testElementWithTwoValues() {
        val template = getTemplate("/convert/templates/MSE - Medication Event Case Summary.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val attributes: Map<String, Any> = mapOf(
            "medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|code" to "J00",
            "medication_event_case_summary/case_summary/patient_outcome_category|code" to "at0064",
            "medication_event_case_summary/case_summary/patient_outcome_category|value" to "Humanistični",
            "medication_event_case_summary/case_summary/patient_outcome_category:1|code" to "at0065",
            "medication_event_case_summary/case_summary/patient_outcome_category:1|value" to "Ekonomski",
            "medication_event_case_summary/context/status" to "IN_PROGRESS",
            "medication_event_case_summary/context/case_identifier" to "10168450",
            "medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|value" to "Akutni nazofaringitis [navadni prehlad] (MKB10AM)",
            "medication_event_case_summary/case_summary/actual_patient_outcome" to "at0053"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("medication_event_case_summary/case_summary/patient_outcome_category:0|code", "at0064"),
            entry("medication_event_case_summary/case_summary/patient_outcome_category:1|code", "at0065")
        )

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testErrorReportMultiple() {
        val template = getTemplate("/convert/templates/MSE - Medication Error Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val dateTime = DateTime()
        val attributes: Map<String, Any> = mapOf(
            "medication_error_report/medication_error/actual_patient_outcome" to "at0053",
            "medication_error_report/medication_error/adverse_effect/datetime_of_reaction_onset" to dateTime,
            "medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention|code" to "at0305",
            "medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|code" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|value" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention|code" to "at0306",
            "medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|code" to "Result: 2",
            "medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|value" to "Result: 2",
            "medication_error_report/medication_error/adverse_effect/liklihood_of_causation" to "at0223",
            "medication_error_report/medication_error/adverse_effect/reaction|code" to "Reaction : 0",
            "medication_error_report/medication_error/adverse_effect/reaction|value" to "Reaction : 0",
            "medication_error_report/medication_error/adverse_effect/severity" to "at0225",
            "medication_error_report/medication_error/adverse_effect:1/datetime_of_reaction_onset" to dateTime,
            "medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention|code" to "at0304",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|code" to "Result: 0",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|value" to "Result: 0",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention|code" to "at0307",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|code" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|value" to "Result: 1",
            "medication_error_report/medication_error/adverse_effect:1/liklihood_of_causation" to "at0223",
            "medication_error_report/medication_error/adverse_effect:1/reaction|code" to "Reaction : 1",
            "medication_error_report/medication_error/adverse_effect:1/reaction|value" to "Reaction : 1",
            "medication_error_report/medication_error/adverse_effect:1/severity" to "at0225",
            "medication_error_report/medication_error/cause_of_event:1/comment" to "Reason 1",
            "medication_error_report/medication_error/cause_of_event:1/estimated_cause_of_event" to "at0067",
            "medication_error_report/medication_error/cause_of_event:2/comment" to "Reason 2",
            "medication_error_report/medication_error/cause_of_event:2/estimated_cause_of_event" to "at0068",
            "medication_error_report/medication_error/cause_of_event/comment" to "Reason 0",
            "medication_error_report/medication_error/cause_of_event/estimated_cause_of_event" to "at0067",
            "medication_error_report/medication_error/event_timestamp" to dateTime,
            "medication_error_report/medication_error/medra_classification|code" to "medra",
            "medication_error_report/medication_error/medra_classification|value" to "medra",
            "medication_error_report/medication_error/patient_outcome_category" to "at0063",
            "medication_error_report/medication_error/potential_patient_outcome" to "at0053",
            "medication_error_report/medication_error/safety_event_type" to "at0250"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("medication_error_report/medication_error/adverse_effect:0/intervention_details:0/intervention|code", "at0305"),
            entry("medication_error_report/medication_error/adverse_effect:0/intervention_details:1/intervention|code", "at0306"),
            entry("medication_error_report/medication_error/adverse_effect:1/intervention_details:0/intervention|code", "at0304"),
            entry("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention|code", "at0307")
        )

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull

        val tmpFile = File("/" + System.getProperty("java.io.tmpdir") + "/json")
        WebTemplateObjectMapper.getWriter(true).writeValue(tmpFile, root)
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCodedWithOther() {
        val template = getTemplate("/convert/templates/MSE - Drug Related Problem Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val attributes: Map<String, Any> = mapOf(
            "drug_related_problem_report/medication_error/related_trigger:0" to "at0271",
            "drug_related_problem_report/medication_error/related_trigger:1" to "at0272",
            "drug_related_problem_report/medication_error/medra_classification|code" to "10022117",
            "drug_related_problem_report/medication_error/additional_comment" to "asdfasdfadsf",
            "drug_related_problem_report/medication_error/safety_event_type|value" to "Interakcija med zdravilom in boleznijo ali zdravilom in laboratorijskim izvidom.",
            "drug_related_problem_report/medication_error/patient_outcome_category:1|code" to "at0065",
            "drug_related_problem_report/medication_error/related_trigger:2|other" to "test",
            "drug_related_problem_report/medication_error/medra_classification|value" to "10022117: Injury, poisoning and procedural complications",
            "drug_related_problem_report/medication_error/cause_of_event/estimated_cause_of_event" to "at0076",
            "drug_related_problem_report/medication_error/actual_patient_outcome_details/actual_patient_outcome" to "at0053",
            "drug_related_problem_report/medication_error/potential_patient_outcome" to "at0056",
            "drug_related_problem_report/medication_error/safety_event_type|code" to "at0262",
            "drug_related_problem_report/medication_error/patient_outcome_category|value" to "Humanistični",
            "drug_related_problem_report/medication_error/patient_outcome_category:1|value" to "Ekonomski",
            "drug_related_problem_report/medication_error/event_timestamp" to "2015-03-04T13:42:00.000+01:00",
            "drug_related_problem_report/medication_error/patient_outcome_category|code" to "at0064"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)
        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("drug_related_problem_report/medication_error/related_trigger:0|code", "at0271"),
            entry("drug_related_problem_report/medication_error/related_trigger:1|code", "at0272"),
            entry("drug_related_problem_report/medication_error/related_trigger:2|other", "test")
        )
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondCodedWithOther() {
        val templateName = "/convert/templates/MSE - Drug Related Problem Report.opt"
        val template = getTemplate(templateName)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val attributes: Map<String, Any> = mapOf(
            "drug_related_problem_report/medication_error/related_trigger" to "at0271",
            "drug_related_problem_report/medication_error/related_trigger|other" to "test",
            "drug_related_problem_report/medication_error/medra_classification|code" to "10022117",
            "drug_related_problem_report/medication_error/additional_comment" to "asdfasdfadsf",
            "drug_related_problem_report/medication_error/safety_event_type|value" to "Interakcija med zdravilom in boleznijo ali zdravilom in laboratorijskim izvidom.",
            "drug_related_problem_report/medication_error/patient_outcome_category:1|code" to "at0065",
            "drug_related_problem_report/medication_error/medra_classification|value" to "10022117: Injury, poisoning and procedural complications",
            "drug_related_problem_report/medication_error/cause_of_event/estimated_cause_of_event" to "at0076",
            "drug_related_problem_report/medication_error/actual_patient_outcome_details/actual_patient_outcome" to "at0053",
            "drug_related_problem_report/medication_error/potential_patient_outcome" to "at0056",
            "drug_related_problem_report/medication_error/safety_event_type|code" to "at0262",
            "drug_related_problem_report/medication_error/patient_outcome_category|value" to "Humanistični",
            "drug_related_problem_report/medication_error/patient_outcome_category:1|value" to "Ekonomski",
            "drug_related_problem_report/medication_error/event_timestamp" to "2015-03-04T13:42:00.000+01:00",
            "drug_related_problem_report/medication_error/patient_outcome_category|code" to "at0064"
        )

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)
        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("drug_related_problem_report/medication_error/related_trigger:0|code", "at0271"),
            entry("drug_related_problem_report/medication_error/related_trigger:1|other", "test")
        )
    }
}
