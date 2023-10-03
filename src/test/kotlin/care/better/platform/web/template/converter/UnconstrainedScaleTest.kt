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
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.History
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datastructures.PointEvent
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvScale
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Maja Razinger
 */
class UnconstrainedScaleTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun unconstrainedScaleTest() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/UnconstrainedScale.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("en").withTerritory("GB").withComposerName("Test").build()

        val root: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/UnconstrainedScale-comp.json")) as ObjectNode
        val composition: Composition = webTemplate.convertFromStructuredToRaw<Composition>(root, context)!!

        val dvScale = (((((composition.content[0] as Observation).data as History).events[0] as PointEvent).data as ItemTree).items[1] as Element).value as DvScale
        Assertions.assertThat(dvScale.value).isEqualTo(1.2)

        val symbol = dvScale.symbol
        Assertions.assertThat(symbol!!.value).isEqualTo("t1")
        Assertions.assertThat(symbol!!.definingCode!!.codeString).isEqualTo("11929db5-a4c8-4d58-9ca3-8478dd7eb9f7")

        val dvCodedText = (((((composition.content[0] as Observation).data as History).events[0] as PointEvent).data as ItemTree).items[0] as Element).value as DvCodedText
        Assertions.assertThat(dvCodedText.value).isEqualTo("I.74 description")
        Assertions.assertThat(dvCodedText.definingCode!!.codeString).isEqualTo("I.74")
        Assertions.assertThat(dvCodedText.definingCode!!.terminologyId!!.value).isEqualTo("external_terminology")
    }
}