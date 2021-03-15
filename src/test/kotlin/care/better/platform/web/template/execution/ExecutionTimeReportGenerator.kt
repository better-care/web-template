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

package care.better.platform.web.template.speed

import care.better.platform.jaxb.JaxbRegistry
import care.better.platform.json.jackson.better.BetterObjectMapper
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.converter.raw.context.ConversionContext
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.common.base.Stopwatch
import org.apache.commons.io.IOUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.openehr.am.aom.Template
import org.openehr.rm.composition.Composition
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import javax.xml.transform.stream.StreamSource

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class ExecutionTimeReportGenerator(resources: List<ResourceDto>, private val repetitions: Int = 50) {
    private val jaxbRegistry: JaxbRegistry = JaxbRegistry.getInstance()

    private val objectMapper: ObjectMapper = ObjectMapper().apply {
        this.enable(JsonParser.Feature.ALLOW_COMMENTS)
    }

    private val reportDto: ReportDto = ReportDto()

    private val resourceDataList: List<ResourceDataDto<*>> = resources.map {
        if (it.compositionFormat == CompositionFormat.FLAT)
            ResourceDataDto(it.name, getFlatComposition(it.name), getWebTemplate(it.name, it.language, it.languages))
        else
            ResourceDataDto(it.name, getStructuredComposition(it.name), getWebTemplate(it.name, it.language, it.languages))
    }

    fun executeTestsAndGenerateReport() {
        warmUp()
        execute()
        generateReport()
    }

    private fun warmUp() {
        val mappingFunction: (String, Long, Long, Long) -> Unit = { name: String, t1: Long, t2: Long, t3: Long ->
            reportDto.warmUpReports[name] = ExecutionDto(t1, t2, t3)
        }
        execute(1, mappingFunction)
    }

    private fun execute() {
        val mappingFunction: (String, Long, Long, Long) -> Unit = { name: String, t1: Long, t2: Long, t3: Long ->
            val list = reportDto.executionReports.computeIfAbsent(name) { mutableListOf() }
            list.add(ExecutionDto(t1, t2, t3))
        }
        execute(repetitions, mappingFunction)
    }

    @Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments")
    private fun execute(size: Int, reportMappingFunction: (String, Long, Long, Long) -> Unit) {
        resourceDataList.forEach {
            (0 .. size).forEach { _ ->
                val webTemplate = it.webTemplate
                val (rawComposition, toRawExecutionTime) = if (it.composition is Map<*, *>) {
                    val flatMap = it.composition as Map<String, Any>
                    executeAndGetWithExecutionTime { webTemplate.convertFromFlatToRaw<Composition>(flatMap, ConversionContext.create().build())!! }
                } else {
                    val structuredObjectNode = it.composition as ObjectNode
                    executeAndGetWithExecutionTime {
                        webTemplate.convertFromStructuredToRaw<Composition>(structuredObjectNode, ConversionContext.create().build())!!
                    }
                }

                val toFlatExecutionTime = executeAndGetWithExecutionTime { webTemplate.convertFromRawToFlat(rawComposition) }.second
                val toStructuredExecutionTime = executeAndGetWithExecutionTime { webTemplate.convertFromRawToStructured(rawComposition) }.second
                reportMappingFunction.invoke(it.name, toRawExecutionTime, toFlatExecutionTime, toStructuredExecutionTime)
            }
        }
    }

    private fun generateReport() {
        FileWriter("web-template-report-${OffsetDateTime.now()}.txt").use { writer ->
            resourceDataList.asSequence().map { it.name }.forEach { name ->
                val rawExecutionStatistic = DescriptiveStatistics().apply {
                    reportDto.executionReports[name]?.asSequence()?.map { it.toRawExecutionTime }?.forEach { this.addValue(it.toDouble()) }
                }
                val flatExecutionStatistic = DescriptiveStatistics().apply {
                    reportDto.executionReports[name]?.asSequence()?.map { it.toFlatExecutionTime }?.forEach { this.addValue(it.toDouble()) }
                }
                val structuredExecutionStatistic = DescriptiveStatistics().apply {
                    reportDto.executionReports[name]?.asSequence()?.map { it.toStructuredExecutionTime }?.forEach { this.addValue(it.toDouble()) }
                }

                writer.write(
                    """
                     File name: $name
                     Web template generation execution time: ${reportDto.webTemplateExecutionTimes[name]}
                     Warm up execution time:
                        RAW conversion: ${reportDto.warmUpReports[name]?.toRawExecutionTime}
                        FLAT conversion: ${reportDto.warmUpReports[name]?.toFlatExecutionTime}
                        STRUCTURED conversion: ${reportDto.warmUpReports[name]?.toStructuredExecutionTime}
                     EXECUTION TIMES:
                        RAW conversion: 
                            Min: ${rawExecutionStatistic.min}
                            Max: ${rawExecutionStatistic.max}
                            Median: ${rawExecutionStatistic.getPercentile(50.0)}
                            95th Percentile: ${rawExecutionStatistic.getPercentile(95.0)}
                        FLAT conversion
                            Min: ${flatExecutionStatistic.min}
                            Max: ${flatExecutionStatistic.max}
                            Median: ${flatExecutionStatistic.getPercentile(50.0)}
                            95th Percentile: ${flatExecutionStatistic.getPercentile(95.0)}
                        STRUCTURED conversion
                            Min: ${structuredExecutionStatistic.min}
                            Max: ${structuredExecutionStatistic.max}
                            Median: ${structuredExecutionStatistic.getPercentile(50.0)}
                            95th Percentile: ${structuredExecutionStatistic.getPercentile(95.0)}
                """.trimIndent())
                writer.write("\n\n")
            }
        }
    }

    private fun <T> executeAndGetWithExecutionTime(executable: () -> T): Pair<T, Long> {
        val stopwatch: Stopwatch = Stopwatch.createStarted()
        val result = executable.invoke()
        return Pair(result, stopwatch.elapsed(TimeUnit.MILLISECONDS))
    }

    private fun getFlatComposition(name: String): Map<String, Any> {
        val path = "/execution/compositions/${name}.json"
        val jsonString = ExecutionTimeReportGenerator::class.java.getResourceAsStream(path).use { stream ->
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(IOUtils.toString(stream, StandardCharsets.UTF_8)))
        }

        return CompatibilityMapper.readValue(jsonString, object : TypeReference<Map<String, Any>>() {})
    }

    private fun getStructuredComposition(name: String): ObjectNode {
        val path = "/execution/compositions/${name}.json"
        val jsonString = ExecutionTimeReportGenerator::class.java.getResourceAsStream(path).use { stream ->
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectMapper.readTree(IOUtils.toString(stream, StandardCharsets.UTF_8)))
        }
        return CompatibilityMapper.readTree(jsonString) as ObjectNode
    }

    private fun getWebTemplate(name: String, language: String, languages: Set<String>): WebTemplate {
        val path = "/execution/templates/${name}.xml"
        val template = ExecutionTimeReportGenerator::class.java.getResourceAsStream(path).use { inputStream ->
            if (inputStream == null) {
                throw RuntimeException("Template resource was not found: $path.")
            }
            jaxbRegistry.createUnmarshaller().unmarshal(StreamSource(inputStream), Template::class.java).value
        }

        val (webTemplate, toWebTemplateExecutionTime) = executeAndGetWithExecutionTime {
            WebTemplateBuilder.buildNonNull(template, WebTemplateBuilderContext(language, languages))
        }

        reportDto.webTemplateExecutionTimes[name] = toWebTemplateExecutionTime

        return webTemplate
    }

    private data class ResourceDataDto<T>(val name: String, val composition: T, val webTemplate: WebTemplate)

    private data class ReportDto(
            val webTemplateExecutionTimes: MutableMap<String, Long> = mutableMapOf(),
            val warmUpReports: MutableMap<String, ExecutionDto> = mutableMapOf(),
            val executionReports: MutableMap<String, MutableList<ExecutionDto>> = mutableMapOf())

    private data class ExecutionDto(val toRawExecutionTime: Long, val toFlatExecutionTime: Long, val toStructuredExecutionTime: Long)

    private object CompatibilityMapper : BetterObjectMapper() {
        init {
            registerModule(JodaModule())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            enable(JsonParser.Feature.ALLOW_COMMENTS)
            setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.USE_DEFAULTS))
            configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        }
    }
}

data class ResourceDto(val name: String, val compositionFormat: CompositionFormat, val language: String, val languages: Set<String>)

enum class CompositionFormat { FLAT, STRUCTURED }

@Suppress("RedundantExplicitType")
fun main() {
    val resources: List<ResourceDto> = listOf(
        ResourceDto("LAB - Laboratory Test Report", CompositionFormat.FLAT, "sl", setOf("sl", "en")),
        ResourceDto("ISPEK - ZN - Nursing careplan Encounter", CompositionFormat.FLAT, "sl", setOf("sl", "en")))
    val repetitions: Int = 50
    ExecutionTimeReportGenerator(resources, repetitions).executeTestsAndGenerateReport()
}
