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
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
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
import javax.xml.bind.JAXBException

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
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .build(),
                ConversionContext.create().build())
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
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("test_constrained_subject/maternal_pregnancy:0/maternal_age", "P25Y")
                        .build(),
                ConversionContext.create().build())
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap.keys).doesNotContain(
                "test_constrained_subject/maternal_pregnancy:0/subject|name",
                "test_constrained_subject/maternal_pregnancy:0/subject|id")

        val evaluation = composition.content[0] as Evaluation
        assertThat(evaluation.subject).isInstanceOf(PartyRelated::class.java)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCustomEntrySubject() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .put("vitals/vitals/haemoglobin_a1c/subject|name", "Marija Medved")
                        .put("vitals/vitals/haemoglobin_a1c/subject|id", "998")
                        .build(),
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
                ImmutableMap.builder<String, String>()
                        .put("ctx/language", "sl")
                        .put("ctx/territory", "SI")
                        .put("ctx/id_scheme", "ispek")
                        .put("ctx/id_namespace", "ispek")
                        .put("ctx/composer_name", "George Orwell")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                        .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                        .build(),
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
                ImmutableMap.builder<String, Any>()
                        .put("ctx/language", "pt")
                        .put("ctx/territory", "BR")
                        .put("ctx/composer_name", "User")
                        .put("clinical_summary_events/_uid", "75bab263-a9d4-4522-b265-bada4b298f56::bostjanl::1")
                        .put("clinical_summary_events/context/start_time", "2015-10-05T10:26:18.000Z")
                        .put("clinical_summary_events/context/setting|code", "")
                        .put("clinical_summary_events/context/setting|value", "")
                        .put("clinical_summary_events/context/setting|terminology", "")
                        .put("clinical_summary_events/episodes/admission/patient_admission/patient_class", "Pronto socorro")
                        .put("clinical_summary_events/episodes/admission/patient_admission/attending_doctor/id_issuer", "CRM-SP")
                        .put("clinical_summary_events/episodes/admission/patient_admission/referring_doctor/id_issuer", "CRM-SP")
                        .put("clinical_summary_events/episodes/admission/patient_admission/consulting_doctor/id", "39")
                        .put("clinical_summary_events/episodes/admission/patient_admission/consulting_doctor/name", "Drª Margarida Martins")
                        .put("clinical_summary_events/episodes/admission/patient_admission/consulting_doctor/id_issuer", "CRM-SP")
                        .put("clinical_summary_events/episodes/admission/patient_admission/admitting_doctor/id", "39")
                        .put("clinical_summary_events/episodes/admission/patient_admission/admitting_doctor/name", "Drª Margarida Martins")
                        .put("clinical_summary_events/episodes/admission/patient_admission/admitting_doctor/id_issuer", "CRM-SP")
                        .put("clinical_summary_events/episodes/admission/patient_admission/admit_date_time", "2013-11-19T17:00:00.000Z")
                        .put("clinical_summary_events/episodes/admission/patient_admission/readmission", false)
                        .put("clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/_uid", "d083c3e3-8403-48fe-8bee-927dbc641f07")
                        .put("clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/_provider|name", "Drª Margarida Martins")
                        .put("clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/presenting_problem", "Choque anafilático")
                        .put("clinical_summary_events/episodes/reason_for_encounter/reason_for_encounter:0/registration_date", "2013-11-19T17:00:00.000Z")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/_uid", "33f205ad-7da7-44cc-92d0-85e4618acf64")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/_provider|name", "CHS Admin")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/substance_agent", "Antibacterianos Beta-Lactâmicos, Penicilinas")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/absolute_contraindication", false)
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/overall_comment", "")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/specific_substance_agent", "Amoxicilina")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/manifestation:0", "Rash, Urticária")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/reaction_type", "Alergia")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/certainty", "Confirmado")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/reaction_description", "Esta é uma reação que aconteceu tardiamente, depois de várias administrações durante a vida da paciente nas quais não houve qualquer tipo de reação adversa manifestada.")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/reaction_event/onset_of_reaction", "2003-11-12T00:00:00.000Z")
                        .put("clinical_summary_events/alergies_adverse_reactions_and_intolerances/allergies_and_adverse_reactions/adverse_reaction:0/registration_date", "2003-11-19T17:00:00.000Z")
                        .put("clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/_uid", "8eaf393d-94ac-485d-8058-e5641935f7ad")
                        .put("clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/_provider|name", "Drª Margarida Martins")
                        .put("clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/systolic|magnitude", 136)
                        .put("clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/systolic|unit", "mm[Hg]")
                        .put("clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/diastolic|magnitude", 81)
                        .put("clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/diastolic|unit", "mm[Hg]")
                        .put("clinical_summary_events/vital_signs/blood_pressure/blood_pressure:0/any_event:0/time", "2013-11-19T17:00:00.000Z").build(),
                ConversionContext.create().build())

        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }
}
