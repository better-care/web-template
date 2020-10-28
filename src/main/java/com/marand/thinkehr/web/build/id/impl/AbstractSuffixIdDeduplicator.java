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

package com.marand.thinkehr.web.build.id.impl;

import com.marand.thinkehr.web.build.id.IdDeduplicator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
public abstract class AbstractSuffixIdDeduplicator implements IdDeduplicator {
    private final Map<String, Set<String>> allIds = new HashMap<>();

    @Override
    public String getUniqueBaseId(String parentId, String baseId) {
        if (!allIds.containsKey(parentId)) {
            allIds.put(parentId, new HashSet<>());
        }

        String id;
        Set<String> ids = allIds.get(parentId);
        if (ids.contains(baseId)) {
            String suffix = getUniqueSuffix(ids, baseId);
            if (ids.contains(baseId + suffix)) {
                throw new IllegalStateException("Unable to deduplicate id=" + parentId + '/' + baseId);
            } else {
                id = baseId + suffix;
                ids.add(id);
            }
        } else {
            id = baseId;
            ids.add(id);
        }
        return id;
    }

    protected abstract String getUniqueSuffix(Set<String> ids, String baseId);
}
