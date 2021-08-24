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
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.google.common.collect.ImmutableMap
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.base.basetypes.GenericId
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Observation
import org.openehr.rm.composition.Section
import java.io.IOException
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class WorkFlowIdTest : AbstractWebTemplateTest() {
    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testWorkflowIdDirect() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val dateTime = ZonedDateTime.of(2015, 1, 1, 10, 31, 16, 0, ZoneId.systemDefault()).toOffsetDateTime()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/composer_name", "Composer")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/end_time", "2016-01-01T12:30:30Z")
                .put("vitals/vitals/haemoglobin_a1c/history_origin", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime))
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                .put("vitals/vitals/haemoglobin_a1c/_work_flow_id|id", "1")
                .put("vitals/vitals/haemoglobin_a1c/_work_flow_id|id_scheme", "x")
                .put("vitals/vitals/haemoglobin_a1c/_work_flow_id|namespace", "y")
                .put("vitals/vitals/haemoglobin_a1c/_work_flow_id|type", "wf")
                .build(),
            ConversionContext.create().build())

        val section = composition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.workFlowId!!.id).isInstanceOf(GenericId::class.java)
        assertThat(observation.workFlowId!!.id!!.value).isEqualTo("1")
        assertThat(observation.workFlowId!!.namespace).isEqualTo("y")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testWorkflowIdInCtx() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/composer_name", "Composer")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/end_time", "2016-01-01T12:30:30Z")
                .put("ctx/work_flow_id|id", "wf_id")
                .put("ctx/work_flow_id|namespace", "wf_ns")
                .put("ctx/work_flow_id|id_scheme", "wf_scheme")
                .put("ctx/work_flow_id|type", "wf_type")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                .build(),
            ConversionContext.create().build())

        val section = composition!!.content[0] as Section
        val observation = section.items[0] as Observation
        assertThat(observation.workFlowId).isNotNull
        assertThat(observation.workFlowId!!.id).isInstanceOf(GenericId::class.java)
        assertThat(observation.workFlowId!!.id!!.value).isEqualTo("wf_id")
        assertThat(observation.workFlowId!!.namespace).isEqualTo("wf_ns")
        assertThat(observation.workFlowId!!.type).isEqualTo("wf_type")
    }

    @Test
    @Throws(IOException::class, JAXBException::class)
    fun testWorkflowIdInCtxAndDirect() {
        val builderContext = WebTemplateBuilderContext("sl")
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Demo Vitals.opt"), builderContext)
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.builder<String, String>()
                .put("ctx/language", "sl")
                .put("ctx/territory", "SI")
                .put("ctx/composer_name", "Composer")
                .put("ctx/id_scheme", "ispek")
                .put("ctx/id_namespace", "ispek")
                .put("ctx/end_time", "2016-01-01T12:30:30Z")
                .put("ctx/work_flow_id|id", "wf_id")
                .put("ctx/work_flow_id|namespace", "wf_ns")
                .put("ctx/work_flow_id|id_scheme", "wf_scheme")
                .put("ctx/work_flow_id|type", "wf_type")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c/any_event/test_status|code", "at0037")
                .put("vitals/vitals/haemoglobin_a1c:1/any_event/test_status|terminology", "local")
                .put("vitals/vitals/haemoglobin_a1c:1/any_event/test_status|code", "at0037")
                .put("vitals/vitals/haemoglobin_a1c:1/_work_flow_id|id", "1")
                .put("vitals/vitals/haemoglobin_a1c:1/_work_flow_id|id_scheme", "x")
                .put("vitals/vitals/haemoglobin_a1c:1/_work_flow_id|namespace", "y")
                .put("vitals/vitals/haemoglobin_a1c:1/_work_flow_id|type", "wf")
                .build(),
            ConversionContext.create().build())

        val section = composition!!.content[0] as Section
        val firstObservation = section.items[0] as Observation
        assertThat(firstObservation.workFlowId).isNotNull
        assertThat(firstObservation.workFlowId!!.id).isInstanceOf(GenericId::class.java)
        assertThat(firstObservation.workFlowId!!.id!!.value).isEqualTo("wf_id")
        assertThat(firstObservation.workFlowId!!.namespace).isEqualTo("wf_ns")
        assertThat(firstObservation.workFlowId!!.type).isEqualTo("wf_type")
        val secondObservation = section.items[1] as Observation
        assertThat(secondObservation.workFlowId).isNotNull
        assertThat(secondObservation.workFlowId!!.id).isInstanceOf(GenericId::class.java)
        assertThat(secondObservation.workFlowId!!.id!!.value).isEqualTo("1")
        assertThat(secondObservation.workFlowId!!.namespace).isEqualTo("y")
        assertThat(secondObservation.workFlowId!!.type).isEqualTo("wf")
    }
}
