package care.better.platform.web.template.converter

import care.better.platform.json.jackson.better.BetterObjectMapper
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.util.*

/**
 * @author Matic Ribic
 */
class CompositionGenericFieldsTest : AbstractWebTemplateTest() {
    private lateinit var webTemplate: WebTemplate
    private val objectMapper = BetterObjectMapper()

    @BeforeEach
    fun setUp() {
        webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Composed document.opt"), WebTemplateBuilderContext("en"))
    }

    @Test
    fun createStructuredCompositionWithGenericFieldsOnly() {
        val inputStructuredComposition =
                objectMapper.readTree(getJson("/convert/compositions/genericfields/StructuredCompositionWithGenericFieldsOnly.json")) as ObjectNode

        val context = ConversionContext.create().withUidGenerator { UUID.randomUUID().toString() }.build()
        val convertedComposition: Composition? = webTemplate.convertFromStructuredToRaw(inputStructuredComposition, context)
        assertThat(convertedComposition).isNotNull
        assertThat(convertedComposition!!.feederAudit?.originalContent).isNotNull
    }

    @Test
    fun createFlatCompositionWithGenericFieldsOnly() {
        val inputFlatComposition =
                objectMapper.readValue(getJson("/convert/compositions/genericfields/FlatCompositionWithGenericFieldsOnly.json"),
                                       object : TypeReference<Map<String, Any>>() {})
        val context = ConversionContext.create().withUidGenerator { UUID.randomUUID().toString() }.build()

        val convertedComposition: Composition? = webTemplate.convertFromFlatToRaw(inputFlatComposition, context)

        assertThat(convertedComposition).isNotNull
        assertThat(convertedComposition!!.feederAudit?.originalContent).isNotNull
    }
}
