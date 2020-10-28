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
import com.marand.thinkehr.web.build.input.range.WebTemplateRange;
import com.marand.thinkehr.web.build.input.range.WebTemplateValidationIntegerRange;

import java.util.Objects;

/**
 * @author Bostjan Lah
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebTemplateValidation {
    // text or number
    private String pattern;

    // range
    private WebTemplateRange<?> range;

    // precision
    private WebTemplateValidationIntegerRange precision;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public WebTemplateRange<?> getRange() {
        return range;
    }

    public void setRange(WebTemplateRange<?> range) {
        this.range = range;
    }

    public WebTemplateValidationIntegerRange getPrecision() {
        return precision;
    }

    public void setPrecision(WebTemplateValidationIntegerRange precision) {
        this.precision = precision;
    }

    @JsonIgnore
    public Integer getMaxPrecision() {
        return precision == null ? null : precision.getMax();
    }

    @JsonIgnore
    public boolean isEmpty() {
        return pattern == null && range == null && precision == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WebTemplateValidation)) {
            return false;
        }
        WebTemplateValidation that = (WebTemplateValidation)o;
        return Objects.equals(pattern, that.pattern) &&
                Objects.equals(range, that.range) &&
                Objects.equals(precision, that.precision);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, range, precision);
    }
}
