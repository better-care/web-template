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

package care.better.platform.web.template.converter.raw.context.setter

import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.SimpleValueConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class LinkContextSetterTest {
    @Test
    fun testSingle() {
        val conversionContextBuilder = ConversionContext.create()

        LinkCtxSetter.set(conversionContextBuilder, SimpleValueConverter, mapOf(Pair("|type", "type"), Pair("|meaning", "meaning"), Pair("|target", "target")))
        assertThat(conversionContextBuilder.getLinks()).hasSize(1)
        assertThat(conversionContextBuilder.getLinks()[0].type?.value).isEqualTo("type")
        assertThat(conversionContextBuilder.getLinks()[0].meaning?.value).isEqualTo("meaning")
        assertThat(conversionContextBuilder.getLinks()[0].target?.value).isEqualTo("target")
    }

    @Test
    fun testSingleCoded() {
        val conversionContextBuilder = ConversionContext.create()

        LinkCtxSetter.set(
            conversionContextBuilder,
            SimpleValueConverter,
            mapOf(Pair("|type", "tterm::tcode::type"), Pair("|meaning", "mterm::mcode::meaning"), Pair("|target", "target")))

        assertThat(conversionContextBuilder.getLinks()).hasSize(1)

        assertThat(conversionContextBuilder.getLinks()[0].type?.value).isEqualTo("type")
        assertThat((conversionContextBuilder.getLinks()[0].type as DvCodedText).definingCode?.codeString).isEqualTo("tcode")
        assertThat((conversionContextBuilder.getLinks()[0].type as DvCodedText).definingCode?.terminologyId?.value).isEqualTo("tterm")

        assertThat(conversionContextBuilder.getLinks()[0].meaning?.value).isEqualTo("meaning")
        assertThat((conversionContextBuilder.getLinks()[0].meaning as DvCodedText).definingCode?.codeString).isEqualTo("mcode")
        assertThat((conversionContextBuilder.getLinks()[0].meaning as DvCodedText).definingCode?.terminologyId?.value).isEqualTo("mterm")

        assertThat(conversionContextBuilder.getLinks()[0].target?.value).isEqualTo("target")
    }

    @Test
    fun testMultipleCoded() {
        val conversionContextBuilder = ConversionContext.create()

        LinkCtxSetter.set(
            conversionContextBuilder,
            SimpleValueConverter,
            listOf(
                mapOf(Pair("|type", "11tterm::11tcode::type"), Pair("|meaning", "11mterm::11mcode::meaning"), Pair("|target", "target11")),
                mapOf(Pair("|type", "type0"), Pair("|meaning", "meaning0"), Pair("|target", "target0"))))

        assertThat(conversionContextBuilder.getLinks()).hasSize(2)

        assertThat(conversionContextBuilder.getLinks()[0].type?.value).isEqualTo("type")
        assertThat((conversionContextBuilder.getLinks()[0].type as DvCodedText).definingCode?.codeString).isEqualTo("11tcode")
        assertThat((conversionContextBuilder.getLinks()[0].type as DvCodedText).definingCode?.terminologyId?.value).isEqualTo("11tterm")

        assertThat(conversionContextBuilder.getLinks()[0].meaning?.value).isEqualTo("meaning")
        assertThat((conversionContextBuilder.getLinks()[0].meaning as DvCodedText).definingCode?.codeString).isEqualTo("11mcode")
        assertThat((conversionContextBuilder.getLinks()[0].meaning as DvCodedText).definingCode?.terminologyId?.value).isEqualTo("11mterm")

        assertThat(conversionContextBuilder.getLinks()[0].target?.value).isEqualTo("target11")

        assertThat(conversionContextBuilder.getLinks()[1].type?.value).isEqualTo("type0")
        assertThat(conversionContextBuilder.getLinks()[1].meaning?.value).isEqualTo("meaning0")
        assertThat(conversionContextBuilder.getLinks()[1].target?.value).isEqualTo("target0")

    }
}
