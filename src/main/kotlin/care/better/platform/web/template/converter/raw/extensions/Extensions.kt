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

@file:JvmName("RawConversionUtils")

package care.better.platform.web.template.converter.raw.extensions

import care.better.openehr.rm.RmObject
import care.better.openehr.terminology.OpenEhrTerminology
import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils
import care.better.platform.utils.RmUtils
import care.better.platform.web.template.converter.constant.WebTemplateConstants.DEFAULT_LANGUAGE
import care.better.platform.web.template.converter.exceptions.ConversionException
import org.openehr.am.aom.CCodePhrase
import org.openehr.base.basetypes.GenericId
import org.openehr.base.basetypes.PartyRef
import org.openehr.rm.common.PartyIdentified
import org.openehr.rm.composition.*
import org.openehr.rm.datastructures.*
import org.openehr.rm.datatypes.CodePhrase
import org.openehr.rm.datatypes.DvCodedText
import org.openehr.rm.datatypes.DvIdentifier

/**
 * @author Primoz Delopst
 */

/**
 * Checks if [RmObject] is empty.
 *
 * @return [Boolean] indicating if [RmObject] is empty
 */
@JvmSynthetic
internal fun RmObject?.isEmpty(): Boolean = isRmObjectEmpty(this)

/**
 * Checks if the RM object in RAW format is empty.
 *
 * @param rmObject RM object in RAW format
 * @return [Boolean] indicating if RM object is empty
 */
private fun isRmObjectEmpty(rmObject: RmObject?): Boolean =
    when (rmObject) {
        is Event -> isRmObjectEmpty(rmObject.data) && isRmObjectEmpty(rmObject.state)
        is ItemTree -> rmObject.items.isEmpty()
        is ItemList -> rmObject.items.isEmpty()
        is ItemSingle -> rmObject.item == null
        is ItemTable -> rmObject.rows.isEmpty()
        is History -> rmObject.events.isEmpty()
        is Section -> rmObject.items.isEmpty()
        is Observation -> isRmObjectEmpty(rmObject.data) && isRmObjectEmpty(rmObject.state) && isRmObjectEmpty(rmObject.protocol)
        is Evaluation -> isRmObjectEmpty(rmObject.data)
        is Instruction -> rmObject.activities.isEmpty() && isRmObjectEmpty(rmObject.protocol)
        is Action -> isRmObjectEmpty(rmObject.description)
        is Activity -> isRmObjectEmpty(rmObject.description)
        is AdminEntry -> isRmObjectEmpty(rmObject.data)
        is Cluster -> rmObject.items.isEmpty()
        is DvIdentifier -> rmObject.id.isNullOrBlank()
        is Element -> rmObject.value == null && rmObject.nullFlavour == null
        else -> rmObject == null
    }


/**
 * Checks if [RmObject] is not empty.
 *
 * @return [Boolean] indicating if [RmObject] is not empty
 */
@JvmSynthetic
internal fun RmObject?.isNotEmpty(): Boolean = !this.isEmpty()

/**
 * Creates and returns [DvCodedText] for the openEHR terminology.
 * Note that group ID and name pair must exits in the [OpenEhrTerminology].
 *
 * @param groupName Group name
 * @param code code
 * @return [DvCodedText] for the openEHR terminology.
 */
@JvmSynthetic
internal fun DvCodedText.Companion.createFromOpenEhrTerminology(groupName: String, code: String): DvCodedText =
    with(OpenEhrTerminology.getInstance().getId(groupName, code)) {
        if (this == null) {
            val text = OpenEhrTerminology.getInstance().getText(DEFAULT_LANGUAGE, code)
                ?: throw ConversionException("OpenEHR code for groupid/name not found: $groupName/$code")
            return create("openehr", code, text)
        }
        return create("openehr", this, code)
    }


/**
 * Creates a new instance of [PartyIdentified] with at least a name.
 * If parameter ID is non-blank, then it also creates external ref with ID, idScheme and idNamespace (in which case
 * idScheme and idNamespace must also be non-blank).
 *
 * @param name        Party name
 * @param id          Party ID
 * @param idScheme    Party ID scheme
 * @param idNamespace Party ID namespace
 * @return [PartyIdentified]
 */
@JvmSynthetic
internal fun PartyIdentified.Companion.createPartyIdentified(name: String, id: String?, idScheme: String?, idNamespace: String?): PartyIdentified =
    PartyIdentified().apply {
        this.name = name
        if (id != null && id.isNotBlank()) {
            this.externalRef = PartyRef.createPartyRef(id, idScheme, idNamespace)
        }
    }

/**
 * Creates a new instance of [PartyRef] with an ID.
 *
 * @param id          ID
 * @param idScheme    ID scheme
 * @param idNamespace ID namespace
 * @return [PartyRef]
 */
@JvmSynthetic
internal fun PartyRef.Companion.createPartyRef(id: String?, idScheme: String?, idNamespace: String?): PartyRef =
    PartyRef().apply {
        this.id = GenericId.createGenericId(id, idScheme)
        this.namespace = idNamespace
        this.type = "ANY"
    }

/**
 * Creates a new instance of [GenericId].
 *
 * @param id       ID
 * @param idScheme ID scheme
 * @return [GenericId]
 */
@JvmSynthetic
internal fun GenericId.Companion.createGenericId(id: String?, idScheme: String?): GenericId =
    GenericId().apply {
        this.value = id
        this.scheme = idScheme
    }

/**
 * Created [DvCodedText] from [AmNode].
 *
 * @param amNode [AmNode]
 * @return [DvCodedText]
 */
@JvmSynthetic
internal fun DvCodedText.Companion.createFromAmNode(amNode: AmNode): DvCodedText? =
    AmUtils.getAmNode(amNode, "defining_code")?.let {
        DvCodedText().apply {
            val cCodePhrase = it.cObject as CCodePhrase
            this.definingCode = CodePhrase().apply {
                this.terminologyId = cCodePhrase.terminologyId
                if (cCodePhrase.codeList.size == 1) {
                    this.codeString = cCodePhrase.codeList[0]
                }
            }
            this.value = OpenEhrTerminology.getInstance().getText(DEFAULT_LANGUAGE, this.definingCode?.codeString!!)
        }
    }

private val elementRmType = RmUtils.getRmTypeName(Element::class.java)

/**
 * Checks if [AmNode] is for ELEMENT RM type.
 * @return [Boolean] indicating if [AmNode] is for ELEMENT RM type
 */
@JvmSynthetic
internal fun AmNode.isForElement() = elementRmType == this.rmType

@JvmSynthetic
internal fun CharSequence?.isNotNullOrBlank(): Boolean = this != null && this.isNotBlank()


@JvmSynthetic
internal fun CharSequence?.isNotNullOrEmpty(): Boolean {
    return !this.isNullOrEmpty()
}

object WebTemplateHelperUtils {
    /**
     * Creates a new instance of [PartyIdentified] with at least a name.
     * If parameter ID is non-blank, then it also creates external ref with ID, idScheme and idNamespace (in which case
     * idScheme and idNamespace must also be non-blank).
     *
     * @param name        Party name
     * @param id          Party ID
     * @param idScheme    Party ID scheme
     * @param idNamespace Party ID namespace
     * @return [PartyIdentified]
     */
    @JvmStatic
    fun createPartyIdentified(name: String, id: String?, idScheme: String?, idNamespace: String?): PartyIdentified =
        PartyIdentified.createPartyIdentified(name, id, idScheme, idNamespace)

}
