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

package care.better.platform.web.template.converter.raw.context

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.converter.constant.WebTemplateConstants.PARTICIPATION_MODE_GROUP_NAME
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.extensions.createFromOpenEhrTerminology
import care.better.platform.web.template.converter.raw.extensions.createPartyIdentified
import care.better.platform.web.template.converter.value.LocaleBasedValueConverter
import care.better.platform.web.template.converter.value.SimpleValueConverter
import care.better.platform.web.template.converter.value.ValueConverter
import org.apache.commons.lang3.StringUtils
import org.openehr.base.basetypes.ObjectRef
import org.openehr.rm.common.*
import org.openehr.rm.composition.IsmTransition
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvIdentifier
import org.openehr.rm.datatypes.DvText
import java.time.OffsetDateTime
import java.time.ZonedDateTime
import java.util.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Set of instructions used to convert the RM object from STRUCTURED to RAW format.
 */
class ConversionContext private constructor(
        val locale: Locale?,
        val language: String?,
        val territory: String?,
        val category: String?,
        val setting: String?,
        val composerName: String?,
        val composerId: String?,
        val composerSelf: Boolean,
        val composer: PartyProxy?,
        val time: OffsetDateTime,
        val actionTime: OffsetDateTime?,
        val historyOrigin: OffsetDateTime?,
        val endTime: OffsetDateTime?,
        val encoding: String?,
        val idNamespace: String?,
        val idScheme: String?,
        val providerName: String?,
        val providerId: String?,
        val participationNames: List<String>,
        val participationIds: List<String>,
        val participationFunctions: List<String>,
        val participationModes: List<String>,
        val participationIdentifiers: List<List<DvIdentifier>>,
        val ismTransitionCurrentState: String,
        val instructionNarrative: String,
        val activityTiming: String,
        val ismTransition: IsmTransition?,
        val subject: PartyProxy?,
        val entryProvider: PartyProxy?,
        val entryParticipation: List<Participation>,
        val identifierIssuer: String?,
        val identifierAssigner: String?,
        val identifierType: String?,
        val activityTimingProvider: ActivityTimingProvider?,
        val instructionNarrativeProvider: InstructionNarrativeProvider?,
        val actionIsmTransitionProvider: ActionIsmTransitionProvider?,
        val uidGenerator: (String) -> String,
        val healthCareFacility: PartyIdentified?,
        val location: String?,
        val workflowId: ObjectRef?,
        val links: List<Link>,
        val termBindingTerminologies: Set<String>,
        val actionToInstructionHandler: ActionToInstructionHandler?,
        val rmVisitors: Map<Class<*>, RmVisitor<*>> = mapOf(),
        val valueConverter: ValueConverter,
        val incompleteMode: Boolean,
        val strictMode: Boolean,
        val aqlPath: String?,
        val webTemplatePath: String?) {


    companion object {
        /**
         * Creates a new instance of [ConversionContext] [Builder].
         *
         * @return [Builder]
         */
        @JvmStatic
        fun create(): Builder = Builder()

        /**
         * Creates a new instance of [ConversionContext] [Builder] for the AQL path.
         * @param aqlPath AQL path
         *
         * @return [Builder]
         */
        @JvmStatic
        fun createForAqlPath(aqlPath: String): Builder = Builder(aqlPath = aqlPath)

        /**
         * Creates a new instance of [ConversionContext] [Builder] for the web template path.
         * @param webTemplatePath Web template path
         *
         * @return [Builder]
         */
        @JvmStatic
        fun createForWebTemplatePath(webTemplatePath: String): Builder = Builder(webTemplatePath = webTemplatePath)
    }

    internal val instructionDetailsDataHolder: InstructionDetailsDataHolder = InstructionDetailsDataHolder()

    private var webTemplate: WebTemplate? = null

    /**
     * Returns [WebTemplate] associated with this [ConversionContext].
     *
     * @return [WebTemplate]
     */
    internal fun getWebTemplate(): WebTemplate = webTemplate!!

    /**
     * Creates and returns [List] of [Participation] from this [ConversionContext].
     *
     * @return [List] of [Participation]
     */
    fun getParticipationList(): List<Participation> =
        if (entryParticipation.isEmpty()) {
            val participationList: MutableList<Participation> = mutableListOf()
            participationNames.forEachIndexed { index, name ->
                participationList.add(Participation().apply {
                    if (participationIds.size > index) {
                        this.function = DvText(participationFunctions[index])
                    }
                    val mode = if (participationModes.size > index) participationModes[index] else null
                    if (mode.isNullOrBlank()) {
                        this.mode = DvCodedText.create("openehr", "193", "not specified")
                    } else {
                        this.mode = DvCodedText.createFromOpenEhrTerminology(PARTICIPATION_MODE_GROUP_NAME, mode)
                    }
                    val id = if (participationIds.size > index) participationIds[index] else null
                    if (StringUtils.isNotBlank(id) && (StringUtils.isBlank(idNamespace) || StringUtils.isBlank(idScheme))) {
                        throw ConversionException("Parameters 'id_scheme' and 'id_namespace' are required when using participations with ids!")
                    }
                    this.performer = PartyIdentified.createPartyIdentified(
                        name,
                        id,
                        idScheme,
                        idNamespace).also {
                        it.identifiers =
                            if (participationIdentifiers.size > index)
                                participationIdentifiers[index].toMutableList()
                            else mutableListOf()
                    }
                })
            }
            participationList
        } else {
            entryParticipation
        }

    /**
     * Creates a new [Builder] from this [ConversionContext].
     *
     * @param webTemplate [WebTemplate]
     */
    internal fun createBuilder(webTemplate: WebTemplate): Builder =
        Builder(
            locale,
            language,
            territory,
            category,
            setting,
            composerName,
            composerId,
            composerSelf,
            composer,
            time,
            actionTime,
            historyOrigin,
            endTime,
            encoding,
            idNamespace,
            idScheme,
            providerName,
            providerId,
            participationNames.toMutableList(),
            participationIds.toMutableList(),
            participationFunctions.toMutableList(),
            participationModes.toMutableList(),
            participationIdentifiers.map { it.toMutableList() }.toMutableList(),
            ismTransitionCurrentState,
            instructionNarrative,
            activityTiming,
            ismTransition,
            subject,
            entryProvider,
            entryParticipation.toMutableList(),
            identifierIssuer,
            identifierAssigner,
            identifierType,
            activityTimingProvider,
            instructionNarrativeProvider,
            actionIsmTransitionProvider,
            uidGenerator,
            healthCareFacility,
            location,
            workflowId,
            links.toMutableList(),
            termBindingTerminologies.toMutableSet(),
            actionToInstructionHandler,
            rmVisitors.toMutableMap(),
            valueConverter,
            incompleteMode,
            strictMode,
            aqlPath,
            webTemplatePath,
            webTemplate)


    /**
     * Builder used to create a new instance of [ConversionContext].
     *
     * @constructor Creates a new instance of [Builder]
     */
    data class Builder(
            private var locale: Locale? = null,
            private var language: String? = null,
            private var territory: String? = null,
            private var category: String? = "event",
            private var setting: String? = "other care",
            private var composerName: String? = null,
            private var composerId: String? = null,
            private var composerSelf: Boolean = false,
            private var composer: PartyProxy? = null,
            private var time: OffsetDateTime = OffsetDateTime.now(),
            private var actionTime: OffsetDateTime? = OffsetDateTime.now(),
            private var historyOrigin: OffsetDateTime? = null,
            private var endTime: OffsetDateTime? = null,
            private var encoding: String? = Charsets.UTF_8.toString(),
            private var idNamespace: String? = null,
            private var idScheme: String? = null,
            private var providerName: String? = null,
            private var providerId: String? = null,
            private val participationNames: MutableList<String> = mutableListOf(),
            private val participationIds: MutableList<String> = mutableListOf(),
            private val participationFunctions: MutableList<String> = mutableListOf(),
            private val participationModes: MutableList<String> = mutableListOf(),
            private val participationIdentifiers: MutableList<MutableList<DvIdentifier>> = mutableListOf(),
            private var ismTransitionCurrentState: String = "completed",
            private var instructionNarrative: String = "<none>",
            private var activityTiming: String = "R1",
            private var ismTransition: IsmTransition? = null,
            private var subject: PartyProxy? = PartySelf(),
            private var entryProvider: PartyProxy? = null,
            private val entryParticipation: MutableList<Participation> = mutableListOf(),
            private var identifierIssuer: String? = null,
            private var identifierAssigner: String? = null,
            private var identifierType: String? = null,
            private var activityTimingProvider: ActivityTimingProvider? = null,
            private var instructionNarrativeProvider: InstructionNarrativeProvider? = null,
            private var actionIsmTransitionProvider: ActionIsmTransitionProvider? = null,
            private var uidGenerator: (String) -> String = { UUID.randomUUID().toString() },
            private var healthCareFacility: PartyIdentified? = null,
            private var location: String? = null,
            private var workflowId: ObjectRef? = null,
            private val links: MutableList<Link> = mutableListOf(),
            private val termBindingTerminologies: MutableSet<String> = mutableSetOf(),
            private var actionToInstructionHandler: ActionToInstructionHandler? = null,
            private var rmVisitors: MutableMap<Class<*>, RmVisitor<*>> = mutableMapOf(),
            private var valueConverter: ValueConverter? = null,
            private var incompleteMode: Boolean = false,
            private var strictMode: Boolean = false,
            private var aqlPath: String? = null,
            private var webTemplatePath: String? = null,
            private var webTemplate: WebTemplate? = null) {

        fun getLocale() = locale
        fun getLanguage() = language
        fun getTerritory() = territory
        fun getCategory() = category
        fun getSetting() = setting
        fun getComposerName() = composerName
        fun getComposerId() = composerId
        fun getComposerSelf() = composerSelf
        fun getComposer() = composer
        fun getTime() = time
        fun getActionTime() = actionTime
        fun getHistoryOrigin() = historyOrigin
        fun getEndTime() = endTime
        fun getEncoding() = encoding
        fun getIdNamespace() = idNamespace
        fun getIdScheme() = idScheme
        fun getProviderName() = providerName
        fun getProviderId() = providerId
        fun getParticipationNames() = participationNames
        fun getParticipationIds() = participationIds
        fun getParticipationFunctions() = participationFunctions
        fun getParticipationModes() = participationModes
        fun getParticipationIdentifiers() = participationIdentifiers
        fun getIsmTransitionCurrentState() = ismTransitionCurrentState
        fun getInstructionNarrative() = instructionNarrative
        fun getActivityTiming() = activityTiming
        fun getIsmTransition() = ismTransition
        fun getSubject() = subject
        fun getEntryProvider() = entryProvider
        fun getEntryParticipation() = entryParticipation
        fun getIdentifierIssuer() = identifierIssuer
        fun getIdentifierAssigner() = identifierAssigner
        fun getIdentifierType() = identifierType
        fun getActivityTimingProvider() = activityTimingProvider
        fun getInstructionNarrativeProvider() = instructionNarrativeProvider
        fun getActionIsmTransitionProvider() = actionIsmTransitionProvider
        internal fun withUidGenerator() = uidGenerator
        fun getHealthCareFacility() = healthCareFacility
        fun getLocation() = location
        fun getWorkflowId() = workflowId
        fun getLinks() = links
        fun getTermBindingTerminologies() = termBindingTerminologies
        fun getActionToInstructionHandler() = actionToInstructionHandler
        fun getRmVisitors() = rmVisitors
        fun getValueConverter() = valueConverter
        fun isForIncompleteMode() = incompleteMode
        fun isForStrictMode() = strictMode
        fun getAqlPath() = aqlPath
        fun getWebTemplatePath() = webTemplatePath

        fun withLocale(locale: Locale) = apply { this.locale = locale }
        fun withLanguage(language: String) = apply {
            if (territory != null && locale == null) {
                this.locale = Locale(language, territory)
            }
            this.language = language
        }

        fun withTerritory(territory: String) = apply {
            if (language != null && locale == null) {
                this.locale = Locale(language, territory)
            }
            this.territory = territory
        }

        fun withCategory(category: String) = apply { this.category = category }
        fun withSetting(setting: String) = apply { this.setting = setting }
        fun withComposerName(composerName: String) = apply { this.composerName = composerName }
        fun withComposerId(composerId: String) = apply { this.composerId = composerId }
        fun withComposerSelf(composerSelf: Boolean) = apply { this.composerSelf = composerSelf }
        fun withComposer(composer: PartyProxy) = apply { this.composer = composer }
        fun withTime(time: OffsetDateTime) = apply { this.time = time }
        fun withTime(time: ZonedDateTime) = apply { this.time = time.toOffsetDateTime() }
        fun withActionTime(time: OffsetDateTime) = apply { this.actionTime = time }
        fun withActionTime(time: ZonedDateTime) = apply { this.actionTime = time.toOffsetDateTime() }
        fun withHistoryOrigin(time: OffsetDateTime) = apply { this.historyOrigin = time }
        fun withHistoryOrigin(time: ZonedDateTime) = apply { this.historyOrigin = time.toOffsetDateTime() }
        fun withEndTime(time: OffsetDateTime) = apply { this.endTime = time }
        fun withEndTime(time: ZonedDateTime) = apply { this.endTime = time.toOffsetDateTime() }
        fun withEncoding(encoding: String) = apply { this.encoding = encoding }
        fun withIdNamespace(idNamespace: String) = apply { this.idNamespace = idNamespace }
        fun withIdScheme(idScheme: String) = apply { this.idScheme = idScheme }
        fun withProviderName(providerName: String) = apply { this.providerName = providerName }
        fun withProviderId(providerId: String) = apply { this.providerId = providerId }
        fun addParticipationName(participationName: String) = apply { this.participationNames.add(participationName) }
        fun addParticipationName(participationName: String, index: Int) = apply { this.participationNames.add(index, participationName) }
        fun addParticipationNames(participationNames: List<String>) = apply { this.participationNames.addAll(participationNames) }
        fun addParticipationId(participationId: String) = apply { this.participationIds.add(participationId) }
        fun addParticipationId(participationId: String, index: Int) = apply { this.participationIds.add(index, participationId) }
        fun addParticipationIds(participationIds: List<String>) = apply { this.participationIds.addAll(participationIds) }
        fun addParticipationFunction(participationFunction: String) = apply { this.participationFunctions.add(participationFunction) }
        fun addParticipationFunction(participationFunction: String, index: Int) = apply { this.participationFunctions.add(index, participationFunction) }
        fun addParticipationFunctions(participationFunctions: List<String>) = apply { this.participationFunctions.addAll(participationFunctions) }
        fun addParticipationMode(participationMode: String) = apply { this.participationModes.add(participationMode) }
        fun addParticipationMode(participationMode: String, index: Int) = apply { this.participationModes.add(index, participationMode) }
        fun addParticipationModes(participationModes: List<String>) = apply { this.participationModes.addAll(participationModes) }
        fun addParticipationIdentifiers(participationIdentifiers: MutableList<DvIdentifier>) = apply {
            this.participationIdentifiers.add(participationIdentifiers)
        }

        fun addParticipationIdentifiers(participationIdentifiers: MutableList<DvIdentifier>, index: Int) = apply {
            this.participationIdentifiers.add(index, participationIdentifiers)
        }

        fun addMultipleParticipationIdentifiers(participationIdentifiers: MutableList<MutableList<DvIdentifier>>) = apply {
            this.participationIdentifiers.addAll(participationIdentifiers)
        }

        fun withIsmTransitionCurrentState(ismTransitionCurrentState: String) = apply { this.ismTransitionCurrentState = ismTransitionCurrentState }
        fun withInstructionNarrative(instructionNarrative: String) = apply { this.instructionNarrative = instructionNarrative }
        fun withActivityTiming(activityTiming: String) = apply { this.activityTiming = activityTiming }
        fun withIsmTransition(ismTransition: IsmTransition) = apply { this.ismTransition = ismTransition }
        fun withSubject(subject: PartyProxy) = apply { this.subject = subject }
        fun withEntryProvider(entryProvider: PartyProxy) = apply { this.entryProvider = entryProvider }
        fun addEntryParticipation(entryParticipation: Participation) = apply { this.entryParticipation.add(entryParticipation) }
        fun addEntryParticipation(entryParticipation: Participation, index: Int) = apply { this.entryParticipation.add(index, entryParticipation) }
        fun addEntryParticipation(entryParticipation: List<Participation>) = apply { this.entryParticipation.addAll(entryParticipation) }
        fun withIdentifierIssuer(identifierIssuer: String) = apply { this.identifierIssuer = identifierIssuer }
        fun withIdentifierAssigner(identifierAssigner: String) = apply { this.identifierAssigner = identifierAssigner }
        fun withIdentifierType(identifierType: String) = apply { this.identifierType = identifierType }
        fun withActivityTimingProvider(activityTimingProvider: ActivityTimingProvider) = apply { this.activityTimingProvider = activityTimingProvider }
        fun withInstructionNarrativeProvider(instructionNarrativeProvider: InstructionNarrativeProvider) = apply {
            this.instructionNarrativeProvider = instructionNarrativeProvider
        }

        fun withActionIsmTransitionProvider(actionIsmTransitionProvider: ActionIsmTransitionProvider) = apply {
            this.actionIsmTransitionProvider = actionIsmTransitionProvider
        }

        internal fun withUidGenerator(uidGenerator: (String) -> String) = apply { this.uidGenerator = uidGenerator }

        fun withHealthCareFacility(healthCareFacility: PartyIdentified) = apply { this.healthCareFacility = healthCareFacility }
        fun withLocation(location: String) = apply { this.location = location }
        fun withWorkFlowId(workflowId: ObjectRef) = apply { this.workflowId = workflowId }
        fun addLink(link: Link) = apply { this.links.add(link) }
        fun addLink(link: Link, index: Int) = apply { this.links.add(index, link) }
        fun addLinks(links: List<Link>) = apply { this.links.addAll(links) }
        fun addTermBindingTerminology(termBindingTerminology: String) = apply { this.termBindingTerminologies.add(termBindingTerminology) }
        fun addTermBindingTerminologies(termBindingTerminologies: List<String>) = apply { this.termBindingTerminologies.addAll(termBindingTerminologies) }
        fun withActionToInstructionHandler(actionToInstructionHandler: ActionToInstructionHandler) = apply {
            this.actionToInstructionHandler =
                actionToInstructionHandler
        }

        fun putRmVisitor(rmObjectClass: Class<*>, rmVisitor: RmVisitor<RmObject>) = apply { this.rmVisitors[rmObjectClass] = rmVisitor }
        fun putRmVisitors(rmVisitors: Map<Class<*>, RmVisitor<RmObject>>) = apply { this.rmVisitors.putAll(rmVisitors) }
        fun withValueConvert(valueConverter: ValueConverter) = apply { this.valueConverter = valueConverter }
        fun withIncompleteMode() = apply {
            if (strictMode) {
                throw ConversionException("Incomplete mode can only be used when strict mode is disabled!")
            }
            this.incompleteMode = true
        }
        fun withStrictMode() = apply {
            if (incompleteMode){
                throw ConversionException("Strict mode can only be used when incomplete mode is disabled!")
            }
            this.strictMode = true
        }
        fun forAqlPath(aqlPath: String) = apply { this.aqlPath = aqlPath }
        fun forWebTemplatePath(webTemplatePath: String) = apply { this.webTemplatePath = webTemplatePath }
        fun withNoLocale() = apply { this.locale = null }

        /**
         * Creates and returns a new instance of [ConversionContext].
         *
         * @return [ConversionContext]
         */
        fun build(): ConversionContext =
            ConversionContext(
                locale,
                language,
                territory,
                category,
                setting,
                composerName,
                composerId,
                composerSelf,
                composer,
                time,
                actionTime,
                historyOrigin,
                endTime,
                encoding,
                idNamespace,
                idScheme,
                providerName,
                providerId,
                participationNames.toList(),
                participationIds.toList(),
                participationFunctions.toList(),
                participationModes.toList(),
                participationIdentifiers.map { it.toList() },
                ismTransitionCurrentState,
                instructionNarrative,
                activityTiming,
                ismTransition,
                subject,
                entryProvider,
                entryParticipation.toList(),
                identifierIssuer,
                identifierAssigner,
                identifierType,
                activityTimingProvider,
                instructionNarrativeProvider,
                actionIsmTransitionProvider,
                uidGenerator,
                healthCareFacility,
                location,
                workflowId,
                links.toList(),
                termBindingTerminologies.toSet(),
                actionToInstructionHandler,
                rmVisitors,
                getOrCreateValueConverter(),
                incompleteMode,
                strictMode,
                aqlPath,
                webTemplatePath).also { it.webTemplate = webTemplate }

        /**
         * Retrieves or creates the [ValueConverter].
         *
         * @return [ValueConverter]
         */
        private fun getOrCreateValueConverter(): ValueConverter =
            when {
                valueConverter != null -> valueConverter!!
                locale != null -> LocaleBasedValueConverter(locale!!)
                else -> SimpleValueConverter
            }
    }
}
