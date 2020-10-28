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

package com.marand.thinkehr.web.build;

import care.better.platform.template.AmNode;
import org.apache.commons.lang3.StringUtils;
import org.openehr.rm.common.Link;

/**
 * @author Bostjan Lah
 */
public class StandardArchetypePredicateProvider implements ArchetypePredicateProvider {

    private static final StandardArchetypePredicateProvider INSTANCE = new StandardArchetypePredicateProvider();

    public static StandardArchetypePredicateProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public String getPredicate(AmNode amNode, int index) {
        return StringUtils.isBlank(amNode.getArchetypeNodeId())
                ? ""
                : '[' + amNode.getArchetypeNodeId() + ',' + Link.getNameSuffix(amNode.getName().trim(), index) + ']';
    }
}
