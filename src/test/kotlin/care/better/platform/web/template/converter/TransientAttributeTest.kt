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
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition

/**
 * @author Primoz Delopst
 */
class TransientAttributeTest : AbstractWebTemplateTest() {

    private val webTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/TM - Discharge Activity Plan Encounter.xml"),
            WebTemplateBuilderContext("sl", ImmutableList.of("sl")))

    @Test
    fun testTransientLeafNode() {
        val root: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/careplan_activities_transient_leaf_node.json")) as ObjectNode
        val composition = webTemplate.convertFromStructuredToRaw<Composition>(
                root,
                ConversionContext.create().withLanguage("sl").withTerritory("si").build())
        assertThat(composition).isNotNull
    }

    @Test
    fun testTransientNode() {
        val root: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/careplan_activities_transient_node.json")) as ObjectNode
        val composition = webTemplate.convertFromStructuredToRaw<Composition>(
                root,
                ConversionContext.create().withLanguage("sl").withTerritory("si").build())
        assertThat(composition).isNotNull
    }
}