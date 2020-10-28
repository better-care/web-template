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
class PartialTimeTest {

    @Test
    fun testPartialTime() {
        assertFields(PartialTime.from("12:33:17"), 12, 33, 17)
        assertFields(PartialTime.from("12:33"), 12, 33)
        assertFields(PartialTime.from("12"), 12)
    }

    @Test
    fun testPartialTimeWithPattern() {
        assertFields(PartialTime.from("12:33:17", "HH:MM:??"), 12, 33, 17)
        assertFields(PartialTime.from("12:33:17", "HH:??:XX"), 12, 33)
        assertFields(PartialTime.from("12:33", "HH:MM:??"), 12, 33)
        assertFields(PartialTime.from("12:33", "HH:??:XX"), 12, 33)
        assertFields(PartialTime.from("12", "HH:??:XX"), 12)
    }

    @Test
    fun invalidPartialTime() {
        assertThatThrownBy { PartialTime(12, null, 1) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialTime.from("12:61:15") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialTime.from("12:61") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialTime.from("12:xy") }.isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { PartialTime.from("12", "HH:MM:??") }.isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun assertFields(partialTime: PartialTime, vararg hms: Int) {
        assertThat(partialTime.hour).isEqualTo(hms[0])
        if (hms.size > 1) {
            assertThat(partialTime.minute).isEqualTo(hms[1])
        } else {
            assertThat(partialTime.minute).isNull()
        }
        if (hms.size > 2) {
            assertThat(partialTime.second).isEqualTo(hms[2])
        } else {
            assertThat(partialTime.second).isNull()
        }
    }
}
