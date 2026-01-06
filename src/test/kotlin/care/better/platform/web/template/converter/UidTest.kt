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
import jakarta.xml.bind.JAXBException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class UidTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testUid() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/clinical-summary-events.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "clinical_summary_events/context/setting|terminology" to "openehr",
                "clinical_summary_events/composer|name" to "Dra. Amelia José",
                "clinical_summary_events/documents/citation:0/citation-report/description" to "FILE NAME TEST",
                "clinical_summary_events/documents/citation:0/citation-report/report_date" to "1970-01-01T00:00:00.000Z",
                "clinical_summary_events/context/setting|238" to "true",
                "clinical_summary_events/context/setting|value" to "other care",
                "clinical_summary_events/documents/citation:0/_uid" to "averyspecialuid",
                "clinical_summary_events/_uid" to "7c8de812-361a-4a08-b954-ebd9df0a15b8::default::1",
                "clinical_summary_events/documents/citation:0/citation-report/report_category" to "string",
                "clinical_summary_events/context/start_time" to "2015-09-29T09:07:29.273Z",
                "clinical_summary_events/context/setting|code" to "238"),
            ConversionContext.create().build())
        assertThat(composition).isNotNull
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create(Locale.ENGLISH))
        assertThat(flatMap).contains(entry("clinical_summary_events/documents/citation:0/_uid", "averyspecialuid"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCompositionUid() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/clinical-summary-events.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            mapOf(
                "ctx/language" to "sl",
                "ctx/territory" to "SI",
                "ctx/id_scheme" to "ispek",
                "ctx/id_namespace" to "ispek",
                "ctx/composer_name" to "George Orwell",
                "clinical_summary_events/_uid" to "compuid",
                "clinical_summary_events/context/setting|terminology" to "openehr",
                "clinical_summary_events/composer|name" to "Dra. Amelia José",
                "clinical_summary_events/documents/citation:0/citation-report/description" to "FILE NAME TEST",
                "clinical_summary_events/documents/citation:0/citation-report/report_date" to "1970-01-01T00:00:00.000Z",
                "clinical_summary_events/context/setting|238" to "true",
                "clinical_summary_events/context/setting|value" to "other care",
                "clinical_summary_events/documents/citation:0/_uid" to "averyspecialuid",
                "clinical_summary_events/documents/citation:0/citation-report/report_category" to "string",
                "clinical_summary_events/context/start_time" to "2015-09-29T09:07:29.273Z",
                "clinical_summary_events/context/setting|code" to "238"),
            ConversionContext.create().build())

        assertThat(composition).isNotNull
        assertThat(composition?.uid?.value).isEqualTo("compuid")
        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create(Locale.ENGLISH))
        assertThat(flatMap).contains(entry("clinical_summary_events/_uid", "compuid"))
        assertThat(flatMap).contains(entry("clinical_summary_events/documents/citation:0/_uid", "averyspecialuid"))
    }
}
