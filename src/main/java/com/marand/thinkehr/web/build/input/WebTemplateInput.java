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

package com.marand.thinkehr.web.build.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.marand.thinkehr.web.build.WebTemplateInputType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
@SuppressWarnings({"NonFinalFieldReferenceInEquals", "NonFinalFieldReferencedInHashCode"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class WebTemplateInput {
    private String suffix;
    private WebTemplateInputType type;

    // select
    private final List<WebTemplateCodedValue> list = new ArrayList<>();
    private Boolean listOpen;
    private WebTemplateValidation validation;
    private boolean fixed;
    private Object defaultValue;
    private String terminology;
    private final Set<String> otherTerminologies = new HashSet<>();

    public WebTemplateInput(WebTemplateInputType type) {
        this(type, null);
    }

    public WebTemplateInput(WebTemplateInputType type, String suffix) {
        this.type = type;
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public WebTemplateInputType getType() {
        return type;
    }

    public void setType(WebTemplateInputType type) {
        this.type = type;
    }

    public List<WebTemplateCodedValue> getList() {
        return list;
    }

    public Boolean getListOpen() {
        return listOpen;
    }

    public void setListOpen(boolean listOpen) {
        this.listOpen = listOpen;
    }

    public WebTemplateValidation getValidation() {
        return validation;
    }

    public void setValidation(WebTemplateValidation validation) {
        this.validation = validation;
    }

    @JsonIgnore
    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTerminology() {
        return terminology;
    }

    public void setTerminology(String terminology) {
        this.terminology = terminology;
    }

    public Set<String> getOtherTerminologies() {
        return otherTerminologies;
    }

    @JsonIgnore
    public boolean isExternalTerminology() {
        return terminology != null;
    }

    @SuppressWarnings("OverlyComplexBooleanExpression")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WebTemplateInput)) {
            return false;
        }
        WebTemplateInput webTemplateInput = (WebTemplateInput)o;
        return fixed == webTemplateInput.fixed &&
                Objects.equals(suffix, webTemplateInput.suffix) &&
                type == webTemplateInput.type &&
                Objects.equals(list, webTemplateInput.list) &&
                Objects.equals(listOpen, webTemplateInput.listOpen) &&
                Objects.equals(validation, webTemplateInput.validation) &&
                Objects.equals(defaultValue, webTemplateInput.defaultValue) &&
                Objects.equals(terminology, webTemplateInput.terminology) &&
                Objects.equals(otherTerminologies, webTemplateInput.otherTerminologies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(suffix, type, list, listOpen, validation, fixed, defaultValue, terminology, otherTerminologies);
    }
}
