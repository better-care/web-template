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
import care.better.platform.web.template.converter.structured.exceptions.PathFormatException
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.collect.ImmutableList
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
class ArchetypeTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOrderedFailed() {
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate( "/convert/templates/IDCR - Cancer MDT Output Report.v0 ordered.xml"), WebTemplateBuilderContext("en"))
        val flatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/Cancer Output Report(1).json"), object : TypeReference<Map<String, Any?>>() {})

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(flatMap, context) }
            .isInstanceOf(PathFormatException::class.java)
            .hasMessage("Wrong format for path segment: problem_diagnosis_name:code.")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOrderedOK() {
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate( "/convert/templates/IDCR - Cancer MDT Output Report.v0 ordered.xml"), WebTemplateBuilderContext("en"))
        val flatMap: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/Cancer Output Report(2).json"), object : TypeReference<Map<String, Any?>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(flatMap, context)
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testNoArchetype() {
        val composition = getComposition("/convert/compositions/privantis.xml")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/DRS Fundus Severity for od form.xml"), WebTemplateBuilderContext("en", ImmutableList.of("en")))
        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(flatMap.size).isGreaterThan(2)
    }
}
