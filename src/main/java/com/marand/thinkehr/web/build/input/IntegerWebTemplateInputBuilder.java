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
import care.better.platform.template.AmUtils;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import com.marand.thinkehr.web.build.input.range.WebTemplateValidationIntegerRange;
import org.openehr.am.aom.CInteger;
import org.openehr.rm.datatypes.DvCount;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

/**
 * @author Bostjan Lah
 */
public class IntegerWebTemplateInputBuilder implements WebTemplateInputBuilder<CInteger> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CInteger cInteger, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.INTEGER);
        if (cInteger != null) {
            WebTemplateValidationIntegerRange range = new WebTemplateValidationIntegerRange(cInteger.getRange());
            if (!range.isEmpty()) {
                input.setValidation(new WebTemplateValidation());
                input.getValidation().setRange(range);
            }
            if (!cInteger.getList().isEmpty()) {
                input.getList().addAll(cInteger.getList().stream().map(WebTemplateCodedValue.TO_CODED_VALUE).collect(Collectors.toList()));
            }
            input.setFixed(range.isFixed() || cInteger.getList().size() == 1);
            if (cInteger.getAssumedValue() != null) {
                input.setDefaultValue(String.valueOf(cInteger.getAssumedValue()));
            }
        }

        DvCount defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvCount.class);
        if (defaultValue != null) {
            input.setDefaultValue(String.valueOf(defaultValue.getMagnitude()));
        }
        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CInteger cInteger = AmUtils.getPrimitiveItem(node.getAmNode(), CInteger.class, WebTemplateConstants.MAGNITUDE_ATTRIBUTE);
        WebTemplateInput input = build(node.getAmNode(), cInteger, context);
        if (cInteger != null && cInteger.getAssumedValue() != null) {
            input.setDefaultValue(cInteger.getAssumedValue());
        }
        DvCount defaultValue = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvCount.class);
        if (defaultValue != null) {
            input.setDefaultValue(defaultValue.getMagnitude());
        }
        node.getInputs().add(input);
    }
}
