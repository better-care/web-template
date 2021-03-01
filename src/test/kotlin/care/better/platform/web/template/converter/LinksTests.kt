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
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.O
 */
class LinksTests : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLinks() {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), WebTemplateBuilderContext("sl"))
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                .put("vitals/vitals/haemoglobin_a1c/_link:0|meaning", "none")
                .put("vitals/vitals/haemoglobin_a1c/_link:0|type", "href")
                .put("vitals/vitals/haemoglobin_a1c/_link:0|target", "http://www.sun.com")
                .put("vitals/vitals/haemoglobin_a1c/_link:1|meaning", "serious")
                .put("vitals/vitals/haemoglobin_a1c/_link:1|type", "url")
                .put("vitals/vitals/haemoglobin_a1c/_link:1|target", "http://www.ehrscape.com")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|meaning", "none"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|type", "href"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|target", "http://www.sun.com"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|meaning", "serious"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|type", "url"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "http://www.ehrscape.com")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLinksFromCtx() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/link|type", "EHR")
                .put("ctx/link|meaning", "link")
                .put("ctx/link|target", "ehr://uid/value")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|meaning", "link"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|type", "EHR"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|target", "ehr://uid/value")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testLinksFromCtxMulti() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/link:17|type", "EHR1")
                .put("ctx/link:17|meaning", "link1")
                .put("ctx/link:17|target", "ehr://uid/value1")
                .put("ctx/link:99|type", "EHR2")
                .put("ctx/link:99|meaning", "link2")
                .put("ctx/link:99|target", "ehr://uid/value2")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|meaning", "link1"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|type", "EHR1"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:0|target", "ehr://uid/value1"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|meaning", "link2"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|type", "EHR2"),
            entry("vitals/vitals/haemoglobin_a1c:0/_link:1|target", "ehr://uid/value2")
        )
    }
}
