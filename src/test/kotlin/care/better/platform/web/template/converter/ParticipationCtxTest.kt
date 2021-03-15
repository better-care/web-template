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
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
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
class ParticipationCtxTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInvalidParticipation() {
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/Visual Acuity Report.opt"),
            WebTemplateBuilderContext("en"))

        val flatMap: Map<String, Any?> =
            getObjectMapper().readValue(getJson("/convert/compositions/Participation Ctx Composition(1).json"), object : TypeReference<Map<String, Any?>>() {})

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(flatMap, ConversionContext.create().build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Unknown participation mode: 'videoconference'")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultipleParticipationsMissingScheme() {
        val webTemplate: WebTemplate =
            WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR - Cancer MDT Output Report.v0.xml"), WebTemplateBuilderContext("en"))
        val flatMap: Map<String, Any?> =
            getObjectMapper().readValue(getJson("/convert/compositions/Participation Ctx Composition(2).json"), object : TypeReference<Map<String, Any?>>() {})

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(flatMap, ConversionContext.create().withComposerName("Composer").build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Parameters 'id_scheme' and 'id_namespace' are required when using participations with ids!")

    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultipleParticipations() {
        val webTemplate: WebTemplate =
            WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/IDCR - Cancer MDT Output Report.v0.xml"), WebTemplateBuilderContext("en"))
        val flatMap: Map<String, Any?> =
            getObjectMapper().readValue(getJson("/convert/compositions/Participation Ctx Composition(3).json"), object : TypeReference<Map<String, Any?>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, ConversionContext.create().withComposerName("Composer").build())
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }
}
