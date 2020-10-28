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

package com.marand.thinkehr.web.build.input.range;

import care.better.platform.template.AmUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openehr.base.foundationtypes.IntervalOfInteger;

import java.util.Objects;

/**
 * @author Bostjan Lah
 */
public class WebTemplateIntegerRange implements WebTemplateRange<Integer> {
    private Integer min;
    private Integer max;

    public WebTemplateIntegerRange(Integer min, Integer max) {
        this.min = min;
        this.max = max;
    }

    public WebTemplateIntegerRange(IntervalOfInteger interval) {
        min = AmUtils.getMin(interval);
        max = AmUtils.getMax(interval);
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return min == null && max == null;
    }

    @Override
    @JsonIgnore
    public boolean isFixed() {
        return min != null && Objects.equals(min, max);
    }

    @Override
    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    @Override
    @JsonIgnore
    public Integer getMax() {
        return max;
    }

    @JsonProperty("max")
    public Integer getJsonMax() {
        return max == null ? Integer.valueOf(-1) : max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WebTemplateIntegerRange)) {
            return false;
        }
        WebTemplateIntegerRange that = (WebTemplateIntegerRange)o;
        return Objects.equals(min, that.min) && Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}
