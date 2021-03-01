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

package care.better.platform.web.template.builder.predicate

import care.better.platform.path.PathSegment
import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils.findTerm
import care.better.platform.template.AmUtils.getNameCodePhrase
import care.better.platform.template.AmUtils.isNameConstrained

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
class MatchingConstrainedChildAmNodePredicate(segment: PathSegment, rmType: String?) : MatchingChildAmNodePredicate(segment, rmType) {
    override fun invoke(amNode: AmNode): Boolean = super.invoke(amNode) && nameMatches(amNode, segment.name)

    private fun nameMatches(amNode: AmNode, name: String?): Boolean {
        if (isNameConstrained(amNode)) {
            val nameCodePhrase = getNameCodePhrase(amNode) ?: return name == amNode.name
            return nameCodePhrase.codeList.any { name == findTerm(amNode.terms, it, "text") }
        }
        return true
    }
}
