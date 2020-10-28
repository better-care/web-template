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

import care.better.platform.template.AmNode;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.input.range.WebTemplateDecimalRange;
import org.openehr.am.aom.CReal;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

/**
 * @author Bostjan Lah
 */
public class DecimalWebTemplateInputBuilder implements WebTemplateInputBuilder<CReal> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CReal cReal, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.DECIMAL);
        if (cReal != null) {
            WebTemplateDecimalRange range = new WebTemplateDecimalRange(cReal.getRange());
            if (!range.isEmpty()) {
                input.setValidation(new WebTemplateValidation());
                input.getValidation().setRange(range);
            }
            if (!cReal.getList().isEmpty()) {
                input.getList().addAll(cReal.getList().stream().map(WebTemplateCodedValue.TO_CODED_VALUE).collect(Collectors.toList()));
            }
            input.setFixed(range.isFixed() || cReal.getList().size() == 1);
            if (cReal.getAssumedValue() != null) {
                input.setDefaultValue(String.valueOf(cReal.getAssumedValue()));
            }
        }

        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        throw new UnsupportedOperationException("This type not allowed!");
    }
}
