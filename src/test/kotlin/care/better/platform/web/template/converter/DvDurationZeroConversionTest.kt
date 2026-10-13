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
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.History
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datastructures.PointEvent
import org.openehr.rm.datatypes.DvDuration

class DvDurationZeroConversionTest : AbstractWebTemplateTest() {
    @Test
    fun durationZero() {
        val template = getTemplate("/convert/templates/HSE - Referral.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").addTermBindingTerminology("*").build()
        val compositionFlatMap = getObjectMapper().readValue<Map<String, Any>>(getJson("/convert/compositions/hse_referral.json"))

        val composition: Composition = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)!!

        val section = composition.content[4] as Section
        val observation = section.items[0] as Observation
        val history = observation.data as History
        val event = history.events[0] as PointEvent
        val tree = event.data as ItemTree

        val duration = tree.items[1] as Element
        val durationDV = duration.value as DvDuration
        assertThat(durationDV.value).isEqualTo("P0D")
    }

    @Test
    fun brokenDuration() {
        val template = getTemplate("/convert/templates/HSE - Referral.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").addTermBindingTerminology("*").build()
        val compositionFlatMap = getObjectMapper().readValue<Map<String, Any>>(getJson("/convert/compositions/hse_referral.json")).toMutableMap()
        compositionFlatMap["referral/breast_clinic_referral/symptom_sign/symptom_duration"] = "P0Z"

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(compositionFlatMap, context) }
            .hasMessageContaining("Error processing value: \"P0Z\"")
    }
}
