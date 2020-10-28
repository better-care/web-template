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

import org.openehr.base.foundationtypes.Interval;

import java.util.Objects;

/**
 * @author Bostjan Lah
 */
public class RangeUtils {
    private RangeUtils() {
    }

    public static String getMinOp(Interval interval) {
        return Boolean.FALSE.equals(interval.isLowerIncluded()) ? ">" : ">=";
    }

    public static String getMaxOp(Interval interval) {
        return Boolean.FALSE.equals(interval.isUpperIncluded()) ? "<" : "<=";
    }

    public static boolean isFixed(WebTemplateRange<?> range, String minOp, String maxOp) {
        return Objects.equals(range.getMin(), range.getMax()) && ">=".equals(minOp) && "<=".equals(maxOp);
    }
}
