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

package care.better.platform.web.template.converter.structured.mapper

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.value.ValueConverter
import com.fasterxml.jackson.databind.JsonNode
import org.openehr.base.basetypes.*
import org.openehr.rm.common.Link
import org.openehr.rm.common.Participation
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartyRelated
import org.openehr.rm.composition.*
import org.openehr.rm.datastructures.Cluster
import org.openehr.rm.datastructures.Element
import org.openehr.rm.datatypes.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton used to delegate the RM object to the [RmObjectToStructuredMapper] based on the RM object class.
 */
object RmObjectToStructuredMapperDelegator {
    private val rmObjectToStructuredMappers: Map<Class<out RmObject>, RmObjectToStructuredMapper<out RmObject>> =
        mapOf(
            Pair(Action::class.java, ActionToStructuredMapper),
            Pair(CodePhrase::class.java, CodePhraseToStructuredMapper),
            Pair(Composition::class.java, CompositionToStructuredMapper),
            Pair(DvBoolean::class.java, DvBooleanToStructuredMapper),
            Pair(DvCodedText::class.java, DvCodedTextToStructuredMapper),
            Pair(DvCount::class.java, DvCountToStructuredMapper),
            Pair(DvDateTime::class.java, DvDateTimeToStructuredMapper),
            Pair(DvDate::class.java, DvDateToStructuredMapper),
            Pair(DvDuration::class.java, DvDurationToStructuredMapper),
            Pair(DvIdentifier::class.java, DvIdentifierToStructuredMapper),
            Pair(DvInterval::class.java, DvIntervalToStructuredMapper),
            Pair(DvMultimedia::class.java, DvMultimediaToStructuredMapper),
            Pair(DvOrdinal::class.java, DvOrdinalToStructuredMapper),
            Pair(DvParsable::class.java, DvParsableToStructuredMapper),
            Pair(DvProportion::class.java, DvProportionToStructuredMapper),
            Pair(DvQuantity::class.java, DvQuantityToStructuredMapper),
            Pair(DvScale::class.java, DvScaleToStructuredMapper),
            Pair(DvText::class.java, DvTextToStructuredMapper),
            Pair(DvTime::class.java, DvTimeToStructuredMapper),
            Pair(DvUri::class.java, DvUriToStructuredMapper),
            Pair(DvEhrUri::class.java, DvUriToStructuredMapper),
            Pair(Element::class.java, ElementToStructuredMapper),
            Pair(Observation::class.java, EntryToStructuredMapper.getInstance()),
            Pair(Evaluation::class.java, EntryToStructuredMapper.getInstance()),
            Pair(Instruction::class.java, EntryToStructuredMapper.getInstance()),
            Pair(AdminEntry::class.java, EntryToStructuredMapper.getInstance()),
            Pair(EventContext::class.java, EventContextToStructuredMapper),
            Pair(Instruction::class.java, InstructionToStructuredMapper),
            Pair(Link::class.java, LinkToStructuredMapper),
            Pair(LocatableRef::class.java, LocatableRefToStructuredMapper),
            Pair(Section::class.java, LocatableToStructuredMapper.getInstance()),
            Pair(Cluster::class.java, LocatableToStructuredMapper.getInstance()),
            Pair(Activity::class.java, LocatableToStructuredMapper.getInstance()),
            Pair(ObjectId::class.java, ObjectIdToStructuredMapper),
            Pair(HierObjectId::class.java, ObjectIdToStructuredMapper),
            Pair(ObjectVersionId::class.java, ObjectIdToStructuredMapper),
            Pair(UidBasedId::class.java, ObjectIdToStructuredMapper),
            Pair(GenericId::class.java, GenericIdToStructuredMapper),
            Pair(ObjectRef::class.java, ObjectRefToStructuredMapper),
            Pair(PartyRef::class.java, ObjectRefToStructuredMapper),
            Pair(AccessGroupRef::class.java, ObjectRefToStructuredMapper),
            Pair(Participation::class.java, ParticipationToStructuredMapper),
            Pair(PartyIdentified::class.java, PartyIdentifiedToStructuredMapper),
            Pair(PartyRelated::class.java, PartyIdentifiedToStructuredMapper))

    /**
     * Delegates the RM object to the [RmObjectToStructuredMapper] and executes the mapping.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [ValueConverter]
     * @param rmObject RM object in RAW format
     * @return RM object in STRUCTURED format
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegate(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T): JsonNode? =
        rmObjectToStructuredMappers[rmObject::class.java]?.let { (it as RmObjectToStructuredMapper<T>).map(webTemplateNode, valueConverter, rmObject) }

    /**
     * Delegates the RM object to the [RmObjectToStructuredMapper] and executes the formatted mapping.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [ValueConverter]
     * @param rmObject RM object in RAW format
     * @return RM object in STRUCTURED format
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegateFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T): JsonNode? =
        rmObjectToStructuredMappers[rmObject::class.java]?.let { (it as RmObjectToStructuredMapper<T>).mapFormatted(webTemplateNode, valueConverter, rmObject) }
}
