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
import care.better.platform.web.template.converter.raw.context.ConversionContext
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartyRelated
import org.openehr.rm.common.PartySelf
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Evaluation
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class SubjectTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testEntrySubject() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            ConversionContext.create().build()
        )
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap.keys).doesNotContain("vitals/vitals/haemoglobin_a1c:0/subject|name", "vitals/vitals/haemoglobin_a1c:0/subject|id")

        val section = composition.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.subject).isInstanceOf(PartySelf::class.java)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testConstrainedEntrySubject() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Test constrained subject.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "test_constrained_subject/maternal_pregnancy:0/maternal_age" to "P25Y"
            ),
            ConversionContext.create().build()
        )
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap.keys).doesNotContain(
            "test_constrained_subject/maternal_pregnancy:0/subject|name",
            "test_constrained_subject/maternal_pregnancy:0/subject|id"
        )

        val evaluation = composition.content[0] as Evaluation
        assertThat(evaluation.subject).isInstanceOf(PartyRelated::class.java)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCustomEntrySubject() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/subject|name" to "Marija Medved",
                "vitals/vitals/haemoglobin_a1c/subject|id" to "998"
            ),
            ConversionContext.create().build()
        )
        val flatMap: MutableMap<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create()).toMutableMap()
        assertThat(flatMap).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/subject|name", "Marija Medved"),
            entry("vitals/vitals/haemoglobin_a1c:0/subject|id", "998")
        )
        flatMap["ctx/language"] = "sl"
        flatMap["ctx/territory"] = "SI"
        flatMap["ctx/id_scheme"] = "ispek"
        flatMap["ctx/id_namespace"] = "ispek"
        flatMap["ctx/composer_name"] = "George Orwell"

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().build())
        val section = secondComposition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.subject!!.externalRef!!.id!!.value).isEqualTo("998")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testNullEntrySubject() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            ConversionContext.create().build()
        )
        val section = composition!!.content[0] as Section
        val observation = section.items[0] as Observation
        observation.subject = null
        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(firstFlatMap.keys).doesNotContain("vitals/vitals/haemoglobin_a1c:0/subject|name", "vitals/vitals/haemoglobin_a1c:0/subject|id")
        observation.subject = PartyIdentified()
        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create())
        assertThat(secondFlatMap.keys).doesNotContain("vitals/vitals/haemoglobin_a1c:0/subject|name", "vitals/vitals/haemoglobin_a1c:0/subject|id")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testFlatMap() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/clinical-summary-events.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "pt",
                "ctx/territory" to "BR",
                "ctx/composer_name" to "User",
                "clinical_summary_events/_uid" to "75bab263-a9d4-4522-b265-bada4b298f56::bostjanl::1",
                "clinical_summary_events/context/start_time" to "2015-10-05T10:26:18.000Z",
                "clinical_summary_events/context/setting|code" to "",
                "clinical_summary_events/context/setting|value" to "",
                "clinical_summary_events/context/setting|terminology" to "",
                "clinical_summary_events/episodes/admission/patient_admission/patient_class" to "Pronto socorro",
                "clinical_summary_events/episodes/admission/patient_admission/attending_doctor/id_issuer" to "CRM-SP",
                "clinical_summary_events/episodes/admission/patient_admission/referring_doctor/id_issuer" to "CRM-SP",
                "clinical_summary_events/episodes/admission/patient_admission/consulting_doctor/id" to "39",
                "clinical_summary_events/episodes/admission/patient_admission/consulting_doctor/name" to "Drª Margarida Martins",
                "clinical_summary_events/episodes/admission/patient_admission/consulting_doctor/id_issuer" to "CRM-SP",
                "clinical_summary_events/episodes/admission/patient_admission/admitting_doctor/id" to "39",
                "clinical_summary_events/episodes/admission/patient_admission/admitting_doctor/name" to "Drª Margarida Martins",
                "clinical_summary_events/episodes/admission/patient_admission/admitting_doctor/id_issuer" to "CRM-SP",
                "clinical_summary_events/episodes/admission/patient_admission/admit_date_time" to "2013-11-19T17:00:00.000Z",
                "clinical_summary_events/episodes/admission/patient_admission/readmission" to false,
                "clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/_uid" to "d083c3e3-8403-48fe-8bee-927dbc641f07",
                "clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/_provider|name" to "Drª Margarida Martins",
                "clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/presenting_problem" to "Choque anafilático",
                "clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/registration_date" to "2013-11-19T17:00:00.000Z",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/_uid" to "33f205ad-7da7-44cc-92d0-85e4618acf64",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/_provider|name" to "CHS Admin",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/substance_agent" to "Antibacterianos Beta-Lactâmicos, Penicilinas",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/absolute_contraindication" to false,
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/overall_comment" to "",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/specific_substance_agent" to "Amoxicilina",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/manifestation:0" to "Rash, Urticária",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/reaction_type" to "Alergia",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/certainty" to "Confirmado",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/reaction_description" to "Esta é uma reação que aconteceu tardiamente, depois de várias administrações durante a vida da paciente nas quais não houve qualquer tipo de reação adversa manifestada.",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/onset_of_reaction" to "2003-11-12T00:00:00.000Z",
                "clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/registration_date" to "2003-11-19T17:00:00.000Z",
                "clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/_uid" to "8eaf393d-94ac-485d-8058-e5641935f7ad",
                "clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/_provider|name" to "Drª Margarida Martins",
                "clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/systolic|magnitude" to 136,
                "clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/systolic|unit" to "mm[Hg]",
                "clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/diastolic|magnitude" to 81,
                "clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/diastolic|unit" to "mm[Hg]",
                "clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/time" to "2013-11-19T17:00:00.000Z"
            ),
            ConversionContext.create().build()
        )

        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }
}
