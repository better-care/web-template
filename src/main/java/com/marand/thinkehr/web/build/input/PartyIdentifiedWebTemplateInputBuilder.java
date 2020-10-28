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

import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.WebTemplateNode;

/**
 * @author Matic Ribic
 */
public class PartyIdentifiedWebTemplateInputBuilder extends PartyProxyWebTemplateInputBuilder {

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        super.build(node, context);
        node.getInputs().add(createInput(node, WebTemplateConstants.NAME_ATTRIBUTE));
    }
}
