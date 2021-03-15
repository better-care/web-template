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
import care.better.platform.template.AmUtils
import care.better.platform.utils.DateTimeConversionUtils
import care.better.platform.utils.JSR310ConversionUtils
import care.better.platform.utils.JodaConversionUtils
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.raw.context.ConversionContext
import org.joda.time.Period
import org.openehr.am.aom.CDuration
import org.openehr.base.basetypes.GenericId
import org.openehr.rm.datastructures.Event
import org.openehr.rm.datastructures.History
import org.openehr.rm.datatypes.DvDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [PostProcessor] that post-processes [GenericId].
 */
internal object HistoryPostProcessor : LocatablePostProcessor<History>() {
    private val FORMATTER = DateTimeFormatter.ISO_DATE_TIME

    private val supportedClass = History::class.java

    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: History, webTemplatePath: WebTemplatePath?) {
        super.postProcess(conversionContext, amNode, instance, webTemplatePath)

        if (instance.events.isNotEmpty() && instance.origin == null) {
            if (conversionContext.historyOrigin != null) {
                instance.origin = DvDateTime.create(conversionContext.historyOrigin)
                instance.events.forEach {
                    val offset = findEventOffset(amNode, it)
                    if (offset == null) {
                        if (it.time == null) {
                            it.time = instance.origin
                        }
                    } else {
                        val offsetDateTime = OffsetDateTime.parse(instance.origin!!.value, FORMATTER)
                        it.time = DvDateTime.create(DateTimeConversionUtils.plusPeriod(offsetDateTime, offset))
                    }
                }
            } else {
                instance.events.asSequence()
                    .filter { it.time != null }
                    .minByOrNull { JSR310ConversionUtils.toOffsetDateTime(it.time!!) }
                    ?.also {
                        val min = JSR310ConversionUtils.toOffsetDateTime(it.time!!)
                        val offset = findEventOffset(amNode, it)
                        if (offset == null) {
                            instance.origin = DvDateTime.create(min)
                        } else {
                            instance.origin = DvDateTime.create(DateTimeConversionUtils.plusPeriod(min, offset.negated()))
                        }
                    }
            }
        }
    }

    /**
     * Finds the event offset from [AmNode].
     *
     * @param amNode [AmNode]
     * @param event [Event]
     */
    private fun findEventOffset(amNode: AmNode?, event: Event): Period? =
        amNode?.attributes?.get("events")?.children?.mapNotNull { node ->
            if (event.archetypeNodeId == node.archetypeNodeId) {
                val offset = AmUtils.getPrimitiveItem(node, CDuration::class.java, "offset", "value")
                if (offset?.range?.lower != null) {
                    return@mapNotNull JodaConversionUtils.toPeriod(offset.range?.lower!!)
                }
            }
            return@mapNotNull null
        }?.firstOrNull()

    override fun getType(): Class<*> = supportedClass
}
