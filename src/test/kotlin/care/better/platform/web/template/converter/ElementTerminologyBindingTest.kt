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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.History
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datastructures.PointEvent

class ElementTerminologyBindingTest : AbstractWebTemplateTest() {
    @Test
    fun allTerminologies() {
        val template = getTemplate("/convert/templates/Blood pressure.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").addTermBindingTerminology("*").build()

        val compositionFlatMap: Map<String, Any> = mapOf(
                "blood_pressure/blood_pressure/any_event:0/systolic|magnitude" to 120,
                "blood_pressure/blood_pressure/any_event:0/systolic|unit" to "mm[Hg]",
                "blood_pressure/blood_pressure/any_event:0/diastolic|magnitude" to 80,
                "blood_pressure/blood_pressure/any_event:0/diastolic|unit" to "mm[Hg]"
        )
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val observation = composition.content[0] as Observation
        val history = observation.data as History
        val event = history.events[0] as PointEvent
        val tree2 = event.data as ItemTree

        val systolic = tree2.items[0] as Element
        val diastolic = tree2.items[1] as Element
        assertThat(systolic.name?.mappings?.map { it.target?.terminologyId?.value to it.target?.codeString }).containsOnly(
                "SNOMED-CT" to "271649006",
                "LOINC" to "3214")
        assertThat(diastolic.name?.mappings?.map { it.target?.terminologyId?.value to it.target?.codeString }).containsOnly(
                "SNOMED-CT" to "271650006",
                "LOINC" to "4123")
    }

    @Test
    fun snomedOnly() {
        val template = getTemplate("/convert/templates/Blood pressure.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val context =
            ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").addTermBindingTerminology("SNOMED-CT").build()

        val compositionFlatMap: Map<String, Any> = mapOf(
                "blood_pressure/blood_pressure/any_event:0/systolic|magnitude" to 120,
                "blood_pressure/blood_pressure/any_event:0/systolic|unit" to "mm[Hg]",
                "blood_pressure/blood_pressure/any_event:0/diastolic|magnitude" to 80,
                "blood_pressure/blood_pressure/any_event:0/diastolic|unit" to "mm[Hg]"
        )
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val observation = composition.content[0] as Observation
        val history = observation.data as History
        val event = history.events[0] as PointEvent
        val tree2 = event.data as ItemTree

        val systolic = tree2.items[0] as Element
        val diastolic = tree2.items[1] as Element
        assertThat(systolic.name?.mappings?.map { it.target?.terminologyId?.value to it.target?.codeString }).containsOnly(
                "SNOMED-CT" to "271649006")
        assertThat(diastolic.name?.mappings?.map { it.target?.terminologyId?.value to it.target?.codeString }).containsOnly(
                "SNOMED-CT" to "271650006")
    }

    @Test
    fun loincOnly() {
        val template = getTemplate("/convert/templates/Blood pressure.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").addTermBindingTerminology("LOINC").build()

        val compositionFlatMap: Map<String, Any> = mapOf(
                "blood_pressure/blood_pressure/any_event:0/systolic|magnitude" to 120,
                "blood_pressure/blood_pressure/any_event:0/systolic|unit" to "mm[Hg]",
                "blood_pressure/blood_pressure/any_event:0/diastolic|magnitude" to 80,
                "blood_pressure/blood_pressure/any_event:0/diastolic|unit" to "mm[Hg]"
        )
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val observation = composition.content[0] as Observation
        val history = observation.data as History
        val event = history.events[0] as PointEvent
        val tree2 = event.data as ItemTree

        val systolic = tree2.items[0] as Element
        val diastolic = tree2.items[1] as Element
        assertThat(systolic.name?.mappings?.map { it.target?.terminologyId?.value to it.target?.codeString }).containsOnly(
                "LOINC" to "3214")
        assertThat(diastolic.name?.mappings?.map { it.target?.terminologyId?.value to it.target?.codeString }).containsOnly(
                "LOINC" to "4123")
    }

    @Test
    fun noBinding() {
        val template = getTemplate("/convert/templates/Blood pressure.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").build()

        val compositionFlatMap: Map<String, Any> = mapOf(
                "blood_pressure/blood_pressure/any_event:0/systolic|magnitude" to 120,
                "blood_pressure/blood_pressure/any_event:0/systolic|unit" to "mm[Hg]",
                "blood_pressure/blood_pressure/any_event:0/diastolic|magnitude" to 80,
                "blood_pressure/blood_pressure/any_event:0/diastolic|unit" to "mm[Hg]"
        )
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        composition!!.archetypeDetails!!.templateId = template.templateId
        assertThat(composition).isNotNull

        val observation = composition.content[0] as Observation
        val history = observation.data as History
        val event = history.events[0] as PointEvent
        val tree2 = event.data as ItemTree

        val systolic = tree2.items[0] as Element
        val diastolic = tree2.items[1] as Element
        assertThat(systolic.name?.mappings).isEmpty()
        assertThat(diastolic.name?.mappings).isEmpty()
    }
}
