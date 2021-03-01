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
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class TermBindingsTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTermBindings() {
        val webTemplate: WebTemplate = getWebTemplate("/DogAPTrace-annot.opt")
        assertThat(webTemplate).isNotNull
        val eventNode: WebTemplateNode = webTemplate.tree.children[1].children[0]
        assertThat(eventNode).isNotNull
        assertThat(eventNode.rmType).isEqualTo("EVENT")
        assertThat(eventNode.nodeId).isEqualTo("at0002")
        assertThat(eventNode.children).hasSize(3)
        assertThat(eventNode.termBindings).isEmpty()
        val actionPotential: WebTemplateNode = eventNode.children[0]
        assertThat(actionPotential).isNotNull
        assertThat(actionPotential.rmType).isEqualTo("DV_QUANTITY")
        assertThat(actionPotential.nodeId).isEqualTo("at0004")
        assertThat(actionPotential.termBindings).hasSize(1)
        assertThat(actionPotential.termBindings.containsKey("MTH")).isTrue
        assertThat(actionPotential.termBindings["MTH"]?.value).isEqualTo("123456")
        assertThat(actionPotential.termBindings["MTH"]?.terminologyId).isEqualTo("MTH(2016)")
        val measurement: WebTemplateNode = eventNode.children[1]
        assertThat(measurement).isNotNull
        assertThat(measurement.rmType).isEqualTo("DV_QUANTITY")
        assertThat(measurement.nodeId).isEqualTo("at0005")
        assertThat(measurement.termBindings).hasSize(1)
        assertThat(measurement.termBindings.containsKey("MTH")).isTrue
        assertThat(measurement.termBindings["MTH"]?.value).isEqualTo("654321")
        assertThat(measurement.termBindings["MTH"]?.terminologyId).isEqualTo("MTH(2016)")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTermBindingMissing() {
        val webTemplate: WebTemplate = getWebTemplate("/build/KorayClinical3.opt")
        val node: WebTemplateNode = webTemplate.findWebTemplateNodeByAqlPath("/content[openEHR-EHR-OBSERVATION.blood_pressure.v1]/data[at0001]/events[at0006]/state[at0007]/items[openEHR-EHR-CLUSTER.level_of_exertion.v1]/items[at0009]/value")
        val codedValues: List<WebTemplateCodedValue> = node.getInput()?.list ?: emptyList()
        assertThat(codedValues[0].termBindings).hasSize(1)
        assertThat(codedValues[0].termBindings["SNOMED-CT"]?.terminologyId).isEqualTo("SNOMED-CT")
        assertThat(codedValues[0].termBindings["SNOMED-CT"]?.value).isEqualTo("128975004")
        assertThat(codedValues[1].termBindings).hasSize(1)
        assertThat(codedValues[1].termBindings["SNOMED-CT"]?.terminologyId).isEqualTo("SNOMED-CT")
        assertThat(codedValues[1].termBindings["SNOMED-CT"]?.value).isEqualTo("128976003")
        assertThat(codedValues[2].termBindings).hasSize(1)
        assertThat(codedValues[2].termBindings["SNOMED-CT"]?.terminologyId).isEqualTo("SNOMED-CT")
        assertThat(codedValues[2].termBindings["SNOMED-CT"]?.value).isEqualTo("128978002")
    }
}
