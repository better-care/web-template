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

package care.better.platform.web.template.builder.utils

import care.better.openehr.terminology.OpenEhrTerminology
import care.better.platform.template.AmNode
import care.better.platform.template.AmUtils.findDescription
import care.better.platform.template.AmUtils.findTermText
import care.better.platform.template.AmUtils.findText
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.input.WebTemplateBindingCodedValue
import care.better.platform.web.template.builder.model.input.WebTemplateCodedValue
import care.better.platform.web.template.converter.constant.WebTemplateConstants.DEFAULT_LANGUAGE
import org.apache.commons.lang3.StringUtils
import org.openehr.am.aom.TermBindingItem

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 */
internal object CodePhraseUtils {
    @JvmStatic
    fun getOpenEhrTerminologyText(code: String, language: String?): String? =
        (if (language == null) null else OpenEhrTerminology.getInstance().getText(language, code)) ?: OpenEhrTerminology.getInstance().getText(DEFAULT_LANGUAGE, code)

    @JvmStatic
    fun getLocalTerminologyText(code: String?, amNode: AmNode, language: String?): String? =
        (if (language != null) findText(amNode, language, code) else null) ?: findTermText(amNode, code)

    @JvmStatic
    fun getCodedValue(terminology: String, code: String, amNode: AmNode, context: WebTemplateBuilderContext): WebTemplateCodedValue {
        val webTemplateCodedValue: WebTemplateCodedValue =
            when (terminology) {
                "openehr" -> WebTemplateCodedValue(code, getOpenEhrTerminologyText(code, context.defaultLanguage))
                "local" -> WebTemplateCodedValue(code, getLocalTerminologyText(code, amNode, context.defaultLanguage))
                else -> WebTemplateCodedValue(code, findTermText(amNode, "$terminology::$code"))
            }

        for (language in context.languages) {
            val label: String? =
                if ("openehr" == terminology) {
                    OpenEhrTerminology.getInstance().getText(language, code)
                } else {
                    if (context.isAddDescriptions) {
                        val description = findDescription(amNode, language, code)
                        if (description != null) {
                            webTemplateCodedValue.localizedDescriptions[language] = StringUtils.defaultString(description)
                        }
                    }
                    findText(amNode, language, code)
                }
            webTemplateCodedValue.localizedLabels[language] = StringUtils.defaultString(label)
        }
        return webTemplateCodedValue
    }

    @JvmStatic
    fun getBindingCodedValue(termBindingItem: TermBindingItem): WebTemplateBindingCodedValue? {
        val value = termBindingItem.value
        if (StringUtils.isNoneBlank(value.codeString)) {
            val terminologyId =
                if (value.terminologyId != null && StringUtils.isNotEmpty(value.terminologyId?.value))
                    value.terminologyId?.value
                else termBindingItem.code

            return WebTemplateBindingCodedValue(value.codeString!!, terminologyId!!)
        }
        return null
    }
}
