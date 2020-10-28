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
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class PartialDateTimeTest {
    @Test
    fun testPartialDate() {
        assertFields(PartialDateTime.from("2016-01-31T12:30:10"), 2016, 1, 31, 12, 30, 10)
        assertFields(PartialDateTime.from("2016-01-31T12:30"), 2016, 1, 31, 12, 30)
        assertFields(PartialDateTime.from("2016-01-31T12"), 2016, 1, 31, 12)
        assertFields(PartialDateTime.from("2016-01-31"), 2016, 1, 31)
        assertFields(PartialDateTime.from("2016-01"), 2016, 1)
        assertFields(PartialDateTime.from("2016"), 2016)
    }

    private fun assertFields(partialDateTime: PartialDateTime, vararg ymdhms: Int) {
        assertThat(partialDateTime.partialDate.year).isEqualTo(ymdhms[0])
        if (ymdhms.size > 1) {
            assertThat(partialDateTime.partialDate.month).isEqualTo(ymdhms[1])
        } else {
            assertThat(partialDateTime.partialDate.month).isNull()
        }
        if (ymdhms.size > 2) {
            assertThat(partialDateTime.partialDate.day).isEqualTo(ymdhms[2])
        } else {
            assertThat(partialDateTime.partialDate.day).isNull()
        }
        if (ymdhms.size > 3) {
            assertThat(partialDateTime.partialTime?.hour).isEqualTo(ymdhms[3])
        } else {
            assertThat(partialDateTime.partialTime).isNull()
        }
        if (ymdhms.size > 4) {
            assertThat(partialDateTime.partialTime?.minute).isEqualTo(ymdhms[4])
        }
        if (ymdhms.size > 5) {
            assertThat(partialDateTime.partialTime?.second).isEqualTo(ymdhms[5])
        }
    }
}
