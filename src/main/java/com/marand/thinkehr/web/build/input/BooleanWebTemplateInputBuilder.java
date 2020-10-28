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
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import org.openehr.am.aom.CBoolean;
import org.openehr.rm.datatypes.DvBoolean;

import javax.annotation.Nullable;

/**
 * @author Bostjan Lah
 */
public class BooleanWebTemplateInputBuilder implements WebTemplateInputBuilder<CBoolean> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CBoolean item, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.BOOLEAN);
        if (item != null) {
            if (item.getFalseValid() && !item.getTrueValid()) {
                input.getList().add(WebTemplateCodedValue.TO_CODED_VALUE.apply(Boolean.FALSE));
                input.setFixed(true);
            } else if (!item.getFalseValid() && item.getTrueValid()) {
                input.getList().add(WebTemplateCodedValue.TO_CODED_VALUE.apply(Boolean.TRUE));
                input.setFixed(true);
            }
            if (item.getAssumedValue() != null) {
                input.setDefaultValue(String.valueOf(item.getAssumedValue()));
            }
        }
        DvBoolean defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvBoolean.class);
        if (defaultValue != null) {
            input.setDefaultValue(String.valueOf(defaultValue.getValue()));
        }
        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CBoolean cBoolean = AmUtils.getPrimitiveItem(node.getAmNode(), CBoolean.class, "value");
        WebTemplateInput input = build(node.getAmNode(), cBoolean, context);
        if (cBoolean != null && cBoolean.getAssumedValue() != null) {
            input.setDefaultValue(cBoolean.getAssumedValue());
        }
        DvBoolean defaultValue = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvBoolean.class);
        if (defaultValue != null) {
            input.setDefaultValue(defaultValue.getValue());
        }
        node.getInputs().add(input);
    }
}
