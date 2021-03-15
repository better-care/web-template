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
import care.better.platform.web.template.builder.utils.WebTemplateBuilderUtils
import org.openehr.rm.datatypes.DvParsable

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ParsableChildrenPostProcessor : WebTemplateNodeChildrenPostProcessor {
    override fun postProcess(webTemplateNode: WebTemplateNode) {
        webTemplateNode.children.clear()
        webTemplateNode.setInput(WebTemplateInput(WebTemplateInputType.TEXT))
        WebTemplateBuilderUtils.getDefaultValue(webTemplateNode.amNode, DvParsable::class.java)?.also {
            webTemplateNode.getInput()?.defaultValue = it.value
        }
    }
}
