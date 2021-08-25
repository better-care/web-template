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

import care.better.platform.jaxb.JaxbRegistry
import care.better.platform.path.PathUtils
import care.better.platform.utils.JSR310ConversionUtils
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.mapper.WebTemplateObjectMapper
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.joda.time.Period
import org.junit.jupiter.api.Test
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Evaluation
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.*
import org.openehr.rm.datatypes.*
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.xml.bind.JAXBElement
import javax.xml.bind.JAXBException
import javax.xml.namespace.QName

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ClinicalTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVitalsSingleNonCompact() {
        val template = getTemplate("/convert/templates/Demo Vitals.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))

        val flatMap: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("vitals/context/setting|code", "238")
            .put("vitals/context/setting|value", "other care")
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", "37.7")
            .put("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C")
            .build()
        assertThat(webTemplate.convertFromFlatToRaw<Composition>(flatMap, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVitalsSingle() {
        val template = getTemplate("/convert/templates/Demo Vitals.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))

        val contextStartTime = LocalDateTime.of(2012, 2, 1, 0, 0)
        val flatMap: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("vitals/vitals/haemoglobin_a1c/any_event/hba1c", "5,1")
            .put("vitals/vitals/haemoglobin_a1c/datetime_result_issued", LocalDateTime.of(2012, 1, 20, 19, 30).toString())
            .put("vitals/vitals/body_temperature/any_event/time", LocalDateTime.of(2012, 1, 1, 0, 0).toString())
            .put("vitals/vitals/body_temperature/site_of_measurement", "at0022")
            .put("vitals/vitals/body_temperature/any_event/temperature|magnitude", "38,1")
            .put("vitals/vitals/body_temperature/any_event/temperature|unit", "°C")
            .put("vitals/vitals/body_temperature:1/any_event/temperature|magnitude", "39,1")
            .put("vitals/vitals/body_temperature:1/any_event/temperature|unit", "°C")
            .put("ctx/time", contextStartTime.toString())
            .put("ctx/category", "event")
            .put("ctx/setting", "dental care")
            .put("ctx/id_schema", "local_sch")
            .put("ctx/id_namespace", "local_ns")
            .put("ctx/provider_name", "Pippa Smith")
            .put("ctx/provider_id", "197")
            .put("ctx/participation_name", "Edna Smith")
            .put("ctx/participation_function", "performer")
            .put("ctx/participation_mode", "face-to-face communication")
            .put("ctx/participation_id", "199")
            .put("ctx/participation_name:1", "Testing Doctor")
            .put("ctx/participation_function:1", "executor")
            .put("ctx/participation_mode:1", "interpreted audio-only")
            .put("ctx/participation_id:1", "198")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull
        val contextStartOffsetDateTime = contextStartTime.atZone(ZoneId.systemDefault()).toOffsetDateTime()
        val formattedOffsetDateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(contextStartOffsetDateTime)
        assertThat(composition.context!!.startTime!!.value).isEqualTo(formattedOffsetDateTime)

        val section = composition.content[0] as Section
        assertThat(section.name!!.value).isEqualTo("Vitals")

        val hba1c = section.items[0] as Observation
        assertThat(hba1c.name!!.value).isEqualTo("Haemoglobin A1c")

        val history2 = hba1c.data
        assertThat(history2!!.origin!!.value).isEqualTo(formattedOffsetDateTime)

        val data2 = history2.events[0].data as ItemTree?
        val element2 = data2!!.items[0] as Element
        val value2 = element2.value as DvProportion?
        assertThat(value2!!.numerator).isEqualTo(5.1f)
        assertThat(value2.denominator).isEqualTo(100.0f)
        assertThat(value2.type).isEqualTo(2)
        assertThat(value2.precision).isNull()

        val provider = hba1c.provider as PartyIdentified?
        assertThat(provider!!.externalRef!!.namespace).isEqualTo("local_ns")
        assertThat(provider.externalRef!!.id!!.value).isEqualTo("197")
        assertThat(provider.name).isEqualTo("Pippa Smith")

        val participation0 = hba1c.otherParticipations[0]
        val participation1 = hba1c.otherParticipations[1]
        assertThat(participation0.mode!!.definingCode!!.codeString).isEqualTo("216")
        assertThat(participation0.function!!.value).isEqualTo("performer")
        assertThat((participation0.performer as PartyIdentified?)!!.name).isEqualTo("Edna Smith")
        assertThat(participation0.performer!!.externalRef!!.id!!.value).isEqualTo("199")
        assertThat(participation1.mode!!.definingCode!!.codeString).isEqualTo("222")
        assertThat(participation1.function!!.value).isEqualTo("executor")
        assertThat((participation1.performer as PartyIdentified?)!!.name).isEqualTo("Testing Doctor")
        assertThat(participation1.performer!!.externalRef!!.id!!.value).isEqualTo("198")

        val temp0 = section.items[1] as Observation
        assertThat(temp0.name!!.value).isEqualTo("Body temperature")

        val protocol0 = temp0.protocol as ItemTree?
        val element0 = protocol0!!.items[0] as Element
        val value0 = element0.value as DvCodedText?
        assertThat(value0!!.definingCode!!.codeString).isEqualTo("at0022")
        assertThat(value0.definingCode!!.terminologyId!!.value).isEqualTo("local")
        assertThat(value0.value).isEqualTo("Usta")
        assertThat(section.items[2].name!!.value).isEqualTo("Body temperature #2")

        val tree = temp0.data!!.events[0].data as ItemTree?
        val tempElement = tree!!.items[0] as Element
        val tempElementValue = tempElement.value as DvQuantity?
        assertThat(tempElementValue!!.precision).isEqualTo(1)


        val marshaller = JaxbRegistry.getInstance().marshaller
        FileWriter(File("/" + System.getProperty("java.io.tmpdir") + "/demovitals-composition.xml")).use { writer ->
            marshaller.marshal(
                JAXBElement(
                    QName("composition"),
                    Composition::class.java,
                    composition
                ), writer
            )
        }
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondVitalsSingle() {
        val template = getTemplate("/convert/templates/Demo Vitals.opt")
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").build()

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("vitals/vitals/haemoglobin_a1c/datetime_result_issued", LocalDateTime.of(2012, 2, 1, 8, 7).toString())
            .put("vitals/vitals/haemoglobin_a1c/receiver_order_identifier", "rec")
            .put("vitals/vitals/haemoglobin_a1c/any_event/test_status", "at0038")
            .put("vitals/vitals/haemoglobin_a1c/any_event/hba1c", "3.2")
            .put("vitals/vitals/haemoglobin_a1c/any_event/overall_interpretation", "overall interp")
            .put("vitals/vitals/haemoglobin_a1c/any_event/diagnostic_service", "diag")
            .put("vitals/vitals/haemoglobin_a1c/laboratory_test_result_identifier", "lab")
            .put("vitals/vitals/haemoglobin_a1c/any_event/test_name", "test name")
            .put("vitals/vitals/haemoglobin_a1c/requestor_order_identifier", "req")
            .put("ctx/participation_mode:0", "face-to-face communication")
            .put("ctx/territory", "IE")
            .put("ctx/category", "event")
            .put("ctx/action_ism_transition_current_state", "initial")
            .put("ctx/id_schema", "local")
            .put("ctx/action_time", "now")
            .put("ctx/time", "now")
            .put("ctx/setting", "other care")
            .put("ctx/language", "en")
            .put("ctx/id_namespace", "local")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        val section = composition.content[0] as Section
        assertThat(section.name!!.value).isEqualTo("Vitals")

        val hba1c = section.items[0] as Observation
        assertThat(hba1c.name!!.value).isEqualTo("Haemoglobin A1c")

        val history = hba1c.data
        assertThat(history!!.events).hasSize(1)


        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/hba1c", 0.032f),
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/hba1c|numerator", 3.2f),
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/hba1c|denominator", 100.0f))

        val retrieveFormatted: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create(Locale("ru")))
        assertThat(retrieveFormatted).contains(entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/hba1c", "3,2%"))


        val structuredComposition = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        val proportion = structuredComposition!!.get("vitals").get("vitals").get(0).get("haemoglobin_a1c").get(0).get("any_event").get(0).get("hba1c").get(0)
        assertThat(proportion.has("")).isFalse
        assertThat(proportion.has("|value")).isTrue
        assertThat(proportion.get("|value").floatValue()).isEqualTo(0.032f)


    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testThirdVitalsSingle() {
        val template = getTemplate("/convert/templates/Demo Vitals.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val context = ConversionContext.create()
            .withLanguage("en")
            .withTerritory("IE")
            .withComposerName("composer")
            .withLocale(Locale("en", "IE"))
            .build()

        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("vitals/vitals/body_temperature/any_event/temperature|magnitude", "39")
            .put("vitals/vitals/body_temperature/any_event/temperature|unit", "°C")
            .put("vitals/vitals/body_temperature/any_event/symptoms|at0.65", "on")
            .put("vitals/vitals/body_temperature/any_event/symptoms|at0.64", "on")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        composition!!.archetypeDetails!!.templateId = template.templateId

        val section = composition.content[0] as Section
        assertThat(section.name!!.value).isEqualTo("Vitals")

        val temp = section.items[0] as Observation
        assertThat(temp.name!!.value).isEqualTo("Body temperature")

        val history = temp.data
        assertThat(history!!.events).hasSize(1)


        val formattedFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create(context.locale!!))
        assertThat(formattedFlatMap).contains(
            entry("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", "39"),
            entry("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C"),
            entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|code", "at0.65"),
            entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:1|code", "at0.64"))

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", 39.0),
            entry("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C"),
            entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:0|code", "at0.65"),
            entry("vitals/vitals/body_temperature:0/any_event:0/symptoms:1|code", "at0.64"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testPerinatal() {
        val template = getTemplate("/convert/templates/MED - Perinatal history Summary.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))

        val contextStartDateTime = LocalDateTime.of(2012, 2, 1, 0, 1)
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("ctx/time", contextStartDateTime.toString())
            .put("ctx/category", "persistent")
            .put("ctx/history_origin", contextStartDateTime.toString())
            .put("perinatal_history/perinatal_history/apgar_score/a1_minute/total", "3")
            .put("perinatal_history/perinatal_history/apgar_score/a10_minute/total", "5")
            .put("perinatal_history/perinatal_history/maternal_pregnancy/labour_or_delivery/duration_of_labour|day", "1")
            .put("perinatal_history/perinatal_history/maternal_pregnancy/labour_or_delivery/duration_of_labour|hour", "2")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull
        val section = composition.content[0] as Section
        assertThat(section.name!!.value).isEqualTo("Perinatal history")

        val observation = section.items[1] as Observation
        assertThat(observation.name!!.value).isEqualTo("Apgar score")

        val history = observation.data
        val contextStartOffsetDateTime = contextStartDateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime()
        assertThat(history!!.origin!!.value).isEqualTo(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(contextStartOffsetDateTime))

        val firstEvent = history.events[0] as PointEvent
        val secondEvent = history.events[1] as PointEvent
        assertThat(JSR310ConversionUtils.toOffsetDateTime(firstEvent.time!!)).isEqualTo(JSR310ConversionUtils.toOffsetDateTime(history.origin!!).plusMinutes(1L))
        assertThat(JSR310ConversionUtils.toOffsetDateTime(secondEvent.time!!)).isEqualTo(JSR310ConversionUtils.toOffsetDateTime(history.origin!!).plusMinutes(10L))

        val firstData = firstEvent.data as ItemList?
        val firstValue = firstData!!.items[0].value as DvCount?
        assertThat(firstValue!!.magnitude).isEqualTo(3L)

        val secondData = secondEvent.data as ItemList?
        val secondValue = secondData!!.items[0].value as DvCount?
        assertThat(secondValue!!.magnitude).isEqualTo(5L)

        val formattedFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create(context.locale!!))
        assertThat(formattedFlatMap).contains(
            entry("perinatal_history/perinatal_history/apgar_score:0/a1_minute/total", "3"),
            entry("perinatal_history/perinatal_history/apgar_score:0/a10_minute/total", "5"),
            entry("perinatal_history/perinatal_history/maternal_pregnancy:0/labour_or_delivery:0/duration_of_labour|day", "1"),
            entry("perinatal_history/perinatal_history/maternal_pregnancy:0/labour_or_delivery:0/duration_of_labour|hour", "2"))

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("perinatal_history/perinatal_history/apgar_score:0/a1_minute/total", 3L),
            entry("perinatal_history/perinatal_history/apgar_score:0/a10_minute/total", 5L),
            entry("perinatal_history/perinatal_history/maternal_pregnancy:0/labour_or_delivery:0/duration_of_labour", Period(0, 0, 0, 1, 2, 0, 0, 0)))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondPerinatal() {
        val template = getTemplate("/convert/templates/MED - Perinatal history Summary.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("perinatal_history/perinatal_history/maternal_pregnancy/maternal_age|year", "33")
            .put("perinatal_history/perinatal_history/maternal_pregnancy/significant_family_history/family_issue", "at0461")
            .put("perinatal_history/perinatal_history/maternal_pregnancy/number_of_previous_pregnancies", "2")
            .put("perinatal_history/perinatal_history/maternal_pregnancy/gynaecological_history/gynaecological_issue", "at0469")
            .put("ctx/participation_mode:0", "face-to-face communication")
            .put("ctx/category", "persistent")
            .put("ctx/action_ism_transition_current_state", "initial")
            .put("ctx/id_schema", "local")
            .put("ctx/action_time", "now")
            .put("ctx/time", "now")
            .put("ctx/setting", "other care")
            .put("ctx/territory", "IE")
            .put("ctx/language", "en")
            .put("ctx/id_namespace", "local")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val section = composition.content[0] as Section
        assertThat(section.name!!.value).isEqualTo("Perinatal history")

        val observation = section.items[0] as Evaluation
        assertThat(observation.name!!.value).isEqualTo("Maternal pregnancy")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testInitialMedicationSafety() {
        val template = getTemplate("/convert/templates/MSE - Initial Medication Safety Report.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|code", "ac001")
            .put("initial_medication_safety_report/medication_safety_event/adverse_effect/reaction|value", "Value")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
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

        val map: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create(context.locale!!))
        assertThat(map).contains(
            entry("initial_medication_safety_report/medication_safety_event:0/adverse_effect:0/reaction|code", "ac001"),
            entry("initial_medication_safety_report/medication_safety_event:0/adverse_effect:0/reaction|value", "Value"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDocument() {
        val template = getTemplate("/convert/templates/MED - Document.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("medical_document/document/date_last_reviewed", "2012-12-01T10:17:00.000+01:00")
            .put("medical_document/document/content", "Hello world!")
            .put("medical_document/document/status", "at0007")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVitalsFixedValues() {
        val templateName = "/convert/templates/ZN - Vital Functions Encounter.opt"
        val template = getTemplate(templateName)
        buildAndExport(templateName, "vitals-noncompact", "sl", ImmutableSet.of("sl", "en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("vital_functions/respiratory_assessment/respiratory_examination/body_position_exercise")
        assertThat(node.dependsOn).isNotNull
        assertThat(node.dependsOn).isNotEmpty

        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("vital_functions/eye_assessment/eye_examination/pupils/left_side/estimated_size", "at0024")
            .put("vital_functions/eye_assessment/eye_examination/pupils/left_side/pupil_size|unit", "mm")
            .put("vital_functions/eye_assessment/eye_examination/pupils/right_side/estimated_size", "at0024")
            .put("vital_functions/eye_assessment/eye_examination/pupils/right_side/measured_size|unit", "mm")
            .put("vital_functions/glasgow_coma_scale/glasgow_coma_scale/best_eye_response_e", "at0013")
            .put("vital_functions/glasgow_coma_scale/glasgow_coma_scale/best_motor_response_m", "at0019")
            .put("vital_functions/glasgow_coma_scale/glasgow_coma_scale/best_verbal_response_-_adult_v", "at0014")
            .put("vital_functions/glasgow_coma_scale/glasgow_coma_scale/best_verbal_response_-_child_v", "at0.50")
            .put("vital_functions/glasgow_coma_scale/glasgow_coma_scale/best_verbal_response_-_infant_v", "at0.55")
            .put("vital_functions/respiratory_assessment/indirect_oximetry/spo2|denominator", "100")
            .put("vital_functions/respiratory_assessment/respirations/any_event/rate|unit", "/min")
            .put("vital_functions/respiratory_assessment/respiratory_examination/body_position_exercise", "at0060")
            .put("vital_functions/vital_signs/blood_pressure/any_event/diastolic|unit", "mm[Hg]")
            .put("vital_functions/vital_signs/blood_pressure/any_event/systolic|unit", "mm[Hg]")
            .put("vital_functions/vital_signs/body_mass_index:0/any_event:0/body_mass_index|unit", "kg/m2")
            .put("vital_functions/vital_signs/body_temperature:0/any_event:0/body_temperature|magnitude", "37")
            .put("vital_functions/vital_signs/body_temperature:0/any_event:0/body_temperature|unit", "°C")
            .put("vital_functions/vital_signs/body_temperature:0/any_event:0/symptoms", "at0.65")
            .put("vital_functions/vital_signs/body_temperature:0/location_of_measurement", "at0.60")
            .put("vital_functions/vital_signs/body_weight:0/any_event:0/body_weight|unit", "kg")
            .put("vital_functions/vital_signs/height_length:0/any_event:0/body_height_length|unit", "cm")
            .put("vital_functions/vital_signs/patient_state/patient_state/body_position", "at0016")
            .put("vital_functions/vital_signs/patient_state/patient_state/level_of_exertion:0/exercise_level", "at0008")
            .put("vital_functions/vital_signs/pulse/any_event/heart_rate|unit", "/min")
            .build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVitalsAqlPath() {
        val templateName = "/convert/templates/ZN - Vital Functions Encounter.opt"
        val template = getTemplate(templateName)
        val builderContext = WebTemplateBuilderContext("en", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val pathSegments = PathUtils.getPathSegments("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Vital signs']/items[openEHR-EHR-OBSERVATION.body_temperature-zn.v1,'Body temperature']/protocol[at0020,'protocol']/items[at0021.1,'Location of measurement']/value")

        val node: WebTemplateNode = webTemplate.findWebTemplateNodeByAqlPath(pathSegments)
        assertThat(node).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVitalsJson() {
        val templateName = "/convert/templates/ZN - Vital Functions Encounter.opt"
        val template = getTemplate(templateName)
        buildAndExport(templateName, "vitals-noncompact", "sl", ImmutableSet.of("sl", "en"))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("vital_functions/respiratory_assessment/respiratory_examination/body_position_exercise")
        assertThat(node.dependsOn).isNotNull
        assertThat(node.dependsOn).isNotEmpty

        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("vital_functions/vital_signs/blood_pressure/any_event/diastolic|magnitude", "90")
            .put("vital_functions/vital_signs/blood_pressure/any_event/diastolic|unit", "mm[Hg]")
            .put("vital_functions/vital_signs/blood_pressure/any_event/systolic|magnitude", "120")
            .put("vital_functions/vital_signs/blood_pressure/any_event/systolic|unit", "mm[Hg]")
            .put("vital_functions/vital_signs/body_mass_index:0/any_event:0/body_mass_index|unit", "kg/m2")
            .put("vital_functions/vital_signs/body_temperature:0/any_event:0/body_temperature|magnitude", "37")
            .put("vital_functions/vital_signs/body_temperature:0/any_event:0/body_temperature|unit", "°C")
            .put("vital_functions/vital_signs/body_temperature:0/any_event:0/symptoms", "at0.65")
            .put("vital_functions/vital_signs/body_temperature:0/location_of_measurement", "at0.60")
            .put("vital_functions/vital_signs/body_weight:0/any_event:0/body_weight|magnitude", "40")
            .put("vital_functions/vital_signs/body_weight:0/any_event:0/body_weight|unit", "kg")
            .put("vital_functions/vital_signs/height_length:0/any_event:0/body_height_length|magnitude", "70")
            .put("vital_functions/vital_signs/height_length:0/any_event:0/body_height_length|unit", "cm")
            .put("vital_functions/vital_signs/pulse/any_event/heart_rate|magnitude", "130")
            .put("vital_functions/vital_signs/pulse/any_event/heart_rate|unit", "/min")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull

        val structuredComposition: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(structuredComposition).isNotNull

        val tmpFile = File("/" + System.getProperty("java.io.tmpdir") + "/json")
        WebTemplateObjectMapper.getWriter(true).writeValue(tmpFile, structuredComposition)

        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(structuredComposition as ObjectNode, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSimpleBodyObservation() {
        val template = getTemplate("/convert/templates/Simple Body Observation2.xml")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("simple_body_observation/context/context_detail:0/period_of_care_identifier", "76024131")
            .put("simple_body_observation/context/setting|238", true)
            .put("simple_body_observation/context/setting|code", "238")
            .put("simple_body_observation/context/setting|terminology", "openehr")
            .put("simple_body_observation/context/setting|value", "other care")
            .put("simple_body_observation/context/start_time", "2013-10-07T17:09:35.472+02:00")
            .put("simple_body_observation/eye_examination/additional_description:0", "BP")
            .put("simple_body_observation/eye_examination/normal", "Normalen vid.")
            .put("simple_body_observation/eye_examination/time", "2013-10-07T17:09:35.472+02:00")
            .put("simple_body_observation/psychological_status/general_appearance/dizzy|at0233", true)
            .put("simple_body_observation/psychological_status/general_appearance/dizzy|code", "at0233")
            .put("simple_body_observation/psychological_status/general_appearance/dizzy|terminology", "local")
            .put("simple_body_observation/psychological_status/general_appearance/dizzy|value", "Da")
            .put("simple_body_observation/psychological_status/orientation/orientation_in_place|code", "at0013")
            .put("simple_body_observation/psychological_status/orientation/orientation_in_place|ordinal", 2)
            .put("simple_body_observation/psychological_status/orientation/orientation_in_place|value", "Slaba")
            .put("simple_body_observation/psychological_status/orientation/orientation_in_time|code", "at0009")
            .put("simple_body_observation/psychological_status/orientation/orientation_in_time|ordinal", 2)
            .put("simple_body_observation/psychological_status/orientation/orientation_in_time|value", "Slaba")
            .put("simple_body_observation/psychological_status/orientation/orientation_to_person|code", "at0017")
            .put("simple_body_observation/psychological_status/orientation/orientation_to_person|ordinal", 2)
            .put("simple_body_observation/psychological_status/orientation/orientation_to_person|value", "Slaba")
            .put("simple_body_observation/psychological_status/psychologgical_assessment_other", "Demenca")
            .put("simple_body_observation/psychological_status/speech/distinct", "Ni razločen.")
            .put("simple_body_observation/psychological_status/speech/indistinct", "Momljanje idr.")
            .put("simple_body_observation/psychological_status/speech/language|at0227", true)
            .put("simple_body_observation/psychological_status/speech/language|code", "at0227")
            .put("simple_body_observation/psychological_status/speech/language|terminology", "local")
            .put("simple_body_observation/psychological_status/speech/language|value", "slovenski")
            .put("simple_body_observation/psychological_status/thinking/confusion|at0218", true)
            .put("simple_body_observation/psychological_status/thinking/confusion|code", "at0218")
            .put("simple_body_observation/psychological_status/thinking/confusion|terminology", "local")
            .put("simple_body_observation/psychological_status/thinking/confusion|value", "Ne")
            .put("simple_body_observation/psychological_status/thinking/dementia|at0220", true)
            .put("simple_body_observation/psychological_status/thinking/dementia|code", "at0220")
            .put("simple_body_observation/psychological_status/thinking/dementia|terminology", "local")
            .put("simple_body_observation/psychological_status/thinking/dementia|value", "Da")
            .put("simple_body_observation/psychological_status/time", "2013-10-07T17:09:35.472+02:00")
            .put("simple_body_observation/simptomi_bolečine/pain/comments", "ggg")
            .put("simple_body_observation/simptomi_bolečine/pain/intensity/pain_scale_method|at0.0.201", true)
            .put("simple_body_observation/simptomi_bolečine/pain/intensity/pain_scale_method|code", "at0.0.201")
            .put("simple_body_observation/simptomi_bolečine/pain/intensity/pain_scale_method|terminology", "local")
            .put("simple_body_observation/simptomi_bolečine/pain/intensity/pain_scale_method|value", "6 obrazov")
            .put("simple_body_observation/simptomi_bolečine/pain/intensity/scale_score", 6)
            .put("simple_body_observation/simptomi_bolečine/time", "2013-10-07T17:09:35.472+02:00")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testActivityTiming() {
        val template = getTemplate("/convert/templates/ZN - Nursing careplan Encounter.xml")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val composition: Composition = getComposition("/convert/compositions/careplan_composition.xml")

        val root: JsonNode? = webTemplate.convertFromRawToStructured(composition, FromRawConversion.create())
        assertThat(root).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testInstructionLink() {
        val template = getTemplate("/convert/templates/Discharge Plan Encounter.xml")

        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)

        val link: String = webTemplate.getLinkPath("discharge_plan_encounter/discharge_plan/healthcare_service_request")
        assertThat(link).isEqualTo("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Discharge plan']/items[openEHR-EHR-INSTRUCTION.request-discharge_zn.v1,'Healthcare service request!']")

        val secondLink: String = webTemplate.getLinkPath("discharge_plan_encounter/discharge_plan/healthcare_service_request:1")
        assertThat(secondLink).isEqualTo("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Discharge plan']/items[openEHR-EHR-INSTRUCTION.request-discharge_zn.v1,'Healthcare service request! #2']")

        val thirdLink: String = webTemplate.getLinkPath("discharge_plan_encounter/discharge_plan/healthcare_service_request:1/request:2")
        assertThat(thirdLink).isEqualTo("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Discharge plan']/items[openEHR-EHR-INSTRUCTION.request-discharge_zn.v1,'Healthcare service request! #2']/activities[at0001,'Request #3']")

        val fourthLink: String = webTemplate.getLinkPath("discharge_plan_encounter:0/discharge_plan:0/healthcare_service_request:1/request:2")
        assertThat(fourthLink).isEqualTo("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Discharge plan']/items[openEHR-EHR-INSTRUCTION.request-discharge_zn.v1,'Healthcare service request! #2']/activities[at0001,'Request #3']")

        val fifthLink: String = webTemplate.getLinkPath("discharge_plan_encounter/discharge_plan/healthcare_service_request:1/request:2/discharge_intervention_service_requested")
        assertThat(fifthLink).isEqualTo("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Discharge plan']/items[openEHR-EHR-INSTRUCTION.request-discharge_zn.v1,'Healthcare service request! #2']/activities[at0001,'Request #3']/description[at0009,'Tree']/items[at0.204,'Discharge intervention service requested']")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOrder() {
        val template = getTemplate("/convert/templates/Discharge Activity Plan Encounter.xml")
        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .withComposerName("composer")
            .withIsmTransitionCurrentState("initial")
            .withInstructionNarrativeProvider { DvText("narrative") }
            .withActivityTimingProvider { DvParsable("R0", "timing") }
            .build()

        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readValue(getJson("/convert/compositions/careplan_activities.json"), ObjectNode::class.java), context)
        assertThat(composition).isNotNull

        val jsonNode: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(jsonNode!!.path("discharge_activity_plan_encounter")
                .path("discharge_activity_plan").path(0)
                .path("healthcare_service_request").path(4)
                .path("request").path(0)
                .path("discharge_intervention_service_requested").path(0)
                .path("|code").asText()).isEqualTo("100.05")
    }
}
