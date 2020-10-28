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

import care.better.platform.template.AmNode;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import org.openehr.am.aom.CCodeReference;
import org.openehr.rm.datatypes.DvCodedText;

import javax.annotation.Nullable;

/**
 * @author Bostjan Lah
 */
public class CodeReferenceWebTemplateInputBuilder implements WebTemplateInputBuilder<CCodeReference> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CCodeReference codeReference, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.CODED_TEXT);
        if (codeReference != null && codeReference.getReferenceSetUri() != null) {
            WebTemplateValidation validation = new WebTemplateValidation();
            validation.setPattern(codeReference.getReferenceSetUri());
            input.setValidation(validation);
        }
        addDefaultValue(amNode, input);
        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        throw new UnsupportedOperationException("This type not allowed!");
    }

    private void addDefaultValue(AmNode amNode, WebTemplateInput input) {
        DvCodedText defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvCodedText.class);
        if (defaultValue != null) {
            input.setDefaultValue(new WebTemplateCodedValue(defaultValue.getDefiningCode().getCodeString(), defaultValue.getValue()));
        }
    }
}
