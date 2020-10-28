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

import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.input.range.WebTemplateTemporalRange;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Bostjan Lah
 */
public abstract class TemporalWebTemplateInputBuilder<T> implements WebTemplateInputBuilder<T> {
    public WebTemplateInput build(WebTemplateInputType type, WebTemplateTemporalRange range, String pattern) {
        WebTemplateInput input = new WebTemplateInput(type);
        if (!range.isEmpty()) {
            input.setValidation(new WebTemplateValidation());
            input.getValidation().setRange(range);
        }
        if (StringUtils.isNotBlank(pattern)) {
            if (input.getValidation() == null) {
                input.setValidation(new WebTemplateValidation());
            }
            input.getValidation().setPattern(pattern);
        }
        input.setFixed(range.isFixed());
        return input;
    }

    public WebTemplateInput build(WebTemplateInputType type) {
        return new WebTemplateInput(type);
    }
}
