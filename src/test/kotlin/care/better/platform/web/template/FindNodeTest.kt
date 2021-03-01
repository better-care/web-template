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

package care.better.platform.web.template

import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.exception.UnknownPathBuilderException
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class FindNodeTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCdaDocument() {
        val templateName = "/CDA Document.opt"
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate(templateName), WebTemplateBuilderContext("sl"))
        assertThat(webTemplate.findWebTemplateNode("cda_document/cda_component:0")).isNotNull
        assertThat(webTemplate.findWebTemplateNode("cda_document/cda_component:199")).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFindNodeByAqlPath() {
        val template = getTemplate("/DogAPTrace-annot.opt")
        assertThat(template).isNotNull
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        assertThat(webTemplate).isNotNull
        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNodeByAqlPath("/content[openEHR-EHR-OBSERVATION.ap_clamp.v9]/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value")
        assertThat(firstNode).isNotNull
        assertThat(firstNode.nodeId).isEqualTo("at0004")
        assertThat(firstNode.jsonId).isEqualTo("action_potential")
        assertThat(firstNode.rmType).isEqualTo("DV_QUANTITY")
        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNodeByAqlPath("/content[openEHR-EHR-OBSERVATION.ap_clamp.v9]/data[at0001]/events[at0002]/data[at0003]/items[at0004]")
        assertThat(secondNode).isNotNull
        assertThat(secondNode.nodeId).isEqualTo(firstNode.nodeId)
        assertThat(secondNode.jsonId).isEqualTo(firstNode.jsonId)
        assertThat(secondNode.rmType).isEqualTo(firstNode.rmType)
        assertThatThrownBy { webTemplate.findWebTemplateNodeByAqlPath("/content[openEHR-EHR-OBSERVATION.ap_clamp.v9]/data[at0001]/events[at0002]/data[at0003]/items[at1004]") }
            .isInstanceOf(UnknownPathBuilderException::class.java)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFindNodeById() {
        val template = getTemplate("/DogAPTrace-annot.opt")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("experiment/ap_clamp/any_event/measurement_time")
        assertThat(webTemplateNode).isNotNull
        assertThat(webTemplateNode.nodeId).isEqualTo("at0005")
        assertThat(webTemplateNode.jsonId).isEqualTo("measurement_time")
        assertThat(webTemplateNode.rmType).isEqualTo("DV_QUANTITY")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFindNodeByAqlPathRelative() {
        val template = getTemplate("/Vital Signs.xml")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val hits: List<WebTemplateNode> = webTemplate.findWebTemplateNodesByAqlPath(
            "openEHR-EHR-OBSERVATION.blood_pressure.v1",
            "/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value")
        assertThat(hits).hasSize(1)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFindNodeByIdNonExisting() {
        val template = getTemplate("/DogAPTrace-annot.opt")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        assertThatThrownBy { webTemplate.findWebTemplateNode("experiment/ap_clamp/any_event/non_existing") }
            .isInstanceOf(UnknownPathBuilderException::class.java)
    }
}
