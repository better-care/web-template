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

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Bostjan Lah
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebTemplateCodedValue extends CodedValue {
    public static final Function<Object, WebTemplateCodedValue> TO_CODED_VALUE = input -> {
        String inputValue = String.valueOf(input);
        return new WebTemplateCodedValue(inputValue, inputValue);
    };

    private final Map<String, String> localizedLabels = new HashMap<>();
    private final Map<String, String> localizedDescriptions = new HashMap<>();
    private final Map<String, WebTemplateBindingCodedValue> termBindings = new LinkedHashMap<>(1);
    private WebTemplateValidation validation;

    public WebTemplateCodedValue(String value, String label) {
        super(value, label);
    }

    public WebTemplateValidation getValidation() {
        return validation;
    }

    public void setValidation(WebTemplateValidation validation) {
        this.validation = validation;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getLocalizedLabels() {
        return localizedLabels;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getLocalizedDescriptions() {
        return localizedDescriptions;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, WebTemplateBindingCodedValue> getTermBindings() {
        return termBindings;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WebTemplateCodedValue other = (WebTemplateCodedValue)o;
        return Objects.equals(getLabel(), other.getLabel()) && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getLabel());
    }
}
