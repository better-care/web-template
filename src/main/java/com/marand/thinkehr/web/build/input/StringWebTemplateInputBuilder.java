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
import org.openehr.am.aom.CString;
import org.openehr.rm.datatypes.DvIdentifier;
import org.openehr.rm.datatypes.DvText;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

/**
 * @author Bostjan Lah
 */
public class StringWebTemplateInputBuilder implements WebTemplateInputBuilder<CString> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CString cString, WebTemplateBuilderContext context) {
        return createTextInput(amNode, cString, null);
    }

    public static WebTemplateInput createTextInput(AmNode amNode, CString cString, String suffix) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.TEXT, suffix);
        if (cString != null) {
            if (cString.getPattern() != null) {
                input.setValidation(new WebTemplateValidation());
                input.getValidation().setPattern(cString.getPattern());
            }
            if (!cString.getList().isEmpty()) {
                input.getList().addAll(cString.getList().stream().map(WebTemplateCodedValue.TO_CODED_VALUE).collect(Collectors.toList()));
            }
            input.setListOpen(Boolean.TRUE.equals(cString.getListOpen()));
            input.setFixed(cString.getList().size() == 1 && !Boolean.TRUE.equals(cString.getListOpen()));
        }

        Class<?> dataValueClass = WebTemplateUtils.getDataValueClass(amNode);
        if (dataValueClass != null) {
            Object defaultValue = WebTemplateUtils.getDefaultValue(amNode, dataValueClass);
            if (defaultValue != null) {
                if (defaultValue instanceof DvIdentifier) {
                    DvIdentifier value = (DvIdentifier)defaultValue;
                    input.setDefaultValue(value.getIssuer() + "::" + value.getAssigner() + "::" + value.getId() + "::" + value.getType());
                } else if (defaultValue instanceof DvText) {
                    DvText value = (DvText)defaultValue;
                    input.setDefaultValue(String.valueOf(value.getValue()));
                }
            }
        }

        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CString cString = node.getAmNode() == null ? null : AmUtils.getPrimitiveItem(node.getAmNode(), CString.class, "value");
        node.getInputs().add(build(node.getAmNode(), cString, context));
    }
}
