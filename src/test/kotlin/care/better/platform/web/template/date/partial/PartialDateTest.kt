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

package care.better.platform.web.template.date.partial

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class PartialDateTest {
    @Test
    fun testPartialDate() {
        assertFields(PartialDate.from("2016-01-31"), 2016, 1, 31)
        assertFields(PartialDate.from("2016-01"), 2016, 1)
        assertFields(PartialDate.from("2016"), 2016)
    }

    @Test
    fun testPartialDateWithPattern() {
        assertFields(PartialDate.from("2016-01-31", "yyyy-mm-??"), 2016, 1, 31)
        assertFields(PartialDate.from("2016-01-31", "yyyy-??-XX"), 2016, 1)
        assertFields(PartialDate.from("2016-01", "yyyy-mm-??"), 2016, 1)
        assertFields(PartialDate.from("2016-01", "yyyy-??-XX"), 2016, 1)
        assertFields(PartialDate.from("2016", "yyyy-??-XX"), 2016)
    }

    @Test
    fun testInvalidPartialDate() {
        assertThatThrownBy { PartialDate(2016, null, 1) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialDate.from("2016-17-01") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialDate.from("2016-17") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialDate.from("2016-aa") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialDate.from("2016", "yyyy-mm-??") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun assertFields(partialDate: PartialDate, vararg ymd: Int) {
        assertThat(partialDate.year).isEqualTo(ymd[0])
        if (ymd.size > 1) {
            assertThat(partialDate.month).isEqualTo(ymd[1])
        } else {
            assertThat(partialDate.month).isNull()
        }
        if (ymd.size > 2) {
            assertThat(partialDate.day).isEqualTo(ymd[2])
        } else {
            assertThat(partialDate.day).isNull()
        }
    }
}
