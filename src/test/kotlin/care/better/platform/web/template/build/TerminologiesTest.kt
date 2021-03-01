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
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class TerminologiesTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTerminologies() {
        val webTemplate: WebTemplate = getWebTemplate("/build/terminology.opt")
        val externalInTemplate: WebTemplateNode = webTemplate.tree.children[1].children[0]
        assertThat(externalInTemplate).isNotNull
        assertThat(externalInTemplate.rmType).isEqualTo("DV_CODED_TEXT")
        assertThat(externalInTemplate.nodeId).isEqualTo("at0002")
        assertThat(externalInTemplate.inputs).hasSize(2)
        val firstCodeInput: WebTemplateInput = externalInTemplate.getInput("code")!!
        assertThat(firstCodeInput).isNotNull
        assertThat(firstCodeInput.terminology).isEqualTo("icd10")
        val firstValueInput: WebTemplateInput = externalInTemplate.getInput("value")!!
        assertThat(firstValueInput).isNotNull
        assertThat(firstValueInput.terminology).isEqualTo("icd10")
        val externalInArchetype: WebTemplateNode = webTemplate.tree.children[1].children[1]
        assertThat(externalInArchetype).isNotNull
        assertThat(externalInArchetype.rmType).isEqualTo("DV_CODED_TEXT")
        assertThat(externalInArchetype.nodeId).isEqualTo("at0003")
        assertThat(externalInArchetype.inputs).hasSize(2)
        val secondCodeInput: WebTemplateInput = externalInArchetype.getInput("code")!!
        assertThat(secondCodeInput).isNotNull
        assertThat(secondCodeInput.terminology).isEqualTo("LOINC")
        val secondValueInput: WebTemplateInput = externalInArchetype.getInput("value")!!
        assertThat(secondValueInput).isNotNull
        assertThat(secondValueInput.terminology).isEqualTo("LOINC")
        val internal: WebTemplateNode = webTemplate.tree.children[1].children[2]
        assertThat(internal).isNotNull
        assertThat(internal.rmType).isEqualTo("DV_CODED_TEXT")
        assertThat(internal.nodeId).isEqualTo("at0004")
        assertThat(internal.inputs).hasSize(1)
        val thirdCodeInput: WebTemplateInput = internal.getInput("code")!!
        assertThat(thirdCodeInput).isNotNull
        assertThat(thirdCodeInput.list).isNotEmpty
        assertThat(thirdCodeInput.list[0].value).isEqualTo("at0006")
        assertThat(thirdCodeInput.list[0].label).isEqualTo("one")
        val externalInTemplateWithValueSet: WebTemplateNode = webTemplate.tree.children[1].children[3]
        assertThat(externalInTemplateWithValueSet).isNotNull
        assertThat(externalInTemplateWithValueSet.rmType).isEqualTo("DV_CODED_TEXT")
        assertThat(externalInTemplateWithValueSet.nodeId).isEqualTo("at0005")
        assertThat(externalInTemplateWithValueSet.inputs).hasSize(1)
        val fourthCodeInput: WebTemplateInput = externalInTemplateWithValueSet.getInput("code")!!
        assertThat(fourthCodeInput).isNotNull
        assertThat(fourthCodeInput.list).isNotEmpty
        assertThat(fourthCodeInput.list[0].value).isEqualTo("m")
        assertThat(fourthCodeInput.list[0].label).isEqualTo("male")
    }
}
