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

package care.better.platform.web.template.builder.input

import care.better.openehr.rm.RmObject
import care.better.platform.template.AmNode
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.WebTemplateInput
import org.openehr.am.aom.*
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.common.PartyProxy
import org.openehr.rm.datatypes.*

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
@Suppress("unused")
object WebTemplateInputBuilderDelegator {

    private val domainTypeBuilders: Map<Class<out CDomainType>, WebTemplateInputBuilder<out CDomainType>> =
        mapOf(
            Pair(CCodePhrase::class.java, CodePhraseWebTemplateInputBuilder),
            Pair(CCodeReference::class.java, CodeReferenceWebTemplateInputBuilder),
            Pair(CDvOrdinal::class.java, OrdinalWebTemplateInputBuilder),
            Pair(CDvScale::class.java, ScaleWebTemplateInputBuilder),
            Pair(CDvQuantity::class.java, QuantityWebTemplateInputBuilder))

    private val primitiveBuilders: Map<Class<out CPrimitive>, WebTemplateInputBuilder<out CPrimitive>> =
        mapOf(
            Pair(CDateTime::class.java, DateTimeWebTemplateInputBuilder),
            Pair(CDate::class.java, DateWebTemplateInputBuilder),
            Pair(CTime::class.java, TimeWebTemplateInputBuilder),
            Pair(CDuration::class.java, DurationWebTemplateInputBuilder),
            Pair(CBoolean::class.java, BooleanWebTemplateInputBuilder),
            Pair(CInteger::class.java, IntegerWebTemplateInputBuilder),
            Pair(CReal::class.java, DecimalWebTemplateInputBuilder),
            Pair(CString::class.java, StringWebTemplateInputBuilder))

    private val dataValueBuilders: Map<Class<*>, WebTemplateInputBuilder<*>> =
        mapOf(
            Pair(DvDateTime::class.java, DateTimeWebTemplateInputBuilder),
            Pair(DvDate::class.java, DateWebTemplateInputBuilder),
            Pair(DvTime::class.java, TimeWebTemplateInputBuilder),
            Pair(DvDuration::class.java, DurationWebTemplateInputBuilder),
            Pair(DvBoolean::class.java, BooleanWebTemplateInputBuilder),
            Pair(DvProportion::class.java, ProportionWebTemplateInputBuilder),
            Pair(DvCount::class.java, IntegerWebTemplateInputBuilder),
            Pair(DvCodedText::class.java, CodePhraseWebTemplateInputBuilder),
            Pair(DvMultimedia::class.java, StringWebTemplateInputBuilder),
            Pair(DvText::class.java, StringWebTemplateInputBuilder),
            Pair(DvIdentifier::class.java, StringWebTemplateInputBuilder),
            Pair(DvParsable::class.java, StringWebTemplateInputBuilder),
            Pair(DvUri::class.java, StringWebTemplateInputBuilder),
            Pair(DvEhrUri::class.java, StringWebTemplateInputBuilder),
            Pair(CodePhrase::class.java, CodePhraseWebTemplateInputBuilder))

    private val typeBuilders: Map<String, WebTemplateInputBuilder<*>> =
        mapOf(
            Pair(RmUtils.getRmTypeName(DvDateTime::class.java), DateTimeWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvDate::class.java), DateWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvTime::class.java), TimeWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvDuration::class.java), DurationWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvBoolean::class.java), BooleanWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvProportion::class.java), ProportionWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvCount::class.java), IntegerWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvCodedText::class.java), CodePhraseWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvMultimedia::class.java), StringWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvText::class.java), StringWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvIdentifier::class.java), IdentifierWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvParsable::class.java), ParsableWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvUri::class.java), StringWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvEhrUri::class.java), StringWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvQuantity::class.java), QuantityWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvOrdinal::class.java), OrdinalWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvScale::class.java), ScaleWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvInterval::class.java), IntervalWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(DvState::class.java), CodePhraseWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(PartyProxy::class.java), PartyIdentifiedWebTemplateInputBuilder),
            Pair(RmUtils.getRmTypeName(PartyIdentified::class.java), PartyIdentifiedWebTemplateInputBuilder),
            Pair("STRING", StringWebTemplateInputBuilder))

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> delegate(amNode: AmNode, validator: T, context: WebTemplateBuilderContext): WebTemplateInput? =
        with(requireNotNull(typeBuilders[RmUtils.getNonGenericRmNamePart(amNode.rmType)]) { "Unsupported type: ${amNode.rmType}" }) {
            (this as WebTemplateInputBuilder<T>).build(amNode, validator, context)
        }

    @JvmStatic
    internal fun delegate(webTemplateNode: WebTemplateNode, context: WebTemplateBuilderContext) {
        with(requireNotNull(typeBuilders[RmUtils.getNonGenericRmNamePart(webTemplateNode.rmType)]) { "Unsupported type: ${webTemplateNode.rmType}" }) {
            this.build(webTemplateNode, context)
        }
    }

    @JvmStatic
    internal fun isRmTypeWithInputs(rmClass: Class<out RmObject?>): Boolean =
        DataValue::class.java.isAssignableFrom(rmClass) || PartyProxy::class.java == rmClass || PartyIdentified::class.java == rmClass
}
