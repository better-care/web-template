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

import care.better.platform.path.PathSegment
import care.better.platform.path.PathUtils
import care.better.platform.template.AmNode
import care.better.platform.web.template.converter.WebTemplatePath
import care.better.platform.web.template.converter.constant.WebTemplateConstants.SETTING_GROUP_NAME
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import org.openehr.base.basetypes.LocatableRef
import org.openehr.base.basetypes.TerminologyId
import org.openehr.rm.common.Link
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.EventContext
import org.openehr.rm.composition.InstructionDetails
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvDateTime
import java.util.regex.Pattern

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [LocatablePostProcessor] that post-processes [Composition].
 */
internal object CompositionPostProcessor : LocatablePostProcessor<Composition>() {
    private val supportedClass = Composition::class.java

    private val NAME_PATTERN = Pattern.compile("\\s*#[0-9]+$")


    override fun postProcess(conversionContext: ConversionContext, amNode: AmNode?, instance: Composition, webTemplatePath: WebTemplatePath?) {
        super.postProcess(conversionContext, amNode, instance, webTemplatePath)

        if (instance.context != null && isPersistent(instance)) {
            instance.context?.also { updateEventContext(it, conversionContext) }
        } else if (!isPersistent(instance)) {
            val context = instance.context
            if (context == null) {
                instance.context = EventContext().apply {
                    updateEventContext(this, conversionContext)
                    EventContextPostProcessor.postProcess(conversionContext, amNode, this, webTemplatePath)
                }
            } else {
                updateEventContext(context, conversionContext)
            }
        }

        val territory = instance.territory
        if (territory != null && territory.terminologyId == null) {
            territory.terminologyId = TerminologyId("ISO_3166-1")
        }

        val language = instance.language
        if (language != null && language.terminologyId == null) {
            language.terminologyId = TerminologyId("ISO_639-1")
        }

        postProcessInstructionDetails(instance, conversionContext)
    }

    /**
     * Checks if [Composition] category is for the persistent type.
     *
     * @param composition [Composition]
     * @return [Boolean] indicating if [Composition] category is for the persistent type or not.
     */
    private fun isPersistent(composition: Composition): Boolean = "431" == composition.category?.definingCode?.codeString

    /**
     * Updates [EventContext] with values from [ConversionContext].
     *
     * @param eventContext [EventContext]
     * @param conversionContext [ConversionContext]
     */
    private fun updateEventContext(eventContext: EventContext, conversionContext: ConversionContext) {
        if (eventContext.startTime == null) {
            eventContext.startTime = DvDateTime.create(conversionContext.time)
        }

        if (eventContext.setting == null) {
            if (conversionContext.setting != null) {
                eventContext.setting = DvCodedText.createFromOpenEhrTerminology(SETTING_GROUP_NAME, conversionContext.setting)
            } else {
                eventContext.setting = DvCodedText.createWithOpenEHRTerminology("238", "other care")
            }
        }

        if (eventContext.healthCareFacility == null) {
            eventContext.healthCareFacility = conversionContext.healthCareFacility
        }

        if (eventContext.endTime == null) {
            conversionContext.endTime?.also { eventContext.endTime = DvDateTime.create(conversionContext.endTime) }
        }
        if (eventContext.location == null) {
            eventContext.location = conversionContext.location
        }
    }

    /**
     * Post-processes [InstructionDetails] crated during the STRUCTURED to RAW conversion.
     *
     * @param composition [Composition]
     * @param conversionContext [ConversionContext]
     */
    private fun postProcessInstructionDetails(composition: Composition, conversionContext: ConversionContext) {
        conversionContext.instructionDetailsDataHolder.getAll().forEach { entry ->
            val instructionDetails = entry.key
            val instructionDetailsData = entry.value

            if (instructionDetails.instructionId != null) {
                val path = when {
                    instructionDetails.instructionId?.path != null -> instructionDetails.instructionId?.path
                    conversionContext.actionToInstructionHandler != null -> conversionContext.actionToInstructionHandler.resolvePath(
                        composition,
                        instructionDetails,
                        instructionDetailsData,
                        conversionContext)
                    else -> null
                }

                if (path != null) {
                    when {
                        instructionDetailsData.instructionUid != null -> {
                            setInstructionPath(instructionDetails.instructionId!!, path, instructionDetailsData.instructionUid)
                        }
                        instructionDetailsData.instructionIndex != null -> {
                            setInstructionPath(instructionDetails.instructionId!!, path, instructionDetailsData.instructionIndex)

                        }
                        else -> {
                            instructionDetails.instructionId?.also { it.path = path }
                        }
                    }
                }


                instructionDetailsData.instructionNode.also { instructionAmNode ->
                    if (instructionAmNode != null) {
                        instructionAmNode.attributes["activities"]?.also {
                            if (it.children.size == 1) {
                                val activityAmNode = it.children[0]
                                val nameSuffix =
                                    instructionDetailsData.activityIndex?.let { index -> ",${Link.getNameSuffix(activityAmNode.name!!, index)}" } ?: ""
                                instructionDetails.activityId = "activities[${activityAmNode.nodeId}${nameSuffix}]"
                            }
                        }
                    } else if (conversionContext.actionToInstructionHandler != null) {
                        conversionContext.actionToInstructionHandler.handle(composition, instructionDetails, instructionDetailsData, conversionContext)
                    }
                }
            }
        }
    }

    /**
     * Formats and sets the path to the [LocatableRef].
     *
     * @param locatableRef [LocatableRef]
     * @param rmPath RM path
     * @param instructionUid Instruction ID
     */
    private fun setInstructionPath(locatableRef: LocatableRef, rmPath: String, instructionUid: String?) {
        val segments = PathUtils.getPathSegments(rmPath).toMutableList()
        val lastSegment = segments.removeAt(segments.size - 1)
        val path = getAqlPath(segments)
        val segment = PathSegment(
            lastSegment.element,
            lastSegment.archetypeNodeId,
            instructionUid,
            "uid/value")
        locatableRef.path = path + '/' + segment.asPathSegment()
    }

    /**
     * Formats and sets the path to the [LocatableRef].
     *
     * @param locatableRef [LocatableRef]
     * @param rmPath RM path
     * @param instructionIndex Instruction index
     */
    private fun setInstructionPath(locatableRef: LocatableRef, rmPath: String, instructionIndex: Int) {
        val segments = PathUtils.getPathSegments(rmPath).toMutableList()
        val lastSegment = segments.removeAt(segments.size - 1)
        val path = getAqlPath(segments)
        val name = lastSegment.name
        if (name == null) {
            locatableRef.path = path + '/' + lastSegment.asPathSegment()
        } else {
            val matcher = NAME_PATTERN.matcher(name)
            val segment = PathSegment(
                lastSegment.element,
                lastSegment.archetypeNodeId,
                matcher.replaceAll("") + if (instructionIndex == 0) "" else " #" + (instructionIndex + 1),
                null)
            locatableRef.path = path + '/' + segment.asPathSegment()
        }
    }

    /**
     * Constructs the AQL path from the [List] of [PathSegment].
     *
     * @param segments [List] of [PathSegment]
     * @return AQL path
     */
    private fun getAqlPath(segments: List<PathSegment>): String = segments.joinToString("/", "/", "") { it.asPathSegment() }

    override fun getType(): Class<*> = supportedClass
}
