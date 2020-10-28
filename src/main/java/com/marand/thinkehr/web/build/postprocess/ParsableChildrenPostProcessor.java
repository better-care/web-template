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

package com.marand.thinkehr.web.build.postprocess;

import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import com.marand.thinkehr.web.build.input.WebTemplateInput;
import org.openehr.rm.datatypes.DvParsable;

/**
 * @author Bostjan Lah
 */
public class ParsableChildrenPostProcessor implements ChildrenPostProcessor {
    @Override
    public void postProcess(WebTemplateNode node) {
        node.getChildren().clear();
        node.setInput(new WebTemplateInput(WebTemplateInputType.TEXT));

        DvParsable defaultValue = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvParsable.class);
        if (defaultValue != null) {
            node.getInput().setDefaultValue(defaultValue.getValue());
        }
    }
}
