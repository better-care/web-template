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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bostjan Lah
 */
public class CareflowStepWebTemplateCodedValue extends WebTemplateCodedValue {
    private final List<String> currentStates = new ArrayList<>();

    public CareflowStepWebTemplateCodedValue(WebTemplateCodedValue codedValue, List<String> currentStates) {
        super(codedValue.getValue(), codedValue.getLabel());
        getLocalizedLabels().putAll(codedValue.getLocalizedLabels());
        this.currentStates.addAll(currentStates);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<String> getCurrentStates() {
        return currentStates;
    }
}
