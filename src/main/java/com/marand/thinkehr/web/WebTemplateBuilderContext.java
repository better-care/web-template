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

import com.google.common.collect.ImmutableSet;
import com.marand.thinkehr.web.filter.NoopPathFilter;
import com.marand.thinkehr.web.filter.PathFilter;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * Context for web template builder.
 *
 * @author Bostjan Lah
 */
public class WebTemplateBuilderContext {
    private String defaultLanguage;
    private Collection<String> languages;
    private boolean addDescriptions = true;
    private PathFilter filter = new NoopPathFilter();

    protected WebTemplateBuilderContext() {
        this(null, Collections.emptySet());
    }

    public WebTemplateBuilderContext(String defaultLanguage) {
        this(defaultLanguage, Collections.emptySet());
    }

    public WebTemplateBuilderContext(@Nonnull Collection<String> languages) {
        this(null, languages);
    }

    public WebTemplateBuilderContext(String defaultLanguage, @Nonnull Collection<String> languages) {
        this.defaultLanguage = defaultLanguage;
        this.languages = ImmutableSet.copyOf(languages);
    }

    /**
     * Gets default language code for web template. This is an ISO 639-1 code. All calls on web template that return
     * language specific texts will return texts in this language.
     *
     * @return language
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Sets language code for web template. This is an ISO 639-1 code.
     *
     * @param defaultLanguage ISO 639-1 language code
     */
    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    /**
     * Gets additional languages for web template. These are ISO 639-1 codes.
     *
     * @return languages
     */
    public Collection<String> getLanguages() {
        return languages;
    }

    /**
     * Sets additional languages for web template. These are ISO 639-1 codes.
     *
     * @param languages languages
     */
    public void setLanguages(@Nonnull Collection<String> languages) {
        this.languages = ImmutableSet.copyOf(languages);
    }

    /**
     * Gets filter for filtering which part of template needs to be used.
     *
     * @return path filter
     * @see PathFilter
     */
    public PathFilter getFilter() {
        return filter;
    }

    /**
     * Sets filter for filtering which part of template needs to be used.
     *
     * @param filter path filter
     * @see PathFilter
     */
    public void setFilter(PathFilter filter) {
        this.filter = filter;
    }

    /**
     * Gets setting for use of descriptions.
     *
     * @return {@code true} if descriptions are used
     */
    public boolean isAddDescriptions() {
        return addDescriptions;
    }

    /**
     * Sets use of descriptions in web templates. This will populate attribute
     * {@code localizedDescriptions} on web template nodes as well as list of options
     * for DV_CODED_TEXTs and DV_ORDINALs.
     * <p>
     * By default this setting is {@code true}.
     *
     * @param addDescriptions {@code true} to add descriptions
     */
    public void setAddDescriptions(boolean addDescriptions) {
        this.addDescriptions = addDescriptions;
    }
}
