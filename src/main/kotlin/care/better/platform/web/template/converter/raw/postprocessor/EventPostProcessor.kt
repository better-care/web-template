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

package care.better.platform.web.template.converter.raw.postprocessor

import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.openehr.rm.datastructures.Event
import org.openehr.rm.datatypes.DvDateTime

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Instance of [LocatablePostProcessor] that post-processes [Event].
 *
 * @constructor Creates a new instance of [EventPostProcessor]
 */
internal open class EventPostProcessor<T : Event> : LocatablePostProcessor<T>() {
    companion object {
        private val INSTANCE: EventPostProcessor<out Event> = EventPostProcessor()

        @JvmStatic
        fun getInstance(): EventPostProcessor<out Event> = INSTANCE
    }

    private val supportedClass = Event::class.java

    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: T, webTemplatePath: WebTemplatePath?) {
        super.postProcess(conversionContext, amNode, instance, webTemplatePath)
        if (instance.time == null) {
            instance.time = DvDateTime.Companion.create(conversionContext.time)
        }
    }

    override fun getOrder(): Int = Integer.MAX_VALUE - 2

    override fun getType(): Class<*> = supportedClass
}
