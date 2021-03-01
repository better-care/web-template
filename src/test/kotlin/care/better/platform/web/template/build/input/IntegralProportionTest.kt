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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class IntegralProportionTest : AbstractWebTemplateTest() {

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIntegral1() {
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/build/input/Testing Template N2.opt"), WebTemplateBuilderContext("en"))
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing/testing/proportion_with_integral")
        assertThat(node.inputs[0].type).isEqualTo(WebTemplateInputType.INTEGER)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondIntegral() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/build/input/Testing Template N3.opt"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("test_encounter/testing/testing/proportion_no_integral")
        assertThat(node.inputs[0].type).isNotEqualTo(WebTemplateInputType.INTEGER)
    }
}
