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

package care.better.platform.web.template.build

import com.marand.thinkehr.web.build.WebTemplateUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test

/**
 * @author Primoz Delopst
 * @since 3.1.0
 */
class TranslationsInAnnotationsTest {
    @Test
    fun testTranslations() {
        val translations: MutableMap<String, String> = mutableMapOf()
        WebTemplateUtils.parseTranslations("gagag a L10n={sl=abc def\\{\\}hello|de=Deutsch} xxzxx", translations)
        assertThat(translations).contains(entry("sl", "abc def{}hello"), entry("de", "Deutsch"))
    }
}
