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
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.converter.exceptions.ConversionException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class EmptynessTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testEmptyComposition() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_allergist_examination_child_lanit.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("Composer").build()
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(emptyMap(), builderContext) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("COMPOSITION has no attribute осмотр_аллерголога-иммунолога.")

    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testEmptyEvaluation() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_specialist_examination.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru", ImmutableList.of("ru")))
        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("Composer").build()
        val objectMapper = ObjectMapper()
        val node = objectMapper.readTree(getJson("/convert/compositions/emptyEvaluation.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, builderContext)
        assertThat(composition).isNotNull
        assertThat(composition!!.content).hasSize(1)
        assertThat(composition.content[0].name!!.value).isEqualTo("Жалобы и анамнез заболевания")
    }
}
