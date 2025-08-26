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
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.common.PartySelf
import org.openehr.rm.composition.Composition
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ComposerTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testComposer() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "ctx/composer_id" to "1191",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            ConversionContext.create().build()
        )
        assertThat(composition!!.composer?.externalRef?.id?.value).isEqualTo("1191")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testComposerSelf() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_self" to "true",
                "ctx/composer_id" to "1191",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            ConversionContext.create().build()
        )

        assertThat(composition!!.composer?.externalRef?.id?.value).isEqualTo("1191")
        assertThat(composition.composer).isInstanceOf(PartySelf::class.java)
    }
}
