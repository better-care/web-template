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

import care.better.openehr.rm.RmObject;
import care.better.openehr.terminology.OpenEhrTerminology;
import care.better.platform.template.AmNode;
import care.better.platform.template.AmUtils;
import care.better.platform.utils.RmUtils;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.RmProperty;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import org.apache.commons.lang3.StringUtils;
import org.openehr.am.aom.CCodePhrase;
import org.openehr.am.aom.CCodeReference;
import org.openehr.am.aom.CString;
import org.openehr.am.aom.TermBindingItem;
import org.openehr.rm.datastructures.IntervalEvent;
import org.openehr.rm.datatypes.CodePhrase;
import org.openehr.rm.datatypes.DvCodedText;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Bostjan Lah
 */
public class CodePhraseWebTemplateInputBuilder implements WebTemplateInputBuilder<CCodePhrase> {
    private static final Map<RmProperty, String> OPENEHR_CONCEPT_GROUPS =
            Collections.singletonMap(new RmProperty(IntervalEvent.class, "math_function"), "14");
    private static final Pattern WILDCARD_VALUE = Pattern.compile("[^:]+:\\*");

    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CCodePhrase codePhrase, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.CODED_TEXT);
        if (codePhrase == null) {
            try {
                buildOpenEhrList(amNode, context, input);
            } catch (ClassNotFoundException ignored) {
            }
        } else {
            String terminology = codePhrase.getTerminologyId() == null ? null : codePhrase.getTerminologyId().getValue();
            List<WebTemplateCodedValue> options = getCodes(amNode, codePhrase, context, terminology);
            if (options.isEmpty()) {
                input.setTerminology(terminology);
            } else {
                input.getList().addAll(options);
                input.setFixed(isFixed(input.getList()));
            }
            addDefaultValue(amNode, codePhrase, input);
        }
        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        List<CCodePhrase> cCodePhrases;
        DvCodedText defaultValue = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvCodedText.class);
        if (node.getAmNode().getCObject() instanceof CCodePhrase) {
            cCodePhrases = Collections.singletonList((CCodePhrase)node.getAmNode().getCObject());
        } else {
            cCodePhrases = AmUtils.getCObjectItems(node.getAmNode(), CCodePhrase.class, "defining_code");
        }
        if (cCodePhrases != null && !cCodePhrases.isEmpty()) {
            CCodePhrase cCodePhrase = cCodePhrases.get(0);
            if (cCodePhrase instanceof CCodeReference) {
                buildCodeReference(node, (CCodeReference)cCodePhrase, defaultValue);
            } else {
                String terminology = cCodePhrase.getTerminologyId() == null ? null : cCodePhrase.getTerminologyId().getValue();
                List<WebTemplateCodedValue> options = getCodes(node.getAmNode(), cCodePhrase, context, terminology);
                CodePhrase assumedValue = cCodePhrase.getAssumedValue();
                if (options.isEmpty()) {
                    addExternalTerminologyInputs(node, terminology, defaultValue, assumedValue);
                } else {
                    WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.CODED_TEXT, WebTemplateConstants.CODE_ATTRIBUTE);
                    input.getList().addAll(options);
                    input.setFixed(isFixed(input.getList()));
                    input.setDefaultValue(assumedValue == null ? null : assumedValue.getCodeString());
                    if (defaultValue != null) {
                        input.setDefaultValue(defaultValue.getDefiningCode().getCodeString());
                    }
                    if (StringUtils.isNotBlank(terminology) && !WebTemplateConstants.TERMINOLOGY_LOCAL.equals(terminology)) {
                        input.setTerminology(terminology);
                    }
                    node.getInputs().add(input);
                }
            }
            if (cCodePhrases.size() > 1) {
                addOtherExternalTerminologies(cCodePhrases.subList(1, cCodePhrases.size()), node.getInputs());
            }
        } else {
            addExternalTerminologyInputs(node, null, defaultValue, null);
        }
        CString cString = AmUtils.getPrimitiveItem(node.getAmNode(), CString.class, "value");
        if (cString != null && Boolean.TRUE.equals(cString.getListOpen())) {
            for (WebTemplateInput webTemplateInput : node.getInputs()) {
                webTemplateInput.setListOpen(true);
            }
        }
    }

    private void addOtherExternalTerminologies(List<CCodePhrase> cCodePhrases, List<WebTemplateInput> inputs) {
        for (CCodePhrase cCodePhrase : cCodePhrases) {
            if (cCodePhrase instanceof CCodeReference) {
                CCodeReference cCodeReference = (CCodeReference)cCodePhrase;
                String referenceSetUri = getReferenceSetUri(cCodeReference);
                if (referenceSetUri != null) {
                    addOtherTerminologies(inputs, referenceSetUri);
                }
            }
        }
    }

    private void addOtherTerminologies(List<WebTemplateInput> inputs, String referenceSetUri) {
        for (WebTemplateInput input : inputs) {
            input.getOtherTerminologies().add(referenceSetUri);
        }
    }

    private void buildCodeReference(WebTemplateNode node, CCodeReference cCodeReference, DvCodedText defaultValue) {
        String referenceSetUri = getReferenceSetUri(cCodeReference);
        if (referenceSetUri != null) {
            addExternalTerminologyInputs(node, referenceSetUri, defaultValue, cCodeReference.getAssumedValue());
        }
    }

    private String getReferenceSetUri(CCodeReference cCodeReference) {
        String referenceSetUri = cCodeReference.getReferenceSetUri();
        return referenceSetUri != null && referenceSetUri.startsWith("terminology:") ? referenceSetUri.substring(12) : referenceSetUri;
    }

    private void addExternalTerminologyInputs(WebTemplateNode node, String terminology, DvCodedText defaultValue, CodePhrase assumedValue) {
        // external terminology
        WebTemplateInput codeInput = new WebTemplateInput(WebTemplateInputType.TEXT, WebTemplateConstants.CODE_ATTRIBUTE);
        codeInput.setTerminology(terminology);
        if (defaultValue != null) {
            codeInput.setDefaultValue(defaultValue.getDefiningCode().getCodeString());
        } else if (assumedValue != null) {
            codeInput.setDefaultValue(assumedValue.getCodeString());
        }
        node.getInputs().add(codeInput);

        WebTemplateInput valueInput = new WebTemplateInput(WebTemplateInputType.TEXT, WebTemplateConstants.VALUE_ATTRIBUTE);
        valueInput.setTerminology(terminology);
        if (defaultValue != null) {
            valueInput.setDefaultValue(defaultValue.getValue());
        }
        node.getInputs().add(valueInput);
    }

    private boolean isFixed(List<WebTemplateCodedValue> values) {
        if (values.size() == 1) {
            WebTemplateCodedValue codedValue = values.get(0);
            if (!WILDCARD_VALUE.matcher(codedValue.getValue()).matches()) {
                return true;
            }
        }
        return false;
    }

    private void addDefaultValue(AmNode amNode, CCodePhrase codePhrase, WebTemplateInput input) {
        WebTemplateCodedValue defaultValue = null;
        if (codePhrase != null && codePhrase.getAssumedValue() != null) {
            for (WebTemplateCodedValue codedValue : input.getList()) {
                if (Objects.equals(codedValue.getValue(), codePhrase.getAssumedValue().getCodeString())) {
                    defaultValue = codedValue;
                    break;
                }
            }
            if (defaultValue == null) {
                defaultValue = new WebTemplateCodedValue(codePhrase.getAssumedValue().getCodeString(), null);
            }
        }

        DvCodedText codedValue = WebTemplateUtils.getDefaultValue(amNode, DvCodedText.class);
        if (codedValue != null) {
            defaultValue = new WebTemplateCodedValue(codedValue.getDefiningCode().getCodeString(), codedValue.getValue());
        }

        input.setDefaultValue(defaultValue);
    }

    private List<WebTemplateCodedValue> getCodes(AmNode amNode, CCodePhrase codePhrase, WebTemplateBuilderContext context, String terminology) {
        ConvertToWebTemplateCodedValueFunction mapper = new ConvertToWebTemplateCodedValueFunction(terminology, amNode, context);
        return codePhrase.getCodeList().stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .collect(Collectors.toList());
    }

    private void buildOpenEhrList(AmNode amNode, WebTemplateBuilderContext context, WebTemplateInput input) throws ClassNotFoundException {
        Class<? extends RmObject> rmClass = RmUtils.getRmClass(amNode.getParent().getRmType());
        String groupId = OPENEHR_CONCEPT_GROUPS.get(new RmProperty(rmClass, amNode.getName()));
        if (groupId != null) {
            for (String termId : OpenEhrTerminology.getInstance().getGroupChildren(groupId)) {
                input.getList().add(new WebTemplateCodedValue(termId, CodePhraseUtils.getOpenEhrTerminologyText(termId, context.getDefaultLanguage())));
            }
            input.setFixed(input.getList().size() == 1);
        }
    }

    private static final class ConvertToWebTemplateCodedValueFunction implements Function<String, WebTemplateCodedValue> {
        private final String terminology;
        private final AmNode amNode;
        private final WebTemplateBuilderContext context;

        private ConvertToWebTemplateCodedValueFunction(String terminology, AmNode amNode, WebTemplateBuilderContext context) {
            this.terminology = terminology;
            this.amNode = amNode;
            this.context = context;
        }

        @Override
        public WebTemplateCodedValue apply(String code) {
            WebTemplateCodedValue codedValue = CodePhraseUtils.getCodedValue(terminology, code, amNode, context);
            Map<String, TermBindingItem> amTermBindings = AmUtils.findTermBindings(amNode, code);
            for (Map.Entry<String, TermBindingItem> termBindingEntry : amTermBindings.entrySet()) {
                WebTemplateBindingCodedValue bindingCodedValue = CodePhraseUtils.getBindingCodedValue(termBindingEntry.getValue());
                if (bindingCodedValue != null) {
                    codedValue.getTermBindings().put(termBindingEntry.getKey(), bindingCodedValue);
                }
            }
            return codedValue;
        }
    }
}
