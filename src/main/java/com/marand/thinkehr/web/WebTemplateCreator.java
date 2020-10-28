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

import care.better.platform.template.AmNode;
import care.better.platform.web.template.WebTemplate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.marand.thinkehr.web.build.WebTemplateNode;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author Bostjan Lah
 */
public class WebTemplateCreator {
    private WebTemplateCreator() {
    }

    public static WebTemplate create(
            @Nonnull WebTemplateNode tree,
            @Nonnull String templateId,
            @Nonnull String defaultLanguage,
            @Nonnull Collection<String> languages,
            @Nonnull Multimap<AmNode, WebTemplateNode> nodes,
            @Nonnull String version) {
        return new WebTemplate(tree, templateId, defaultLanguage, ImmutableSet.copyOf(languages), version, nodes);
    }
}
