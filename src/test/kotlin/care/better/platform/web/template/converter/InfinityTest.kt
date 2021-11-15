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
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableSet
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import java.util.*
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class InfinityTest : AbstractWebTemplateTest() {

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTemplateMismatch0_12() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_endocrinologist_examination (0-12).opt"
        val template = getTemplate(templateName)

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val locale = Locale(webTemplate.defaultLanguage, webTemplate.defaultLanguage.uppercase())

        val conversionContext = ConversionContext.create()
            .withLanguage(locale.language)
            .withTerritory(locale.country)
            .withEncoding("UTF-8")
            .withComposerName("test")
            .withValueConvert(LocaleBasedValueConverter(locale))
            .build()

        val node: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/openEHR-EHR-COMPOSITION.t_endocrinologist_examination 0-12.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, conversionContext)

        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTemplateMismatch1_17() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_endocrinologist_examination (1-17).opt"
        val template = getTemplate(templateName)

        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val locale = Locale(webTemplate.defaultLanguage, webTemplate.defaultLanguage.uppercase())

        val conversionContext = ConversionContext.create()
            .withLanguage(locale.language)
            .withTerritory(locale.country)
            .withEncoding("UTF-8")
            .withComposerName("test")
            .withValueConvert(LocaleBasedValueConverter(locale))
            .build()

        val node: ObjectNode =
            getObjectMapper().readTree(getJson("/convert/compositions/openEHR-EHR-COMPOSITION.t_endocrinologist_examination 1-17.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, conversionContext)
        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOccurrences() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_oncology_reference_form_027_1.v1.opt")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val firstNode: WebTemplateNode =
            webTemplate.findWebTemplateNodeByAqlPath("/content[openEHR-EHR-SECTION.adhoc.v1,'Форма N 027-1У']/items[openEHR-EHR-ADMIN_ENTRY.oncology_reference_form_027_1_simi.v0,'Форма N 027-1У']/data[at0001]/items[at0012]/items[openEHR-EHR-CLUSTER.cancer_diagnosis_gel-simi.v0,'Опухоль']/items[at0002,'Топография опухоли']/value")
        assertThat(firstNode).isNotNull
        assertThat(firstNode.occurences?.min).isEqualTo(1)

        val secondNode: WebTemplateNode =
            webTemplate.findWebTemplateNodeByAqlPath("/content[openEHR-EHR-SECTION.adhoc.v1,'Форма N 027-1У']/items[openEHR-EHR-ADMIN_ENTRY.oncology_reference_form_027_1_simi.v0,'Форма N 027-1У']/data[at0001]/items[at0012]/items[openEHR-EHR-CLUSTER.cancer_diagnosis_gel-simi.v0,'Опухоль']")

        val nonMandatory: WebTemplateNode? = secondNode.children.firstOrNull { node -> "Морфологический тип опухоли" == node.name }
        assertThat(nonMandatory).isNotNull
        assertThat(nonMandatory?.occurences?.min).isEqualTo(0)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCompositionNotNull() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.prevaccinal_examination.v1.xml")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val locale = Locale(webTemplate.defaultLanguage, webTemplate.defaultLanguage.uppercase())

        val node: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/composition_with_activity.json")) as ObjectNode

        val conversionContext = ConversionContext.create()
            .withLanguage(locale.language)
            .withTerritory(locale.country)
            .withEncoding("UTF-8")
            .withComposerName("test")
            .withValueConvert(LocaleBasedValueConverter(locale))
            .build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(node, conversionContext)
        assertThat(composition).isNotNull
    }
}
