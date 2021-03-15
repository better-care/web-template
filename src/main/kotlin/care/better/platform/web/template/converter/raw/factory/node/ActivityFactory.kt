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
import org.openehr.am.aom.CString
import org.openehr.rm.composition.Activity
import org.openehr.rm.datatypes.DvParsable

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [LocatableFactory] that creates a new instance of [Activity].
 */
internal object ActivityFactory : LocatableFactory<Activity>() {
    override fun createLocatable(conversionContext: ConversionContext, amNode: AmNode?, webTemplatePath: WebTemplatePath?): Activity =
        Activity().apply {
            val activityTimingProvider = conversionContext.activityTimingProvider
            if (activityTimingProvider != null)
                this.timing = activityTimingProvider.getTiming(webTemplatePath.toString())
            else
                this.timing = DvParsable().apply { this.formalism = "timing" }

            val cString = amNode?.let { AmUtils.getPrimitiveItem(it, CString::class.java, "action_archetype_id") }
            if (cString?.pattern != null)
                this.actionArchetypeId = cString.pattern
            else
                this.actionArchetypeId = "/.*/"

        }
}
