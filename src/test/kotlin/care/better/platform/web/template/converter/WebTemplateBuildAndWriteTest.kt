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
import com.google.common.collect.ImmutableSet
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class WebTemplateBuildAndWriteTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testPerinatal() {
        buildAndExport("/convert/templates/MED - Perinatal history Summary.opt", "perinatal")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testVital() {
        buildAndExport("/convert/templates/ZN - Vital Functions Encounter.opt", "vital-all")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAnamnesis() {
        buildAndExport("/convert/templates/MED - Medical Admission History.opt", "anamnesis")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testVitals() {
        buildAndExport("/convert/templates/Demo Vitals.opt", "vitals")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInitialMedicationSafety() {
        buildAndExport("/convert/templates/MSE - Initial Medication Safety Report.opt", "initialMedicationSafety")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMedicationErrorReport() {
        buildAndExport("/convert/templates/MSE - Medication Error Report.xml", "medicationErrorReport")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMedicationEventCaseSummary() {
        buildAndExport("/convert/templates/MSE - Medication Event Case Summary.opt", "medicationEventCaseSummary")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAdverseReaction() {
        buildAndExport("/convert/templates/adverse4.opt", "adverse", "sl", ImmutableSet.of("en", "sl"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testRussian() {
        buildAndExport("/convert/templates/openEHR-EHR-COMPOSITION.t_primary_therapeutist_examination.opt", "therapeutist_examination")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testRadImaging() {
        buildAndExport("/convert/templates/RAD - Imaging status event.opt", "imagingStatus")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testErco() {
        buildAndExport("/convert/templates/Vaccination Record.opt", "erco")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDefaultValues() {
        buildAndExport("/convert/templates/Новый Шаблон.opt", "test")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testParsable() {
        buildAndExport("/convert/templates/MED - Document.opt", "meddoc")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testScales() {
        val builderContext = WebTemplateBuilderContext("sl")
        writeOut("scales", WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ZN - Assessment Scales Encounter.opt"), builderContext))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCabinet() {
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Cabinet.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.tree.children[0].children[0]
        assertThat(webTemplateNode.getInput()?.defaultValue).isEqualTo("at0007")
        writeOut("cabinet", webTemplate)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testCabinet123() {
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Cabinet123.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.tree.children[0].children[0]
        assertThat(webTemplateNode.annotations).contains(
            Assertions.entry("GUI Directives.Widget Type", "List"),
            Assertions.entry("GUI Directives.Show Description", "true"))
        writeOut("cabinet123", webTemplate)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDefaultValue() {
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate: WebTemplate =
            WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_allergologist_examination.opt"), builderContext)
        writeOut("allergology", webTemplate)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testHelloworld() {
        buildAndExport("/convert/templates/helloworld.opt", "helloworld")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAdmission() {
        buildAndExport("/convert/templates/ZN - Admition Summary Encounter new.opt", "admission", "sl", ImmutableSet.of("en", "sl"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testFormsDemo() {
        buildAndExport("/convert/templates/Forms Demo.opt", "formsdemo", "sl", ImmutableSet.of("en", "sl"))
    }

    @Throws(JAXBException::class, IOException::class)
    private fun buildAndExport(templateName: String, prefix: String) {
        buildAndExport(templateName, prefix, "sl", ImmutableSet.of("sl", "en"))
    }
}
