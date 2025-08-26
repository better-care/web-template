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

import care.better.platform.json.jackson.openehr.OpenEhrObjectMapper
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.mapper.convertRawJsonNode
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.datatypes.DvQuantity

/**
 * @author Maja Razinger
 * @since 4.0.6
 */
class RawConversionApiTest : AbstractWebTemplateTest() {
    private val contextBetter = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").build()
    private val contextOpenEhr = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("Composer").withRawDataMapper(OpenEhrObjectMapper()).build()
    val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/older/Demo Vitals.xml"), WebTemplateBuilderContext("en", listOf("en", "sl")))

    private val temperatureNode: WebTemplateNode = webTemplate.findWebTemplateNodeByAqlPath("/content[openEHR-EHR-SECTION.ispek_dialog.v1,'Vitals']/items[openEHR-EHR-OBSERVATION.body_temperature-zn.v1]/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value")

    @Test
    fun testBetterApi() {
        val jsonNode = getObjectMapper().readTree(
            """
                {
                    "|raw": { 
                      "@class": "DV_QUANTITY",
                      "magnitude": 38.8,
                      "precision": 1,
                      "units": "\u00b0C"
                    }
                }
            """.trimIndent()
        )

        val conversion = contextBetter.rawDataMapper.convertRawJsonNode(
            contextBetter,
            temperatureNode.amNode,
            jsonNode,
            WebTemplatePath("temperature")
        )

        assertThat(conversion).isNotNull
        val conversionAsDvQuantity = conversion as DvQuantity
        assertThat(conversionAsDvQuantity.magnitude).isEqualTo(38.8)
        assertThat(conversionAsDvQuantity.units).isEqualTo("\u00b0C")
        assertThat(conversionAsDvQuantity.precision).isEqualTo(1)
    }

    @Test
    fun testOpenEhrApi() {
        val jsonNode = getObjectMapper().readTree(
            """
                {
                    "|raw": { 
                      "_type": "DV_QUANTITY",
                      "magnitude": 38.8,
                      "precision": 1,
                      "units": "\u00b0C"
                    }
                }
            """.trimIndent()
        )

        val conversion = contextOpenEhr.rawDataMapper.convertRawJsonNode(
            contextOpenEhr,
            temperatureNode.amNode,
            jsonNode,
            WebTemplatePath("temperature")
        )

        assertThat(conversion).isNotNull
        val conversionAsDvQuantity = conversion as DvQuantity
        assertThat(conversionAsDvQuantity.magnitude).isEqualTo(38.8)
        assertThat(conversionAsDvQuantity.units).isEqualTo("\u00b0C")
        assertThat(conversionAsDvQuantity.precision).isEqualTo(1)
    }

    @Test
    fun testError() {
        val jsonNodeBetter = getObjectMapper().readTree(
            """
                {
                    "|raw": { 
                      "@class": "DV_QUANTITY",
                      "magnitude": 38.8,
                      "precision": 1,
                      "units": "\u00b0C"
                    }
                }
            """.trimIndent()
        )

        assertThatThrownBy {
            contextOpenEhr.rawDataMapper.convertRawJsonNode(
                contextOpenEhr,
                temperatureNode.amNode,
                jsonNodeBetter,
                WebTemplatePath("temperature")
            )
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Class RM_OBJECT is not an instance of RM class.")


        val jsonNodeOpenEHR = getObjectMapper().readTree(
            """
                {
                    "|raw": { 
                      "_type": "DV_QUANTITY",
                      "magnitude": 38.8,
                      "precision": 1,
                      "units": "\u00b0C"
                    }
                }
            """.trimIndent()
        )

        assertThatThrownBy {
            contextBetter.rawDataMapper.convertRawJsonNode(
                contextBetter,
                temperatureNode.amNode,
                jsonNodeOpenEHR,
                WebTemplatePath("temperature")
            )
        }.isInstanceOf(Exception::class.java)
            .hasMessage("Class RM_OBJECT is not an instance of RM class.")
    }
}
