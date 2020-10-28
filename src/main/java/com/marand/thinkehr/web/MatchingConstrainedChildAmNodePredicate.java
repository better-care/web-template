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

package com.marand.thinkehr.web;

import care.better.platform.path.PathSegment;
import care.better.platform.template.AmNode;
import care.better.platform.template.AmUtils;
import org.openehr.am.aom.CCodePhrase;

import java.util.Objects;

/**
 * @author Primoz Delopst
 */
public class MatchingConstrainedChildAmNodePredicate extends MatchingChildAmNodePredicate {
    public MatchingConstrainedChildAmNodePredicate(PathSegment segment, String rmType) {
        super(segment, rmType);
    }

    @Override
    public boolean test(AmNode amNode) {
        return super.test(amNode) && nameMatches(amNode, segment.getName());
    }

    private boolean nameMatches(AmNode amNode, String name) {
        if (AmUtils.isNameConstrained(amNode)) {
            CCodePhrase nameCodePhrase = AmUtils.getNameCodePhrase(amNode);
            if (nameCodePhrase == null) {
                return Objects.equals(name, amNode.getName());
            }
            return nameCodePhrase.getCodeList().stream()
                    .anyMatch(code -> Objects.equals(name, AmUtils.findTerm(amNode.getTerms(), code, WebTemplateConstants.TEXT_ID)));
        }
        return true;
    }
}
