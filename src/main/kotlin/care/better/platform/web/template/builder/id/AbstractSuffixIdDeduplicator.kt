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
abstract class AbstractSuffixIdDeduplicator : IdDeduplicator {
    private val allIds: MutableMap<String, MutableSet<String>> = mutableMapOf()

    override fun getUniqueBaseId(parentId: String, baseId: String): String {
        val ids: MutableSet<String> = with(allIds[parentId]) { this ?: hashSetOf<String>().also { allIds[parentId] = it } }
        return if (ids.contains(baseId)) {
            val suffix = getUniqueSuffix(ids, baseId)
            check(!ids.contains(baseId + suffix)) { "Unable to deduplicate id=$parentId/$baseId" }
            (baseId + suffix).also { ids.add(it) }
        } else {
            baseId.also { ids.add(baseId) }
        }
    }

    protected abstract fun getUniqueSuffix(ids: Set<String>, baseId: String): String
}
