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

package care.better.platform.web.template.builder.postprocess

import care.better.platform.web.template.builder.model.WebTemplateNode


/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
class CompositionChildrenPostProcessor : WebTemplateNodeChildrenPostProcessor {
    override fun postProcess(webTemplateNode: WebTemplateNode) {
        var categoryIndex = -1
        var languageIndex = -1

        webTemplateNode.children.forEachIndexed { index, node ->
            if ("/category" == node.path)
                categoryIndex = index
            else if ("/language" == node.path)
                languageIndex = index
        }

        if (categoryIndex != -1) {
            if (languageIndex == -1)
                webTemplateNode.children.add(webTemplateNode.children.removeAt(categoryIndex))
            else
                webTemplateNode.children.add(languageIndex - 1, webTemplateNode.children.removeAt(categoryIndex))
        }
    }
}
