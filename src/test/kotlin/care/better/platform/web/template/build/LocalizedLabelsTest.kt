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

package care.better.platform.web.template.build

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 */
class LocalizedLabelsTest : AbstractWebTemplateTest() {
    @Test
    fun testBuildingLocalTerminologyDvCodedTextLocalizedLabels() {
        val template = getTemplate("/build/KDS_Biobank.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", languages = listOf("en", "de")))
        val node = webTemplate.findWebTemplateNodeByAqlPath("content[openEHR-EHR-EVALUATION.biospecimen_summary.v0]/data[at0001]/items[openEHR-EHR-CLUSTER.specimen.v1]/items[at0097]/value")
        assertThat(node).isNotNull
        node.inputs[0].list.forEach {
            assertThat(it.localizedLabels["en"]).isNotBlank()
        }
    }

    @Test
    fun testBuildingNotEmptyDvCodedText() {
        val template = getTemplate("/build/UCC_App_Fragebogen_Daten_TEMPLATE.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("de", languages = listOf("de")))
        val node = webTemplate.findWebTemplateNodeByAqlPath("content[openEHR-EHR-OBSERVATION.questionnaire_entry.v0,'Gesamtergebnis']/data[at0001]/events[at0002]/data[at0003]/items[openEHR-EHR-CLUSTER.questionnaire_item.v0,'Gesamtstatus']/items[openEHR-EHR-CLUSTER.questionnaire_item.v0,'Frage 8']/items[openEHR-EHR-CLUSTER.questionnaire_item.v0,'Frage 8b']/items[at0003]/value")
        assertThat(node).isNotNull
        node.inputs[0].list.forEach {
            assertThat(it.localizedLabels["de"]).isNotBlank()
        }

    }
}