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
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.mapper.WebTemplateObjectMapper
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
import javax.xml.bind.JAXBException

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

        val compositionFlatMap: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("initial_medication_safety_report/context/start_time", "2013-01-01T10:00:00.000+01:00")
            .put("initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|code", "ac001")
            .put("initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|value", "Value")
            .build()
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
            entry("initial_medication_safety_report/medication_safety_event:0/adverse_effect:0/reaction|value", "Value"))

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
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("initial_medication_safety_report/medication_safety_event/event_description", "Just some description !!")
            .build()

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
            entry("initial_medication_safety_report/medication_safety_event:0/event_description", "Just some description !!"))

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
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("initial_medication_safety_report/context/context_detail/period_of_care_identifier", "id")
            .build()

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
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("initial_medication_safety_report/context/event_participant/participant_clinical_role", "at0.0.51")
            .put("initial_medication_safety_report/context/event_participant/participant_event_role", "at0.0.60")
            .put("initial_medication_safety_report/context/event_participant:1/participant_event_role", "at0.0.64")
            .put("initial_medication_safety_report/context/event_participant:1/participant_clinical_role", "at0.0.52")
            .put("initial_medication_safety_report/context/event_participant:2/participant_event_role", "at0.0.64")
            .put("initial_medication_safety_report/context/event_participant:2/participant_clinical_role", "at0.0.52")
            .build()
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
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("adverse_drug_reaction_report/adverse_drug_reaction/event_type", "at0250")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/estimated_cause_of_event", "at0067")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/event_timestamp", DateTime.now())
            .put("adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/comment", "Comment")
            .build()

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
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("adverse_drug_reaction_report/adverse_drug_reaction/event_type", "at0250")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/estimated_cause_of_event", "at0067")
            .build()
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
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("adverse_drug_reaction_report/adverse_drug_reaction/actual_patient_outcome", "at0053")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention|code", "Intervention: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention|value", "Intervention: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention_result|code", "Result: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:1/intervention_result|value", "Result: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention|code", "Intervention: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention|value", "Intervention: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention_result|code", "Result: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details:2/intervention_result|value", "Result: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention|code", "Intervention: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention|value", "Intervention: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention_result|code", "Result: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/intervention_details/intervention_result|value", "Result: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/reaction|code", "Reaction : 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/reaction|value", "Reaction : 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect:1/severity", "at0225")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention|code", "Intervention: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention|value", "Intervention: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention_result|code", "Result: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:1/intervention_result|value", "Result: 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention|code", "Intervention: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention|value", "Intervention: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention_result|code", "Result: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details:2/intervention_result|value", "Result: 2")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention|code", "Intervention: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention|value", "Intervention: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention_result|code", "Result: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/intervention_details/intervention_result|value", "Result: 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/reaction|code", "Reaction : 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/reaction|value", "Reaction : 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/adverse_effect/severity", "at0225")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event:1/comment", "Reason 1")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event:1/estimated_cause_of_event", "at0067")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/comment", "Reason 0")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/cause_of_event/estimated_cause_of_event", "at0066")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/event_timestamp", "2012-11-06T17:29:37.360+01:00")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/event_type", "at0250")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/medra_classification|code", "medra")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/medra_classification|value", "medra")
            .put("adverse_drug_reaction_report/adverse_drug_reaction/patient_outcome_category", "at0063")
            .build()

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
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("medication_error_report/medication_error/actual_patient_outcome", "at0053")
            .put("medication_error_report/medication_error/adverse_effect:1/datetime_of_reaction_onset", dateTime)
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention|code", "at0304")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|code", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|value", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:2/intervention|code", "at0304")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:2/intervention_result|code", "Result: 2")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:2/intervention_result|value", "Result: 2")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention|code", "at0304")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|code", "Result: 0")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|value", "Result: 0")
            .put("medication_error_report/medication_error/adverse_effect:1/liklihood_of_causation", "at0223")
            .put("medication_error_report/medication_error/adverse_effect:1/reaction|code", "Reaction : 1")
            .put("medication_error_report/medication_error/adverse_effect:1/reaction|value", "Reaction : 1")
            .put("medication_error_report/medication_error/adverse_effect:1/severity", "at0225")
            .put("medication_error_report/medication_error/adverse_effect/datetime_of_reaction_onset", dateTime)
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention|code", "at0304")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|code", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|value", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention|code", "at0304")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|code", "Result: 2")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|value", "Result: 2")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details/intervention|code", "at0304")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details/intervention_result|code", "Result: 0")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details/intervention_result|value", "Result: 0")
            .put("medication_error_report/medication_error/adverse_effect/liklihood_of_causation", "at0223")
            .put("medication_error_report/medication_error/adverse_effect/reaction|code", "Reaction : 0")
            .put("medication_error_report/medication_error/adverse_effect/reaction|value", "Reaction : 0")
            .put("medication_error_report/medication_error/adverse_effect/severity", "at0225")
            .put("medication_error_report/medication_error/cause_of_event:1/comment", "Reason 1")
            .put("medication_error_report/medication_error/cause_of_event:1/estimated_cause_of_event", "at0067")
            .put("medication_error_report/medication_error/cause_of_event:2/comment", "Reason 2")
            .put("medication_error_report/medication_error/cause_of_event:2/estimated_cause_of_event", "at0068")
            .put("medication_error_report/medication_error/cause_of_event/comment", "Reason 0")
            .put("medication_error_report/medication_error/cause_of_event/estimated_cause_of_event", "at0067")
            .put("medication_error_report/medication_error/event_timestamp", dateTime)
            .put("medication_error_report/medication_error/medra_classification|code", "medra")
            .put("medication_error_report/medication_error/medra_classification|value", "medra")
            .put("medication_error_report/medication_error/patient_outcome_category", "at0063")
            .put("medication_error_report/medication_error/potential_patient_outcome", "at0053")
            .put("medication_error_report/medication_error/safety_event_type", "at0250")
            .build()

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
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("initial_medication_safety_report/context/case_identifier", "10135201")
            .put("initial_medication_safety_report/context/context_detail/original_location|code", "44132-H")
            .put("initial_medication_safety_report/context/context_detail/original_location|value", "Endo-Hospital")
            .put("initial_medication_safety_report/context/context_detail/period_of_care_identifier", "29885508")
            .put("initial_medication_safety_report/context/event_participant/participant_clinical_role", "at0.0.53")
            .put("initial_medication_safety_report/context/event_participant/participant_event_role", "at0.0.60")
            .put("initial_medication_safety_report/context/event_participant:1/participant_clinical_role", "at0.0.87")
            .put("initial_medication_safety_report/context/event_participant:1/participant_event_role", "at0.0.63")
            .put("initial_medication_safety_report/context/status", "SAVED")
            .put("initial_medication_safety_report/medication_safety_event/actual_patient_outcome", "at0057")
            .put("initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|code", "10")
            .put("initial_medication_safety_report/medication_safety_event/adverse_effect/severity", "at0224")
            .put("initial_medication_safety_report/medication_safety_event/event_timestamp", "2013-03-26T09:33:00.000+01:00")
            .put("initial_medication_safety_report/medication_safety_event/safety_event_type", "at0252")
            .build()

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
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("medication_event_case_summary/case_summary/patient_outcome_category", "at0064")
            .put("medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|code", "R69")
            .put("medication_event_case_summary/context/case_identifier", 10118153L)
            .put("medication_event_case_summary/context/status", "IN_PROGRESS")
            .put("medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|value", "Neznani in neopredeljeni vzroki bolezni (MKB10AM)")
            .put("medication_event_case_summary/case_summary/actual_patient_outcome", "at0053")
            .put("medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|code", "E68")
            .put("medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|value", "E68: Kasne posledice (sekvele) prenahranjenosti (MKB10AM)")
            .build()

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
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("medication_event_case_summary/context/report_type", "at0.0.74")
            .put("medication_event_case_summary/case_summary/patient_outcome_category", "at0064")
            .put("medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|code", "R69")
            .put("medication_event_case_summary/context/case_identifier", 10118153L)
            .put("medication_event_case_summary/context/status", "IN_PROGRESS")
            .put("medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|value", "Neznani in neopredeljeni vzroki bolezni (MKB10AM)")
            .put("medication_event_case_summary/case_summary/actual_patient_outcome", "at0053")
            .put("medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|code", "E68")
            .put("medication_event_case_summary/case_summary/summary_details/discharge_diagnosis_classification|value", "E68: Kasne posledice (sekvele) prenahranjenosti (MKB10AM)")
            .build()

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
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|code", "J00")
            .put("medication_event_case_summary/case_summary/patient_outcome_category|code", "at0064")
            .put("medication_event_case_summary/case_summary/patient_outcome_category|value", "Humanistični")
            .put("medication_event_case_summary/case_summary/patient_outcome_category:1|code", "at0065")
            .put("medication_event_case_summary/case_summary/patient_outcome_category:1|value", "Ekonomski")
            .put("medication_event_case_summary/context/status", "IN_PROGRESS")
            .put("medication_event_case_summary/context/case_identifier", "10168450")
            .put("medication_event_case_summary/case_summary/summary_details/admission_diagnosis_classification|value", "Akutni nazofaringitis [navadni prehlad] (MKB10AM)")
            .put("medication_event_case_summary/case_summary/actual_patient_outcome", "at0053")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("medication_event_case_summary/case_summary/patient_outcome_category:0|code", "at0064"),
            entry("medication_event_case_summary/case_summary/patient_outcome_category:1|code", "at0065"))

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
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("medication_error_report/medication_error/actual_patient_outcome", "at0053")
            .put("medication_error_report/medication_error/adverse_effect/datetime_of_reaction_onset", dateTime)
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention|code", "at0305")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|code", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:1/intervention_result|value", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention|code", "at0306")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|code", "Result: 2")
            .put("medication_error_report/medication_error/adverse_effect/intervention_details:2/intervention_result|value", "Result: 2")
            .put("medication_error_report/medication_error/adverse_effect/liklihood_of_causation", "at0223")
            .put("medication_error_report/medication_error/adverse_effect/reaction|code", "Reaction : 0")
            .put("medication_error_report/medication_error/adverse_effect/reaction|value", "Reaction : 0")
            .put("medication_error_report/medication_error/adverse_effect/severity", "at0225")
            .put("medication_error_report/medication_error/adverse_effect:1/datetime_of_reaction_onset", dateTime)
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention|code", "at0304")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|code", "Result: 0")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details/intervention_result|value", "Result: 0")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention|code", "at0307")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|code", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention_result|value", "Result: 1")
            .put("medication_error_report/medication_error/adverse_effect:1/liklihood_of_causation", "at0223")
            .put("medication_error_report/medication_error/adverse_effect:1/reaction|code", "Reaction : 1")
            .put("medication_error_report/medication_error/adverse_effect:1/reaction|value", "Reaction : 1")
            .put("medication_error_report/medication_error/adverse_effect:1/severity", "at0225")
            .put("medication_error_report/medication_error/cause_of_event:1/comment", "Reason 1")
            .put("medication_error_report/medication_error/cause_of_event:1/estimated_cause_of_event", "at0067")
            .put("medication_error_report/medication_error/cause_of_event:2/comment", "Reason 2")
            .put("medication_error_report/medication_error/cause_of_event:2/estimated_cause_of_event", "at0068")
            .put("medication_error_report/medication_error/cause_of_event/comment", "Reason 0")
            .put("medication_error_report/medication_error/cause_of_event/estimated_cause_of_event", "at0067")
            .put("medication_error_report/medication_error/event_timestamp", dateTime)
            .put("medication_error_report/medication_error/medra_classification|code", "medra")
            .put("medication_error_report/medication_error/medra_classification|value", "medra")
            .put("medication_error_report/medication_error/patient_outcome_category", "at0063")
            .put("medication_error_report/medication_error/potential_patient_outcome", "at0053")
            .put("medication_error_report/medication_error/safety_event_type", "at0250")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("medication_error_report/medication_error/adverse_effect:0/intervention_details:0/intervention|code", "at0305"),
            entry("medication_error_report/medication_error/adverse_effect:0/intervention_details:1/intervention|code", "at0306"),
            entry("medication_error_report/medication_error/adverse_effect:1/intervention_details:0/intervention|code", "at0304"),
            entry("medication_error_report/medication_error/adverse_effect:1/intervention_details:1/intervention|code", "at0307"))

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
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("drug_related_problem_report/medication_error/related_trigger:0", "at0271")
            .put("drug_related_problem_report/medication_error/related_trigger:1", "at0272")
            .put("drug_related_problem_report/medication_error/medra_classification|code", "10022117")
            .put("drug_related_problem_report/medication_error/additional_comment", "asdfasdfadsf")
            .put("drug_related_problem_report/medication_error/safety_event_type|value", "Interakcija med zdravilom in boleznijo ali zdravilom in laboratorijskim izvidom.")
            .put("drug_related_problem_report/medication_error/patient_outcome_category:1|code", "at0065")
            .put("drug_related_problem_report/medication_error/related_trigger:2|other", "test")
            .put("drug_related_problem_report/medication_error/medra_classification|value", "10022117: Injury, poisoning and procedural complications")
            .put("drug_related_problem_report/medication_error/cause_of_event/estimated_cause_of_event", "at0076")
            .put("drug_related_problem_report/medication_error/actual_patient_outcome_details/actual_patient_outcome", "at0053")
            .put("drug_related_problem_report/medication_error/potential_patient_outcome", "at0056")
            .put("drug_related_problem_report/medication_error/safety_event_type|code", "at0262")
            .put("drug_related_problem_report/medication_error/patient_outcome_category|value", "Humanistični")
            .put("drug_related_problem_report/medication_error/patient_outcome_category:1|value", "Ekonomski")
            .put("drug_related_problem_report/medication_error/event_timestamp", "2015-03-04T13:42:00.000+01:00")
            .put("drug_related_problem_report/medication_error/patient_outcome_category|code", "at0064")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)
        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("drug_related_problem_report/medication_error/related_trigger:0|code", "at0271"),
            entry("drug_related_problem_report/medication_error/related_trigger:1|code", "at0272"),
            entry("drug_related_problem_report/medication_error/related_trigger:2|other", "test"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondCodedWithOther() {
        val templateName = "/convert/templates/MSE - Drug Related Problem Report.opt"
        val template = getTemplate(templateName)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val attributes: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("drug_related_problem_report/medication_error/related_trigger", "at0271")
            .put("drug_related_problem_report/medication_error/related_trigger|other", "test")
            .put("drug_related_problem_report/medication_error/medra_classification|code", "10022117")
            .put("drug_related_problem_report/medication_error/additional_comment", "asdfasdfadsf")
            .put("drug_related_problem_report/medication_error/safety_event_type|value", "Interakcija med zdravilom in boleznijo ali zdravilom in laboratorijskim izvidom.")
            .put("drug_related_problem_report/medication_error/patient_outcome_category:1|code", "at0065")
            .put("drug_related_problem_report/medication_error/medra_classification|value", "10022117: Injury, poisoning and procedural complications")
            .put("drug_related_problem_report/medication_error/cause_of_event/estimated_cause_of_event", "at0076")
            .put("drug_related_problem_report/medication_error/actual_patient_outcome_details/actual_patient_outcome", "at0053")
            .put("drug_related_problem_report/medication_error/potential_patient_outcome", "at0056")
            .put("drug_related_problem_report/medication_error/safety_event_type|code", "at0262")
            .put("drug_related_problem_report/medication_error/patient_outcome_category|value", "Humanistični")
            .put("drug_related_problem_report/medication_error/patient_outcome_category:1|value", "Ekonomski")
            .put("drug_related_problem_report/medication_error/event_timestamp", "2015-03-04T13:42:00.000+01:00")
            .put("drug_related_problem_report/medication_error/patient_outcome_category|code", "at0064")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(attributes, context)
        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve).contains(
            entry("drug_related_problem_report/medication_error/related_trigger:0|code", "at0271"),
            entry("drug_related_problem_report/medication_error/related_trigger:1|other", "test"))
    }
}
