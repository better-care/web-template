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

package care.better.platform.web.template.converter.raw.context.setter

import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.value.ValueConverter
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.ObjectRef

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [CtxSetter] that sets the work_flow_id to the [ConversionContext.Builder].
 */
internal object WorkFlowIdCtxSetter : CtxSetter {
    @Suppress("UNCHECKED_CAST")
    override fun set(builder: ConversionContext.Builder, valueConverter: ValueConverter, value: Any) {
        if (builder.getWorkflowId() == null) {
            builder.withWorkFlowId(ObjectRef().apply {
                this.namespace = builder.getIdNamespace()
                this.type = builder.getIdentifierType()
                this.id = GenericId().apply {
                    this.scheme = builder.getIdScheme()
                }
            })
        }

        with(value as Map<String, String>) {
            val objectRef = builder.getWorkflowId()!!
            this["|namespace"]?.also { objectRef.namespace = it }
            this["|type"]?.also { objectRef.type = it }
            this["|id_scheme"]?.also {
                if (objectRef.id == null)
                    objectRef.id = GenericId().apply { this.scheme = it }
                else
                    (objectRef.id as GenericId).scheme = it
            }
            this["|id"]?.also {
                if (objectRef.id == null)
                    objectRef.id = GenericId().apply { this.value = it }
                else
                    (objectRef.id as GenericId).value = it
            }
        }
    }
}
