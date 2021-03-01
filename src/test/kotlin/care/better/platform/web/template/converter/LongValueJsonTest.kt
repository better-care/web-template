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
class LongValueJsonTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testLongValueJson() {
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/ZN - Assessment Scales Encounter2.opt"),
            WebTemplateBuilderContext("en", ImmutableList.of("en", "sl")))

        val compositionFlatMap: Map<String, Any?> = mapOf(
            Pair("assessment_scales/pain_assessment/story/pain/patient_described_current_intensity/degree_level", 5000000000000000000L))

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)

        val retrieve: ObjectNode = webTemplate.convertFromRawToStructured(composition!!, FromRawConversion.create()) as ObjectNode
        assertThat(retrieve["assessment_scales"].isObject).isTrue
        assertThat(retrieve["assessment_scales"]["pain_assessment"].isArray).isTrue
        assertThat(retrieve["assessment_scales"]["pain_assessment"][0]["story"].isArray).isTrue
        assertThat(retrieve["assessment_scales"]["pain_assessment"][0]["story"][0]["pain"].isArray).isTrue
        assertThat(retrieve["assessment_scales"]["pain_assessment"][0]["story"][0]["pain"][0]["patient_described_current_intensity"].isArray).isTrue
        assertThat(retrieve["assessment_scales"]["pain_assessment"][0]["story"][0]["pain"][0]["patient_described_current_intensity"][0]["degree_level"].isArray).isTrue
        assertThat(retrieve["assessment_scales"]["pain_assessment"][0]["story"][0]["pain"][0]["patient_described_current_intensity"][0]["degree_level"][0].isLong).isTrue
        assertThat(retrieve["assessment_scales"]["pain_assessment"][0]["story"][0]["pain"][0]["patient_described_current_intensity"][0]["degree_level"][0].asLong()).isEqualTo(5000000000000000000L)
    }
}
