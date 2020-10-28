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
import org.openehr.base.foundationtypes.IntervalOfInteger;

import java.util.Objects;

/**
 * @author Bostjan Lah
 */
public class WebTemplateValidationIntegerRange implements WebTemplateRange<Integer> {
    private String minOp;
    private Integer min;
    private String maxOp;
    private Integer max;

    public WebTemplateValidationIntegerRange(Integer min, Integer max) {
        this.min = min;
        if (min != null) {
            minOp = ">=";
        }
        this.max = max;
        if (max != null) {
            maxOp = "<=";
        }
    }

    public WebTemplateValidationIntegerRange(IntervalOfInteger interval) {
        this(AmUtils.getMin(interval), getMax(interval));
    }

    private static Integer getMax(IntervalOfInteger interval) {
        if (interval != null && !interval.isUpperUnbounded() && interval.getUpper() != null) {
            return Objects.equals(Boolean.FALSE, interval.isUpperIncluded()) ? interval.getUpper() - 1 : interval.getUpper();
        } else {
            return null;
        }
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

    public String getMinOp() {
        return minOp;
    }

    public void setMinOp(String minOp) {
        this.minOp = minOp;
    }

    @Override
    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public String getMaxOp() {
        return maxOp;
    }

    public void setMaxOp(String maxOp) {
        this.maxOp = maxOp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WebTemplateValidationIntegerRange)) {
            return false;
        }
        WebTemplateValidationIntegerRange that = (WebTemplateValidationIntegerRange)o;
        return Objects.equals(minOp, that.minOp) &&
                Objects.equals(min, that.min) &&
                Objects.equals(maxOp, that.maxOp) &&
                Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minOp, min, maxOp, max);
    }
}
