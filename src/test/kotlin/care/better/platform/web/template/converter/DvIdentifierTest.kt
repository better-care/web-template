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
class DvIdentifierTest : AbstractWebTemplateTest() {

    private val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(
        getTemplate("/build/input/Testing.opt"),
        WebTemplateBuilderContext("en"))

    private val context: ConversionContext = ConversionContext.create()
        .withLanguage("en")
        .withTerritory("US")
        .withComposerName("Test Composer")
        .build()

    private val identifierPath = "encounter/testing:0/identifier"

    @Test
    fun testBareValueInputIsAccepted() {
        val flatMap: Map<String, Any> = mapOf(identifierPath to "12345")

        assertThat(identifierId(flatMap)).isEqualTo("12345")
    }

    @Test
    fun testIdInputIsAccepted() {
        val flatMap: Map<String, Any> = mapOf("$identifierPath|id" to "12345")

        assertThat(identifierId(flatMap)).isEqualTo("12345")
    }

    @Test
    fun testIdIsAlwaysReturnedWithSuffix() {
        val flatMap: Map<String, Any> = mapOf(identifierPath to "12345")

        val composition = webTemplate.convertFromFlatToRaw<Composition>(flatMap, context)!!
        val roundTripped = webTemplate.convertFromRawToFlat(composition)

        assertThat(roundTripped["$identifierPath|id"]).isEqualTo("12345")
        assertThat(roundTripped).doesNotContainKey(identifierPath)
    }

    private fun identifierId(flatMap: Map<String, Any>): String? {
        val composition = webTemplate.convertFromFlatToRaw<Composition>(flatMap, context)!!
        return webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())["$identifierPath|id"] as String?
    }
}
