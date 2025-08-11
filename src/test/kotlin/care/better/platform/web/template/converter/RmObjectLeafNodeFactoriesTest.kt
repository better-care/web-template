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

import care.better.platform.template.AmNode
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.mapper.ConversionObjectMapper
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.factory.leaf.DvProportionFactory
import care.better.platform.web.template.converter.raw.factory.leaf.DvQuantityFactory
import care.better.platform.web.template.converter.raw.factory.leaf.DvTimeFactory
import care.better.platform.web.template.converter.utils.WebTemplateConversionUtils
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import com.fasterxml.jackson.databind.node.TextNode
import com.google.common.collect.ImmutableMap
import org.assertj.core.api.Assertions.*
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class RmObjectLeafNodeFactoriesTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testFixedValues() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val composition: Composition? =
            webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/fixed_values/fixed_text|code", "at0009"), context)

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("testing_template/context/testing/fixed_values/fixed_text|code", "at0008"),
            entry("testing_template/context/testing/fixed_values/fixed_ordinal|code", "at0009"),
            entry("testing_template/context/testing/fixed_values/fixed_ordinal|ordinal", "1"),
            entry("testing_template/context/testing/fixed_values/fixed_count", "1"),
            entry("testing_template/context/testing/fixed_values/fixed_boolean", "true")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultimedia() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of(
                "testing_template/context/testing/multimedia", "http://here.com/123",
                "testing_template/context/testing/multimedia|alternatetext", "Hello world!",
                "testing_template/context/testing/multimedia|mediatype", "png",
                "testing_template/context/testing/multimedia|size", "999"
            ),
            context)

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("testing_template/context/testing/multimedia", "http://here.com/123"),
            entry("testing_template/context/testing/multimedia|alternatetext", "Hello world!"),
            entry("testing_template/context/testing/multimedia|mediatype", "png"),
            entry("testing_template/context/testing/multimedia|size", "999")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultimediaUnknownAttribute() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()

        assertThatThrownBy {
            webTemplate.convertFromFlatToRaw<Composition>(
                ImmutableMap.of(
                    "testing_template/context/testing/multimedia", "http://here.com/123",
                    "testing_template/context/testing/multimedia|xyz", "Hello world!"
                ),
                context)
        }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageStartingWith("DV_MULTIMEDIA has no attribute |xyz (path: testing_template/context/testing/multimedia|xyz).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultimediaFailedInvalidValue() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        assertThatThrownBy {
            webTemplate.convertFromFlatToRaw<Composition>(
                ImmutableMap.of(
                    "testing_template/context/testing/multimedia", "http://here.com/123",
                    "testing_template/context/testing/multimedia|size", "XYZ"
                ),
                context)
        }.isInstanceOf(ConversionException::class.java).hasMessageStartingWith("Invalid value for attribute 'size' of DV_MULTIMEDIA")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testProportion() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of(
                "testing_template/context/testing/proportion|numerator", "10",
                "testing_template/context/testing/proportion|denominator", "100"
            ),
            context)

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("testing_template/context/testing/proportion|numerator", "10.0"),
            entry("testing_template/context/testing/proportion|denominator", "100.0")
        )
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testProportionWithInvalidNumerator() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()

        assertThatThrownBy {
            webTemplate.convertFromFlatToRaw<Composition>(
                ImmutableMap.of(
                    "testing_template/context/testing/proportion|numerator", "xyz",
                    "testing_template/context/testing/proportion|denominator", "100"
                ),
                context)
        }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageStartingWith("Invalid decimal value: xyz (path: testing_template/context/testing/proportion).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testProportionWithInvalidDenominator() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()

        assertThatThrownBy {
            webTemplate.convertFromFlatToRaw<Composition>(
                ImmutableMap.of(
                    "testing_template/context/testing/proportion|numerator", "10",
                    "testing_template/context/testing/proportion|denominator", "abc"
                ),
                context)
        }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageStartingWith("Invalid decimal value: abc (path: testing_template/context/testing/proportion).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testUri() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val composition: Composition? =
            webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/uri", "http://www.google.com"), context)

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("testing_template/context/testing/uri", "http://www.google.com"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testFirstDate() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date", "2014-1-13"), context)

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("testing_template/context/testing/date", "2014-01-13"))

        val secondComposition: Composition? =
            webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date", LocalDate(2014, 1, 13)), context)

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("testing_template/context/testing/date", "2014-01-13"))

        val thirdComposition: Composition? =
            webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date", DateTime(2014, 1, 13, 10, 13)), context)

        val thirdFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(thirdComposition!!, FromRawConversion.create())
        assertThat(thirdFlatMap).contains(entry("testing_template/context/testing/date", "2014-01-13"))

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(ImmutableMap.of("testing_template/context/testing/date", true), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageContaining("Error processing value \"true\" for pattern \"\" (path: testing_template/context/testing/date).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondDate() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date", "2014-1-13"), context)

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("testing_template/context/testing/date", "2014-01-13"))

        val secondComposition: Composition? =
            webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date", LocalDate(2014, 1, 13)), context)

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("testing_template/context/testing/date", "2014-01-13"))

        val thirdComposition: Composition? =
            webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date", DateTime(2014, 1, 13, 10, 13)), context)

        val thirdFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(thirdComposition!!, FromRawConversion.create())
        assertThat(thirdFlatMap).contains(entry("testing_template/context/testing/date", "2014-01-13"))

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(ImmutableMap.of("testing_template/context/testing/date", "2014-a-b"), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageContaining("Error processing value \"2014-a-b\" for pattern \"\" (path: testing_template/context/testing/date).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testFirstTime() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val time = LocalTime.of(14, 35)

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/time", time), context)

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("testing_template/context/testing/time", DateTimeFormatter.ISO_LOCAL_TIME.format(time)))

        val localTime = LocalTime.of(14, 35, 10, 117000000)
        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of("testing_template/context/testing/time", localTime),
            context)

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("testing_template/context/testing/time", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime)))

        val zonedDateTime = ZonedDateTime.of(2014, 1, 13, 14, 35, 10, 117000000, ZoneId.systemDefault())
        val thirdComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of(
                "testing_template/context/testing/time",
                zonedDateTime),
            context)

        val thirdFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(thirdComposition!!, FromRawConversion.create())
        assertThat(thirdFlatMap).contains(entry("testing_template/context/testing/time", DateTimeFormatter.ISO_OFFSET_TIME.format(zonedDateTime.toOffsetDateTime())))

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(ImmutableMap.of("testing_template/context/testing/time", true), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Error processing value \"true\" for pattern \"\" (path: testing_template/context/testing/time).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondTime() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/time", "14:35"), context)

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("testing_template/context/testing/time", "14:35"))

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/time", "14:35+02:00"), context)

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("testing_template/context/testing/time", "14:35+02:00"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testThirdTime() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/time", "14:35"), context)

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("testing_template/context/testing/time", "14:35"))

        val jodaLocalTime = org.joda.time.LocalTime(14, 35, 10, 117)
        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of("testing_template/context/testing/time", jodaLocalTime),
            context)
        val localTime = WebTemplateConversionUtils.convert(jodaLocalTime)

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("testing_template/context/testing/time", DateTimeFormatter.ISO_LOCAL_TIME.format(localTime)))

        val dateTime = DateTime(2014, 1, 13, 14, 35, 10, 117)
        val thirdComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of("testing_template/context/testing/time", dateTime),
            context)
        val convertOffsetTime = WebTemplateConversionUtils.convertOffsetTime(dateTime)

        val thirdFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(thirdComposition!!, FromRawConversion.create())
        assertThat(thirdFlatMap).contains(entry("testing_template/context/testing/time", DateTimeFormatter.ISO_OFFSET_TIME.format(convertOffsetTime)))

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(ImmutableMap.of("testing_template/context/testing/time", "17:aa"), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Error processing value \"17:aa\" for pattern \"\" (path: testing_template/context/testing/time).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testFirstDateTime() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of("testing_template/context/testing/date_time", "2014-1-13T14:35:00.000"),
            context)

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create())
        val dateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.of(2014, 1, 13, 14, 35, 0, 0, ZoneId.systemDefault()))
        assertThat(firstFlatMap).contains(entry("testing_template/context/testing/date_time", dateTime))

        val dt = DateTime(2014, 1, 13, 14, 35, 10, 117)
        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date_time", dt), context)

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("testing_template/context/testing/date_time", "2014-01-13T14:35:10+01:00"))

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(ImmutableMap.of("testing_template/context/testing/date_time", true), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessage("Error processing value \"true\" for pattern \"yyyy-mm-ddTHH:MM:SS\" (path: testing_template/context/testing/date_time).")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSecondDateTime() {
        val builderContext = WebTemplateBuilderContext("en")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Testing Template.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SL").withComposerName("composer").build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of("testing_template/context/testing/date_time", "2014-1-13T14:35:00.000"),
            context)

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition!!, FromRawConversion.create())
        val dateTime = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.of(2014, 1, 13, 14, 35, 0, 0, ZoneId.systemDefault()))
        assertThat(firstFlatMap).contains(entry("testing_template/context/testing/date_time", dateTime))

        val dt = DateTime(2014, 1, 13, 14, 35, 10, 117)
        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(ImmutableMap.of("testing_template/context/testing/date_time", dt), context)

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition!!, FromRawConversion.create())
        assertThat(secondFlatMap).contains(entry("testing_template/context/testing/date_time", "2014-01-13T14:35:10+01:00"))

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(ImmutableMap.of("testing_template/context/testing/date_time", "17:aa"), context) }
            .isInstanceOf(ConversionException::class.java)
            .hasMessageContaining("Error processing value \"17:aa\" for pattern \"yyyy-mm-ddTHH:MM:SS\" (path: testing_template/context/testing/date_time).")
    }

    @Test
    fun testTime() {
        val dvTime = DvTimeFactory.create(
            ConversionContext.create().build(),
            AmNode(null, "DV_TIME"),
            TextNode.valueOf("23:30:33.001"),
            WebTemplatePath("value"), emptyList())

        assertThat(dvTime!!.value).isEqualTo("23:30:33.001")
    }

    @Test
    fun testTimeNano() {
        val dvTime = DvTimeFactory.create(
            ConversionContext.create().build(),
            AmNode(null, "DV_TIME"),
            TextNode.valueOf("23:30:33.000000017"),
            WebTemplatePath("value"), emptyList())

        assertThat(dvTime!!.value).isEqualTo("23:30:33.000000017")
    }

    @Test
    fun testQuantity() {
        val firstObjectNode = ConversionObjectMapper.createObjectNode().apply {
            this.put("|magnitude", 13.01)
        }
        val firstDvQuantity = DvQuantityFactory.create(
            ConversionContext.create().withValueConvert(LocaleBasedValueConverter(Locale("sl", "SI"))).build(),
            AmNode(null, "DV_QUANTITY"),
            firstObjectNode,
            WebTemplatePath("value"), emptyList())
        assertThat(firstDvQuantity!!.magnitude).isEqualTo(13.01)

        val secondObjectNode = ConversionObjectMapper.createObjectNode().apply {
            this.put("|magnitude", BigDecimal("13.01"))
        }
        val secondDvQuantity = DvQuantityFactory.create(
            ConversionContext.create().withValueConvert(LocaleBasedValueConverter(Locale("sl", "SI"))).build(),
            AmNode(null, "DV_QUANTITY"),
            secondObjectNode,
            WebTemplatePath("value"), emptyList())
        assertThat(secondDvQuantity!!.magnitude).isEqualTo(13.01)

        val thirdObjectNode = ConversionObjectMapper.createObjectNode().apply {
            this.put("|magnitude", "13,01")
        }
        val thirdDvQuantity = DvQuantityFactory.create(
            ConversionContext.create().withValueConvert(LocaleBasedValueConverter(Locale("sl", "SI"))).build(),
            AmNode(null, "DV_QUANTITY"),
            thirdObjectNode,
            WebTemplatePath("value"), emptyList())
        assertThat(thirdDvQuantity!!.magnitude).isEqualTo(13.01)

        val fourthObjectNode = ConversionObjectMapper.createObjectNode().apply {
            this.put("|magnitude", "13.01")
        }
        val fourthDvQuantity = DvQuantityFactory.create(
            ConversionContext.create().withValueConvert(LocaleBasedValueConverter(Locale("sl", "SI"))).build(),
            AmNode(null, "DV_QUANTITY"),
            fourthObjectNode,
            WebTemplatePath("value"), emptyList())
        assertThat(fourthDvQuantity!!.magnitude).isEqualTo(1301.0)
    }

    @Test
    fun testProportionFactory() {
        val firstObjectNode = ConversionObjectMapper.createObjectNode().apply {
            this.put("|numerator", 13.01)
            this.put("|denominator", 100.0)
        }
        val firstDvProportion = DvProportionFactory.create(
            ConversionContext.create().withValueConvert(LocaleBasedValueConverter(Locale("sl", "SI"))).build(),
            AmNode(null, "DV_PROPORTION"),
            firstObjectNode,
            WebTemplatePath("value"), emptyList())
        assertThat(firstDvProportion!!.numerator).isEqualTo(13.01f)
        assertThat(firstDvProportion.denominator).isEqualTo(100.0f)

        val secondObjectNode = ConversionObjectMapper.createObjectNode().apply {
            this.put("|numerator", "13,01")
            this.put("|denominator", 100.0)
        }
        val secondDvProportion = DvProportionFactory.create(
            ConversionContext.create().withValueConvert(LocaleBasedValueConverter(Locale("sl", "SI"))).build(),
            AmNode(null, "DV_PROPORTION"),
            secondObjectNode,
            WebTemplatePath("value"), emptyList())
        assertThat(secondDvProportion!!.numerator).isEqualTo(13.01f)
        assertThat(firstDvProportion.denominator).isEqualTo(100.0f)
    }
}
