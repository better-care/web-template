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

package care.better.platform.web.template.abstraction

import care.better.platform.jaxb.JaxbRegistry
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import org.apache.commons.io.IOUtils
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.openehr.am.aom.Template
import org.openehr.rm.composition.Composition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
abstract class AbstractWebTemplateTest {

    companion object {
        private lateinit var defaultTimeZone: TimeZone
        private lateinit var defaultDateTimeZone: DateTimeZone

        @BeforeAll
        @JvmStatic
        @Suppress("unused")
        internal fun init() {
            defaultTimeZone = TimeZone.getDefault()
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Ljubljana"))
            defaultDateTimeZone = DateTimeZone.getDefault()
            DateTimeZone.setDefault(DateTimeZone.forID("Europe/Ljubljana"))
        }

        @AfterAll
        @JvmStatic
        @Suppress("unused")
        internal fun tearDown() {
            TimeZone.setDefault(defaultTimeZone)
            DateTimeZone.setDefault(defaultDateTimeZone)
        }
    }

    private val unmarshaller: Unmarshaller = JaxbRegistry.getInstance().unmarshaller

    private val objectMapper: ObjectMapper = createObjectMapper()

    /**
     * Returns [ObjectMapper].
     *
     * @return [ObjectMapper]
     */
    protected open fun getObjectMapper(): ObjectMapper = objectMapper

    /**
     * Creates new instance of [ObjectMapper].
     *
     * @return [ObjectMapper]
     */
    protected fun createObjectMapper(): ObjectMapper = ObjectMapper().apply {
        this.enable(JsonParser.Feature.ALLOW_COMMENTS)
    }

    /**
     * Reads and returns [Template].
     *
     * @param templateFile Path to the template resource including resource name
     * @return [Template]
     * @throws [RuntimeException] if template resource was not found
     */
    @Throws(JAXBException::class, IOException::class)
    protected open fun getTemplate(templateFile: String): Template =
            AbstractWebTemplateTest::class.java.getResourceAsStream(templateFile).use { inputStream ->
                if (inputStream == null) {
                    throw RuntimeException("Template resource was not found: $templateFile.")
                }
                unmarshaller.unmarshal(StreamSource(inputStream), Template::class.java).value
            }

    /**
     * Reads and returns [Composition].
     *
     * @param compositionFile Path to the composition resource including resoruce name
     * @return [Composition]
     * @throws [RuntimeException] if composition resource was not found
     */
    @Throws(JAXBException::class, IOException::class)
    protected open fun getComposition(compositionFile: String): Composition =
            AbstractWebTemplateTest::class.java.getResourceAsStream(compositionFile).use { stream ->
                if (stream == null)
                    throw RuntimeException("Composition resource was not found: $compositionFile.")
                else
                    unmarshaller.unmarshal(StreamSource(stream), Composition::class.java).value
            }

    /**
     * Reads [Template] and convert it to the [WebTemplate].
     *
     * @param templateFile Path to the template resource including resource name
     * @return [WebTemplate]
     * @throws [RuntimeException] if template resource was not found
     */
    @Throws(JAXBException::class, IOException::class)
    protected open fun getWebTemplate(templateFile: String): WebTemplate {
        val template = getTemplate(templateFile)
        return WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext("en", Lists.newArrayList("en")))
    }

    /**
     * Creates web template and exports it to the file system.
     *
     * @param templateFile Path to the template resource including resource name
     * @param prefix JSON file prefix
     * @param defaultLanguage Default [WebTemplate] language
     * @param languages [Set] of other supported [WebTemplate] languages
     */
    @Throws(JAXBException::class, IOException::class)
    protected open fun buildAndExport(templateFile: String, prefix: String, defaultLanguage: String, languages: Set<String>) {
        val webTemplate = WebTemplateBuilder.buildNonNull(getTemplate(templateFile), WebTemplateBuilderContext(defaultLanguage, languages))
        writeOut(prefix, webTemplate)
    }

    /**
     * Exports [WebTemplate] to the file system.
     *
     * @param prefix JSON file prefix
     * @param webTemplate [WebTemplate]
     */
    protected fun writeOut(prefix: String, webTemplate: WebTemplate) {
        FileOutputStream(File(System.getProperty("java.io.tmpdir") + File.separator + prefix + ".json")).use { outputStream ->
            webTemplate.write(outputStream, false)
        }
    }

    /**
     * Reads and returns JSON [String].
     *
     * @param jsonFile Path to the JSON resource including resource name
     * @return JSON [String]
     * @throws [RuntimeException] if JSON resource was not found
     */
    protected open fun getJson(jsonFile: String): String =
            AbstractWebTemplateTest::class.java.getResourceAsStream(jsonFile).use { stream ->
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(IOUtils.toString(stream, StandardCharsets.UTF_8)))
            }

    /**
     * Reads and returns JSON [String].
     *
     * @param jsonFile Path to the JSON resource including resource name
     * @param objectMapper [ObjectMapper]
     * @return JSON [String]
     * @throws [RuntimeException] if JSON resource was not found
     */
    protected open fun getJson(jsonFile: String, objectMapper: ObjectMapper): String =
            AbstractWebTemplateTest::class.java.getResourceAsStream(jsonFile).use { stream ->
                objectMapper.writeValueAsString(objectMapper.readTree(IOUtils.toString(stream, StandardCharsets.UTF_8)))
            }

    protected open fun getWebTemplateNodes(webTemplateNode: WebTemplateNode, webTemplateNodePredicate: (WebTemplateNode) -> Boolean): List<WebTemplateNode> {
        val list: MutableList<WebTemplateNode> = mutableListOf()
        if (webTemplateNodePredicate.invoke(webTemplateNode)) {
            list.add(webTemplateNode)
        }

        if (webTemplateNode.children.isEmpty()) {
            return list
        }


        webTemplateNode.children.forEach {
            list.addAll(getWebTemplateNodes(it, webTemplateNodePredicate))
        }
        return list
    }
}
