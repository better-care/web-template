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
import com.fasterxml.jackson.databind.node.ObjectNode
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
class ProportionTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testProportion() {
        val templateName = "/convert/templates/older/Demo Vitals.opt"
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate(templateName), WebTemplateBuilderContext("en"))
        val structuredComposition: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/vitals_proportion.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(structuredComposition, ConversionContext.create().build())
        assertThat(composition?.content ?: emptyList()).isEmpty()
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInvalidAttributeFailed() {
        val templateName = "/convert/templates/older/Demo Vitals.opt"
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate(templateName), WebTemplateBuilderContext("en"))
        val structuredComposition: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/vitals_proportion-fail.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(structuredComposition, ConversionContext.create().build())
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInvalidDenominatorAttribute() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Visual Acuity Report.opt"), WebTemplateBuilderContext("en"))
        val json = getJson("/convert/compositions/Visual Acuity Report.json")
        val values = getObjectMapper().readValue(json, object : TypeReference<Map<String, Any?>>() {})
        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(values, ConversionContext.create().build()) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageContaining("DV_PROPORTION has no attribute |demominator (path: visual_acuity_report/visual_acuity:0/any_event:0/right_eye/notation/metric_snellen|demominator).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInvalidIntegral() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Visual Acuity Report.opt"), WebTemplateBuilderContext("en"))
        val json = getJson("/convert/compositions/Second Visual Acuity Report.json")
        val values = getObjectMapper().readValue(json, object : TypeReference<Map<String, Any?>>() {})
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }
}
