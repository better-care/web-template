/* Copyright 2024 Better Ltd (www.better.care)
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
import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 * @since 4.3.7
 */
class ExternalTerminologyLimitToListTest : AbstractWebTemplateTest() {

    @Test
    fun testExternalValueSetWithLimitToListFalse() {
        val webTemplate: WebTemplate = getWebTemplate("/build/terminology-limit-to-list.opt")
        val node: WebTemplateNode = webTemplate.tree.children[0].children[0]

        assertThat(node.rmType).isEqualTo("DV_CODED_TEXT")
        assertThat(node.nodeId).isEqualTo("at0002")
        assertThat(node.inputs).hasSize(3)

        val codeInput = node.getInput("code")!!
        assertThat(codeInput.type).isEqualTo(WebTemplateInputType.TEXT)
        assertThat(codeInput.terminology).isEqualTo("icd10")
        assertThat(codeInput.listOpen).isTrue

        val valueInput = node.getInput("value")!!
        assertThat(valueInput.type).isEqualTo(WebTemplateInputType.TEXT)
        assertThat(valueInput.terminology).isEqualTo("icd10")

        val otherInput = node.getInput("other")!!
        assertThat(otherInput.type).isEqualTo(WebTemplateInputType.TEXT)
        assertThat(otherInput.terminology).isNull()
    }
}
