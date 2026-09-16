/* Copyright 2024 Better Ltd (www.better.care)
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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition

/**
 * @author Tilen Zabkar
 * @since 4.0
 */
class DvParsableTest : AbstractWebTemplateTest() {

    private val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
        getTemplate("/build/input/Testing.opt"),
        WebTemplateBuilderContext("en"))

    private val context: ConversionContext = ConversionContext.create()
        .withLanguage("en")
        .withTerritory("US")
        .withComposerName("Test Composer")
        .build()

    private val parsablePath = "encounter/testing:0/parsable"

    @Test
    fun testBareValueInputIsAccepted() {
        val flatMap: Map<String, Any> = mapOf(
            parsablePath to "some original content",
            "$parsablePath|formalism" to "text/plain")

        assertThat(parsableValue(flatMap)).isEqualTo("some original content")
    }

    @Test
    fun testValueInputIsAccepted() {
        val flatMap: Map<String, Any> = mapOf(
            "$parsablePath|value" to "some original content",
            "$parsablePath|formalism" to "text/plain")

        assertThat(parsableValue(flatMap)).isEqualTo("some original content")
    }

    @Test
    fun testValueIsAlwaysReturnedWithSuffix() {
        val flatMap: Map<String, Any> = mapOf(
            parsablePath to "some original content",
            "$parsablePath|formalism" to "text/plain")

        val composition = webTemplate.convertFromFlatToRaw<Composition>(flatMap, context)!!
        val roundTripped = webTemplate.convertFromRawToFlat(composition)

        assertThat(roundTripped["$parsablePath|value"]).isEqualTo("some original content")
        assertThat(roundTripped["$parsablePath|formalism"]).isEqualTo("text/plain")
        assertThat(roundTripped).doesNotContainKey(parsablePath)
    }

    private fun parsableValue(flatMap: Map<String, Any>): String? {
        val composition = webTemplate.convertFromFlatToRaw<Composition>(flatMap, context)!!
        return webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())["$parsablePath|value"] as String?
    }
}
