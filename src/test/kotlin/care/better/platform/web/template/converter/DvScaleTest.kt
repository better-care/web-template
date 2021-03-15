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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DvScaleTest : AbstractWebTemplateTest() {

    @Test
    fun testDvScale() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/scaleTest.opt"), builderContext)
        assertThat(webTemplate).isNotNull

        val firstScaleWebTemplateNode = webTemplate.findWebTemplateNode("scaletest/rm110test/any_event/scalewithvalue")
        val secondScaleWebTemplateNode = webTemplate.findWebTemplateNode("scaletest/rm110test/any_event/scalewithoutvalue")

        assertThat(firstScaleWebTemplateNode.rmType).isNotNull()
        assertThat(secondScaleWebTemplateNode.rmType).isNotNull()

        val flatMap: Map<String, Any> = mapOf(
            Pair("scaletest/rm110test/any_event/scalewithvalue|code", "at0005"),
            Pair("scaletest/rm110test/any_event/scalewithvalue|value", "Test"),
            Pair("scaletest/rm110test/any_event/scalewithvalue|scale", 22.2))

        val composition = webTemplate.convertFromFlatToRaw<Composition>(flatMap, ConversionContext.create().build())!!

        val compositionFlatMap = webTemplate.convertFromRawToFlat(composition)
        assertThat(compositionFlatMap).isNotEmpty()
        assertThat(compositionFlatMap.containsKey("scaletest/rm110test/any_event:0/scalewithvalue|code"))
        assertThat(compositionFlatMap.containsKey("scaletest/rm110test/any_event:0/scalewithvalue|value"))
        assertThat(compositionFlatMap.containsKey("scaletest/rm110test/any_event:0/scalewithvalue|scale"))
    }
}
