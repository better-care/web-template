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
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class AdlDesignerTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testMissingCategory() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Request_for_Pancreas_Special_Urgency_Listing.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("en").withTerritory("GB").withComposerName("Test").build()

        val root: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/pancreas.json")) as ObjectNode
        assertThat(webTemplate.convertFromStructuredToRaw<Composition>(root, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCreationFailure() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/recursivecomposition.opt"), builderContext)
        assertThat(webTemplate).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCategoryInContextTest() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/DRP Report.xml"), builderContext)
        assertThat(webTemplate).isNotNull
        val category: WebTemplateNode? = webTemplate.tree.children.firstOrNull { node -> "category" == node.jsonId }
        assertThat(category!!.inContext).isTrue
    }
}
