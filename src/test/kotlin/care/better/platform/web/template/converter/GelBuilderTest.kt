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
import com.fasterxml.jackson.core.type.TypeReference
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class GelBuilderTest : AbstractWebTemplateTest() {

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMissingNameMapping() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/GEL_-_Generic_Lab_Report_import.v0.opt"), builderContext)

        val compositionFlatMap = getObjectMapper().readValue(
            getJson("/convert/compositions/GEL_-_Generic_Lab_Report_import.json"),
            object : TypeReference<Map<String, Any?>>() {})

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, ConversionContext.create().build())
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry(
                "laboratory_result_report/laboratory_test:0/laboratory_test_panel:0/laboratory_result:0/result_value/_name/_mapping:0/target|code",
                "5195-3"))
    }
}
