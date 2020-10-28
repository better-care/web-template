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

package com.marand.thinkehr.web.build;

import care.better.openehr.rm.RmObject;
import care.better.platform.template.AmNode;
import care.better.platform.template.AmUtils;
import care.better.platform.utils.RmUtils;
import care.better.platform.utils.exception.RmClassCastException;
import com.google.common.base.Splitter;
import com.marand.thinkehr.builder.BuilderException;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.input.CodePhraseUtils;
import com.marand.thinkehr.web.build.input.range.WebTemplateDecimalRange;
import org.apache.commons.lang3.StringUtils;
import org.openehr.am.aom.Annotation;
import org.openehr.am.aom.TAttribute;
import org.openehr.am.aom.TComplexObject;
import org.openehr.base.basetypes.TerminologyId;
import org.openehr.base.foundationtypes.IntervalOfReal;
import org.openehr.rm.common.StringDictionaryItem;
import org.openehr.rm.datatypes.CodePhrase;
import org.openehr.rm.datatypes.DataValue;
import org.openehr.rm.datatypes.DvCodedText;
import org.openehr.rm.datatypes.DvText;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bostjan Lah
 */
public class WebTemplateUtils {
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("L10n=\\{(.+?)(?<!\\\\)}");
    private static final Pattern DELIMITER_PATTERN = Pattern.compile("::", Pattern.LITERAL);

    private WebTemplateUtils() {
    }

    public static Float getFixedValue(IntervalOfReal interval) {
        if (interval != null) {
            WebTemplateDecimalRange range = new WebTemplateDecimalRange(interval);
            if (range.isFixed()) {
                return range.getMin();
            }
        }
        return null;
    }

    @SuppressWarnings("MethodWithMultipleLoops")
    public static <T> T getDefaultValue(AmNode amNode, Class<T> clazz) {
        AmNode node = amNode.getConstraints() == null && amNode.getParent() != null ? amNode.getParent() : amNode;
        if (node.getConstraints() != null) {
            for (TAttribute attribute : node.getConstraints()) {
                for (TComplexObject complexObject : attribute.getChildren()) {
                    if (clazz.isInstance(complexObject.getDefaultValue())) {
                        return clazz.cast(complexObject.getDefaultValue());
                    }
                }
            }
        }
        return null;
    }

    public static Class<?> getDataValueClass(AmNode amNode) {
        for (AmNode node : new AmNode[]{amNode, amNode.getParent()}) {
            if (node != null) {
                try {
                    Class<? extends RmObject> rmClass = RmUtils.getRmClass(node.getRmType());
                    if (DataValue.class.isAssignableFrom(rmClass)) {
                        return rmClass;
                    }
                } catch (RmClassCastException ignored) {
                }
            }
        }
        return null;
    }

    public static void buildChain(WebTemplateNode node, WebTemplateNode parent) {
        AmNode amNode = node.getAmNode();
        while (amNode != null) {
            node.getChain().add(0, amNode);
            amNode = amNode.getParent();
            if (amNode.equals(parent.getAmNode())) {
                break;
            }
        }
    }

    public static String getTermText(AmNode amNode, String terminologyId, String codeString, String language) {
        String term = null;
        if (WebTemplateConstants.TERMINOLOGY_OPENEHR.equals(terminologyId)) {
            term = CodePhraseUtils.getOpenEhrTerminologyText(codeString, language);
        } else {
            if (amNode.getTermDefinitions().containsKey(language)) {
                term = AmUtils.findTerm(amNode.getTermDefinitions().get(language), codeString, WebTemplateConstants.TEXT_ID);
            }
            if (term == null) {
                term = AmUtils.findTerm(amNode.getTerms(), codeString, WebTemplateConstants.TEXT_ID);
            }
        }
        return term;
    }

