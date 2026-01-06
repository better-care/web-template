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
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Evaluation
import org.openehr.rm.datatypes.DvParsable
import java.io.IOException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class VisitorTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVisitCdaDocument() {
        val template = getTemplate("/convert/templates/CDA Document.opt")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))

        val visited: MutableList<String> = ArrayList()
        val names: MutableList<String> = ArrayList()

        val values: Map<String, String> = mapOf(
            "cda_document/context/setting|code" to "238",
            "cda_document/context/setting|value" to "other care",
            "cda_document/cda_component:0/name" to "HOSPITAL ADMISSION DX",
            "cda_document/cda_component:0/templateid" to "1.3.6.1.4.1.19376.1.5.3.1.3.3",
            "cda_document/cda_component:0/code|code" to "46241-6",
            "cda_document/cda_component:0/code|value" to "HOSPITAL ADMISSION DX",
            "cda_document/cda_component:0/title" to "2. Aktivni zdravstveni problemi:",
            "cda_document/cda_component:0/text" to "Osteoartroza in TEP kolena leta 2000.",
            "cda_document/cda_component:0/text|formalism" to "text/html",
            "cda_document/cda_component:1/name" to "HOSPITAL ADMISSION DX1",
            "cda_document/cda_component:1/templateid" to "1.3.6.1.4.1.19376.1.5.3.1.3.3",
            "cda_document/cda_component:1/code|code" to "46241-6",
            "cda_document/cda_component:1/code|value" to "HOSPITAL ADMISSION DX",
            "cda_document/cda_component:1/title" to "2. Aktivni zdravstveni problemi:",
            "cda_document/cda_component:1/text" to "Osteoartroza in TEP kolena leta 2000.",
            "cda_document/cda_component:1/text|formalism" to "text/html"
        )

        val context = ConversionContext.create()
            .withLanguage("sl")
            .withTerritory("SI")
            .putRmVisitor(Composition::class.java) { _, id -> visited.add(id) }
            .putRmVisitor(Evaluation::class.java) { rm, id -> visited.add(id); names.add((rm as Evaluation).name?.value!!) }
            .putRmVisitor(DvParsable::class.java) { _, id -> visited.add(id) }
            .withComposerName("composer")
            .build()

        webTemplate.convertFromFlatToRaw<Composition>(values, context)
        assertThat(names).containsOnly("HOSPITAL ADMISSION DX", "HOSPITAL ADMISSION DX1")

        assertThat(visited).containsOnly(
            "cda_document",
            "cda_document/cda_component:0",
            "cda_document/cda_component:1",
            "cda_document/cda_component:0/text",
            "cda_document/cda_component:1/text"
        )
    }
}
