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
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException

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
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037",
                "vitals/vitals/haemoglobin_a1c/_link:0|meaning" to "none",
                "vitals/vitals/haemoglobin_a1c/_link:0|type" to "href",
                "vitals/vitals/haemoglobin_a1c/_link:0|target" to "http://www.sun.com",
                "vitals/vitals/haemoglobin_a1c/_link:1|meaning" to "serious",
                "vitals/vitals/haemoglobin_a1c/_link:1|type" to "url",
                "vitals/vitals/haemoglobin_a1c/_link:1|target" to "http://www.ehrscape.com"
            ),
            ConversionContext.create().build()
        )

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
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/link|type" to "EHR",
                "ctx/link|meaning" to "link",
                "ctx/link|target" to "ehr://uid/value",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            ConversionContext.create().build()
        )

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
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/link:17|type" to "EHR1",
                "ctx/link:17|meaning" to "link1",
                "ctx/link:17|target" to "ehr://uid/value1",
                "ctx/link:99|type" to "EHR2",
                "ctx/link:99|meaning" to "link2",
                "ctx/link:99|target" to "ehr://uid/value2",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology" to "local",
                "vitals/vitals/haemoglobin_a1c/any_event/test_status|code" to "at0037"
            ),
            ConversionContext.create().build()
        )

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
