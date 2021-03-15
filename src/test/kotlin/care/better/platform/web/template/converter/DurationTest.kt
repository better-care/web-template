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
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.factory.leaf.DvDurationFactory
import com.fasterxml.jackson.databind.node.TextNode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.openehr.rm.datatypes.DvDuration

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class DurationTest : AbstractWebTemplateTest() {

    @Test
    fun testInvalidOrder() {
        assertThatThrownBy {
            DvDurationFactory.create(
                ConversionContext.create().build(),
                AmNode(null, "DV_DURATION"),
                TextNode.valueOf("P0Y1M1D0WT0H0M0S"),
                WebTemplatePath("id"),
                emptyList())
        }.isInstanceOf(ConversionException::class.java).hasMessage("Error processing value: \"P0Y1M1D0WT0H0M0S\" (path: id).")
    }

    @Test
    fun testMixed() {
        validateDvDurationValue("P0Y1M0W1DT0H0M0S", "P1M1D")
    }

    @Test
    fun testNegative() {
        validateDvDurationValue("P0Y-1M0W1DT0H0M0S", "P-1M1D")
        validateDvDurationValue("P0Y1M0W-1DT0H0M0S", "P1M-1D")
        validateDvDurationValue("-P0Y1M1W1DT0H0M0S", "P-1M-1W-1D")
        validateDvDurationValue("-P0Y1M1W1DT0H0M0S", "P-1M-1W-1D")
        validateDvDurationValue("-P0Y-1M1W1DT0H0M0S", "P1M-1W-1D")
        validateDvDurationValue("-P0Y-1M-1W-1DT0H0M0S", "P1M1W1D")
    }

    @Test
    fun testMixedWeeksAndRest() {
        validateDvDurationValue("P0Y1M2W1DT0H0M0S", "P1M2W1D")
    }

    private fun validateDvDurationValue(pattern: String, valueToCompare: String) {
        val dvDuration1 = DvDurationFactory.create(
                ConversionContext.create().build(),
                AmNode(null, "DV_DURATION"),
                TextNode.valueOf(pattern),
                WebTemplatePath("id"),
                emptyList()) as DvDuration
        assertThat(dvDuration1.value).isEqualTo(valueToCompare)
    }
}
