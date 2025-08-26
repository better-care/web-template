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
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class XdsBuilderTest : AbstractWebTemplateTest() {

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCdaDocument() {
        val template = getTemplate("/convert/templates/CDA Document.opt")

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))

        val values: Map<String, Any> = mapOf(
            "cda_document/context/setting|code" to "238",
            "cda_document/context/setting|value" to "other care",
            "cda_document/xds_metadata/mime_type" to "text/xml",
            "cda_document/xds_metadata/creation_time" to DateTime.now(),
            "cda_document/xds_metadata/title" to "Document name",
            "cda_document/xds_metadata/class|code" to "class_code",
            "cda_document/xds_metadata/class|value" to "class_value",
            "cda_document/xds_metadata/format|code" to "format_value",
            "cda_document/xds_metadata/format|value" to "format_value",
            "cda_document/xds_metadata/practice_setting|code" to "practice_value",
            "cda_document/xds_metadata/practice_setting|value" to "practice_value",
            "cda_document/xds_metadata/type|code" to "type_code",
            "cda_document/xds_metadata/type|value" to "type_value",
            "cda_document/xds_metadata/event:0|code" to "event1_code",
            "cda_document/xds_metadata/event:0|value" to "event1_value",
            "cda_document/xds_metadata/event:1|code" to "event2_code",
            "cda_document/xds_metadata/event:1|value" to "event2_value",
            "cda_document/cda_component:0/name" to "name1",
            "cda_document/cda_component:0/templateid" to "1.3.6.1.4.1.19376.1.5.3.1.3.3",
            "cda_document/cda_component:0/code|code" to "46241-6",
            "cda_document/cda_component:0/code|value" to "HOSPITAL ADMISSION DX",
            "cda_document/cda_component:0/title" to "2. Aktivni zdravstveni problemi:",
            "cda_document/cda_component:0/text" to "Osteoartroza in TEP kolena leta 2000.",
            "cda_document/cda_component:0/text|formalism" to "text/html",
            "cda_document/cda_component:1/name" to "name2",
            "cda_document/cda_component:1/templateid" to "1.3.6.1.4.1.19376.1.5.3.1.3.3",
            "cda_document/cda_component:1/code|code" to "46241-6",
            "cda_document/cda_component:1/code|value" to "HOSPITAL ADMISSION DX",
            "cda_document/cda_component:1/title" to "2. Aktivni zdravstveni problemi:",
            "cda_document/cda_component:1/text" to "Osteoartroza in TEP kolena leta 2000.",
            "cda_document/cda_component:1/text|formalism" to "text/html"
        )

        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap["cda_document/xds_metadata/event:0|code"]).isEqualTo("event1_code")
        assertThat(flatMap["cda_document/xds_metadata/event:1|code"]).isEqualTo("event2_code")
        assertThat(flatMap["cda_document/cda_component:0/templateid"]).isEqualTo("1.3.6.1.4.1.19376.1.5.3.1.3.3")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testXdsGenericDocument() {
        val template = getTemplate("/convert/templates/XDS Document.opt")

        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))

        val values: Map<String, Any> = mapOf(
            "ctx/health_care_facility|name" to "hospital",
            "ctx/health_care_facility|id" to "hospital id",
            "ctx/language" to "en",
            "xds_document/context/setting|code" to "238",
            "xds_document/context/setting|value" to "other care",
            "xds_document/context/start_time" to DateTime.now(),
            "xds_document/context/end_time" to DateTime.now(),
            "xds_document/xds_metadata/mime_type" to "text/xml",
            "xds_document/xds_metadata/creation_time" to DateTime.now(),
            "xds_document/xds_metadata/title" to "Document name",
            "xds_document/xds_metadata/class|code" to "class_code",
            "xds_document/xds_metadata/class|value" to "class_value",
            "xds_document/xds_metadata/format|code" to "format_value",
            "xds_document/xds_metadata/format|value" to "format_value",
            "xds_document/xds_metadata/practice_setting|code" to "practice_value",
            "xds_document/xds_metadata/practice_setting|value" to "practice_value",
            "xds_document/xds_metadata/type|code" to "type_code",
            "xds_document/xds_metadata/type|value" to "type_value",
            "xds_document/xds_metadata/event:0|code" to "event1_code",
            "xds_document/xds_metadata/event:0|value" to "event1_value",
            "xds_document/xds_metadata/event:1|code" to "event2_code",
            "xds_document/xds_metadata/event:1|value" to "event2_value",
            "xds_document/xds_metadata/author/author_person" to "Jim Smith"
        )

        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .withIdScheme("x")
            .withIdNamespace("y")
            .withComposerName("composer").build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull
        assertThat(composition?.context?.healthCareFacility?.name).isEqualTo("hospital")
        assertThat(composition?.context?.healthCareFacility?.externalRef?.id?.value).isEqualTo("hospital id")
        assertThat(composition?.context?.endTime).isNotNull
    }
}
