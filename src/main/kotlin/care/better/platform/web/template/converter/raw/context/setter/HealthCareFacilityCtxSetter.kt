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
import org.openehr.base.basetypes.PartyRef
import org.openehr.rm.common.PartyIdentified

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Implementation of [CtxSetter] that sets the health care facility to the [ConversionContext.Builder].
 */
internal object HealthCareFacilityCtxSetter : CtxSetter {
    @Suppress("UNCHECKED_CAST")
    override fun set(builder: ConversionContext.Builder, valueConverter: ValueConverter, value: Any) {
        with(value as Map<String, String>) {
            val healthCareFacility: PartyIdentified =
                if (builder.getHealthCareFacility() == null)
                    PartyIdentified().also { builder.withHealthCareFacility(it) }
                else builder.getHealthCareFacility()!!

            this["|id"]?.also { id ->
                healthCareFacility.also {
                    it.externalRef = PartyRef().apply {
                        this.type = "PARTY"
                        this.id = GenericId().apply {
                            this.value = java.lang.String.valueOf(id)
                            this.scheme = builder.getIdScheme()
                        }
                        this.namespace = builder.getIdNamespace()
                    }
                }
            }

            this["|name"]?.also { name ->
                healthCareFacility.name = java.lang.String.valueOf(name)
            }
        }
    }
}
