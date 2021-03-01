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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class OceanTemplateDesignerSpecialPathsTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOptInvalidPath27() {
        val webTemplate: WebTemplate = getWebTemplate("/build/parser_test2.7.opt")
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/glasgow_coma_scale/examination_of_a_pupil/clinical_description")
        assertThat(node.annotations["view:pass_through"]).isEqualTo("true")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOptInvalidPath28() {
        val webTemplate: WebTemplate = getWebTemplate("/build/parser_test2.8.opt")
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("encounter/glasgow_coma_scale/examination_of_a_pupil/clinical_description")
        assertThat(node.annotations["view:pass_through"]).isEqualTo("true")
    }
}
