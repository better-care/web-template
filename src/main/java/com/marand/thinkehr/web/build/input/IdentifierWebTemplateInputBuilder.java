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
import org.openehr.am.aom.CString;
import org.openehr.rm.datatypes.DvIdentifier;

import javax.annotation.Nullable;

/**
 * @author Bostjan Lah
 */
public class IdentifierWebTemplateInputBuilder implements WebTemplateInputBuilder<Object> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable Object ignored, WebTemplateBuilderContext context) {
        return new WebTemplateInput(WebTemplateInputType.TEXT);
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        DvIdentifier defaultValue = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvIdentifier.class);

        node.getInputs().add(createInput(node, "id", defaultValue == null ? null : defaultValue.getId()));
        node.getInputs().add(createInput(node, WebTemplateConstants.TYPE_ATTRIBUTE, defaultValue == null ? null : defaultValue.getType()));
        node.getInputs().add(createInput(node, "issuer", defaultValue == null ? null : defaultValue.getIssuer()));
        node.getInputs().add(createInput(node, "assigner", defaultValue == null ? null : defaultValue.getAssigner()));
    }

    private WebTemplateInput createInput(WebTemplateNode node, String suffix, String defaultValue) {
        CString cString = AmUtils.getPrimitiveItem(node.getAmNode(), CString.class, suffix);
        WebTemplateInput input = StringWebTemplateInputBuilder.createTextInput(node.getAmNode(), cString, suffix);
        input.setDefaultValue(defaultValue);
        return input;
    }
}
