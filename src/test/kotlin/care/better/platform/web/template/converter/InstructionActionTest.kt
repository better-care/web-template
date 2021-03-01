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

package care.better.platform.web.template.converter

import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.converter.raw.context.ConversionContext
import care.better.platform.web.template.converter.raw.context.InCompositionActionToInstructionHandler
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Instruction
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class InstructionActionTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithUUID() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/time", "2015-01-01T01:01:01.000Z")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put("medication_order/medication_detail/medication_action/_instruction_details|instruction_uid", "insuid")
                .put("medication_order/medication_detail/medication_action/_instruction_details|wt_path", "medication_order/medication_detail/medication_instruction")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|path",
                  "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1 and uid/value='insuid']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSingleInstructionDetails() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/time", "2015-01-01T01:01:01.000Z")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put("medication_order/medication_detail/medication_action/_instruction_details|wt_path", "medication_order/medication_detail/medication_instruction")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|path",
                  "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultipleActivities() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order:0/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_instruction/order:1/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order:1/timing", "R2/2014-01-12T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action:0/time", "2015-01-01T01:01:01.000Z")
                .put("medication_order/medication_detail/medication_action:0/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action:0/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action:0/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action:0/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|wt_path", "medication_order/medication_detail/medication_instruction")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|activity_index", "0")
                .put("medication_order/medication_detail/medication_action:1/time", "2015-01-01T01:01:01.000Z")
                .put("medication_order/medication_detail/medication_action:1/ism_transition/current_state", "completed")
                .put("medication_order/medication_detail/medication_action:1/medicine|code", "b")
                .put("medication_order/medication_detail/medication_action:1/medicine|value", "Aspirin B")
                .put("medication_order/medication_detail/medication_action:1/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|composition_uid", "compositionuid")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|wt_path", "medication_order/medication_detail/medication_instruction")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|activity_index", "1")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_instruction:0/order:1/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:1/timing", "R2/2014-01-12T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001,'Order']"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction']"),
            entry("medication_order/medication_detail/medication_action:1/_instruction_details|activity_id", "activities[at0001,'Order #2']"),
            entry(
                "medication_order/medication_detail/medication_action:1/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testMultipleActivitiesPathAndIndex() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val context = ConversionContext.create()
            .withActionToInstructionHandler(InCompositionActionToInstructionHandler())
            .build()

        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order:0/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_instruction/order:1/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order:1/timing", "R2/2014-01-12T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action:0/time", "2015-01-01T01:01:01.000Z")
                .put("medication_order/medication_detail/medication_action:0/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action:0/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action:0/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action:0/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "\$selfComposition")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|path",
                     "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction']")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|activity_index", "0") // action #2
                .put("medication_order/medication_detail/medication_action:1/time", "2015-01-01T01:01:01.000Z")
                .put("medication_order/medication_detail/medication_action:1/ism_transition/current_state", "completed")
                .put("medication_order/medication_detail/medication_action:1/medicine|code", "b")
                .put("medication_order/medication_detail/medication_action:1/medicine|value", "Aspirin B")
                .put("medication_order/medication_detail/medication_action:1/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|composition_uid", "\$selfComposition")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|path",
                     "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction']")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|activity_id", "activities[at0001]")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|activity_index", "1")
                .build(),
            context)

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_instruction:0/order:1/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:1/timing", "R2/2014-01-12T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001,'Order']"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction']"),
            entry("medication_order/medication_detail/medication_action:1/_instruction_details|activity_id", "activities[at0001,'Order #2']"),
            entry(
                "medication_order/medication_detail/medication_action:1/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testAction() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_action:0/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action:0/medicine|code", "aspirin")
                .put("medication_order/medication_detail/medication_action:0/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action:0/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "cuid")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|instruction_uid", "iuid1")
                .put("medication_order/medication_detail/medication_action:0/_instruction_details|wt_path", "medication_order/medication_detail/medication_instruction")
                .put("medication_order/medication_detail/medication_action:1/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action:1/medicine|code", "medrol")
                .put("medication_order/medication_detail/medication_action:1/medicine|value", "Medrol")
                .put("medication_order/medication_detail/medication_action:1/instructions", "Take Medrol as needed")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|composition_uid", "cuid")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|instruction_uid", "iuid2")
                .put("medication_order/medication_detail/medication_action:1/_instruction_details|wt_path", "medication_order/medication_detail/medication_instruction")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "cuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1 and uid/value='iuid1']"),
            entry("medication_order/medication_detail/medication_action:1/_instruction_details|composition_uid", "cuid"),
            entry("medication_order/medication_detail/medication_action:1/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:1/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1 and uid/value='iuid2']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithUUIDOnRmPath() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|activity_id", "activities[at0001]")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put("medication_order/medication_detail/medication_action/_instruction_details|instruction_uid", "insuid")
                .put(
                    "medication_order/medication_detail/medication_action/_instruction_details|path",
                    "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1 and name/value='Medication instruction']")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1 and uid/value='insuid']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithIndex() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put("medication_order/medication_detail/medication_action/_instruction_details|instruction_index", "2")
                .put("medication_order/medication_detail/medication_action/_instruction_details|wt_path", "medication_order/medication_detail/medication_instruction")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #3']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithIndexOnRmPath() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|activity_id", "activities[at0001]")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put("medication_order/medication_detail/medication_action/_instruction_details|instruction_index", "2")
                .put("medication_order/medication_detail/medication_action/_instruction_details|path",
                    "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #17']")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #3']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithExistingUidPath() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|activity_id", "activities[at0001]")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put(
                    "medication_order/medication_detail/medication_action/_instruction_details|path",
                    "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1 and uid/value='abc']")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1 and uid/value='abc']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithExistingIndexedPath() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|activity_id", "activities[at0001]")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put(
                    "medication_order/medication_detail/medication_action/_instruction_details|path",
                    "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #37']")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #37']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithExistingInstructionUid() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/_uid", "insuid")
                .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
                .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .put("medication_order/medication_detail/medication_action/_instruction_details|activity_id", "activities[at0001]")
                .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
                .put(
                    "medication_order/medication_detail/medication_action/_instruction_details|path",
                    "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #37']")
                .build(),
            ConversionContext.create().build())

        val retrieved: Map<String, String?> = webTemplate.convertFormattedFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(retrieved).contains(
            entry("medication_order/medication_detail/medication_instruction:0/_uid", "insuid"),
            entry("medication_order/medication_detail/medication_instruction:0/narrative", "Take Aspirin as needed"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/medicine", "Aspirin"),
            entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R3/2014-01-10T00:00:00.000+01:00"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|composition_uid", "compositionuid"),
            entry("medication_order/medication_detail/medication_action:0/_instruction_details|activity_id", "activities[at0001]"),
            entry(
                "medication_order/medication_detail/medication_action:0/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #37']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionExpiryTime() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Vaccination Encounter.opt"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("vaccination_encounter/vaccination_instruction/expiry_time")
        assertThat(node).isNotNull
        assertThat(node.occurences?.min).isEqualTo(0)
        assertThat(node.occurences?.max).isEqualTo(1)
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testSpeed() {
        val webTemplateBuilderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), webTemplateBuilderContext)
        val map = ImmutableMap.builder<String, String>()
            .put("ctx/language", "sl")
            .put("ctx/territory", "SI")
            .put("ctx/id_scheme", "ispek")
            .put("ctx/id_namespace", "ispek")
            .put("ctx/composer_name", "George Orwell")
            .put("medication_order/medication_detail/medication_instruction/narrative", "Take Aspirin as needed")
            .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
            .put("medication_order/medication_detail/medication_instruction/order/timing", "R3/2014-01-10T00:00:00.000+01:00")
            .put("medication_order/medication_detail/medication_action/ism_transition/current_state", "524")
            .put("medication_order/medication_detail/medication_action/medicine|code", "a")
            .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
            .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
            .put("medication_order/medication_detail/medication_action/_instruction_details|activity_id", "activities[at0001]")
            .put("medication_order/medication_detail/medication_action/_instruction_details|composition_uid", "compositionuid")
            .put(
                "medication_order/medication_detail/medication_action/_instruction_details|path",
                "/content[openEHR-EHR-SECTION.medication.v1,'Medication detail']/items[openEHR-EHR-INSTRUCTION.medication.v1,'Medication instruction #37']")
            .build()

        val context = ConversionContext.create().build()
        assertThat(webTemplate.convertFromFlatToRaw<Composition>(map, context)).isNotNull
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionNoTimingNarrative() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("medication_order/medication_detail/medication_instruction:0/narrative", "<none>"))
        assertThat(flatMap).contains(entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R1"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionNoTimingNarrativeCtx() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("ctx/instruction_narrative", "Hello world!")
                .put("ctx/activity_timing", "R7")
                .put("medication_order/medication_detail/medication_instruction/order/medicine", "Aspirin")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("medication_order/medication_detail/medication_instruction:0/narrative", "Hello world!"))
        assertThat(flatMap).contains(entry("medication_order/medication_detail/medication_instruction:0/order:0/timing", "R7"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testActionNoISMCurrentState() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("medication_order/medication_detail/medication_action:0/ism_transition/current_state|value", "completed"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testActionNoISMCurrentStateCtx() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/composer_name", "George Orwell")
                .put("ctx/action_ism_transition_current_state", "active")
                .put("medication_order/medication_detail/medication_action/medicine|code", "a")
                .put("medication_order/medication_detail/medication_action/medicine|value", "Aspirin")
                .put("medication_order/medication_detail/medication_action/instructions", "Take Aspirin as needed")
                .build(),
            ConversionContext.create().build())

        val flatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(composition!!, FromRawConversion.create())
        assertThat(flatMap).contains(entry("medication_order/medication_detail/medication_action:0/ism_transition/current_state|value", "active"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testEmptyActivity() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/ZN - Restraint.opt"), builderContext)
        val jsonNode = getObjectMapper().readTree(getJson("/convert/compositions/restraint.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(jsonNode, ConversionContext.create().build())

        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testEmptyInstruction() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Melanoma ST.xml"), builderContext)
        val jsonNode = getObjectMapper().readTree(getJson("/convert/compositions/melanoma.json")) as ObjectNode
        val composition: Composition? = webTemplate.convertFromStructuredToRaw(jsonNode, ConversionContext.create().build())

        assertThat(composition?.content ?: emptyList()).isNotEmpty

    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInstructionDetailsWithMultipleActivities() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))

        val json = """
            {
              "ctx/language": "en",
              "ctx/territory": "US",
              "ctx/composer_name": "tester",
              "service/service:0/ism_transition/current_state": "active",
              "service/service:0/service_name": "Walk a mile",
              "service/service:0/_instruction_details|composition_uid": "aaaa",
              "service/service:0/_instruction_details|activity_id": "activities[at0001, 'Request']",
              "service/service:0/_instruction_details|path": "/content[openEHR-EHR-INSTRUCTION.request.v0]",
              "service/service:1/ism_transition/current_state": "active",
              "service/service:1/service_name": "Swim",
              "service/service:1/_instruction_details|composition_uid": "aaaa",
              "service/service:1/_instruction_details|activity_id": "activities[at0001, 'Request #2']",
              "service/service:1/_instruction_details|path": "/content[openEHR-EHR-INSTRUCTION.request.v0]"
            }
        """.trimIndent()

        val webTemplateAct: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Service.xml"), builderContext)
        val compositionAct: Composition? = webTemplateAct.convertFromFlatToRaw(
            getObjectMapper().readValue(json, object : TypeReference<Map<String, Any?>>(){}),
            ConversionContext.create().build())

        val retrieved: Map<String, String?> = webTemplateAct.convertFormattedFromRawToFlat(compositionAct!!, FromRawConversion.create())
        assertThat(retrieved).contains(
            entry("service/service:0/_instruction_details|activity_id", "activities[at0001, 'Request']"),
            entry("service/service:1/_instruction_details|activity_id", "activities[at0001, 'Request #2']"))
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testInvalidState() {
        val builderContext = WebTemplateBuilderContext("ru", ImmutableSet.of("ru", "en"))
        val template = getTemplate("/convert/templates/openEHR-EHR-COMPOSITION.t_suspension_drug_therapy.v1.xml")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(template, builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            getObjectMapper().readValue(getJson("/convert/compositions/t_suspension_drug_therapy.json"), object : TypeReference<Map<String, Any?>>(){}),
            ConversionContext.create().build())

        assertThat(composition?.content ?: emptyList()).isNotEmpty
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testWfDefinition() {
        val builderContext = WebTemplateBuilderContext("sl", ImmutableSet.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Service Request.xml"), builderContext)
        val firstFlatComposition: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("ctx/language", "en")
            .put("ctx/territory", "US")
            .put("ctx/composer_name", "tester")
            .put("service_request/service_request:0/request:0/service_name", "Walk a mile")
            .put("service_request/service_request:0/request:1/service_name", "Swim")
            .put("service_request/service_request:0/request:2/service_name", "Drink water")
            .put("service_request/service_request:0/_wf_definition", "Hello world")
            .put("service_request/service_request:0/_wf_definition|formalism", "myown")
            .build()

        val secondFlatComposition: Map<String, Any> = ImmutableMap.Builder<String, Any>()
            .put("ctx/language", "en")
            .put("ctx/territory", "US")
            .put("ctx/composer_name", "tester")
            .put("service_request/service_request:0/request:0/service_name", "Walk a mile")
            .put("service_request/service_request:0/request:1/service_name", "Swim")
            .put("service_request/service_request:0/request:2/service_name", "Drink water")
            .put("service_request/service_request:0/_wf_definition|value", "Hello world")
            .put("service_request/service_request:0/_wf_definition|formalism", "xxx")
            .build()

        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(firstFlatComposition, ConversionContext.create().build())
        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(secondFlatComposition, ConversionContext.create().build())

        val firstInstruction = firstComposition!!.content[0] as Instruction
        assertThat(firstInstruction.wfDefinition).isNotNull
        assertThat(firstInstruction.wfDefinition?.formalism).isEqualTo("myown")
        assertThat(firstInstruction.wfDefinition?.value).isEqualTo("Hello world")

        val secondInstruction = secondComposition!!.content[0] as Instruction
        assertThat(secondInstruction.wfDefinition).isNotNull
        assertThat(secondInstruction.wfDefinition?.formalism).isEqualTo("xxx")
        assertThat(secondInstruction.wfDefinition?.value).isEqualTo("Hello world")

        val firstFlatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(firstComposition, FromRawConversion.create())
        assertThat(firstFlatMap).contains(
            entry("service_request/service_request:0/_wf_definition", "Hello world"), //Both "" and "|value" are valid. Before new version we used |value. But in new version we are now using "".
            entry("service_request/service_request:0/_wf_definition|formalism", "myown"))

        val secondFlatMap: Map<String, Any?> = webTemplate.convertFromRawToFlat(secondComposition, FromRawConversion.create())
        assertThat(secondFlatMap).contains(
            entry("service_request/service_request:0/_wf_definition", "Hello world"), //Both "" and "|value" are valid. Before new version we used |value. But in new version we are now using "".
            entry("service_request/service_request:0/_wf_definition|formalism", "xxx"))

        val firstStructuredComposition: JsonNode? = webTemplate.convertFromRawToStructured(firstComposition, FromRawConversion.create())
        val firstWfDefNode = firstStructuredComposition!!.path("service_request")
            .path("service_request")
            .path(0)
            .path("_wf_definition")
            .path(0)
        assertThat(firstWfDefNode.path("|value").textValue()).isEqualTo("Hello world")
        assertThat(firstWfDefNode.path("|formalism").textValue()).isEqualTo("myown")

        val secondStructuredComposition: JsonNode? = webTemplate.convertFromRawToStructured(secondComposition, FromRawConversion.create())
        val secondWfDefNode = secondStructuredComposition!!.path("service_request")
            .path("service_request")
            .path(0)
            .path("_wf_definition")
            .path(0)
        assertThat(secondWfDefNode.path("|value").textValue()).isEqualTo("Hello world")
        assertThat(secondWfDefNode.path("|formalism").textValue()).isEqualTo("xxx")
    }
}
