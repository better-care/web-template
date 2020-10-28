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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.marand.thinkehr.web.build.input.range.WebTemplateIntegerRange;

import java.util.List;

/**
 * @author Bostjan Lah
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebTemplateCardinality {
    private final WebTemplateIntegerRange range;
    private String path;
    private List<String> ids;

    public WebTemplateCardinality(WebTemplateIntegerRange range) {
        this.range = range;
    }

    @JsonUnwrapped
    public WebTemplateIntegerRange getRange() {
        return range;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @JsonIgnore
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
