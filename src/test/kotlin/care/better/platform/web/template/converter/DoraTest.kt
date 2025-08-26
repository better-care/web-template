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
import com.fasterxml.jackson.databind.JsonNode
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.PartyRef
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.composition.Composition
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DoraTest : AbstractWebTemplateTest() {

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOccurencesBug() {
        val builderContext = WebTemplateBuilderContext("en")
        val templateName = "/convert/templates/Breast - Radiographer Mammography Report.xml"
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate(templateName), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "radiographer_mammography_report/context/report_name" to "at0.0.19",
                "radiographer_mammography_report/context/episode_code" to "30-03052010",
                "radiographer_mammography_report/context/round_number" to "1",
                "radiographer_mammography_report/context/start_time" to "2010-05-03T13:59:23.000+02:00",
                "radiographer_mammography_report/context/report_id" to "SCR_7505",
                "radiographer_mammography_report/procedure_details/procedure_details/examination_request_details/accession_number" to "",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/imaging_quality" to "at0012",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/completion_status" to "at0014",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/number_of_images" to "4",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/repeat_image_status" to "at0005",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/number_of_repeat_images" to "0",
                "radiographer_mammography_report/procedure_details/procedure_details/time" to "2010-05-03T09:49:54.000+02:00",
                "radiographer_mammography_report/procedure_details/procedure_details/ism_transition/current_state" to "526",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/location" to "dora",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/time" to "2008-01-01T00:00:00.000+01:00",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/comments" to "",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/intervention" to "at0.68",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/ism_transition/current_state" to "524",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:0/ism_transition/current_state" to "524",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:0/breast_location/specific_location/side" to "at0003",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:1/ism_transition/current_state" to "524",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:1/breast_location/specific_location/side" to "at0004",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/breast_location/specific_location/side" to "at0003",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/examination_name:0" to "at0.30",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/examination_name:1" to "at0.31",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/ism_transition/current_state" to "526",
                "radiographer_mammography_report/examination_findings:0/breast_exam_screen/breast_findings/breast_location/specific_location/side" to "at0003",
                "radiographer_mammography_report/examination_findings:0/breast_exam_screen/breast_findings/breast_finding:0" to "at0010",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/breast_location/specific_location/side" to "at0004",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/examination_name:0" to "at0.30",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/examination_name:1" to "at0.31",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/ism_transition/current_state" to "526",
                "radiographer_mammography_report/examination_findings:1/breast_exam_screen/breast_findings/breast_location/specific_location/side" to "at0004",
                "radiographer_mammography_report/examination_findings:1/breast_exam_screen/breast_findings/breast_finding:0" to "at0010"
            ),
            context
        )

        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(
            node!!.path("radiographer_mammography_report")
                .path("past_therapies").path(0)
                .path("breast-related_interventions_tmds").path(0)
                .path("ism_transition").path(0)
                .path("current_state").path(0).path("|code")
                .asText()
        ).isEqualTo("524")

        assertThat(
            node.path("radiographer_mammography_report")
                .path("past_procedures").path(0)
                .path("breast-related_interventions_tmds").path(1)
                .path("ism_transition").path(0)
                .path("current_state").path(0).path("|code")
                .asText()
        ).isEqualTo("524")

        assertThat(
            node.path("radiographer_mammography_report")
                .path("procedure_details").path(0)
                .path("procedure_details").path(0)
                .path("ism_transition").path(0)
                .path("current_state").path(0).path("|code")
                .asText()
        ).isEqualTo("526")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIsmTransitionWtNode() {
        val builderContext = WebTemplateBuilderContext("en")
        val templateName = "/convert/templates/Breast - Radiographer Mammography Report.xml"
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate(templateName), builderContext)

        val partyIdentified = PartyIdentified.forName("name").apply {
            this.externalRef = PartyRef().apply {
                this.id = GenericId().apply {
                    this.scheme = "dora"
                    this.value = "1234"
                }
                this.namespace = "dora"
                this.type = "PERSON"
            }
        }

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposer(partyIdentified).build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "radiographer_mammography_report/context/report_name" to "at0.0.19",
                "radiographer_mammography_report/context/episode_code" to "30-03052010",
                "radiographer_mammography_report/context/round_number" to "1",
                "radiographer_mammography_report/context/start_time" to "2010-05-03T13:59:23.000+02:00",
                "radiographer_mammography_report/context/report_id" to "SCR_7505",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/imaging_quality" to "at0012",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/completion_status" to "at0014",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/number_of_images" to "4",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/repeat_image_status" to "at0005",
                "radiographer_mammography_report/procedure_details/procedure_details/mammography_procedure_details/number_of_repeat_images" to "0",
                "radiographer_mammography_report/procedure_details/procedure_details/time" to "2010-05-03T09:49:54.000+02:00",
                "radiographer_mammography_report/procedure_details/procedure_details/ism_transition/current_state" to "526",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/location" to "dora",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/time" to "2008-01-01T00:00:00.000+01:00",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/comments" to "",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/intervention" to "at0.68",
                "radiographer_mammography_report/past_therapies/breast-related_interventions_tmds/ism_transition/current_state" to "524",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:0/ism_transition/current_state" to "524",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:0/breast_location/specific_location/side" to "at0003",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:1/ism_transition/current_state" to "524",
                "radiographer_mammography_report/past_procedures/breast-related_interventions_tmds:1/breast_location/specific_location/side" to "at0004",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/breast_location/specific_location/side" to "at0003",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/examination_name:0" to "at0.30",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/examination_name:1" to "at0.31",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:0/ism_transition/current_state" to "526",
                "radiographer_mammography_report/examination_findings:0/breast_exam_screen/breast_findings/breast_location/specific_location/side" to "at0003",
                "radiographer_mammography_report/examination_findings:0/breast_exam_screen/breast_findings/breast_finding:0" to "at0010",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/breast_location/specific_location/side" to "at0004",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/examination_name:0" to "at0.30",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/examination_name:1" to "at0.31",
                "radiographer_mammography_report/mammography_procedures/imaging_examination_dora:1/ism_transition/current_state" to "526",
                "radiographer_mammography_report/examination_findings:1/breast_exam_screen/breast_findings/breast_location/specific_location/side" to "at0004",
                "radiographer_mammography_report/examination_findings:1/breast_exam_screen/breast_findings/breast_finding:0" to "at0010"
            ), context
        )

        val node: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        assertThat(
            node!!.path("radiographer_mammography_report")
                .path("past_therapies").path(0)
                .path("breast-related_interventions_tmds").path(0)
                .path("ism_transition").path(0)
                .path("current_state").path(0).path("|code")
                .asText()
        ).isEqualTo("524")

        assertThat(
            node.path("radiographer_mammography_report")
                .path("past_procedures").path(0)
                .path("breast-related_interventions_tmds").path(1)
                .path("ism_transition").path(0)
                .path("current_state").path(0).path("|code")
                .asText()
        ).isEqualTo("524")

        assertThat(
            node.path("radiographer_mammography_report")
                .path("procedure_details").path(0)
                .path("procedure_details").path(0)
                .path("ism_transition").path(0)
                .path("current_state").path(0).path("|code")
                .asText()
        ).isEqualTo("526")
    }
}
