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
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datastructures.PointEvent
import org.openehr.rm.datatypes.DvCodedText
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class CodedTextTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCodedText() {
        val template = getTemplate("/convert/templates/Demo Vitals.opt")
        val context = ConversionContext.create().withLanguage("en").withTerritory("IE").withComposerName("composer").build()
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en"))

        val values: Map<String, String> = ImmutableMap.builder<String, String>()
            .put("vitals/vitals/haemoglobin_a1c/any_event/test_status", "at0038")
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        val section = composition!!.content[0] as Section
        val hba1c = section.items[0] as Observation
        val history = hba1c.data
        val event = history!!.events[0] as PointEvent
        val tree = event.data as ItemTree?
        val element = tree!!.items[0] as Element
        val codedText = element.value as DvCodedText?
        assertThat(codedText!!.value).isNotEmpty
    }
}
