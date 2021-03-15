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
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class WebTemplateDescriptionTest : AbstractWebTemplateTest() {

    @Test
    fun testWebTemplateHasDescriptions() {
        val template = getTemplate("/build/Demo Vitals.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))

        assertThat(webTemplate.tree.localizedDescriptions["en"]).isEqualTo("Generic encounter or progress note composition")

        val siteOfMeasurement: WebTemplateNode = webTemplate.findWebTemplateNode("vitals/vitals/body_temperature/site_of_measurement")
        assertThat(siteOfMeasurement.getInput()!!.list[0].localizedDescriptions["en"]).isEqualTo("The temperature was measured at the buccal mucosa.")
    }

    @Test
    fun testWebTemplateNotHasDescriptions() {
        val template = getTemplate("/build/Demo Vitals.opt")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"), null, false))

        assertThat(webTemplate.tree.localizedDescriptions).isEmpty()

        val siteOfMeasurement: WebTemplateNode = webTemplate.findWebTemplateNode("vitals/vitals/body_temperature/site_of_measurement")
        assertThat(siteOfMeasurement.getInput()!!.list[0].localizedDescriptions).isEmpty()
    }
}
