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
class EthercisTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPartyIdentifiedWithHierObjectId() {
        val composition = getComposition("/convert/compositions/action_test.xml")
        val template = getTemplate("/convert/templates/action test.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val values: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(values).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCtxValues() {
        val composition = getComposition("/convert/compositions/conformance test.xml")
        val template = getTemplate("/convert/templates/ConformanceTesttemplate.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val values: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())

        val secondCompositions: Composition? = webTemplate.convertFromFlatToRaw(values, ConversionContext.create().build())
        assertThat(secondCompositions!!.context?.startTime?.value).isEqualTo(composition.context?.startTime?.value)
        assertThat(secondCompositions.language?.codeString).isEqualTo(composition.language?.codeString)
        assertThat(secondCompositions.territory?.codeString).isEqualTo(composition.territory?.codeString)
    }
}
