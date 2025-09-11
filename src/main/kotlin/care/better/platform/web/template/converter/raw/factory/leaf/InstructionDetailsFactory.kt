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
import care.better.platform.web.template.builder.exception.UnknownPathBuilderException
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.openehr.base.basetypes.HierObjectId
import org.openehr.base.basetypes.LocatableRef
import org.openehr.rm.composition.InstructionDetails

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [RmObjectLeafNodeFactory] that creates a new instance of [InstructionDetails].
 */
internal object InstructionDetailsFactory : RmObjectLeafNodeFactory<InstructionDetails>() {

    override fun createForValueNode(
            conversionContext: ConversionContext,
            amNode: AmNode,
            valueNode: ValueNode,
            webTemplatePath: WebTemplatePath): InstructionDetails =
        throw ConversionException("${amNode.rmType} can not be created from simple value", webTemplatePath.toString())

    override fun createInstance(attributes: Set<AttributeDto>): InstructionDetails = InstructionDetails()

    override fun handleField(
            conversionContext: ConversionContext,
            amNode: AmNode,
            attribute: AttributeDto,
            rmObject: InstructionDetails,
            jsonNode: JsonNode,
            webTemplatePath: WebTemplatePath): Boolean =
        when (attribute.attribute) {
            "composition_uid" -> {
                getOrCreateInstructionId(rmObject).also {
                    it.id = HierObjectId(jsonNode.asText())
                    it.type = "INSTRUCTION"
                    it.namespace = "local"
                }
                conversionContext.instructionDetailsDataHolder.computeIfAbsent(rmObject).isReferenceSelfComposition(jsonNode.asText())
                true
            }
            "path" -> {
                getOrCreateInstructionId(rmObject).also {
                    it.path = jsonNode.asText()
                }
                true
            }
            "wt_path" -> {
                try {
                    conversionContext.getWebTemplate().findWebTemplateNode(jsonNode.asText()).also {
                        conversionContext.instructionDetailsDataHolder.computeIfAbsent(rmObject).withInstructionNode(it.amNode)
                    }
                    getOrCreateInstructionId(rmObject).path = conversionContext.getWebTemplate().getLinkPath(jsonNode.asText())
                } catch (ex: UnknownPathBuilderException) {
                    conversionContext.instructionDetailsDataHolder.computeIfAbsent(rmObject).withWebTemplatePath(jsonNode.asText())
                }
                true
            }
            "instruction_uid" -> {
                conversionContext.instructionDetailsDataHolder.computeIfAbsent(rmObject).withInstructionUid(jsonNode.asText())
                true
            }
            "instruction_index" -> {
                conversionContext.instructionDetailsDataHolder.computeIfAbsent(rmObject).withInstructionIndex(jsonNode.asText().toInt())
                true
            }
            "activity_id" -> {
                rmObject.activityId = jsonNode.asText()
                true
            }
            "activity_index" -> {
                conversionContext.instructionDetailsDataHolder.computeIfAbsent(rmObject).withActivityIndex(jsonNode.asText().toInt())
                true
            }
            else -> false
        }

    /**
     * Retrieves or creates the instruction ID from [InstructionDetails]
     *
     * @param rmObject [InstructionDetails]
     * @return Instruction ID as [LocatableRef]
     */
    private fun getOrCreateInstructionId(rmObject: InstructionDetails): LocatableRef =
        if (rmObject.instructionId == null)
            LocatableRef().also { rmObject.instructionId = it }
        else
            rmObject.instructionId as LocatableRef
}
