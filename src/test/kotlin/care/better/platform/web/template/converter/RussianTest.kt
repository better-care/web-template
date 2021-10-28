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

import care.better.platform.path.NameAndNodeMatchingPathValueExtractor
import care.better.platform.path.PathValueExtractor
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import care.better.platform.web.template.converter.value.SimpleValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.common.Link
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Instruction
import org.openehr.rm.composition.Section
import org.openehr.rm.datatypes.DvEhrUri
import org.openehr.rm.datatypes.DvParsable
import org.openehr.rm.datatypes.DvText
import java.io.IOException
import java.text.NumberFormat
import java.time.OffsetDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class RussianTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testExternalTerminology() {
        val template = getTemplate("/convert/templates/NSI_test_information.opt")
        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val values: Map<String, Any> = ImmutableMap.builder<String, Any>()
                .put("информация/справочная_информация/из_справочника", "123")
                .put("информация/справочная_информация/из_справочника|value", "Первая категория")
                .put("информация/справочная_информация/time", "2012-12-07T02:46:03")
                .build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(values, context)
        assertThat(composition).isNotNull

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap["информация/справочная_информация:0/из_справочника|code"]).isEqualTo("123")
        assertThat(flatMap["информация/справочная_информация:0/из_справочника|value"]).isEqualTo("Первая категория")
        assertThat(flatMap["информация/category|value"]).isEqualTo("episodic")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOccurrences() {
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Test_repeat_5_times.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.tree.children[0]
        assertThat(webTemplateNode.occurences?.getJsonMax()).isEqualTo(-1)
        assertThat(webTemplateNode.children[0].occurences?.getJsonMax()).isEqualTo(5)
        buildAndExport("/convert/templates/Test_repeat_5_times.opt", "test5", "ru", ImmutableSet.of("ru", "en"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOccurrencesUnbounded() {
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Test_repeat_unbounded.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.tree.children[0]
        assertThat(webTemplateNode.occurences?.getJsonMax()).isEqualTo(-1)
        assertThat(webTemplateNode.children[0].occurences?.getJsonMax()).isEqualTo(-1)
        buildAndExport("/convert/templates/Test_repeat_unbounded.opt", "unbounded", "ru", ImmutableSet.of("ru", "en"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInvalidOccurrences() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_gynecologist_anamnesis_pregnant .v1.xml")
        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val compositionBuilder = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val compositionFlatMap: Map<String, Any> = ImmutableMap.builder<String, Any>()
                .put("прием_пациента/метаданные/идентификатор_специалиста", "ва")
                .put("прием_пациента/метаданные/должность|code", "123")
                .put("прием_пациента/метаданные/должность|value", "Медицинская сестра по физиотерапии")
                .put("прием_пациента/метаданные/специальность|code", "1178")
                .put("прием_пациента/метаданные/специальность|value", "Врач-ортодонт")
                .put("прием_пациента/метаданные/структурное_подразделение_медорганизации_автора", "в")
                .put("прием_пациента/метаданные/название_лпу", "в")
                .put("прием_пациента/метаданные/уникальный_идентификатор", "в")
                .put("прием_пациента/метаданные/код_класса_документа", "в")
                .put("прием_пациента/метаданные/дата_и_время_создания_документа", "2013-02-27T00:00:00")
                .put("прием_пациента/метаданные/вид_медицинской_помощи|code", "at0030")
                .put("прием_пациента/метаданные/основной_диагноз|code", "114118")
                .put("прием_пациента/метаданные/основной_диагноз|value", "T67 - Эффекты воздействия высокой температуры и света")
                .put("прием_пациента/метаданные/клиническая_область|code", "at0039")
                .put("прием_пациента/метаданные/название_документа", "ы")
                .put("прием_пациента/метаданные/комментарии", "ы")
                .put("прием_пациента/социально-бытовые_условия:0/жилищные_условия/тип_жилья|code", "at0006")
                .put("прием_пациента/социально-бытовые_условия:0/тяжелая_физическая_работа", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/гепатит", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/герпес", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/цитомегаловирус", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/ветрянка", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/краснуха", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/корь", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/паротит", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/бесплодие", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/пороки_сердца_без_нарушения_кровообращения", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/пороки_сердца_с_нарушением_кровообращения", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/гипертоническая_болезнь", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/вегето-сосудистая_дистония", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/варикозное_расширение_вен", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/рубец_на_матке", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/гемотрансфузии", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:0/без_особенностей", "true")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:0/непереносимость_лекарственных_средств", "л")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:0/аллергологические_заболевания_в_анамнезе", "л")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:1/без_особенностей", "true")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:1/непереносимость_лекарственных_средств", "г")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:1/аллергологические_заболевания_в_анамнезе", "г")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/профессиональные_вредности", "false")
                .put("прием_пациента/перенесенные_заболевания_и_операции:0/эмоциональные_нагрузки", "false")
                .put("прием_пациента/анамнез_жизни_беременной/получала_препараты_содержащие_актг_и_гормоны_надпочечников", "false")
                .put("прием_пациента/исходы_предыдущих_беременностей:0/аномалия_развития_у_детей", "false")
                .put("прием_пациента/исходы_предыдущих_беременностей:0/неврологические_нарушения_у_детей", "false")
                .put("прием_пациента/метаданные/доступность_документа", "at0019")
                .put("прием_пациента/социально-бытовые_условия:0/бытовые_условия", "at0015")
                .put("прием_пациента/социально-бытовые_условия:0/беременность", "at0018")
                .put("прием_пациента/санитарно-эпидемиологический_анамнез:0/прививки", "at0018")
                .put("прием_пациента/санитарно-эпидемиологический_анамнез:0/находилась_в_районах_опасных_по_эпидемиологической_ситуации", "at0025")
                .put("прием_пациента/санитарно-эпидемиологический_анамнез:0/выезжала_ли_за_границу", "at0027")
                .put("прием_пациента/санитарно-эпидемиологический_анамнез:0/беседа_о_сан.-эпид._режиме", "at0030")
                .put("прием_пациента/санитарно-эпидемиологический_анамнез:0/контакт_с_туберкулезными_больными", "at0033")
                .put("прием_пациента/санитарно-эпидемиологический_анамнез:0/педикулез", "at0036")
                .put("прием_пациента/санитарно-эпидемиологический_анамнез:0/трихофития", "at0039")
                .put("прием_пациента/исходы_предыдущих_беременностей:0/ребенок_родился", "at0015")
                .put("прием_пациента/исходы_предыдущих_беременностей:0/ребенок", "at0019")
                .build()

        val composition: Composition? = compositionBuilder.convertFromFlatToRaw(compositionFlatMap, context)
        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = compositionBuilder.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve["прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:0/непереносимость_лекарственных_средств"]).isEqualTo("л")
        assertThat(retrieve["прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:1/непереносимость_лекарственных_средств"]).isEqualTo("г")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondInvalidOccurrences() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_gynecologist_prophylactic_examination.v1.xml"
        val template = getTemplate(templateName)
        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val compositionBuilder = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val compositionFlatMap: Map<String, Any> = ImmutableMap.builder<String, Any>()
                .put("прием_пациента/метаданные/идентификатор_специалиста", "j")
                .put("прием_пациента/метаданные/должность|code", "123")
                .put("прием_пациента/метаданные/должность|value", "Медицинская сестра по физиотерапии")
                .put("прием_пациента/метаданные/специальность|code", "1178")
                .put("прием_пациента/метаданные/специальность|value", "Врач-ортодонт")
                .put("прием_пациента/метаданные/структурное_подразделение_медорганизации_автора", "s")
                .put("прием_пациента/метаданные/название_лпу", "s")
                .put("прием_пациента/метаданные/уникальный_идентификатор", "s")
                .put("прием_пациента/метаданные/код_класса_документа", "s")
                .put("прием_пациента/метаданные/дата_и_время_создания_документа", "2013-02-27T00:00:00")
                .put("прием_пациента/метаданные/вид_медицинской_помощи|code", "at0030")
                .put("прием_пациента/метаданные/основной_диагноз|code", "114118")
                .put("прием_пациента/метаданные/основной_диагноз|value", "T67 - Эффекты воздействия высокой температуры и света")
                .put("прием_пациента/метаданные/клиническая_область|code", "at0039")
                .put("прием_пациента/метаданные/название_документа", "s")
                .put("прием_пациента/метаданные/комментарии", "s")
                .put("прием_пациента/общая_информация/тип_приема|code", "at0066")
                .put("прием_пациента/общий_осмотр/молочные_железы:0/не_изменены", "false")
                .put("прием_пациента/общий_осмотр/молочные_железы:0/нет_отделяемого_из_сосков", "false")
                .put("прием_пациента/общий_осмотр/язык/чистый", "false")
                .put("прием_пациента/общий_осмотр/живот/участвует_в_акте_дыхания", "false")
                .put("прием_пациента/заключение_осмотра/основной_диагноз/основной_диагноз", "false")
                .put("прием_пациента/заключение_осмотра/основной_диагноз/код_по_мкб|code", "114118")
                .put("прием_пациента/заключение_осмотра/основной_диагноз/код_по_мкб|value", "T67 - Эффекты воздействия высокой температуры и света")
                .put("прием_пациента/метаданные/доступность_документа", "at0019")
                .put("прием_пациента/общая_информация/место_приема", "at0067")
                .put("прием_пациента/общая_информация/цель_посещения", "at0071")
                .put("прием_пациента/общий_осмотр/общий_осмотр/общее_состояние", "at0005")
                .put("прием_пациента/общий_осмотр/видимые_кожные_покровы/окраска", "at0080")
                .put("прием_пациента/общий_осмотр/молочные_железы:0/пальпация", "at0011")
                .put("прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/размеры", "at0006")
                .put("прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:0/локализация", "at0010")
                .put("прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:0/консистенция", "at0014")
                .put("прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:0/болезненность", "at0016")
                .put("прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:1/локализация", "at0009")
                .put("прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:1/консистенция", "at0013")
                .put("прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:1/болезненность", "at0016")
                .put("прием_пациента/общий_осмотр/язык/влажность", "at0012")
                .put("прием_пациента/общий_осмотр/живот/состояние", "at0175")
                .put("прием_пациента/общий_осмотр/живот/болезненность_при_пальпации", "at0182")
                .put("прием_пациента/общий_осмотр/мочевыводящая_система/мочеиспускание", "at0005")
                .put("прием_пациента/общий_осмотр/мочевыводящая_система/симптом_пастернацкого", "at0024")
                .put("прием_пациента/общий_осмотр/мочевыводящая_система/область_почек", "at0044")
                .put("прием_пациента/гинекологический_осмотр:0/основные_данные/состояние_половой_щели", "at0014")
                .put("прием_пациента/гинекологический_осмотр:0/основные_данные/опущение_стенок_влагалища", "at0017")
                .put("прием_пациента/гинекологический_осмотр:0/шейка_матки:0/слизистая", "at0005")
                .put("прием_пациента/гинекологический_осмотр:0/шейка_матки:0/форма_шейки_матки", "at0012")
                .put("прием_пациента/гинекологический_осмотр:0/шейка_матки:0/зев_шейки_матки", "at0016")
                .put("прием_пациента/гинекологический_осмотр:0/шейка_матки:0/симптом_зрачка", "at0019")
                .put("прием_пациента/гинекологический_осмотр:0/тело_матки:0/положение", "at0007")
                .put("прием_пациента/гинекологический_осмотр:0/тело_матки:0/размеры", "at0011")
                .put("прием_пациента/гинекологический_осмотр:0/тело_матки:0/консистенция", "at0013")
                .put("прием_пациента/гинекологический_осмотр:0/тело_матки:0/болезненность", "at0016")
                .put("прием_пациента/гинекологический_осмотр:0/своды/проходимы", "at0005")
                .put("прием_пациента/гинекологический_осмотр:0/правые_придатки:0/пальпация", "at0019")
                .put("прием_пациента/гинекологический_осмотр:0/правые_придатки:0/болезненность", "at0027")
                .put("прием_пациента/гинекологический_осмотр:0/левые_придатки/пальпация", "at0019")
                .put("прием_пациента/гинекологический_осмотр:0/левые_придатки/болезненность", "at0027")
                .build()

        val composition: Composition? = compositionBuilder.convertFromFlatToRaw(compositionFlatMap, context)
        assertThat(composition).isNotNull

        val retrieve: Map<String, Any?> = compositionBuilder.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieve["прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:0/локализация|code"]).isEqualTo("at0010")
        assertThat(retrieve["прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:1/локализация|code"]).isEqualTo("at0009")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testNonCompacted() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_new_rheumatologist_examination.opt"
        val template = getTemplate(templateName)
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("прием_пациента/общий_осмотр/общий_осмотр_больного/простые_неврологические_тесты/поза_ромберга")
        assertThat(node.rmType).isEqualTo("DV_CODED_TEXT")
        buildAndExport(templateName, "medium", "ru", ImmutableSet.of("ru", "en"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAnnotations() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_anamnesis_gynecologist.opt"
        val template = getTemplate(templateName)
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val node: WebTemplateNode = webTemplate.tree.children[1].children[5]
        assertThat(node.annotations).contains(entry("default", "hideOnForm"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testActivity() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_new_physiatrist_examination.v1.xml"
        val template = getTemplate(templateName)
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val compositionFlatMap: Map<String, Any> = ImmutableMap.builder<String, Any>()
                .put("прием_пациента/административная_информация/автор_документа", "user")
                .put("прием_пациента/административная_информация/вид_медпомощи|code", "at0024")
                .put("прием_пациента/административная_информация/вид_медпомощи|value", "амбулаторная медицинская помощь")
                .put("прием_пациента/административная_информация/дата_приема", "2013-03-19T00:00:00.000+06:00")
                .put("прием_пациента/административная_информация/документ_создан", "2013-03-19T10:42:52.000+06:00")
                .put("прием_пациента/административная_информация/должность|code", "123")
                .put("прием_пациента/административная_информация/должность|value", "Медицинская сестра по физиотерапии")
                .put("прием_пациента/административная_информация/клиническая_область|code", "at0005")
                .put("прием_пациента/административная_информация/клиническая_область|value", "терапия")
                .put("прием_пациента/административная_информация/медицинское_учреждение|code", "770038")
                .put("прием_пациента/административная_информация/медицинское_учреждение|value", "ГБУЗ ГП № 138 ДЗМ")
                .put("прием_пациента/административная_информация/название_документа", "openEHR-EHR-COMPOSITION.t_new_rheumatologist_examination.v1 2013-03-19T00:00:00")
                .put("прием_пациента/административная_информация/основной_диагноз|code", "114118")
                .put("прием_пациента/административная_информация/основной_диагноз|value", "T67 - Эффекты воздействия высокой температуры и света")
                .put("прием_пациента/административная_информация/отделение", "authorInstitution")
                .put("прием_пациента/административная_информация/специальность|code", "1178")
                .put("прием_пациента/административная_информация/специальность|value", "Врач-ортодонт")
                .put("прием_пациента/административная_информация/статус_документа|value", "черновик")
                .put("прием_пациента/административная_информация/статус_документа|code", "at0014")
                .put("прием_пациента/общая_информация/тип_приема|code", "at0026")
                .put("прием_пациента/общая_информация/тип_приема|value", "первичный")
                .put("прием_пациента/общая_информация/место_приема|value", "поликлиника")
                .put("прием_пациента/общая_информация/цель_посещения|value", "заболевание")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|code", "114118")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|value", "T67 - Эффекты воздействия высокой температуры и света")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|value", "острое")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/учитывать_в_листе_уточненных_диагнозов", "false")
                .put("прием_пациента/диагноз_и_результат_обращения/осложнение_основного_диагноза:0/учитывать_в_листе_уточненных_диагнозов", "false")
                .put("прием_пациента/диагноз_и_результат_обращения/сопутствующий_диагноз:0/учитывать_в_листе_уточненных_диагнозов", "false")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|code", "at0051")
                .put("прием_пациента/общая_информация/место_приема|code", "at0067")
                .put("прием_пациента/общая_информация/цель_посещения|code", "at0070")
                .put("прием_пациента/процедуры/narrative", "narrative")
                .put("прием_пациента/процедуры/request:0/timing", "R0")
                .put("прием_пациента/процедуры/request:0/название_процедуры|code", "синусные модульные токи")
                .put("прием_пациента/процедуры/request:0/название_процедуры|value", "синусные модульные токи")
                .put("прием_пациента/процедуры/request:0/описание_процедуры", "1")
                .put("прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|magnitude", "1")
                .put("прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|unit", "/d")
                .build()

        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testActivityWithProviders() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_new_physiatrist_examination.v1.xml"
        val template = getTemplate(templateName)
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val compositionFlatMap: Map<String, Any> = ImmutableMap.builder<String, Any>()
                .put("прием_пациента/административная_информация/автор_документа", "user")
                .put("прием_пациента/административная_информация/вид_медпомощи|code", "at0024")
                .put("прием_пациента/административная_информация/вид_медпомощи|value", "амбулаторная медицинская помощь")
                .put("прием_пациента/административная_информация/дата_приема", "2013-03-19T00:00:00.000+06:00")
                .put("прием_пациента/административная_информация/документ_создан", "2013-03-19T10:42:52.000+06:00")
                .put("прием_пациента/административная_информация/должность|code", "123")
                .put("прием_пациента/административная_информация/должность|value", "Медицинская сестра по физиотерапии")
                .put("прием_пациента/административная_информация/клиническая_область|code", "at0005")
                .put("прием_пациента/административная_информация/клиническая_область|value", "терапия")
                .put("прием_пациента/административная_информация/медицинское_учреждение|code", "770038")
                .put("прием_пациента/административная_информация/медицинское_учреждение|value", "ГБУЗ ГП № 138 ДЗМ")
                .put("прием_пациента/административная_информация/название_документа", "openEHR-EHR-COMPOSITION.t_new_rheumatologist_examination.v1 2013-03-19T00:00:00")
                .put("прием_пациента/административная_информация/основной_диагноз|code", "114118")
                .put("прием_пациента/административная_информация/основной_диагноз|value", "T67 - Эффекты воздействия высокой температуры и света")
                .put("прием_пациента/административная_информация/отделение", "authorInstitution")
                .put("прием_пациента/административная_информация/специальность|code", "1178")
                .put("прием_пациента/административная_информация/специальность|value", "Врач-ортодонт")
                .put("прием_пациента/административная_информация/статус_документа|value", "черновик")
                .put("прием_пациента/административная_информация/статус_документа|code", "at0014")
                .put("прием_пациента/общая_информация/тип_приема|code", "at0026")
                .put("прием_пациента/общая_информация/тип_приема|value", "первичный")
                .put("прием_пациента/общая_информация/место_приема|value", "поликлиника")
                .put("прием_пациента/общая_информация/цель_посещения|value", "заболевание")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|code", "114118")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|value", "T67 - Эффекты воздействия высокой температуры и света")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|value", "острое")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/учитывать_в_листе_уточненных_диагнозов", "false")
                .put("прием_пациента/диагноз_и_результат_обращения/осложнение_основного_диагноза:0/учитывать_в_листе_уточненных_диагнозов", "false")
                .put("прием_пациента/диагноз_и_результат_обращения/сопутствующий_диагноз:0/учитывать_в_листе_уточненных_диагнозов", "false")
                .put("прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|code", "at0051")
                .put("прием_пациента/общая_информация/место_приема|code", "at0067")
                .put("прием_пациента/общая_информация/цель_посещения|code", "at0070")
                .put("прием_пациента/процедуры/request:0/название_процедуры|code", "синусные модульные токи")
                .put("прием_пациента/процедуры/request:0/название_процедуры|value", "синусные модульные токи")
                .put("прием_пациента/процедуры/request:0/описание_процедуры", "1")
                .put("прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|magnitude", "1")
                .put("прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|unit", "/d")
                .build()
        val context = ConversionContext.create().withLanguage("ru")
           .withTerritory("RU")
           .withComposerName("composer")
           .withActivityTimingProvider {
               DvParsable().apply {
                   this.formalism = "timing"
                   this.value = "R0"
               }
           }.withInstructionNarrativeProvider {
                    DvText("Description of what instruction is about!")
           }.build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testDentalFormule() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_dental_formule.v1.xml"
        val template = getTemplate(templateName)
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val parameters: Map<String, String> = mapOf(
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/название_документа", "Прием пациента врачом-стоматологом-хирургом 23.04.2013"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/дата_приема", "2013-04-26T00:00:00"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/клиническая_область|code", "at0005"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/клиническая_область|value", "терапия"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/автор_документа", "user"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/должность|code", "123"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/должность|value", "Медицинская сестра по физиотерапии"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/специальность|code", "0.1"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/специальность|value", "Врачебные специальности. Лечебное дело. Педиатрия"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/отделение", "Неизвестное отделение"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/медицинское_учреждение|code", "174"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/медицинское_учреждение|value", "174"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/вид_медпомощи|code", "at0024"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/вид_медпомощи|value", "амбулаторная медицинская помощь"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/основной_диагноз|code", "Z04.9"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/основной_диагноз|value", "Обсл. и набл. по неуточ. поводам"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/документ_создан", "2013-04-26T00:00:00"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/статус_документа|code", "at0014"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/административная_информация/статус_документа|value", "черновик"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/стоматологический_осмотр/зубная_формула/осмотр_полости_рта_32/a18/состояние_зуба_до_лечения|code", "at0010"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/стоматологический_осмотр/зубная_формула/осмотр_полости_рта_32/a18/состояние_зуба_до_лечения|value", "П/С"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/стоматологический_осмотр/зубная_формула/осмотр_полости_рта_16/a48/состояние_зуба_до_лечения|code", "at0009"),
            Pair("прием_пациента_врачом-стоматологом-хирургом/стоматологический_осмотр/зубная_формула/осмотр_полости_рта_16/a48/состояние_зуба_до_лечения|value", "П"))

        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(parameters, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testLinks() {
        val webTemplate = WebTemplateBuilder.buildNonNull(
                getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_dermatologist_examination.v1.xml"),
                WebTemplateBuilderContext("ru"))

        val composition = getComposition("/convert/compositions/composition_with_Dv_Interval.xml").apply {
            this.links.add(Link().apply {
                this.target = DvEhrUri().apply { this.value =  "ehr:///c1"}
                this.meaning = DvText("follow up")
                this.type = DvText("issue")
            })

            this.links.add(Link().apply {
                this.target = DvEhrUri().apply { this.value =  "ehr:///c2"}
                this.meaning = DvText("follow up2")
                this.type = DvText("issue2")
            })
        }

        val retrieve: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition, FromRawConversion.create())
        assertThat(retrieve).contains(
                entry("прием_пациента_врачом-дерматолог/_link:0|meaning", "follow up"),
                entry("прием_пациента_врачом-дерматолог/_link:0|type", "issue"),
                entry("прием_пациента_врачом-дерматолог/_link:0|target", "ehr:///c1"),
                entry("прием_пациента_врачом-дерматолог/_link:1|meaning", "follow up2"),
                entry("прием_пациента_врачом-дерматолог/_link:1|type", "issue2"),
                entry("прием_пациента_врачом-дерматолог/_link:1|target", "ehr:///c2"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testIntervalQuantity() {
        val templateName = "/convert/templates/openEHR-EHR-COMPOSITION.t_interval_quantity_test.v1.xml"
        val template = getTemplate(templateName)
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val compositionFlatMap: Map<String, Any> = mapOf(
            Pair("test/административная_информация/автор_документа", "User"),
            Pair("test/административная_информация/вид_медпомощи|code", "at0024"),
            Pair("test/административная_информация/вид_медпомощи|terminology", "local"),
            Pair("test/административная_информация/вид_медпомощи|value", "амбулаторная медицинская помощь"),
            Pair("test/административная_информация/дата_приема", "2013-06-03T00:00:00.000+06:00"),
            Pair("test/административная_информация/документ_создан", "2013-06-03T13:29:45.000+06:00"),
            Pair("test/административная_информация/должность|code", "135"),
            Pair("test/административная_информация/должность|terminology", "NSI"),
            Pair("test/административная_информация/должность|value", "Средний медицинский персонал: Медицинская сестра по физиотерапии"),
            Pair("test/административная_информация/клиническая_область|code", "at0005"),
            Pair("test/административная_информация/клиническая_область|terminology", "local"),
            Pair("test/административная_информация/клиническая_область|value", "терапия"),
            Pair("test/административная_информация/медицинское_учреждение|code", "174"),
            Pair("test/административная_информация/медицинское_учреждение|terminology", "external"),
            Pair("test/административная_информация/медицинское_учреждение|value", "Городская поликлиника № 67"),
            Pair("test/административная_информация/название_документа", "test 03.06.2013"),
            Pair("test/административная_информация/основной_диагноз|code", "A01.3"),
            Pair("test/административная_информация/основной_диагноз|terminology", "NSI"),
            Pair("test/административная_информация/основной_диагноз|value", "Паратиф C"),
            Pair("test/административная_информация/отделение", "Неизвестное отделение"),
            Pair("test/административная_информация/специальность|code", "0.1"),
            Pair("test/административная_информация/специальность|terminology", "NSI"),
            Pair("test/административная_информация/специальность|value", "Врачебные специальности. Лечебное дело. Педиатрия"),
            Pair("test/административная_информация/статус_документа|code", "at0014"),
            Pair("test/административная_информация/статус_документа|terminology", "local"),
            Pair("test/административная_информация/статус_документа|value", "черновик"),
            Pair("test/interval_quantity/fiels_for_test/upper", "90"),
            Pair("test/interval_quantity/fiels_for_test/lower", "120"))

        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(compositionFlatMap, context)
        assertThat(composition).isNotNull
        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("test/interval_quantity/fiels_for_test/lower|magnitude", 120.0))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testNPE() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_pediatrist_examination (0-12)_lanit.opt")
        val context = WebTemplateBuilderContext(template.language!!.codeString)
        WebTemplateBuilder.buildNonNull(template, context)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testMultipleExternalTerminologies() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_allergist_examination_child_lanit.opt")
        val context = WebTemplateBuilderContext(template.language!!.codeString)
        val webTemplate = WebTemplateBuilder.buildNonNull(template, context)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("осмотр_аллерголога-иммунолога/интерпретация_результатов_обследования/интерпретация_результатов_обследования/фвд_-_спирометрия/интерпретация_результатов/препарат/мнн")
        assertThat(node.inputs[0].terminology).isEqualTo("NSI?subset=DRUGS_EXT_MNN_ALLERG_CH&language=GB")
        assertThat(node.inputs[0].otherTerminologies).containsOnly("NSI?subset=DRUGS_EXT_MNN_V")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testUntypedInterval() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_anamnesis_vitae_pediatrist.opt")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))
        assertThat(WebTemplateBuilder.buildNonNull(template, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testMissingInstruction() {
        val template = getTemplate("/convert/templates/opt referral.xml")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))
        val webTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val structuredComposition: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/missingInstruction.json")) as ObjectNode

        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").build()
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(structuredComposition, builderContext)
        assertThat(composition).isNotNull

        val section = composition!!.content[0] as Section
        assertThat(section.items[0]).isInstanceOf(Instruction::class.java)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDvTextWithTerminologyOverridedCodedText() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_laboratory_test_result_report.opt")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))

        val webTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("результат_лабораторного_исследования/результат_исследования/лабораторное_исследование/любое_событие/статус")
        val input: WebTemplateInput = node.getInput()!!
        assertThat(input.list).hasSize(9)
        assertThat(input.otherTerminologies).hasSize(1)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testRuTherapistExamination() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_therapist_examination.opt")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))
        val webTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").build()

        val firstFlatComposition: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/therapist_saved.json"), object : TypeReference<Map<String, Any?>>(){})
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, builderContext)
        assertThat(firstComposition?.content ?: emptyList()).hasSize(5)

        val secondFlatComposition: Map<String, Any?> = getObjectMapper().readValue(getJson("/convert/compositions/therapist_saved_fixed.json"), object : TypeReference<Map<String, Any?>>(){})

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, builderContext)
        assertThat(secondComposition!!.content).hasSize(6)
        assertThat(secondComposition.content[5].name!!.value).isEqualTo("Сведения о выполнении назначения")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCardiologistExamination() {
        val webTemplate = WebTemplateBuilder.buildNonNull(
                getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_cardiologist_examination.v3.xml"),
                WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru")))
        assertThat(webTemplate).isNotNull

        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").build()

        val firstStructuredComposition: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/cardio.json")) as ObjectNode
        val firstComposition: Composition? = webTemplate.convertFromStructuredToRaw(firstStructuredComposition, builderContext)
        assertThat(firstComposition).isNotNull

        val secondWebTemplate = WebTemplateBuilder.buildNonNull(
                getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_cardiologist_examination.v3-fix.xml"),
                WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru")))
        assertThat(secondWebTemplate).isNotNull

        val secondComposition: Composition? = secondWebTemplate.convertFromStructuredToRaw(firstStructuredComposition, builderContext)
        assertThat(secondComposition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testPrevaccinalExamination() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.prevaccinal_examination.opt")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))
        val webTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").build()

        val structuredComposition: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/vaccination.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(structuredComposition, builderContext)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVaccinationCard() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.vaccination_card.opt")
        val context = WebTemplateBuilderContext("ru", ImmutableSet.of("en", "ru"))
        val webTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val builderContext = ConversionContext.create()
                .withLanguage("ru")
                .withTerritory("RU")
                .withValueConvert(SimpleValueConverter)
                .build()

        val structuredComposition: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/vaccination_card_composition.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(structuredComposition, builderContext)

        assertThat(webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create()))
                .contains(entry("карта_профилактических_прививок/туберкулезные_пробы/заготовка_заголовка:0/результат_иммунодиагностики/дата", Year.of(2013)))

        assertThat(webTemplate.convertFormattedFromRawToFlat(composition, FromRawConversion.create()))
                .contains(entry("карта_профилактических_прививок/туберкулезные_пробы/заготовка_заголовка:0/результат_иммунодиагностики/дата", "2013"))
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testVitals() {
        val template = getTemplate("/convert/templates/Demo Vitals.opt")
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()

        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("sl"))
        val flatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/time", "2015-01-01T10:00:00.000+05:00")
                .put("vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude", "37.7")
                .put("vitals/vitals/body_temperature:0/any_event:0/temperature|unit", "°C")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(flatComposition, context)

        val valueConverter: ValueConverter = RussianLocaleValueConverter(Locale("ru", "RU"))

        val secondContext = ConversionContext.create()
                .withLanguage("sl")
                .withTerritory("SI")
                .withComposerName("composer")
                .withValueConvert(valueConverter)
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(
                webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create(valueConverter)),
                secondContext)
        assertThat(secondComposition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testAssesmentReferral() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_sociomedical_assessment_referral.v1.xml")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))

        val json: String = getJson("/convert/compositions/openEHR-EHR-COMPOSITION.t_sociomedical_assessment_referral.v1.json").replace("\r\n", "\n")

        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(json) as ObjectNode, context)
        assertThat(composition).isNotNull

        val extractor: PathValueExtractor = NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.container_simi.v0,'Сведения о врачебной комиссии']/data[at0001]/items[openEHR-EHR-CLUSTER.health_authorities_simi.v0]/items[at0005]/value")
        val values = extractor.getValue(composition)
        assertThat(values).hasSize(1)
        assertThat(values[0]).isExactlyInstanceOf(DvText::class.java)
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testReferenceForm() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_outpatient_reference_form_025_1.v4.xml")
        val webTemplate = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val json: String = getJson("/convert/compositions/openEHR-EHR-COMPOSITION.t_outpatient_reference_form_025_1.v4-composition.json").replace("\r\n", "\n")

        val context = ConversionContext.create()
                .withLanguage("ru")
                .withTerritory("RU")
                .withComposerName("composer")
                .withValueConvert(LocaleBasedValueConverter(Locale("ru", "RU")))
                .build()

        val composition: Composition? = webTemplate.convertFromStructuredToRaw(getObjectMapper().readTree(json) as ObjectNode, context)
        assertThat(composition).isNotNull
    }


    private class RussianLocaleValueConverter(val locale: Locale) : ValueConverter by LocaleBasedValueConverter(locale) {
        override fun parseDouble(value: String): Double {
            try {
                return NumberFormat.getInstance(locale).parse(value).toDouble()
            } catch (ignored: Exception) {
            }
            return value.replace(",", ".").toDouble()
        }

        override fun formatDouble(value: Double): String = NumberFormat.getInstance(locale).format(value)

        override fun formatDateTime(dateTime: OffsetDateTime): String =
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(locale).format(dateTime)
    }
}
