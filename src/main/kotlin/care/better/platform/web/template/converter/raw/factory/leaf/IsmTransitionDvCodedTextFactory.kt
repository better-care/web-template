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

package care.better.platform.web.template.converter.raw.factory.leaf

import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.constant.WebTemplateConstants.ISM_TRANSITION_GROUP_NAME
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.datatypes.DvCodedText

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [DvCodedTextFactory] that creates a new instance of [DvCodedText].
 */
internal object IsmTransitionDvCodedTextFactory : DvCodedTextFactory() {

    override fun afterPropertiesSet(amNode: AmNode, rmObject: DvCodedText) {
        if (isCurrentStateNode(amNode)) {
            if (rmObject.definingCode?.terminologyId == null) {
                rmObject.definingCode?.terminologyId = getTerminologyIdFromAnNode(amNode)
            }
        }
    }

    /**
     * Sets value to [DvCodedText] from [JsonNode] "" entry value.
     *
     * @param conversionContext [ConversionContext]
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvCodedText]
     */
    override fun handleBlankAttribute(conversionContext: ConversionContext, amNode: AmNode, jsonNode: JsonNode, rmObject: DvCodedText) {
        if (isCurrentStateNode(amNode)) {
            try {
                DvCodedText.createFromOpenEhrTerminology(ISM_TRANSITION_GROUP_NAME, jsonNode.asText()).also {
                    rmObject.definingCode = it.definingCode
                    rmObject.value = it.value
                }
                handleDvCodedTextStringTerminology(amNode, rmObject.definingCode!!, null)
            } catch (ignored: ConversionException) {
            }
        } else {
            handleDvCodedTextString(conversionContext, amNode, rmObject, jsonNode.asText())
        }
    }


    /**
     * Sets value to [DvCodedText] from [JsonNode] "|value" entry value.
     *
     * @param amNode [amNode]
     * @param jsonNode [JsonNode]
     * @param rmObject [DvCodedText]
     */
    override fun handleValueAttribute(amNode: AmNode, jsonNode: JsonNode, rmObject: DvCodedText) {
        super.handleValueAttribute(amNode, jsonNode, rmObject)
        if (isCurrentStateNode(amNode)) {
            try {
                DvCodedText.createFromOpenEhrTerminology(ISM_TRANSITION_GROUP_NAME, jsonNode.asText()).also { rmObject.definingCode = it.definingCode }
            } catch (ignored: ConversionException) {
            }
        }
    }

    override fun getTerminologyIdFromAnNode(amNode: AmNode): TerminologyId =
        TerminologyId().apply { this.value = if (isCareFlowStepNode(amNode)) "local" else "openehr" }

    /**
     * Checks if [AmNode] is for careflow_step.
     *
     * @param amNode [AmNode]
     * @return [Boolean] indicating if [AmNode] is for current_state or not
     */
    private fun isCurrentStateNode(amNode: AmNode): Boolean = amNode.name == "current_state"

    /**
     * Checks if [AmNode] is for careflow_step.
     *
     * @param amNode [AmNode]
     * @return [Boolean] indicating if [AmNode] is for careflow_step or not
     */
    private fun isCareFlowStepNode(amNode: AmNode): Boolean = amNode.name == "careflow_step"
}
