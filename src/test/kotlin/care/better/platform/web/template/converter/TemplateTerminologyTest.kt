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

package care.better.platform.web.template.converter

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition

/**
 * @author Bostjan Lah
 * @since 3.1.0
 */
class TemplateTerminologyTest : AbstractWebTemplateTest() {
    @Test
    fun testTemplateTerminologyWithValue() {
        val webTemplate: WebTemplate = getWebTemplate()
        val values = getObjectMapper().readValue<Map<String, Any>>(getJson("/convert/compositions/template-terminology-with-value.json"))
        assertThat(values).isNotEmpty

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())
        assertThat(composition).isNotNull

        val flat = webTemplate.convertFromRawToFlat(composition!!)
        assertThat(flat).containsEntry("template_terminology/body_weight/any_event:0/confounding_factors:0|value", "Custom value")
    }

    @Test
    fun testTemplateTerminologyWithoutValue() {
        val webTemplate: WebTemplate = getWebTemplate()
        val values = getObjectMapper().readValue<Map<String, Any>>(getJson("/convert/compositions/template-terminology-without-value.json"))
        assertThat(values).isNotEmpty

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())
        assertThat(composition).isNotNull

        val flat = webTemplate.convertFromRawToFlat(composition!!)
        assertThat(flat).containsEntry("template_terminology/body_weight/any_event:0/confounding_factors:0|value", "Unknown")
    }

    private fun getWebTemplate(): WebTemplate {
        val builderContext = WebTemplateBuilderContext("en")
        return WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Template Terminology.opt"), builderContext)
    }
}
