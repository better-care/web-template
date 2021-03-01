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
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.joda.JodaModule
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Action
import org.openehr.rm.composition.Composition
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.DvIdentifier
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DvIdentifierWithIdAttributeTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDvIdentifierId() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Generic service request.v0.xml"), builderContext)

        val mapper = createObjectMapper().apply {
            this.registerModule(JodaModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

        val node: ObjectNode = mapper.readValue(getJson("/convert/compositions/PSKY - Generic service request.v0.json", mapper), ObjectNode::class.java)

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, ConversionContext.create().build())
        assertThat(((((composition!!.content[1] as Action).protocol as ItemTree).items[0] as Element).value as DvIdentifier).id).isEqualTo("d415ad1c-abdc-448d-9609-5485e4c3bc8a")
    }
}
