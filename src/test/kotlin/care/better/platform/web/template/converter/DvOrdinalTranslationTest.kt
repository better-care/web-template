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

import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.node.ObjectNode
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.*
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Matija Polajnar
 * @since 3.1.0
 */
class DvOrdinalTranslationTest : AbstractWebTemplateTest() {
    private val webTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/Translated DV_ORDINAL.opt"),
            WebTemplateBuilderContext("en"))

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTermMappingToObjectMap() {
        val node: ObjectNode = getObjectMapper().readValue(getJson("/convert/compositions/Translated DV_ORDINAL.json"), ObjectNode::class.java)

        val compositionSl: Composition? = webTemplate.convertFromStructuredToRaw(node, ConversionContext.create().build())
        assertThat(compositionSl).isNotNull
        val ordinalSl = (((compositionSl!!.content[0] as Observation).data!!.events[0].data!! as ItemTree).items[0] as Element).value as DvOrdinal
        assertThat(ordinalSl.symbol?.value).isEqualTo("Vrednost 2 v slovenščini")

        node.put("ctx/language", "de")
        val compositionDe: Composition? = webTemplate.convertFromStructuredToRaw(node, ConversionContext.create().build())
        assertThat(compositionDe).isNotNull
        val ordinalDe = (((compositionDe!!.content[0] as Observation).data!!.events[0].data!! as ItemTree).items[0] as Element).value as DvOrdinal
        assertThat(ordinalDe.symbol?.value).isEqualTo("Wert 2 im Deutsch")
    }
}
