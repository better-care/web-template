/* Copyright 2026 Better Ltd (www.better.care)
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

import care.better.platform.json.jackson.better.BetterObjectMapper
import care.better.platform.json.jackson.time.OpenEhrTimeModule
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.structured.FlatToStructuredConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition

class CompositionFieldsTest : AbstractWebTemplateTest() {
    private val template = WebTemplateBuilder.buildNonNull(
        getTemplate("/convert/templates/wt_compatibility_composition.opt"),
        WebTemplateBuilderContext("en"))

    private val context = ConversionContext.create()
        .withLanguage("en")
        .withTerritory("SI")
        .withComposerName("Test Composer")
        .build()

    private val flat: Map<String, Any> = createObjectMapper().readValue(
        getJson("/convert/compositions/wt-compatibility-flat.json"),
        object : TypeReference<Map<String, Any>>() {})

    private val flatAsStrings: Map<String, String> = createObjectMapper().readValue(
        getJson("/convert/compositions/wt-compatibility-flat.json"),
        object : TypeReference<Map<String, String>>() {})

    private val flatFormatted: Map<String, String> = createObjectMapper().readValue(
        getJson("/convert/compositions/wt-compatibility-flat-formatted.json"),
        object : TypeReference<Map<String, String>>() {})

    private val flatMapper = BetterObjectMapper().apply {
        registerModule(JodaModule())
        registerModule(JavaTimeModule())
        registerModule(OpenEhrTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    private fun Map<String, Any>.asFlatStrings(): Map<String, String> =
        flatMapper.convertValue(this, object : TypeReference<Map<String, String>>() {})

    @Test
    fun testFullFlatRoundTrip() {
        val raw = template.convertFromFlatToRaw<Composition>(flat, context)!!
        val roundTripped = template.convertFromRawToFlat(raw, FromRawConversion.create())

        assertThat(roundTripped.asFlatStrings()).containsExactlyInAnyOrderEntriesOf(flatAsStrings)
    }

    @Test
    fun testFullFormattedFlatRoundTrip() {
        val raw = template.convertFromFlatToRaw<Composition>(flat, context)!!
        val roundTripped = template.convertFormattedFromRawToFlat(raw, FromRawConversion.create())

        assertThat(roundTripped).containsExactlyInAnyOrderEntriesOf(flatFormatted)
    }

    @Test
    fun testFullFlatToStructuredRoundTrip() {
        val raw = template.convertFromStructuredToRaw<Composition>(
            FlatToStructuredConverter.getInstance().invoke(flat) as ObjectNode, context)!!
        val roundTripped = template.convertFromRawToFlat(raw, FromRawConversion.create())

        assertThat(roundTripped.asFlatStrings()).containsExactlyInAnyOrderEntriesOf(flatAsStrings)
    }

    @Test
    fun testFullFormattedFlatToStructuredRoundTrip() {
        val raw = template.convertFromStructuredToRaw<Composition>(
            FlatToStructuredConverter.getInstance().invoke(flat) as ObjectNode, context)!!
        val roundTripped = template.convertFormattedFromRawToFlat(raw, FromRawConversion.create())

        assertThat(roundTripped).containsExactlyInAnyOrderEntriesOf(flatFormatted)
    }

    @Test
    fun testFullStructuredRoundTrip() {
        val raw = template.convertFromFlatToRaw<Composition>(flat, context)!!
        val structured = template.convertFromRawToStructured(raw, FromRawConversion.create()) as ObjectNode
        val rawFromStructured = template.convertFromStructuredToRaw<Composition>(structured, context)!!
        val roundTripped = template.convertFromRawToFlat(rawFromStructured, FromRawConversion.create())

        assertThat(roundTripped.asFlatStrings()).containsExactlyInAnyOrderEntriesOf(flatAsStrings)
    }

    @Test
    fun testFullFormattedStructuredRoundTrip() {
        val raw = template.convertFromFlatToRaw<Composition>(flat, context)!!
        val structured = template.convertFromRawToStructured(raw, FromRawConversion.create()) as ObjectNode
        val rawFromStructured = template.convertFromStructuredToRaw<Composition>(structured, context)!!
        val roundTripped = template.convertFormattedFromRawToFlat(rawFromStructured, FromRawConversion.create())

        assertThat(roundTripped).containsExactlyInAnyOrderEntriesOf(flatFormatted)
    }
}