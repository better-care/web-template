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
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ParentTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testConvertToFlat() {
        val template = getTemplate("/convert/templates/RES Primary Hip Arthroplasty Report.xml")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", ImmutableList.of("en")))

        val context = ConversionContext.create().withLanguage("pt").withTerritory("US").withComposerName("Compoeser").build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(
            getObjectMapper().readTree(getJson("/convert/compositions/Primary Hip Arthroplasty Report.json")) as ObjectNode,
            context)

        assertThat(composition).isNotNull
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }
}
