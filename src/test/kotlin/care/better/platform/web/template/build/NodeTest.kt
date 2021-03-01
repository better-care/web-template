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

package care.better.platform.web.template.build

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.datastructures.Element
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class NodeTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testMatchingNodes() {
        val template = getTemplate("/build/AssistedVentilation.opt")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))

        val firstNode: WebTemplateNode = webTemplate.findWebTemplateNode("report/ventilator_observations:0/ventilator_settings_findings/flow_sensor:0/value2|unit")
        val secondNode: WebTemplateNode = webTemplate.findWebTemplateNode("report/ventilator_observations:0/ventilator_settings_findings/flow_sensor:0/value")

        val parent = Element()
        val firstNodeKey = NodeKey(firstNode, 1, parent)
        val secondNodeKeY = NodeKey(secondNode, 1, parent)

        assertThat(firstNodeKey == secondNodeKeY).isFalse
    }

    internal class NodeKey(val node: WebTemplateNode, private val index: Int, private val parent: Any?) {

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val nodeKey = other as NodeKey
            if (index != nodeKey.index) {
                return false
            }
            if (node != nodeKey.node) {
                return false
            }
            return !if (parent != null) parent != nodeKey.parent else nodeKey.parent != null
        }

        override fun hashCode(): Int {
            var result: Int = node.hashCode()
            result = 31 * result + index
            result = 31 * result + (parent?.hashCode() ?: 0)
            return result
        }
    }
}
