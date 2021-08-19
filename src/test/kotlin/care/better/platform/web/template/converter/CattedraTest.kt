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
import care.better.platform.web.template.builder.WebTemplateBuilder.Companion.build
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext.Companion.create
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class CattedraTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCommentWithUid() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate = build(getTemplate("/convert/templates/CATTEDRA JDM Tanner stages.xml"), builderContext)
        val structuredComposition: JsonNode = getObjectMapper().readTree(getJson("/convert/compositions/CATTEDRA JDM Tanner stages.json"))
        val composition = webTemplate!!.convertFromStructuredToRaw<Composition>(structuredComposition as ObjectNode, create().build())!!
        val convertedStructuredComposition = webTemplate.convertFromRawToStructured(composition)!!
        val convertedFlatComposition = webTemplate.convertFromRawToFlat(composition)
        val commentNode = convertedStructuredComposition["tanner_stages"]["tanner_stages"][0]["any_point_in_time_event"][0]["comment"][0]
        assertThat(commentNode["_uid"]).isNull()
        assertThat(commentNode).isInstanceOf(TextNode::class.java)
        assertThat(commentNode.asText()).isEqualTo("this is a comment")
        assertThat(convertedFlatComposition["tanner_stages/tanner_stages/any_point_in_time_event:0/comment"]).isEqualTo("this is a comment")
        assertThat(convertedFlatComposition["tanner_stages/tanner_stages/any_point_in_time_event:0/comment/_uid"]).isNull()
    }
}
