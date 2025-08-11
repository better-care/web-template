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
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.History
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datastructures.PointEvent
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvOrdinal
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Maja Razinger
 */

class UnconstrainedOrdinalTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun unconstrainedOrdinalTest() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/UnconstrainedOrdinal.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("en").withTerritory("GB").withComposerName("Test").build()

        val root: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/UnconstrainedOrdinal-comp.json")) as ObjectNode
        val composition: Composition = webTemplate.convertFromStructuredToRaw<Composition>(root, context)!!

        val dvOrdinal = (((((composition.content[0] as Observation).data as History).events[0] as PointEvent).data as ItemTree).items[0] as Element).value as DvOrdinal
        assertThat(dvOrdinal.value).isEqualTo(1)

        val symbol = dvOrdinal.symbol
        assertThat(symbol!!.value).isEqualTo("t1")
        assertThat(symbol!!.definingCode!!.codeString).isEqualTo("11929db5-a4c8-4d58-9ca3-8478dd7eb9f7")
        assertThat(symbol.definingCode!!.preferredTerm).isEqualTo("term1")

        val dvCodedText = (((((composition.content[0] as Observation).data as History).events[0] as PointEvent).data as ItemTree).items[1] as Element).value as DvCodedText

        assertThat(dvCodedText.value).isEqualTo("1")
        assertThat(dvCodedText.definingCode!!.codeString).isEqualTo("f6e5e39e-d665-485b-a16b-4902869474d5")
        assertThat(dvCodedText.definingCode!!.preferredTerm).isEqualTo("term2")

    }
}