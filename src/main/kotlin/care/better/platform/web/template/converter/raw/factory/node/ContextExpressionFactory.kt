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

package care.better.platform.web.template.converter.raw.factory.node

import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.openehr.proc.taskplanning.BooleanContextExpression
import org.openehr.proc.taskplanning.ContextExpression

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectInstanceClassFactory] that creates a new instance of [ContextExpression].
 */
internal object ContextExpressionFactory : RmObjectInstanceClassFactory<ContextExpression<*>>(ContextExpression::class.java) {
    override fun create(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): ContextExpression<*> =
        (if (isBoolean(amNode)) BooleanContextExpression() else super.create(conversionContext, amNode, webTemplatePath)).apply {
            val name = amNode?.name
            val parentName = amNode?.parent?.name
            if (name == null && parentName != null) {
                this.name = parentName
            }
        }

    private fun isBoolean(amNode: AmNode?): Boolean =
        if (amNode == null) {
            false
        } else {
            with(AmUtils.resolvePath(amNode, "type")) {
                amNode.rmType == "BOOLEAN_CONTEXT_EXPRESSION"
                        || amNode.rmType == "CONTEXT_EXPRESSION<TYPE_DEF_BOOLEAN>"
                        || amNode.rmType == "CONTEXT_EXPRESSION<BOOLEAN>"
                        || (this != null && this.rmType == "TYPE_DEF_BOOLEAN")
            }
        }
}
