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
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import javax.xml.bind.JAXBException

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

        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("cda_document/context/setting|code", "238")
            .put("cda_document/context/setting|value", "other care")
            .put("cda_document/xds_metadata/mime_type", "text/xml")
            .put("cda_document/xds_metadata/creation_time", DateTime.now())
            .put("cda_document/xds_metadata/title", "Document name")
            .put("cda_document/xds_metadata/class|code", "class_code")
            .put("cda_document/xds_metadata/class|value", "class_value")
            .put("cda_document/xds_metadata/format|code", "format_value")
            .put("cda_document/xds_metadata/format|value", "format_value")
            .put("cda_document/xds_metadata/practice_setting|code", "practice_value")
            .put("cda_document/xds_metadata/practice_setting|value", "practice_value")
            .put("cda_document/xds_metadata/type|code", "type_code")
            .put("cda_document/xds_metadata/type|value", "type_value")
            .put("cda_document/xds_metadata/event:0|code", "event1_code")
            .put("cda_document/xds_metadata/event:0|value", "event1_value")
            .put("cda_document/xds_metadata/event:1|code", "event2_code")
            .put("cda_document/xds_metadata/event:1|value", "event2_value")
            .put("cda_document/cda_component:0/name", "name1")
            .put("cda_document/cda_component:0/templateid", "1.3.6.1.4.1.19376.1.5.3.1.3.3")
            .put("cda_document/cda_component:0/code|code", "46241-6")
            .put("cda_document/cda_component:0/code|value", "HOSPITAL ADMISSION DX")
            .put("cda_document/cda_component:0/title", "2. Aktivni zdravstveni problemi:")
            .put("cda_document/cda_component:0/text", "Osteoartroza in TEP kolena leta 2000.")
            .put("cda_document/cda_component:0/text|formalism", "text/html")
            .put("cda_document/cda_component:1/name", "name2")
            .put("cda_document/cda_component:1/templateid", "1.3.6.1.4.1.19376.1.5.3.1.3.3")
            .put("cda_document/cda_component:1/code|code", "46241-6")
            .put("cda_document/cda_component:1/code|value", "HOSPITAL ADMISSION DX")
            .put("cda_document/cda_component:1/title", "2. Aktivni zdravstveni problemi:")
            .put("cda_document/cda_component:1/text", "Osteoartroza in TEP kolena leta 2000.")
            .put("cda_document/cda_component:1/text|formalism", "text/html")
            .build()

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

        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
            .put("ctx/health_care_facility|name", "hospital")
            .put("ctx/health_care_facility|id", "hospital id")
            .put("ctx/language", "en")
            .put("xds_document/context/setting|code", "238")
            .put("xds_document/context/setting|value", "other care")
            .put("xds_document/context/start_time", DateTime.now())
            .put("xds_document/context/end_time", DateTime.now())
            .put("xds_document/xds_metadata/mime_type", "text/xml")
            .put("xds_document/xds_metadata/creation_time", DateTime.now())
            .put("xds_document/xds_metadata/title", "Document name")
            .put("xds_document/xds_metadata/class|code", "class_code")
            .put("xds_document/xds_metadata/class|value", "class_value")
            .put("xds_document/xds_metadata/format|code", "format_value")
            .put("xds_document/xds_metadata/format|value", "format_value")
            .put("xds_document/xds_metadata/practice_setting|code", "practice_value")
            .put("xds_document/xds_metadata/practice_setting|value", "practice_value")
            .put("xds_document/xds_metadata/type|code", "type_code")
            .put("xds_document/xds_metadata/type|value", "type_value")
            .put("xds_document/xds_metadata/event:0|code", "event1_code")
            .put("xds_document/xds_metadata/event:0|value", "event1_value")
            .put("xds_document/xds_metadata/event:1|code", "event2_code")
            .put("xds_document/xds_metadata/event:1|value", "event2_value")
            .put("xds_document/xds_metadata/author/author_person", "Jim Smith")
            .build()

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
