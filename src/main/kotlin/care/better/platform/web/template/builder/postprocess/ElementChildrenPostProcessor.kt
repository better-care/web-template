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

import care.better.platform.web.template.builder.model.WebTemplateInputType
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ElementChildrenPostProcessor : WebTemplateNodeChildrenPostProcessor {
    override fun postProcess(webTemplateNode: WebTemplateNode) {
        if (webTemplateNode.children.size == 2) {
            val firstType = if (webTemplateNode.children[0].getInput() == null) null else webTemplateNode.children[0].getInput()!!.type
            val secondType = if (webTemplateNode.children[1].getInput() == null) null else webTemplateNode.children[1].getInput()!!.type

            if (firstType == WebTemplateInputType.CODED_TEXT && secondType == WebTemplateInputType.TEXT)
                compactToCodedTextWithOther(webTemplateNode, 0, 1)
            else if (secondType == WebTemplateInputType.CODED_TEXT && firstType == WebTemplateInputType.TEXT)
                compactToCodedTextWithOther(webTemplateNode, 1, 0)
        }

        if (webTemplateNode.children.size > 1) {
            webTemplateNode.children.forEach { it.occurences?.min = 0 }
        }
    }

    private fun compactToCodedTextWithOther(webTemplateNode: WebTemplateNode, codedTextIndex: Int, textIndex: Int) {
        webTemplateNode.children[codedTextIndex].also {
            it.inputs.add(WebTemplateInput(WebTemplateInputType.TEXT, "other"))
        }
        webTemplateNode.children[codedTextIndex].getInput()?.listOpen = true
        webTemplateNode.children.removeAt(textIndex)
    }
}
