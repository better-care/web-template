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
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import care.better.platform.web.template.converter.value.SimpleValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import jakarta.xml.bind.JAXBException
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
        val values: Map<String, Any> = mapOf(
            "информация/справочная_информация/из_справочника" to "123",
            "информация/справочная_информация/из_справочника|value" to "Первая категория",
            "информация/справочная_информация/time" to "2012-12-07T02:46:03"
        )
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
        buildAndExport("/convert/templates/Test_repeat_5_times.opt", "test5", "ru", setOf("ru", "en"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testOccurrencesUnbounded() {
        val builderContext = WebTemplateBuilderContext("ru")
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Test_repeat_unbounded.opt"), builderContext)
        val webTemplateNode: WebTemplateNode = webTemplate.tree.children[0]
        assertThat(webTemplateNode.occurences?.getJsonMax()).isEqualTo(-1)
        assertThat(webTemplateNode.children[0].occurences?.getJsonMax()).isEqualTo(-1)
        buildAndExport("/convert/templates/Test_repeat_unbounded.opt", "unbounded", "ru", setOf("ru", "en"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInvalidOccurrences() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_gynecologist_anamnesis_pregnant .v1.xml")
        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val compositionBuilder = WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("ru"))
        val compositionFlatMap: Map<String, Any> = mapOf(
            "прием_пациента/метаданные/идентификатор_специалиста" to "ва",
            "прием_пациента/метаданные/должность|code" to "123",
            "прием_пациента/метаданные/должность|value" to "Медицинская сестра по физиотерапии",
            "прием_пациента/метаданные/специальность|code" to "1178",
            "прием_пациента/метаданные/специальность|value" to "Врач-ортодонт",
            "прием_пациента/метаданные/структурное_подразделение_медорганизации_автора" to "в",
            "прием_пациента/метаданные/название_лпу" to "в",
            "прием_пациента/метаданные/уникальный_идентификатор" to "в",
            "прием_пациента/метаданные/код_класса_документа" to "в",
            "прием_пациента/метаданные/дата_и_время_создания_документа" to "2013-02-27T00:00:00",
            "прием_пациента/метаданные/вид_медицинской_помощи|code" to "at0030",
            "прием_пациента/метаданные/основной_диагноз|code" to "114118",
            "прием_пациента/метаданные/основной_диагноз|value" to "T67 - Эффекты воздействия высокой температуры и света",
            "прием_пациента/метаданные/клиническая_область|code" to "at0039",
            "прием_пациента/метаданные/название_документа" to "ы",
            "прием_пациента/метаданные/комментарии" to "ы",
            "прием_пациента/социально-бытовые_условия:0/жилищные_условия/тип_жилья|code" to "at0006",
            "прием_пациента/социально-бытовые_условия:0/тяжелая_физическая_работа" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/гепатит" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/герпес" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/цитомегаловирус" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/ветрянка" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/краснуха" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/корь" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/паротит" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/бесплодие" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/пороки_сердца_без_нарушения_кровообращения" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/пороки_сердца_с_нарушением_кровообращения" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/гипертоническая_болезнь" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/вегето-сосудистая_дистония" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/варикозное_расширение_вен" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/рубец_на_матке" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/гемотрансфузии" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:0/без_особенностей" to "true",
            "прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:0/непереносимость_лекарственных_средств" to "л",
            "прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:0/аллергологические_заболевания_в_анамнезе" to "л",
            "прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:1/без_особенностей" to "true",
            "прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:1/непереносимость_лекарственных_средств" to "г",
            "прием_пациента/перенесенные_заболевания_и_операции:0/аллергологический_анамнез:1/аллергологические_заболевания_в_анамнезе" to "г",
            "прием_пациента/перенесенные_заболевания_и_операции:0/профессиональные_вредности" to "false",
            "прием_пациента/перенесенные_заболевания_и_операции:0/эмоциональные_нагрузки" to "false",
            "прием_пациента/анамнез_жизни_беременной/получала_препараты_содержащие_актг_и_гормоны_надпочечников" to "false",
            "прием_пациента/исходы_предыдущих_беременностей:0/аномалия_развития_у_детей" to "false",
            "прием_пациента/исходы_предыдущих_беременностей:0/неврологические_нарушения_у_детей" to "false",
            "прием_пациента/метаданные/доступность_документа" to "at0019",
            "прием_пациента/социально-бытовые_условия:0/бытовые_условия" to "at0015",
            "прием_пациента/социально-бытовые_условия:0/беременность" to "at0018",
            "прием_пациента/санитарно-эпидемиологический_анамнез:0/прививки" to "at0018",
            "прием_пациента/санитарно-эпидемиологический_анамнез:0/находилась_в_районах_опасных_по_эпидемиологической_ситуации" to "at0025",
            "прием_пациента/санитарно-эпидемиологический_анамнез:0/выезжала_ли_за_границу" to "at0027",
            "прием_пациента/санитарно-эпидемиологический_анамнез:0/беседа_о_сан.-эпид._режиме" to "at0030",
            "прием_пациента/санитарно-эпидемиологический_анамнез:0/контакт_с_туберкулезными_больными" to "at0033",
            "прием_пациента/санитарно-эпидемиологический_анамнез:0/педикулез" to "at0036",
            "прием_пациента/санитарно-эпидемиологический_анамнез:0/трихофития" to "at0039",
            "прием_пациента/исходы_предыдущих_беременностей:0/ребенок_родился" to "at0015",
            "прием_пациента/исходы_предыдущих_беременностей:0/ребенок" to "at0019"
        )

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
        val compositionFlatMap: Map<String, Any> = mapOf(
            "прием_пациента/метаданные/идентификатор_специалиста" to "j",
            "прием_пациента/метаданные/должность|code" to "123",
            "прием_пациента/метаданные/должность|value" to "Медицинская сестра по физиотерапии",
            "прием_пациента/метаданные/специальность|code" to "1178",
            "прием_пациента/метаданные/специальность|value" to "Врач-ортодонт",
            "прием_пациента/метаданные/структурное_подразделение_медорганизации_автора" to "s",
            "прием_пациента/метаданные/название_лпу" to "s",
            "прием_пациента/метаданные/уникальный_идентификатор" to "s",
            "прием_пациента/метаданные/код_класса_документа" to "s",
            "прием_пациента/метаданные/дата_и_время_создания_документа" to "2013-02-27T00:00:00",
            "прием_пациента/метаданные/вид_медицинской_помощи|code" to "at0030",
            "прием_пациента/метаданные/основной_диагноз|code" to "114118",
            "прием_пациента/метаданные/основной_диагноз|value" to "T67 - Эффекты воздействия высокой температуры и света",
            "прием_пациента/метаданные/клиническая_область|code" to "at0039",
            "прием_пациента/метаданные/название_документа" to "s",
            "прием_пациента/метаданные/комментарии" to "s",
            "прием_пациента/общая_информация/тип_приема|code" to "at0066",
            "прием_пациента/общий_осмотр/молочные_железы:0/не_изменены" to "false",
            "прием_пациента/общий_осмотр/молочные_железы:0/нет_отделяемого_из_сосков" to "false",
            "прием_пациента/общий_осмотр/язык/чистый" to "false",
            "прием_пациента/общий_осмотр/живот/участвует_в_акте_дыхания" to "false",
            "прием_пациента/заключение_осмотра/основной_диагноз/основной_диагноз" to "false",
            "прием_пациента/заключение_осмотра/основной_диагноз/код_по_мкб|code" to "114118",
            "прием_пациента/заключение_осмотра/основной_диагноз/код_по_мкб|value" to "T67 - Эффекты воздействия высокой температуры и света",
            "прием_пациента/метаданные/доступность_документа" to "at0019",
            "прием_пациента/общая_информация/место_приема" to "at0067",
            "прием_пациента/общая_информация/цель_посещения" to "at0071",
            "прием_пациента/общий_осмотр/общий_осмотр/общее_состояние" to "at0005",
            "прием_пациента/общий_осмотр/видимые_кожные_покровы/окраска" to "at0080",
            "прием_пациента/общий_осмотр/молочные_железы:0/пальпация" to "at0011",
            "прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/размеры" to "at0006",
            "прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:0/локализация" to "at0010",
            "прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:0/консистенция" to "at0014",
            "прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:0/болезненность" to "at0016",
            "прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:1/локализация" to "at0009",
            "прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:1/консистенция" to "at0013",
            "прием_пациента/общий_осмотр/периферические_лимфатические_узлы:0/лимфатические_узлы_увеличены:1/болезненность" to "at0016",
            "прием_пациента/общий_осмотр/язык/влажность" to "at0012",
            "прием_пациента/общий_осмотр/живот/состояние" to "at0175",
            "прием_пациента/общий_осмотр/живот/болезненность_при_пальпации" to "at0182",
            "прием_пациента/общий_осмотр/мочевыводящая_система/мочеиспускание" to "at0005",
            "прием_пациента/общий_осмотр/мочевыводящая_система/симптом_пастернацкого" to "at0024",
            "прием_пациента/общий_осмотр/мочевыводящая_система/область_почек" to "at0044",
            "прием_пациента/гинекологический_осмотр:0/основные_данные/состояние_половой_щели" to "at0014",
            "прием_пациента/гинекологический_осмотр:0/основные_данные/опущение_стенок_влагалища" to "at0017",
            "прием_пациента/гинекологический_осмотр:0/шейка_матки:0/слизистая" to "at0005",
            "прием_пациента/гинекологический_осмотр:0/шейка_матки:0/форма_шейки_матки" to "at0012",
            "прием_пациента/гинекологический_осмотр:0/шейка_матки:0/зев_шейки_матки" to "at0016",
            "прием_пациента/гинекологический_осмотр:0/шейка_матки:0/симптом_зрачка" to "at0019",
            "прием_пациента/гинекологический_осмотр:0/тело_матки:0/положение" to "at0007",
            "прием_пациента/гинекологический_осмотр:0/тело_матки:0/размеры" to "at0011",
            "прием_пациента/гинекологический_осмотр:0/тело_матки:0/консистенция" to "at0013",
            "прием_пациента/гинекологический_осмотр:0/тело_матки:0/болезненность" to "at0016",
            "прием_пациента/гинекологический_осмотр:0/своды/проходимы" to "at0005",
            "прием_пациента/гинекологический_осмотр:0/правые_придатки:0/пальпация" to "at0019",
            "прием_пациента/гинекологический_осмотр:0/правые_придатки:0/болезненность" to "at0027",
            "прием_пациента/гинекологический_осмотр:0/левые_придатки/пальпация" to "at0019",
            "прием_пациента/гинекологический_осмотр:0/левые_придатки/болезненность" to "at0027"
        )

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
        buildAndExport(templateName, "medium", "ru", setOf("ru", "en"))
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
        val compositionFlatMap: Map<String, Any> = mapOf(
            "прием_пациента/административная_информация/автор_документа" to "user",
            "прием_пациента/административная_информация/вид_медпомощи|code" to "at0024",
            "прием_пациента/административная_информация/вид_медпомощи|value" to "амбулаторная медицинская помощь",
            "прием_пациента/административная_информация/дата_приема" to "2013-03-19T00:00:00.000+06:00",
            "прием_пациента/административная_информация/документ_создан" to "2013-03-19T10:42:52.000+06:00",
            "прием_пациента/административная_информация/должность|code" to "123",
            "прием_пациента/административная_информация/должность|value" to "Медицинская сестра по физиотерапии",
            "прием_пациента/административная_информация/клиническая_область|code" to "at0005",
            "прием_пациента/административная_информация/клиническая_область|value" to "терапия",
            "прием_пациента/административная_информация/медицинское_учреждение|code" to "770038",
            "прием_пациента/административная_информация/медицинское_учреждение|value" to "ГБУЗ ГП № 138 ДЗМ",
            "прием_пациента/административная_информация/название_документа" to "openEHR-EHR-COMPOSITION.t_new_rheumatologist_examination.v1 2013-03-19T00:00:00",
            "прием_пациента/административная_информация/основной_диагноз|code" to "114118",
            "прием_пациента/административная_информация/основной_диагноз|value" to "T67 - Эффекты воздействия высокой температуры и света",
            "прием_пациента/административная_информация/отделение" to "authorInstitution",
            "прием_пациента/административная_информация/специальность|code" to "1178",
            "прием_пациента/административная_информация/специальность|value" to "Врач-ортодонт",
            "прием_пациента/административная_информация/статус_документа|value" to "черновик",
            "прием_пациента/административная_информация/статус_документа|code" to "at0014",
            "прием_пациента/общая_информация/тип_приема|code" to "at0026",
            "прием_пациента/общая_информация/тип_приема|value" to "первичный",
            "прием_пациента/общая_информация/место_приема|value" to "поликлиника",
            "прием_пациента/общая_информация/цель_посещения|value" to "заболевание",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|code" to "114118",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|value" to "T67 - Эффекты воздействия высокой температуры и света",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|value" to "острое",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/учитывать_в_листе_уточненных_диагнозов" to "false",
            "прием_пациента/диагноз_и_результат_обращения/осложнение_основного_диагноза:0/учитывать_в_листе_уточненных_диагнозов" to "false",
            "прием_пациента/диагноз_и_результат_обращения/сопутствующий_диагноз:0/учитывать_в_листе_уточненных_диагнозов" to "false",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|code" to "at0051",
            "прием_пациента/общая_информация/место_приема|code" to "at0067",
            "прием_пациента/общая_информация/цель_посещения|code" to "at0070",
            "прием_пациента/процедуры/narrative" to "narrative",
            "прием_пациента/процедуры/request:0/timing" to "R0",
            "прием_пациента/процедуры/request:0/название_процедуры|code" to "синусные модульные токи",
            "прием_пациента/процедуры/request:0/название_процедуры|value" to "синусные модульные токи",
            "прием_пациента/процедуры/request:0/описание_процедуры" to "1",
            "прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|magnitude" to "1",
            "прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|unit" to "/d"
        )

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
        val compositionFlatMap: Map<String, Any> = mapOf(
            "прием_пациента/административная_информация/автор_документа" to "user",
            "прием_пациента/административная_информация/вид_медпомощи|code" to "at0024",
            "прием_пациента/административная_информация/вид_медпомощи|value" to "амбулаторная медицинская помощь",
            "прием_пациента/административная_информация/дата_приема" to "2013-03-19T00:00:00.000+06:00",
            "прием_пациента/административная_информация/документ_создан" to "2013-03-19T10:42:52.000+06:00",
            "прием_пациента/административная_информация/должность|code" to "123",
            "прием_пациента/административная_информация/должность|value" to "Медицинская сестра по физиотерапии",
            "прием_пациента/административная_информация/клиническая_область|code" to "at0005",
            "прием_пациента/административная_информация/клиническая_область|value" to "терапия",
            "прием_пациента/административная_информация/медицинское_учреждение|code" to "770038",
            "прием_пациента/административная_информация/медицинское_учреждение|value" to "ГБУЗ ГП № 138 ДЗМ",
            "прием_пациента/административная_информация/название_документа" to "openEHR-EHR-COMPOSITION.t_new_rheumatologist_examination.v1 2013-03-19T00:00:00",
            "прием_пациента/административная_информация/основной_диагноз|code" to "114118",
            "прием_пациента/административная_информация/основной_диагноз|value" to "T67 - Эффекты воздействия высокой температуры и света",
            "прием_пациента/административная_информация/отделение" to "authorInstitution",
            "прием_пациента/административная_информация/специальность|code" to "1178",
            "прием_пациента/административная_информация/специальность|value" to "Врач-ортодонт",
            "прием_пациента/административная_информация/статус_документа|value" to "черновик",
            "прием_пациента/административная_информация/статус_документа|code" to "at0014",
            "прием_пациента/общая_информация/тип_приема|code" to "at0026",
            "прием_пациента/общая_информация/тип_приема|value" to "первичный",
            "прием_пациента/общая_информация/место_приема|value" to "поликлиника",
            "прием_пациента/общая_информация/цель_посещения|value" to "заболевание",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|code" to "114118",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/код_по_мкб|value" to "T67 - Эффекты воздействия высокой температуры и света",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|value" to "острое",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/учитывать_в_листе_уточненных_диагнозов" to "false",
            "прием_пациента/диагноз_и_результат_обращения/осложнение_основного_диагноза:0/учитывать_в_листе_уточненных_диагнозов" to "false",
            "прием_пациента/диагноз_и_результат_обращения/сопутствующий_диагноз:0/учитывать_в_листе_уточненных_диагнозов" to "false",
            "прием_пациента/диагноз_и_результат_обращения/основной_диагноз/характер_заболевания|code" to "at0051",
            "прием_пациента/общая_информация/место_приема|code" to "at0067",
            "прием_пациента/общая_информация/цель_посещения|code" to "at0070",
            "прием_пациента/процедуры/request:0/название_процедуры|code" to "синусные модульные токи",
            "прием_пациента/процедуры/request:0/название_процедуры|value" to "синусные модульные токи",
            "прием_пациента/процедуры/request:0/описание_процедуры" to "1",
            "прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|magnitude" to "1",
            "прием_пациента/процедуры/request:0/процедуры_-_физиотерапия:0/частота_процедуры|unit" to "/d"
        )
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
            Pair("прием_пациента_врачом-стоматологом-хирургом/стоматологический_осмотр/зубная_формула/осмотр_полости_рта_16/a48/состояние_зуба_до_лечения|value", "П")
        )

        val context = ConversionContext.create().withLanguage("ru").withTerritory("RU").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(parameters, context)
        assertThat(composition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testLinks() {
        val webTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_dermatologist_examination.v1.xml"),
            WebTemplateBuilderContext("ru")
        )

        val composition = getComposition("/convert/compositions/composition_with_Dv_Interval.xml").apply {
            this.links.add(Link().apply {
                this.target = DvEhrUri().apply { this.value = "ehr:///c1" }
                this.meaning = DvText("follow up")
                this.type = DvText("issue")
            })

            this.links.add(Link().apply {
                this.target = DvEhrUri().apply { this.value = "ehr:///c2" }
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
            entry("прием_пациента_врачом-дерматолог/_link:1|target", "ehr:///c2")
        )
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
            Pair("test/interval_quantity/fiels_for_test/lower", "120")
        )

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
        val node: WebTemplateNode =
            webTemplate.findWebTemplateNode("осмотр_аллерголога-иммунолога/интерпретация_результатов_обследования/интерпретация_результатов_обследования/фвд_-_спирометрия/интерпретация_результатов/препарат/мнн")
        assertThat(node.inputs[0].terminology).isEqualTo("NSI?subset=DRUGS_EXT_MNN_ALLERG_CH&language=GB")
        assertThat(node.inputs[0].otherTerminologies).containsOnly("NSI?subset=DRUGS_EXT_MNN_V")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testUntypedInterval() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_anamnesis_vitae_pediatrist.opt")
        val context = WebTemplateBuilderContext("ru", setOf("en", "ru"))
        assertThat(WebTemplateBuilder.buildNonNull(template, context)).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testMissingInstruction() {
        val template = getTemplate("/convert/templates/opt referral.xml")
        val context = WebTemplateBuilderContext("ru", setOf("en", "ru"))
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
        val context = WebTemplateBuilderContext("ru", setOf("en", "ru"))

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
        val context = WebTemplateBuilderContext("ru", setOf("en", "ru"))
        val webTemplate = WebTemplateBuilder.buildNonNull(template, context)
        assertThat(webTemplate).isNotNull

        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").build()

        val firstFlatComposition: Map<String, Any?> =
            getObjectMapper().readValue(getJson("/convert/compositions/therapist_saved.json"), object : TypeReference<Map<String, Any?>>() {})
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, builderContext)
        assertThat(firstComposition?.content ?: emptyList()).hasSize(5)

        val secondFlatComposition: Map<String, Any?> =
            getObjectMapper().readValue(getJson("/convert/compositions/therapist_saved_fixed.json"), object : TypeReference<Map<String, Any?>>() {})

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, builderContext)
        assertThat(secondComposition!!.content).hasSize(6)
        assertThat(secondComposition.content[5].name!!.value).isEqualTo("Сведения о выполнении назначения")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCardiologistExamination() {
        val webTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_cardiologist_examination.v3.xml"),
            WebTemplateBuilderContext("ru", setOf("en", "ru"))
        )
        assertThat(webTemplate).isNotNull

        val builderContext = ConversionContext.create().withLanguage("ru").withTerritory("RU").build()

        val firstStructuredComposition: ObjectNode = getObjectMapper().readTree(getJson("/convert/compositions/cardio.json")) as ObjectNode
        val firstComposition: Composition? = webTemplate.convertFromStructuredToRaw(firstStructuredComposition, builderContext)
        assertThat(firstComposition).isNotNull

        val secondWebTemplate = WebTemplateBuilder.buildNonNull(
            getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_cardiologist_examination.v3-fix.xml"),
            WebTemplateBuilderContext("ru", setOf("en", "ru"))
        )
        assertThat(secondWebTemplate).isNotNull

        val secondComposition: Composition? = secondWebTemplate.convertFromStructuredToRaw(firstStructuredComposition, builderContext)
        assertThat(secondComposition).isNotNull
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testPrevaccinalExamination() {
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.prevaccinal_examination.opt")
        val context = WebTemplateBuilderContext("ru", setOf("en", "ru"))
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
        val context = WebTemplateBuilderContext("ru", setOf("en", "ru"))
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
        val flatComposition: Map<String, String> = mapOf(
            "ctx/time" to "2015-01-01T10:00:00.000+05:00",
            "vitals/vitals/body_temperature:0/any_event:0/temperature|magnitude" to "37.7",
            "vitals/vitals/body_temperature:0/any_event:0/temperature|unit" to "°C"
        )

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
            secondContext
        )
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

        val extractor: PathValueExtractor =
            NameAndNodeMatchingPathValueExtractor("/content[openEHR-EHR-ADMIN_ENTRY.container_simi.v0,'Сведения о врачебной комиссии']/data[at0001]/items[openEHR-EHR-CLUSTER.health_authorities_simi.v0]/items[at0005]/value")
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
