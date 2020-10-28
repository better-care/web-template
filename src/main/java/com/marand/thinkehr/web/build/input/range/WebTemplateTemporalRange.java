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
import org.openehr.base.foundationtypes.Interval;
import org.openehr.base.foundationtypes.IntervalOfDate;
import org.openehr.base.foundationtypes.IntervalOfDateTime;
import org.openehr.base.foundationtypes.IntervalOfDuration;
import org.openehr.base.foundationtypes.IntervalOfTime;

import java.util.Objects;

/**
 * @author Bostjan Lah
 */
public class WebTemplateTemporalRange implements WebTemplateRange<String> {
    private String min;
    private String minOp;
    private String max;
    private String maxOp;

    public WebTemplateTemporalRange() {
    }

    public WebTemplateTemporalRange(IntervalOfTime interval) {
        if (interval != null) {
            build(interval, interval.getLower(), interval.getUpper());
        }
    }

    public WebTemplateTemporalRange(IntervalOfDateTime interval) {
        if (interval != null) {
            build(interval, interval.getLower(), interval.getUpper());
        }
    }

    public WebTemplateTemporalRange(IntervalOfDate interval) {
        if (interval != null) {
            build(interval, interval.getLower(), interval.getUpper());
        }
    }

    public WebTemplateTemporalRange(IntervalOfDuration interval) {
        if (interval != null) {
            build(interval, interval.getLower(), interval.getUpper());
        }
    }

    private void build(Interval interval, String min, String max) {
        if (!interval.isLowerUnbounded()) {
            minOp = RangeUtils.getMinOp(interval);
            this.min = min;
        }
        if (!interval.isUpperUnbounded()) {
            maxOp = RangeUtils.getMaxOp(interval);
            this.max = max;
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
        return RangeUtils.isFixed(this, minOp, maxOp);
    }

    @Override
    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMinOp() {
        return minOp;
    }

    public void setMinOp(String minOp) {
        this.minOp = minOp;
    }

    @Override
    public String getMax() {
        return max;
    }

    public void setMax(String max) {
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
        if (!(o instanceof WebTemplateTemporalRange)) {
            return false;
        }
        WebTemplateTemporalRange that = (WebTemplateTemporalRange)o;
        return Objects.equals(min, that.min) &&
                Objects.equals(minOp, that.minOp) &&
                Objects.equals(max, that.max) &&
                Objects.equals(maxOp, that.maxOp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, minOp, max, maxOp);
    }
}
