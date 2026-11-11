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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.Event
import org.openehr.rm.datastructures.IntervalEvent

/**
 *
 * @author Primoz Delopst
 * @since 4.3.6
 */
class IntervalEventWidthOrderTest : AbstractWebTemplateTest() {

    private val dataElementKey =
        "bitte_kreisen_sie_die_zahl_ein_0-10_die_am_besten_beschreibt_wie_belastet_sie_sich_in_der_letzten_woche_einschließlich_heute_gefühlt_haben."

    private fun buildWebTemplate(): WebTemplate {
        val builderContext = WebTemplateBuilderContext("de", ImmutableList.of("de", "en"))
        return WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/complete_proms.opt"), builderContext)
    }

    private fun context(): ConversionContext =
        ConversionContext.create().withLanguage("de").withTerritory("SI").withComposerName("Test").build()

    private fun composition(json: String): Composition {
        val root = getObjectMapper().readTree(json) as ObjectNode
        return buildWebTemplate().convertFromStructuredToRaw<Composition>(root, context())!!
    }

    private fun findObservation(items: List<Any?>): Observation? {
        for (item in items) {
            when (item) {
                is Observation -> return item
                is Section -> findObservation(item.items)?.let { return it }
            }
        }
        return null
    }

    private fun intervalEvent(composition: Composition): IntervalEvent =
        findObservation(composition.content)!!.data!!.events.filterIsInstance<IntervalEvent>().first()

    private fun dataElement() =
        """ "$dataElementKey": [ { "|code": "at0008", "|value": "2", "|ordinal": 2 } ] """

    private fun payload(vararg eventFields: String): String =
        """
        {
          "alle_fragebögen": {
            "nccn_distress-thermometer_deutsche_version": [
              { ${eventFields.joinToString(",")} }
            ]
          },
          "ctx": { "language": "de", "territory": "SI", "template_id": "de.UDUS::openEHR-EHR-COMPOSITION.complete_proms.v1.0.0" }
        }
        """.trimIndent()

    @Test
    fun testWidthAfterData() {
        assertThat(intervalEvent(composition(payload(dataElement(), """ "width": [{ "week": 1 }] """))).width?.value).isEqualTo("P1W")
    }

    @Test
    fun testWidthBeforeData() {
        assertThat(intervalEvent(composition(payload(""" "width": [{ "week": 1 }] """, dataElement()))).width?.value).isEqualTo("P1W")
    }

    @Test
    fun testTimeAfterData() {
        assertThat((intervalEvent(composition(payload(dataElement(), """ "time": ["2024-01-02T10:00:00"] """))) as Event).time?.value)
            .startsWith("2024-01-02T10:00:00")
    }

    @Test
    fun testTimeBeforeData() {
        assertThat((intervalEvent(composition(payload(""" "time": ["2024-01-02T10:00:00"] """, dataElement()))) as Event).time?.value)
            .startsWith("2024-01-02T10:00:00")
    }

    @Test
    fun testWidthAndTimeBeforeData() {
        val event = intervalEvent(composition(payload(""" "width": [{ "week": 1 }] """, """ "time": ["2024-01-02T10:00:00"] """, dataElement())))
        assertThat(event.width?.value).isEqualTo("P1W")
        assertThat(event.time?.value).startsWith("2024-01-02T10:00:00")
    }
}
