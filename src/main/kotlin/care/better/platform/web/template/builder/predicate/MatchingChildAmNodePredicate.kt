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
import org.apache.commons.lang3.StringUtils

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
open class MatchingChildAmNodePredicate(val segment: PathSegment, val rmType: String?) : (AmNode) -> Boolean {
    override fun invoke(amNode: AmNode): Boolean =
        ((segment.archetypeNodeId == null || StringUtils.trimToNull(segment.archetypeNodeId) == StringUtils.trimToNull(amNode.archetypeNodeId))
                && (rmType == null || rmType == amNode.rmType))
}