    @SuppressWarnings("MethodWithMultipleLoops")
    public static Map<String, String> getTranslationsFromAnnotations(AmNode amNode) {
        Map<String, String> localizedNamesFromAnnotations = new HashMap<>();
        if (amNode.getAnnotations() != null) {
            for (Annotation annotation : amNode.getAnnotations()) {
                for (StringDictionaryItem item : annotation.getItems()) {
                    if (item.getValue().contains("L10n={")) {
                        parseTranslations(item.getValue(), localizedNamesFromAnnotations);
                    } else if (item.getId().toLowerCase().startsWith("l10n.")) {
                        localizedNamesFromAnnotations.put(item.getId().substring(5), item.getValue());
                    }
                }
            }
        }
        return localizedNamesFromAnnotations;
    }

    public static void parseTranslations(String value, Map<String, String> localizedNamesFromAnnotations) {
        Matcher matcher = LOCALIZATION_PATTERN.matcher(value);
        if (matcher.find()) {
            String translations = matcher.group(1);
            for (String translation : Splitter.on('|').split(translations)) {
                int i = translation.indexOf('=');
                if (i != -1) {
                    String language = translation.substring(0, i).trim();
                    String text = translation.substring(i + 1).trim().replace("\\{", "{").replace("\\}", "}");
                    localizedNamesFromAnnotations.put(language, text);
                }
            }
        }
    }

    public static RmObject createDataValue(AmNode amNode, String subField) {
        try {
            return (RmObject)createDataValue(RmUtils.getRmClass(amNode.getRmType()), subField).orElse(null);
        } catch (RmClassCastException ignored) {
            throw new BuilderException("Unknown type: " + amNode.getRmType());
        }
    }

    public static Optional<Object> createDataValue(Class<?> rmClass, String subField) {
        try {
            Class<?> type;
            if (rmClass.equals(DvText.class) &&
                    (Objects.equals(WebTemplateConstants.TERMINOLOGY_ATTRIBUTE, subField) || Objects.equals(WebTemplateConstants.CODE_ATTRIBUTE, subField))) {
                type = DvCodedText.class;
            } else if (rmClass.equals(DvCodedText.class) && WebTemplateConstants.OTHER_ATTRIBUTE.equals(subField)) {
                type = DvText.class;
            } else {
                type = rmClass;
            }
            if (DataValue.class.isAssignableFrom(type) || CodePhrase.class.isAssignableFrom(type)) {
                return Optional.of(type.getConstructor().newInstance());
            }
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ignored) {
            throw new BuilderException("Unable to create an object of type: " + rmClass.getSimpleName());
        }
        return Optional.empty();
    }

    public static String getChoiceWebTemplateId(String typeName) {
        return typeName.substring(3).toLowerCase() + "_value";
    }

    public static <T extends DataValue> String getChoiceWebTemplateId(Class<T> rmClazz) {
        return getChoiceWebTemplateId(RmUtils.getRmTypeName(rmClazz));
    }

    public static DvText getTextOrCodedText(String textOrCodedText) {
        String[] parts = DELIMITER_PATTERN.split(textOrCodedText);
        String codeString;
        String terminologyString = null;
        String value = null;
        if (parts.length > 2) { // terminology::code::value
            terminologyString = parts[0];
            codeString = parts[1];
            value = parts[2];
        } else if (parts.length > 1) { // code::value
            codeString = parts[0];
            value = parts[1];
        } else {
            codeString = null;
            value = parts[0];
        }

        if (StringUtils.isNotBlank(codeString)) {
            DvCodedText codedText = new DvCodedText();
            codedText.setDefiningCode(new CodePhrase());
            codedText.getDefiningCode().setCodeString(codeString);
            if (StringUtils.isNotBlank(terminologyString)) {
                TerminologyId terminologyId = new TerminologyId();
                terminologyId.setValue(terminologyString);
                codedText.getDefiningCode().setTerminologyId(terminologyId);
            }
            codedText.setValue(value);
            return codedText;
        }

        DvText text = new DvText();
        text.setValue(value);
        return text;
    }
}
