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
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class BuilderTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCodedText() {
        val templateName = "/build/CheckList.opt"
        val template = getTemplate(templateName)

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        assertThat(webTemplate).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testWebTemplateNodesAreNotEquals() {
        val template = getTemplate("/build/ICU - Ventilator device Report3.opt")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en")))
        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/peak_airway_pressure_p_peak")
        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("ventilator_device_report/nbp840/nbp840_observtions/ventilator_findings/plateau_airway_pressure_p_plateau")
        assertThat(firstNode.path).isNotEqualTo(secondNode.path)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCardinalities() {
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/build/openEHR-EHR-COMPOSITION.t_otolaryngologist_examination_lanit.v1.opt"), builderContext)
        assertThat(webTemplate.tree.children[0].cardinalities).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLocalizedNames() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/build/ZN - Assessment Scales Encounter.opt"), builderContext)
        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("assessment_scales/pain_assessment/story/pain/relieving_factor")
        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("assessment_scales/pain_assessment/story/pain/exascerbating_factor")
        assertThat(firstNode.localizedNames["sl"]).isEqualTo("Bolečino ublaži")
        assertThat(secondNode.localizedNames["sl"]).isEqualTo("Bolečino poslabša")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOccurrences() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/build/template-selenium-all-fields.opt"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/all_fields/any_event/navaden_text")
        assertThat(node.occurences?.min).isEqualTo(10)
    }
}
