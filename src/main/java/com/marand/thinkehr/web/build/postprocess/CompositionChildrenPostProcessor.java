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

import com.marand.thinkehr.web.build.WebTemplateNode;

/**
 * @author Bostjan Lah
 */
public class CompositionChildrenPostProcessor implements ChildrenPostProcessor {
    @Override
    public void postProcess(WebTemplateNode node) {
        int categoryIndex = -1;
        int languageIndex = -1;
        for (int i = 0; i < node.getChildren().size(); i++) {
            WebTemplateNode webTemplateNode = node.getChildren().get(i);
            if ("/category".equals(webTemplateNode.getPath())) {
                categoryIndex = i;
            } else if ("/language".equals(webTemplateNode.getPath())) {
                languageIndex = i;
            }
        }

        if (categoryIndex != -1) {
            if (languageIndex == -1) {
                node.getChildren().add(node.getChildren().remove(categoryIndex));
            } else {
                node.getChildren().add(languageIndex - 1, node.getChildren().remove(categoryIndex));
            }
        }
    }
}
