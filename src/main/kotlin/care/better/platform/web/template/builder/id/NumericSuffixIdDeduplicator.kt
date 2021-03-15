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

package care.better.platform.web.template.builder.id

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
@Suppress("SpellCheckingInspection")
internal class NumericSuffixIdDeduplicator : AbstractSuffixIdDeduplicator() {
    companion object {
        private const val MAX_SUFFIX = 100
    }

    override fun getUniqueSuffix(ids: Set<String>, baseId: String): String {
        var i = 2
        while (ids.contains(baseId + i) && i < MAX_SUFFIX) {
            i++
        }
        return i.toString()
    }
}
