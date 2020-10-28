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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openehr.base.foundationtypes.IntervalOfReal;

import java.util.Objects;

/**
 * @author Bostjan Lah
 */
@SuppressWarnings({"NonFinalFieldReferenceInEquals", "NonFinalFieldReferencedInHashCode"})
public class WebTemplateDecimalRange implements WebTemplateRange<Float> {
    private String minOp;
    private Float min;
    private String maxOp;
    private Float max;

    public WebTemplateDecimalRange() {
    }

    public WebTemplateDecimalRange(float min, String minOp, float max, String maxOp) {
        this.min = min;
        this.minOp = minOp;
        this.max = max;
        this.maxOp = maxOp;
    }

    public WebTemplateDecimalRange(IntervalOfReal interval) {
        if (interval != null) {
            if (!interval.isLowerUnbounded()) {
                minOp = RangeUtils.getMinOp(interval);
                min = interval.getLower();
            }
            if (!interval.isUpperUnbounded()) {
                maxOp = RangeUtils.getMaxOp(interval);
                max = interval.getUpper();
            }
        }
    }

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return minOp == null && min == null && maxOp == null && max == null;
    }

    @Override
    @JsonIgnore
    public boolean isFixed() {
        return RangeUtils.isFixed(this, minOp, maxOp);

    }

    public String getMinOp() {
        return minOp;
    }

    public void setMinOp(String minOp) {
        this.minOp = minOp;
    }

    @Override
    public Float getMin() {
        return min;
    }

    public void setMin(Float min) {
        this.min = min;
    }

    public String getMaxOp() {
        return maxOp;
    }

    public void setMaxOp(String maxOp) {
        this.maxOp = maxOp;
    }

    @Override
    public Float getMax() {
        return max;
    }

    public void setMax(Float max) {
        this.max = max;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WebTemplateDecimalRange)) {
            return false;
        }
        WebTemplateDecimalRange that = (WebTemplateDecimalRange)o;
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
