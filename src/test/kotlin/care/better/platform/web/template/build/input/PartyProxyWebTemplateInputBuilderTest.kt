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

package care.better.platform.web.template.build.input

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class PartyProxyWebTemplateInputBuilderTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testComposer() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/Testing.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/composer")

        assertThat(webTemplateNode.inputs).hasSize(4)

        assertIdInput(webTemplateNode)
        assertIdSchemeInput(webTemplateNode)
        assertIdNamespaceInput(webTemplateNode)
        assertNameInput(webTemplateNode)
    }

    @Test
    fun testPartyProxy() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/TestingParty.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("info/testing_party_proxy/subject")

        assertThat(webTemplateNode.inputs).hasSize(4)

        assertIdInput(webTemplateNode)
        assertIdSchemeInput(webTemplateNode)
        assertIdNamespaceInput(webTemplateNode)
        assertNameInput(webTemplateNode)
    }

    @Test
    fun testPartyIdentified() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/TestingParty.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("info/testing_party_identified/subject")

        assertThat(webTemplateNode.inputs).hasSize(4)

        assertIdInput(webTemplateNode)
        assertIdSchemeInput(webTemplateNode)
        assertIdNamespaceInput(webTemplateNode)
        assertNameInput(webTemplateNode)
    }

    @Test
    fun testPartySelf() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/TestingParty.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("info/testing_party_self/subject")

        assertThat(webTemplateNode.inputs).isEmpty()
    }

    @Test
    fun testPartyRelated() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/TestingParty.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("info/testing_party_related/subject")

        assertThat(webTemplateNode.inputs).isEmpty()
    }

    private fun assertIdInput(webTemplateNode: WebTemplateNode) {
        val idInput: WebTemplateInput = webTemplateNode.inputs[0]
        assertThat(idInput.suffix).isEqualTo("id")
        assertThat(idInput.type).isEqualTo(WebTemplateInputType.TEXT)
    }

    private fun assertIdSchemeInput(webTemplateNode: WebTemplateNode) {
        val idSchemeInput: WebTemplateInput = webTemplateNode.inputs[1]
        assertThat(idSchemeInput.suffix).isEqualTo("id_scheme")
        assertThat(idSchemeInput.type).isEqualTo(WebTemplateInputType.TEXT)
    }

    private fun assertIdNamespaceInput(webTemplateNode: WebTemplateNode) {
        val idNamespaceInput: WebTemplateInput = webTemplateNode.inputs[2]
        assertThat(idNamespaceInput.suffix).isEqualTo("id_namespace")
        assertThat(idNamespaceInput.type).isEqualTo(WebTemplateInputType.TEXT)
    }

    private fun assertNameInput(webTemplateNode: WebTemplateNode) {
        val nameInput: WebTemplateInput = webTemplateNode.inputs[3]
        assertThat(nameInput.suffix).isEqualTo("name")
        assertThat(nameInput.type).isEqualTo(WebTemplateInputType.TEXT)
    }
}
