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

package com.marand.thinkehr.web.build.input;

import care.better.openehr.terminology.OpenEhrTerminology;
import care.better.platform.template.AmNode;
import care.better.platform.template.AmUtils;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.WebTemplateConstants;
import org.apache.commons.lang3.StringUtils;
import org.openehr.am.aom.TermBindingItem;
import org.openehr.rm.datatypes.CodePhrase;

/**
 * @author Bostjan Lah
 */
public class CodePhraseUtils {
    private CodePhraseUtils() {
    }

    public static String getOpenEhrTerminologyText(String code, String language) {
        String text = language == null ? null : OpenEhrTerminology.getInstance().getText(language, code);
        return text == null
                ? OpenEhrTerminology.getInstance().getText(WebTemplateConstants.DEFAULT_LANGUAGE, code)
                : text;
    }

    public static String getLocalTerminologyText(String code, AmNode amNode, String language) {
        String text = AmUtils.findText(amNode, language, code);
        return text == null ? AmUtils.findTermText(amNode, code) : text;
    }

    public static WebTemplateCodedValue getCodedValue(String terminology, String code, AmNode amNode, WebTemplateBuilderContext context) {
        WebTemplateCodedValue webTemplateCodedValue;
        if (WebTemplateConstants.TERMINOLOGY_OPENEHR.equals(terminology)) {
            webTemplateCodedValue = new WebTemplateCodedValue(code, getOpenEhrTerminologyText(code, context.getDefaultLanguage()));
        } else if (WebTemplateConstants.TERMINOLOGY_LOCAL.equals(terminology)) {
            webTemplateCodedValue = new WebTemplateCodedValue(code, getLocalTerminologyText(code, amNode, context.getDefaultLanguage()));
        } else {
            webTemplateCodedValue = new WebTemplateCodedValue(code, AmUtils.findTermText(amNode, terminology + "::" + code));
        }
        for (String language : context.getLanguages()) {
            String label;
            if (WebTemplateConstants.TERMINOLOGY_OPENEHR.equals(terminology)) {
                label = OpenEhrTerminology.getInstance().getText(language, code);
            } else {
                label = AmUtils.findText(amNode, language, code);
                if (context.isAddDescriptions()) {
                    String description = AmUtils.findDescription(amNode, language, code);
                    if (description != null) {
                        webTemplateCodedValue.getLocalizedDescriptions().put(language, StringUtils.defaultString(description));
                    }
                }
            }
            webTemplateCodedValue.getLocalizedLabels().put(language, StringUtils.defaultString(label));
        }
        return webTemplateCodedValue;
    }

    public static WebTemplateBindingCodedValue getBindingCodedValue(TermBindingItem termBindingItem) {
        CodePhrase value = termBindingItem.getValue();

        if (value != null && StringUtils.isNoneBlank(value.getCodeString())) {
            String terminologyId = value.getTerminologyId() != null && StringUtils.isNotEmpty(value.getTerminologyId().getValue())
                    ? value.getTerminologyId().getValue()
                    : termBindingItem.getCode();

            return new WebTemplateBindingCodedValue(value.getCodeString(), terminologyId);
        }
        return null;
    }
}
