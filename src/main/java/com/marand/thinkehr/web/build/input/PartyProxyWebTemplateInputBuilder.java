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
import org.openehr.am.aom.CString;

import javax.annotation.Nullable;

/**
 * @author Matic Ribic
 */
public class PartyProxyWebTemplateInputBuilder implements WebTemplateInputBuilder<Object> {

    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable Object ignored, WebTemplateBuilderContext context) {
        return new WebTemplateInput(WebTemplateInputType.TEXT);
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        node.getInputs().add(createInput(node, WebTemplateConstants.ID_ATTRIBUTE));
        node.getInputs().add(createInput(node, WebTemplateConstants.ID_SCHEME_ATTRIBUTE));
        node.getInputs().add(createInput(node, WebTemplateConstants.ID_NAMESPACE_ATTRIBUTE));
    }

    protected WebTemplateInput createInput(WebTemplateNode node, String suffix) {
        CString cString = AmUtils.getPrimitiveItem(node.getAmNode(), CString.class, suffix);
        return StringWebTemplateInputBuilder.createTextInput(node.getAmNode(), cString, suffix);
    }
}
