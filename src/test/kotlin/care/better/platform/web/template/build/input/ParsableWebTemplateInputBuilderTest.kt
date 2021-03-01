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
class ParsableWebTemplateInputBuilderTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTwoInputs() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/Testing.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/parsable")

        assertThat(webTemplateNode.inputs).hasSize(2)
        assertValueInput(webTemplateNode, null)
        assertFormalismInput(webTemplateNode, null)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDefaultValues() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/TestingParsable.opt")

        val webTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/parsable")

        assertThat(webTemplateNode.inputs).hasSize(2)
        assertValueInput(webTemplateNode, "Hello world!")
        assertFormalismInput(webTemplateNode, "text/plain")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testFormalismOptions() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("en")
        val template = getTemplate("/build/input/TestingParsable.opt")

        val webTemplate = WebTemplateBuilder.buildNonNull(template, webTemplateBuilderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/testing/parsable")

        assertThat(webTemplateNode.inputs).hasSize(2)
        val formalismInput: WebTemplateInput = webTemplateNode.inputs[1]

        assertThat(formalismInput.suffix).isEqualTo("formalism")
        assertThat(formalismInput.list.map { it.value }).containsExactlyInAnyOrder("text/xml", "text/html", "text/rtf")
        assertThat(formalismInput.list.map { it.label }).containsExactlyInAnyOrder("text/xml", "text/html", "text/rtf")
    }

    private fun assertValueInput(node: WebTemplateNode, defaultValue: Any?) {
        val valueInput: WebTemplateInput = node.inputs[0]
        assertThat(valueInput.suffix).isEqualTo("value")
        assertThat(valueInput.type).isEqualTo(WebTemplateInputType.TEXT)
        assertThat(valueInput.defaultValue).isEqualTo(defaultValue)
    }

    private fun assertFormalismInput(node: WebTemplateNode, defaultValue: Any?) {
        val formalismInput: WebTemplateInput = node.inputs[1]
        assertThat(formalismInput.suffix).isEqualTo("formalism")
        assertThat(formalismInput.type).isEqualTo(WebTemplateInputType.TEXT)
        assertThat(formalismInput.defaultValue).isEqualTo(defaultValue)
    }
}
