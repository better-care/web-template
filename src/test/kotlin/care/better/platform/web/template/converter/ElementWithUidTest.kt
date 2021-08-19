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
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ElementWithUidTest : AbstractWebTemplateTest() {

    @Test
    fun testElementWithUid() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/JDM Tanner stages.xml"), builderContext)
        assertThat(webTemplate).isNotNull

        val rawComposition = getComposition("/convert/compositions/JDM Tanner Stages.xml")

        val structuredComposition = webTemplate.convertFromRawToStructured(rawComposition)
        assertThat(structuredComposition).isNotEmpty

        val commentNode = structuredComposition!!.get("tanner_stages").get("tanner_stages").get(0).get("any_point_in_time_event").get(0).get("comment").get(0)

        assertThat(commentNode).isInstanceOf(ObjectNode::class.java)
        assertThat(commentNode.get("")).isNull()
        assertThat(commentNode.get("|value").asText()).isEqualTo("this is a comment. very informative")
        assertThat(commentNode.get("_uid").get(0).asText()).isEqualTo("f24ec4bb-4791-4358-9459-e1d14f82c4b1")
    }
}
