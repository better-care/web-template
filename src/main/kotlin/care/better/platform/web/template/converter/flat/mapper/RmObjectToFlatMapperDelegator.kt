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

package care.better.platform.web.template.converter.flat.mapper

import care.better.openehr.rm.RmObject
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.flat.context.FormattedFlatMappingContext
import care.better.platform.web.template.converter.value.ValueConverter
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
 * Singleton used to delegate the RM object to the [RmObjectToFlatMapper] based on the RM object class.
 */
object RmObjectToFlatMapperDelegator {

    private val rmObjectToFlatMappers: Map<Class<out RmObject>, RmObjectToFlatMapper<out RmObject>> = mapOf(
        Pair(Action::class.java, ActionToFlatMapper),
        Pair(CodePhrase::class.java, CodePhraseToFlatMapper),
        Pair(Composition::class.java, CompositionToFlatMapper),
        Pair(DvBoolean::class.java, DvBooleanToFlatMapper),
        Pair(DvCodedText::class.java, DvCodedTextToFlatMapper),
        Pair(DvCount::class.java, DvCountToFlatMapper),
        Pair(DvDateTime::class.java, DvDateTimeToFlatMapper),
        Pair(DvDate::class.java, DvDateToFlatMapper),
        Pair(DvDuration::class.java, DvDurationToFlatMapper),
        Pair(DvIdentifier::class.java, DvIdentifierToFlatMapper),
        Pair(DvInterval::class.java, DvIntervalToFlatMapper),
        Pair(DvMultimedia::class.java, DvMultimediaToFlatMapper),
        Pair(DvOrdinal::class.java, DvOrdinalToFlatMapper),
        Pair(DvParsable::class.java, DvParsableToFlatMapper),
        Pair(DvProportion::class.java, DvProportionToFlatMapper),
        Pair(DvQuantity::class.java, DvQuantityToFlatMapper),
        Pair(DvScale::class.java, DvScaleToFlatMapper),
        Pair(DvText::class.java, DvTextToFlatMapper),
        Pair(DvTime::class.java, DvTimeToFlatMapper),
        Pair(DvUri::class.java, DvUriToFlatMapper),
        Pair(DvEhrUri::class.java, DvUriToFlatMapper),
        Pair(Element::class.java, ElementToFlatMapper),
        Pair(Observation::class.java, EntryToFlatMapper.getInstance()),
        Pair(Evaluation::class.java, EntryToFlatMapper.getInstance()),
        Pair(Instruction::class.java, EntryToFlatMapper.getInstance()),
        Pair(AdminEntry::class.java, EntryToFlatMapper.getInstance()),
        Pair(EventContext::class.java, EventContextToFlatMapper),
        Pair(Instruction::class.java, InstructionToFlatMapper),
        Pair(Link::class.java, LinkToFlatMapper),
        Pair(LocatableRef::class.java, LocatableRefToFlatMapper),
        Pair(Section::class.java, LocatableToFlatMapper.getInstance()),
        Pair(Cluster::class.java, LocatableToFlatMapper.getInstance()),
        Pair(Activity::class.java, LocatableToFlatMapper.getInstance()),
        Pair(ObjectId::class.java, ObjectIdToFlatMapper),
        Pair(HierObjectId::class.java, ObjectIdToFlatMapper),
        Pair(ObjectVersionId::class.java, ObjectIdToFlatMapper),
        Pair(UidBasedId::class.java, ObjectIdToFlatMapper),
        Pair(GenericId::class.java, GenericIdToFlatMapper),
        Pair(ObjectRef::class.java, ObjectRefToFlatMapper),
        Pair(PartyRef::class.java, ObjectRefToFlatMapper),
        Pair(AccessGroupRef::class.java, ObjectRefToFlatMapper),
        Pair(Participation::class.java, ParticipationToFlatMapper),
        Pair(PartyIdentified::class.java, PartyIdentifiedToFlatMapper),
        Pair(PartyRelated::class.java, PartyIdentifiedToFlatMapper))


    /**
     * Delegates the RM object to the [RmObjectToFlatMapper] and executes the mapping.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [ValueConverter]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path
     * @param flatConversionContext [FlatMappingContext]
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegate(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            flatConversionContext: FlatMappingContext) {
        rmObjectToFlatMappers[rmObject::class.java]?.also {
            (it as RmObjectToFlatMapper<T>).map(webTemplateNode, valueConverter, rmObject, webTemplatePath, flatConversionContext)
        }

    }

    /**
     * Delegates the RM object to the [RmObjectToFlatMapper] and executes the formatted mapping.
     *
     * @param webTemplateNode [WebTemplateNode]
     * @param valueConverter [ValueConverter]
     * @param rmObject RM object in RAW format
     * @param webTemplatePath Web template path
     * @param formattedFlatConversionContext [FormattedFlatMappingContext]
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T : RmObject> delegateFormatted(
            webTemplateNode: WebTemplateNode,
            valueConverter: ValueConverter,
            rmObject: T,
            webTemplatePath: String,
            formattedFlatConversionContext: FormattedFlatMappingContext) {
        rmObjectToFlatMappers[rmObject::class.java]?.also {
            (it as RmObjectToFlatMapper<T>).mapFormatted(webTemplateNode, valueConverter, rmObject, webTemplatePath, formattedFlatConversionContext)
        }
    }
}
