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
import org.openehr.am.aom.CDvOrdinal;
import org.openehr.am.aom.CObject;
import org.openehr.rm.datatypes.CodePhrase;
import org.openehr.rm.datatypes.DvOrdinal;

import javax.annotation.Nullable;

/**
 * @author Bostjan Lah
 */
public class OrdinalWebTemplateInputBuilder implements WebTemplateInputBuilder<CDvOrdinal> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CDvOrdinal dvOrdinal, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.CODED_TEXT);
        if (dvOrdinal != null && !dvOrdinal.getList().isEmpty()) {
            for (DvOrdinal ordinal : dvOrdinal.getList()) {
                CodePhrase definingCode = ordinal.getSymbol().getDefiningCode();
                WebTemplateOrdinalCodedValue codedValue = new WebTemplateOrdinalCodedValue(CodePhraseUtils.getCodedValue(
                        definingCode.getTerminologyId().getValue(),
                        definingCode.getCodeString(),
                        amNode,
                        context));
                codedValue.setOrdinal(ordinal.getValue());
                input.getList().add(codedValue);
            }
            input.setFixed(input.getList().size() == 1);
            if (dvOrdinal.getAssumedValue() != null) {
                input.setDefaultValue(dvOrdinal.getAssumedValue().getSymbol().getDefiningCode().getCodeString());
            }
            DvOrdinal defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvOrdinal.class);
            if (defaultValue != null) {
                input.setDefaultValue(defaultValue.getSymbol().getDefiningCode().getCodeString());
            }
        }
        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CObject cObject = node.getAmNode().getCObject();
        if (cObject instanceof CDvOrdinal) {
            node.getInputs().add(build(node.getAmNode(), (CDvOrdinal)cObject, context));
        } else {
            node.getInputs().add(build(node.getAmNode(), null, context));
        }
    }
}
