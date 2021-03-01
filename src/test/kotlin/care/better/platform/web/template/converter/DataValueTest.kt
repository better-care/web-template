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
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.datatypes.DvQuantity
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DataValueTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDataValues() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, Any>()
                .put("vitals/vitals/body_temperature/any_event/temperature", DvQuantity(39.1, "°C"))
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                .build(),
            context)

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", "39.1"),
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_status|code", "at0037"),
            entry("vitals/vitals/haemoglobin_a1c:0/any_event:0/test_status|value", "Začasen"))
    }
}
