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

package care.better.platform.web.template.converter.flat.mapper

import care.better.platform.web.template.converter.flat.context.FlatMappingContext
import care.better.platform.web.template.converter.value.SimpleValueConverter
import com.marand.thinkehr.web.build.WebTemplateInputType
import com.marand.thinkehr.web.build.WebTemplateNode
import com.marand.thinkehr.web.build.input.WebTemplateInput
import com.marand.thinkehr.web.build.input.WebTemplateValidation
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.openehr.rm.datatypes.DvDate

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class PartialDateRmObjectToFlatMappingTest {

    @Test
    fun testDateWithYearOnly() {
        val webTemplateNode = WebTemplateNode().apply {
            this.input = WebTemplateInput(WebTemplateInputType.DATE)
            this.input.validation = WebTemplateValidation()
            this.input.validation.pattern = "yyyy-??-XX"
        }

        val dvDate = DvDate().apply { this.value = "2016" }
        val flatConversionContext = FlatMappingContext()

        DvDateToFlatMapper.map(webTemplateNode, SimpleValueConverter, dvDate, "id", flatConversionContext)
        Assertions.assertThat(flatConversionContext.get()).contains(Assertions.entry("id", "2016"))
    }
}
