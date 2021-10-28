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

package care.better.platform.web.template.builder.mapper

import care.better.platform.json.jackson.time.OpenEhrTimeModule
import care.better.platform.web.template.WebTemplate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

/**
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Singleton instance of [WebTemplateObjectMapper] that is used to serialize and deserialize [WebTemplate]
 */
internal object WebTemplateObjectMapper : ObjectMapper() {
    init {
        this.registerModule(JodaModule())
        this.registerModule(KotlinModule())
        this.registerModule(OpenEhrTimeModule())
        this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    private val writer = this.writer()
    private val prettyWriter = this.writerWithDefaultPrettyPrinter()

    @JvmStatic
    fun getWriter(pretty: Boolean): ObjectWriter = if (pretty) prettyWriter else writer
}
