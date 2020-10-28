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

import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.input.WebTemplateInput;

/**
 * @author Bostjan Lah
 */
public class ElementChildrenPostProcessor implements ChildrenPostProcessor {
    @Override
    public void postProcess(WebTemplateNode node) {
        if (node.getChildren().size() == 2) {
            WebTemplateInputType type0 = node.getChildren().get(0).getInput() == null ? null : node.getChildren().get(0).getInput().getType();
            WebTemplateInputType type1 = node.getChildren().get(1).getInput() == null ? null : node.getChildren().get(1).getInput().getType();
            if (type0 == WebTemplateInputType.CODED_TEXT && type1 == WebTemplateInputType.TEXT) {
                compactToCodedTextWithOther(node, 0, 1);
            } else if (type1 == WebTemplateInputType.CODED_TEXT && type0 == WebTemplateInputType.TEXT) {
                compactToCodedTextWithOther(node, 1, 0);
            }
        }

        // make sure multiple choice ELEMENTs don't mark all choices as mandatory
        if (node.getChildren().size() > 1) {
            for (WebTemplateNode webTemplateNode : node.getChildren()) {
                webTemplateNode.getOccurences().setMin(0);
            }
        }
    }

    private void compactToCodedTextWithOther(WebTemplateNode node, int codedTextIndex, int textIndex) {
        WebTemplateNode node0 = node.getChildren().get(codedTextIndex);
        node0.getInputs().add(new WebTemplateInput(WebTemplateInputType.TEXT, WebTemplateConstants.OTHER_ATTRIBUTE));
        node.getChildren().get(codedTextIndex).getInput().setListOpen(true);
        node.getChildren().remove(textIndex);
    }
}
