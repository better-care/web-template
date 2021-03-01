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
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.builder.model.input.CareflowStepWebTemplateCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Action
import org.openehr.rm.composition.Composition
import org.openehr.rm.composition.Section
import java.io.IOException
import javax.xml.bind.JAXBException

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class IsmTransitionTest : AbstractWebTemplateTest() {
    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCareflowStepAqlPath() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val node: WebTemplateNode = webTemplate.findWebTemplateNode("medication_order/medication_detail/medication_action/ism_transition/careflow_step")
        assertThat(node.path).contains("/ism_transition/careflow_step")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCareflowStepCurrentState() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)

        val nodes = getWebTemplateNodes(webTemplate.tree) { it.jsonId == "careflow_step" }
        assertThat(nodes).hasSize(1)
        assertThat((nodes[0].inputs[0].list[0] as CareflowStepWebTemplateCodedValue).currentStates[0]).isEqualTo("524")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCurrentState() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)

        val nodes = getWebTemplateNodes(webTemplate.tree) { it.jsonId == "current_state" }
        val inputs = nodes[0].inputs[0].list
        assertThat(inputs).hasSize(8)
        assertThat(inputs[0].value).isEqualTo("530")
        assertThat(inputs[0].label).isEqualTo("suspended")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCareflowStepWT() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)

        val node: WebTemplateNode = webTemplate.findWebTemplateNode("medication_order/medication_detail/medication_action/ism_transition/careflow_step")

        val options: List<WebTemplateCodedValue> = node.inputs[0].list
        assertThat(options).isNotEmpty
        assertThat(options[0].value).isEqualTo("at0001")
        assertThat(options[0].label).isEqualTo("Plan medication")
        assertThat(options[0].localizedLabels["sl"]).isEqualTo("*Plan medication(en)")
        assertThat(options[0].localizedLabels["en"]).isEqualTo("Plan medication")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCareflowStepBuilder() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of<String, Any>(
                "medication_order/medication_detail/medication_action/ism_transition/careflow_step", "at0001",
                "medication_order/medication_detail/medication_action/medicine", "Aspirin"),
            context)

        val action = (composition!!.content[0] as Section).items[0] as Action

        val currentState = action.ismTransition!!.currentState
        assertThat(currentState!!.definingCode!!.terminologyId!!.value).isEqualTo("openehr")
        assertThat(currentState.definingCode!!.codeString).isEqualTo("524")
        assertThat(currentState.value).isEqualTo("initial")

        val careflowStep = action.ismTransition!!.careflowStep
        assertThat(careflowStep!!.definingCode!!.terminologyId!!.value).isEqualTo("local")
        assertThat(careflowStep.definingCode!!.codeString).isEqualTo("at0001")
        assertThat(careflowStep.value).isEqualTo("*Plan medication(en)")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCareflowStepBuilderMultipleCurrentState() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val composition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of<String, Any>(
                "medication_order/medication_detail/medication_action/ism_transition/careflow_step", "at0002",
                "medication_order/medication_detail/medication_action/medicine", "Aspirin"),
            context)

        val action = (composition!!.content[0] as Section).items[0] as Action

        val currentState = action.ismTransition!!.currentState
        assertThat(currentState!!.definingCode!!.terminologyId!!.value).isEqualTo("openehr")
        assertThat(currentState.definingCode!!.codeString).isEqualTo("245")
        assertThat(currentState.value).isEqualTo("active")

        val careflowStep = action.ismTransition!!.careflowStep
        assertThat(careflowStep!!.definingCode!!.terminologyId!!.value).isEqualTo("local")
        assertThat(careflowStep.definingCode!!.codeString).isEqualTo("at0002")
        assertThat(careflowStep.value).isEqualTo("*Issue prescription for medication(en)")
    }

    @Test
    @Throws(JAXBException::class, IOException::class)
    fun testCareflowStepBuilderMultipleCurrentStateOverride() {
        val builderContext = WebTemplateBuilderContext("en", ImmutableList.of("en", "sl"))
        val webTemplate: WebTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/MED - Medication Order.opt"), builderContext)
        val context = ConversionContext.create().withLanguage("sl").withTerritory("SI").withComposerName("composer").build()
        val firstComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of<String, Any>(
                "medication_order/medication_detail/medication_action/ism_transition/careflow_step", "at0002",
                "medication_order/medication_detail/medication_action/ism_transition/current_state|value", "initial",
                "medication_order/medication_detail/medication_action/medicine", "Aspirin"),
            context)

        val firstAction = (firstComposition!!.content[0] as Section).items[0] as Action

        val firstCurrentState = firstAction.ismTransition!!.currentState
        assertThat(firstCurrentState!!.definingCode!!.terminologyId!!.value).isEqualTo("openehr")
        assertThat(firstCurrentState.definingCode!!.codeString).isEqualTo("524")
        assertThat(firstCurrentState.value).isEqualTo("initial")

        val firstCareFlowStep = firstAction.ismTransition!!.careflowStep
        assertThat(firstCareFlowStep!!.definingCode!!.terminologyId!!.value).isEqualTo("local")
        assertThat(firstCareFlowStep.definingCode!!.codeString).isEqualTo("at0002")
        assertThat(firstCareFlowStep.value).isEqualTo("*Issue prescription for medication(en)")

        val secondComposition: Composition? = webTemplate.convertFromFlatToRaw(
            ImmutableMap.of<String, Any>(
                "medication_order/medication_detail/medication_action/ism_transition/careflow_step", "at0002",
                "medication_order/medication_detail/medication_action/ism_transition/current_state", "524",
                "medication_order/medication_detail/medication_action/medicine", "Aspirin"),
            context)

        val secondAction = (secondComposition!!.content[0] as Section).items[0] as Action
        val secondCurrentState = secondAction.ismTransition!!.currentState
        assertThat(secondCurrentState!!.definingCode!!.terminologyId!!.value).isEqualTo("openehr")
        assertThat(secondCurrentState.definingCode!!.codeString).isEqualTo("524")
        assertThat(secondCurrentState.value).isEqualTo("initial")

        val secondCareFlowStep = secondAction.ismTransition!!.careflowStep
        assertThat(secondCareFlowStep!!.definingCode!!.terminologyId!!.value).isEqualTo("local")
        assertThat(secondCareFlowStep.definingCode!!.codeString).isEqualTo("at0002")
        assertThat(secondCareFlowStep.value).isEqualTo("*Issue prescription for medication(en)")
    }
}
