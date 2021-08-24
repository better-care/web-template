package care.better.platform.web.template.converter

import care.better.platform.json.jackson.better.BetterObjectMapper
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.abstraction.AbstractWebTemplateTest
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.exceptions.ConversionException
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openehr.rm.composition.Composition
import java.util.*

/**
 * @author Matic Ribic
 */
class EmptyCompositionTest : AbstractWebTemplateTest() {
    private lateinit var webTemplate: WebTemplate
    private val objectMapper = BetterObjectMapper()

    @BeforeEach
    fun setUp() {
        webTemplate = WebTemplateBuilder.buildNonNull(getTemplate("/convert/templates/Composed document.opt"), WebTemplateBuilderContext("en"))
    }

    @Test
    fun structuredCompositionNotCreated() {
        val inputStructuredComposition =
                objectMapper.readTree(getJson("/convert/compositions/empty/EmptyStructuredComposition.json")) as ObjectNode
        val context = ConversionContext.create().withUidGenerator { UUID.randomUUID().toString() }.build()

        val convertedComposition: Composition? = webTemplate.convertFromStructuredToRaw(inputStructuredComposition, context)

        assertThat(convertedComposition).isNull()
    }

    @Test
    fun flatCompositionNotCreated() {
        val inputFlatComposition =
                objectMapper.readValue(getJson("/convert/compositions/empty/EmptyFlatComposition.json"),
                                       object : TypeReference<Map<String, Any>>() {})
        val context = ConversionContext.create().withUidGenerator { UUID.randomUUID().toString() }.build()

        assertThatThrownBy { val convertedComposition: Composition? = webTemplate.convertFromFlatToRaw(inputFlatComposition, context) }
                .isInstanceOf(ConversionException::class.java)
                .hasMessage("COMPOSITION has no attribute composed_document.")
    }
}