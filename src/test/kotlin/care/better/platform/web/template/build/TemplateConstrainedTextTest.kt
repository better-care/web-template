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
import care.better.platform.web.template.builder.WebTemplateBuilder
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
class TemplateConstrainedTextTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testConstrainedText() {
        val template = getTemplate("/build/Testing Template Terminology.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("testing_template/terminologyid/problem_diagnosis")
        val codeInput: WebTemplateInput = node.inputs[0]
        assertThat(codeInput.terminology).isEqualTo("ICD10?subset=ICD10&language=en-GB")
        val valueInput: WebTemplateInput = node.inputs[1]
        assertThat(valueInput.terminology).isEqualTo("ICD10?subset=ICD10&language=en-GB")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testSecondConstrainedText() {
        val template = getTemplate("/build/Testing Template Terminology.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("testing_template/terminologyvalues/problem_diagnosis")
        val codeInput: WebTemplateInput = node.inputs[0]
        assertThat(codeInput.terminology).isEqualTo("dips#ark-sklvproblem")
        assertThat(codeInput.list).isNotEmpty
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testThirdConstrainedText() {
        val template = getTemplate("/build/Testing Template Terminology.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("testing_template/test3/problem_diagnosis")
        val codeInput: WebTemplateInput = node.inputs[0]
        assertThat(codeInput.terminology).isEqualTo("ICD10")
        assertThat(codeInput.list).isNotEmpty
    }
}
