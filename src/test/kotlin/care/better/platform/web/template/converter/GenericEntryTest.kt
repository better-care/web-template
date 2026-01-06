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
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openehr.rm.composition.Composition

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenericEntryTest : AbstractWebTemplateTest() {

    private lateinit var webTemplate: WebTemplate

    @BeforeAll
    fun loadWebTemplate() {
        val builderContext = WebTemplateBuilderContext("en")
        webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/generic_entry.opt"), builderContext)
        assertThat(webTemplate).isNotNull
    }

    @Test
    fun testGenericEntryFlat() {
        val flatMap = getObjectMapper().readValue(
                getJson("/convert/compositions/generic_entry-flat.json"),
                object : TypeReference<Map<String, Any?>>() {})

        val composition = webTemplate.convertFromFlatToRaw<Composition>(flatMap, ConversionContext.create().build())!!

        val compositionFlatMap = webTemplate.convertFromRawToFlat(composition)

        assertThat(compositionFlatMap)
            .isNotEmpty()
            .containsEntry("composition_with_generic_entry/mojgeneric/mojtekst", "MojTekst 72")
    }

    @Test
    fun testGenericEntryStructured() {
        val structuredMap = getObjectMapper().readTree(
                getJson("/convert/compositions/generic_entry-structured.json")) as ObjectNode

        val composition = webTemplate.convertFromStructuredToRaw<Composition>(structuredMap, ConversionContext.create().build())!!

        val compositionFlatMap = webTemplate.convertFromRawToFlat(composition)

        assertThat(compositionFlatMap)
            .isNotEmpty()
            .containsEntry("composition_with_generic_entry/mojgeneric/mojtekst", "MojTekst 66")
    }
}
