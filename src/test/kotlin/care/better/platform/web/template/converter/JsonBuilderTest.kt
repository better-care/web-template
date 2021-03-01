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
import com.fasterxml.jackson.databind.JsonNode
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
class JsonBuilderTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testJson() {
        val json = getJson("/convert/compositions/ISPEK - ZN - Vital Functions Encounter.json")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/ZN - Vital Functions Encounter.xml"),
            WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(json) as ObjectNode, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testRawJson() {
        val json = """
            { 
                "vital_functions": {
                    "_uid": [
                      "b430dbed-468c-4ab1-9440-6383109a56c0::default::1"
                    ],
                    "vital_signs": [
                      {
                        "pulse": [
                          {
                            "any_event": [
                              {
                                "heart_rate": [
                                  {
                                    "|raw": "{ \"@class\": \"DV_QUANTITY\", \"magnitude\": 90, \"other_reference_ranges\": [], \"units\": \"/min\" }"
                                  }
                                ],
                                "time": [
                                  "2014-04-15T10:28:22.736+02:00"
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                }
            }""".trimIndent()

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/ZN - Vital Functions Encounter.xml"),
            WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(json) as ObjectNode, context)
        val jsonNode: JsonNode? = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create())
        val path = jsonNode!!.path("vital_functions")
            .path("vital_signs")
            .path(0)
            .path("pulse")
            .path(0)
            .path("any_event")
            .path(0)
            .path("heart_rate")
            .path(0)

        assertThat(path.isMissingNode).isFalse
        assertThat(path.path("|magnitude").doubleValue()).isEqualTo(90.0)
        assertThat(path.path("|unit").textValue()).isEqualTo("/min")
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOptIssue() {
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/ZN - Vital Functions Encounter-1.xml"),
            WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))
        assertThat(webTemplate).isNotNull
    }
}
