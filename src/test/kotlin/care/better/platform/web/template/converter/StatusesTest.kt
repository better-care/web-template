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
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.google.common.collect.ImmutableMap
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datastructures.ItemTree
import org.openehr.rm.datatypes.*
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class StatusesTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOrdinal() {
        val webTemplate: WebTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/ordinal", "1")
                .put("test_statuses/test_statuses:0/ordinal|normal_status", "L")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        assertThat(getDataValue<DvOrdinal>(firstComposition!!)?.normalStatus?.codeString).isEqualTo("L")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/ordinal|normal_status", "L"))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/ordinal", "1")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        assertThat(getDataValue<DvOrdinal>(secondComposition!!)?.normalStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOrdinalN() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/ordinal", "1")
                .put("test_statuses/test_statuses:0/ordinal|normal_status", "N")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        assertThat(getDataValue<DvOrdinal>(firstComposition!!)?.normalStatus?.codeString).isEqualTo("N")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/ordinal|normal_status", "N"))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/ordinal", "1")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        assertThat(getDataValue<DvOrdinal>(secondComposition!!)?.normalStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testOrdinalInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val flatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/ordinal", "1")
                .put("test_statuses/test_statuses:0/ordinal|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(flatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDuration() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/duration", "P1Y")
                .put("test_statuses/test_statuses:0/duration|normal_status", "L")
                .put("test_statuses/test_statuses:0/duration|magnitude_status", ">=")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        val firstDuration = getDataValue<DvDuration>(firstComposition!!)
        assertThat(firstDuration?.normalStatus?.codeString).isEqualTo("L")
        assertThat(firstDuration?.magnitudeStatus).isEqualTo(">=")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/duration|normal_status", "L"))
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/duration|magnitude_status", ">="))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/duration", "P1M")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        val secondDuration = getDataValue<DvDuration>(secondComposition!!)
        assertThat(secondDuration?.normalStatus).isNull()
        assertThat(secondDuration?.magnitudeStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
        assertThat(secondFlatMap.keys.filter { it.contains("|magnitude_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDurationInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/duration", "P1Y")
                .put("test_statuses/test_statuses:0/duration|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(firstFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/duration", "P1Y")
                .put("test_statuses/test_statuses:0/duration|magnitude_status", "!")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(secondFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid MAGNITUDE_STATUS: !")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testQuantity() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/quantity|unit", "m")
                .put("test_statuses/test_statuses:0/quantity|magnitude", "1")
                .put("test_statuses/test_statuses:0/quantity|normal_status", "L")
                .put("test_statuses/test_statuses:0/quantity|magnitude_status", ">=")
                .build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        val firstQuantity = getDataValue<DvQuantity>(firstComposition!!)
        assertThat(firstQuantity?.normalStatus?.codeString).isEqualTo("L")
        assertThat(firstQuantity?.magnitudeStatus).isEqualTo(">=")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/quantity|normal_status", "L"))
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/quantity|magnitude_status", ">="))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/quantity|unit", "m")
                .put("test_statuses/test_statuses:0/quantity|magnitude", "1")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        val secondQuantity = getDataValue<DvQuantity>(secondComposition!!)
        assertThat(secondQuantity?.normalStatus).isNull()
        assertThat(secondQuantity?.magnitudeStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
        assertThat(secondFlatMap.keys.filter { it.contains("|magnitude_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testQuantityInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/quantity|unit", "m")
                .put("test_statuses/test_statuses:0/quantity|magnitude", "1")
                .put("test_statuses/test_statuses:0/quantity|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(firstFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/quantity|unit", "m")
                .put("test_statuses/test_statuses:0/quantity|magnitude", "1")
                .put("test_statuses/test_statuses:0/quantity|magnitude_status", "!")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(secondFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid MAGNITUDE_STATUS: !")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDate() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/date", "2017-10-01")
                .put("test_statuses/test_statuses:0/date|normal_status", "L")
                .put("test_statuses/test_statuses:0/date|magnitude_status", ">=")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        val firstDate = getDataValue<DvDate>(firstComposition!!)
        assertThat(firstDate?.normalStatus?.codeString).isEqualTo("L")
        assertThat(firstDate?.magnitudeStatus).isEqualTo(">=")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/date|normal_status", "L"))
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/date|magnitude_status", ">="))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/date", "2017-10-01")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        val secondDate = getDataValue<DvDate>(secondComposition!!)
        assertThat(secondDate?.normalStatus).isNull()
        assertThat(secondDate?.magnitudeStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
        assertThat(secondFlatMap.keys.filter { it.contains("|magnitude_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDateInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/date", "2017-10-01")
                .put("test_statuses/test_statuses:0/date|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(firstFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/date", "2017-10-01")
                .put("test_statuses/test_statuses:0/quantity|magnitude_status", "!")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(secondFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid MAGNITUDE_STATUS: !")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTime() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/time", "13:20")
                .put("test_statuses/test_statuses:0/time|normal_status", "L")
                .put("test_statuses/test_statuses:0/time|magnitude_status", ">=")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        val firstTime = getDataValue<DvTime>(firstComposition!!)
        assertThat(firstTime?.normalStatus?.codeString).isEqualTo("L")
        assertThat(firstTime?.magnitudeStatus).isEqualTo(">=")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/time|normal_status", "L"))
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/time|magnitude_status", ">="))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/time", "13:20")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        val secondTime = getDataValue<DvTime>(secondComposition!!)
        assertThat(secondTime?.normalStatus).isNull()
        assertThat(secondTime?.magnitudeStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
        assertThat(secondFlatMap.keys.filter { it.contains("|magnitude_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testTimeInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/time", "13:20")
                .put("test_statuses/test_statuses:0/time|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(firstFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/time", "13:20")
                .put("test_statuses/test_statuses:0/time|magnitude_status", "!")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(secondFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid MAGNITUDE_STATUS: !")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDateTime() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/datetime", "2017-10-01T13:20:00Z")
                .put("test_statuses/test_statuses:0/datetime|normal_status", "L")
                .put("test_statuses/test_statuses:0/datetime|magnitude_status", ">=")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        val firstDateTime = getDataValue<DvDateTime>(firstComposition!!)
        assertThat(firstDateTime?.normalStatus?.codeString).isEqualTo("L")
        assertThat(firstDateTime?.magnitudeStatus).isEqualTo(">=")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/datetime|normal_status", "L"))
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/datetime|magnitude_status", ">="))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/datetime", "2017-10-01T13:20:00Z")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        val secondDateTime = getDataValue<DvDateTime>(secondComposition!!)
        assertThat(secondDateTime?.normalStatus).isNull()
        assertThat(secondDateTime?.magnitudeStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
        assertThat(secondFlatMap.keys.filter { it.contains("|magnitude_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testDateTimeInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/datetime", "2017-10-01T13:20:00Z")
                .put("test_statuses/test_statuses:0/datetime|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(firstFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/datetime", "2017-10-01T13:20:00Z")
                .put("test_statuses/test_statuses:0/datetime|magnitude_status", "!")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(secondFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid MAGNITUDE_STATUS: !")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCount() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/count", "17")
                .put("test_statuses/test_statuses:0/count|normal_status", "L")
                .put("test_statuses/test_statuses:0/count|magnitude_status", ">=")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        val firstCount = getDataValue<DvCount>(firstComposition!!)
        assertThat(firstCount?.normalStatus?.codeString).isEqualTo("L")
        assertThat(firstCount?.magnitudeStatus).isEqualTo(">=")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/count|normal_status", "L"))
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/count|magnitude_status", ">="))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/count", "17")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        val secondCount = getDataValue<DvCount>(secondComposition!!)
        assertThat(secondCount?.normalStatus).isNull()
        assertThat(secondCount?.magnitudeStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
        assertThat(secondFlatMap.keys.filter { it.contains("|magnitude_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCountInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/count", "17")
                .put("test_statuses/test_statuses:0/count|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(firstFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/count", "17")
                .put("test_statuses/test_statuses:0/count|magnitude_status", "!")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(secondFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid MAGNITUDE_STATUS: !")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testProportion() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/proportion|numerator", "17")
                .put("test_statuses/test_statuses:0/proportion|denominator", "33")
                .put("test_statuses/test_statuses:0/proportion|normal_status", "L")
                .put("test_statuses/test_statuses:0/proportion|magnitude_status", ">=")
                .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        assertThat(firstComposition).isNotNull
        val firstProportion = getDataValue<DvProportion>(firstComposition!!)
        assertThat(firstProportion?.normalStatus?.codeString).isEqualTo("L")
        assertThat(firstProportion?.magnitudeStatus).isEqualTo(">=")

        val firstFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/proportion|normal_status", "L"))
        assertThat(firstFlatMap).contains(entry("test_statuses/test_statuses:0/proportion|magnitude_status", ">="))

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/proportion|numerator", "17")
                .put("test_statuses/test_statuses:0/proportion|denominator", "33")
                .build()

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())
        assertThat(secondComposition).isNotNull
        val secondProportion = getDataValue<DvProportion>(secondComposition!!)
        assertThat(secondProportion?.normalStatus).isNull()
        assertThat(secondProportion?.magnitudeStatus).isNull()

        val secondFlatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap.keys.filter { it.contains("|normal_status") }).isEmpty()
        assertThat(secondFlatMap.keys.filter { it.contains("|magnitude_status") }).isEmpty()
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testProportionInvalid() {
        val webTemplate = getWebTemplate("/convert/templates/test_statuses.opt")
        val firstFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/proportion|numerator", "17")
                .put("test_statuses/test_statuses:0/proportion|denominator", "33")
                .put("test_statuses/test_statuses:0/proportion|normal_status", "X")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(firstFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid NORMAL_STATUS code: X")

        val secondFlatComposition: Map<String, String> = ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("test_statuses/test_statuses:0/proportion|numerator", "17")
                .put("test_statuses/test_statuses:0/proportion|denominator", "33")
                .put("test_statuses/test_statuses:0/proportion|magnitude_status", "!")
                .build()

        assertThatThrownBy { webTemplate.convertFromFlatToRaw<Composition>(secondFlatComposition, ConversionContext.create().build()) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessageContaining("Invalid MAGNITUDE_STATUS: !")
    }


    private fun <T : DvOrdered?> getDataValue(composition: Composition): T? {
        val observation = composition.content[0] as Observation
        val itemTree = observation.data?.events?.get(0)?.data as ItemTree?
        val element = itemTree?.items?.get(0) as Element?
        @Suppress("UNCHECKED_CAST")
        return element?.value as T?
    }
}
