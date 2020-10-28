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
import care.better.openehr.terminology.OpenEhrTerminology;
import care.better.platform.template.AmAttribute;
import care.better.platform.template.AmNode;
import care.better.platform.template.AmTreeBuilder;
import care.better.platform.template.AmUtils;
import care.better.platform.template.type.CollectionInfo;
import care.better.platform.template.type.CollectionType;
import care.better.platform.template.type.TypeInfo;
import care.better.platform.utils.RmUtils;
import care.better.platform.utils.exception.RmClassCastException;
import care.better.platform.web.template.WebTemplate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.WebTemplateCreator;
import com.marand.thinkehr.web.build.input.CareflowStepWebTemplateCodedValue;
import com.marand.thinkehr.web.build.input.CodePhraseUtils;
import com.marand.thinkehr.web.build.input.InputBuilders;
import com.marand.thinkehr.web.build.input.WebTemplateBindingCodedValue;
import com.marand.thinkehr.web.build.input.WebTemplateCodedValue;
import com.marand.thinkehr.web.build.input.WebTemplateInput;
import com.marand.thinkehr.web.build.input.range.WebTemplateIntegerRange;
import com.marand.thinkehr.web.build.postprocess.ActionChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.ChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.CompositionChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.ElementChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.EvaluationChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.EventChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.InstructionChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.ObservationChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.ParsableChildrenPostProcessor;
import com.marand.thinkehr.web.build.postprocess.ProportionChildrenPostProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openehr.am.aom.Annotation;
import org.openehr.am.aom.ArchetypeSlot;
import org.openehr.am.aom.ArchetypeTerm;
import org.openehr.am.aom.CCodePhrase;
import org.openehr.am.aom.TView;
import org.openehr.am.aom.Template;
import org.openehr.am.aom.TermBindingItem;
import org.openehr.base.foundationtypes.IntervalOfInteger;
import org.openehr.rm.common.Locatable;
import org.openehr.rm.common.StringDictionaryItem;
import org.openehr.rm.composition.Action;
import org.openehr.rm.composition.Activity;
import org.openehr.rm.composition.Composition;
import org.openehr.rm.composition.IsmTransition;
import org.openehr.rm.datastructures.Element;
import org.openehr.rm.datastructures.History;
import org.openehr.rm.datatypes.DataValue;
import org.openehr.rm.datatypes.DvCodedText;
import org.openehr.rm.datatypes.DvInterval;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * Builder for building JSON and HTML web templates from OpenEHR templates.
 *
 * @author Bostjan Lah
 */
@SuppressWarnings({"OverlyComplexClass", "OverlyCoupledClass"})
public final class WTBuilder {
    private static final String CURRENT_VERSION = "2.3";
    private static final String ISM_TRANSITION_ATTRIBUTE = "ism_transition";
    private static final String VIEW_ANNOTATION_PREFIX = "view:";
    private static final String DESCRIPTION_ID = "description";

    private static final Set<String> SKIP_PATHS = ImmutableSet.of("name");

    private static final Set<RmProperty> IGNORED_RM_PROPERTIES =
            ImmutableSet.<RmProperty>builder()
                    .add(new RmProperty(Composition.class, "category")) // always constrained in the template otherwise default is OK
                    .add(new RmProperty(History.class, "origin")) // can be calculated from events
                    .add(new RmProperty(DvCodedText.class, "value")) // from code
                    .add(new RmProperty(Locatable.class, "archetype_node_id")) // from template
                    .add(new RmProperty(Locatable.class, "name")) // from template
                    .build();

    private static final Set<RmProperty> OVERRIDE_OPTIONAL =
            ImmutableSet.<RmProperty>builder()
                    .add(new RmProperty(Activity.class, "timing")) // no longer mandatory in RM 1.0.4 (added for backward compatibility)
                    .build();

    private static final WebTemplateIntegerRange EXISTENCE_REQUIRED = new WebTemplateIntegerRange(1, 1);

    private static final Map<String, ChildrenPostProcessor> CHILDREN_POST_PROCESSORS =
            ImmutableMap.<String, ChildrenPostProcessor>builder()
                    .put("ELEMENT", new ElementChildrenPostProcessor())
                    .put("ACTION", new ActionChildrenPostProcessor())
                    .put("EVALUATION", new EvaluationChildrenPostProcessor())
                    .put("INSTRUCTION", new InstructionChildrenPostProcessor())
                    .put("OBSERVATION", new ObservationChildrenPostProcessor())
                    .put("DV_PROPORTION", new ProportionChildrenPostProcessor())
                    .put("DV_PARSABLE", new ParsableChildrenPostProcessor())
                    .put("EVENT", new EventChildrenPostProcessor())
                    .put("POINT_EVENT", new EventChildrenPostProcessor())
                    .put("INTERVAL_EVENT", new EventChildrenPostProcessor())
                    .put("COMPOSITION", new CompositionChildrenPostProcessor())
                    .build();

    private static final Predicate<Map.Entry<String, AmAttribute>> ATTRIBUTE_IS_NOT_RM_ONLY = e -> !e.getValue().getRmOnly();
    private static final String[] ANY_DATA_TYPES = {
            "DV_CODED_TEXT",
            "DV_TEXT",
            "DV_MULTIMEDIA",
            "DV_PARSABLE",
            "DV_STATE",
            "DV_BOOLEAN",
            "DV_IDENTIFIER",
            "DV_URI",
            "DV_EHR_URI",
            "DV_DURATION",
            "DV_QUANTITY",
            "DV_COUNT",
            "DV_PROPORTION",
            "DV_DATE_TIME",
            "DV_DATE",
            "DV_TIME",
            "DV_ORDINAL"};

    private final Deque<WebTemplateNode> segments = new ArrayDeque<>();
    private final WebTemplateBuilderContext context;

    private final WebTemplateCompactor compactor;
    private final WebTemplateIdBuilder idBuilder;
    private final boolean postProcess;
    private final String templateLanguage;

    private WTBuilder(Template template, WebTemplateBuilderContext context) {
        this.context = context;
        templateLanguage = template.getLanguage().getCodeString();
        if (context.getDefaultLanguage() == null) {
            context.setDefaultLanguage(templateLanguage);
        }
        postProcess = true;
        compactor = new MediumWebTemplateCompactor();
        idBuilder = new WebTemplateIdBuilder();
    }

    /**
     * Builds web template.
     *
     * @param template OpenEHR template
     * @param context  builder context
     * @return web template
     */
    public static WebTemplate build(Template template, WebTemplateBuilderContext context) {
        WTBuilder builder = new WTBuilder(template, context);
        return builder.build(new AmTreeBuilder(template).build(), null, template.getTemplateId().getValue());
    }

    /**
     * Builds web template. An additional starting RM path can be specified in which case web template will be built only for
     * the portion of the template under the path.
     *
     * @param template OpenEHR template
     * @param from     start at a specified RM path
     * @param context  builder context
     * @return web template
     */
    public static WebTemplate build(Template template, String from, WebTemplateBuilderContext context) {
        WTBuilder builder = new WTBuilder(template, context);
        return builder.build(new AmTreeBuilder(template).build(), from, template.getTemplateId().getValue());
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private WebTemplate build(AmNode root, String from, String templateId) {
        AmNode node = from == null ? root : AmUtils.resolvePath(root, from);
        Multimap<AmNode, WebTemplateNode> nodes = ArrayListMultimap.create();
        if (node != null) {
            WebTemplateNode wtNode = buildNode(null, node);
            compactor.compact(wtNode);
            idBuilder.buildIds(wtNode, nodes);

            return WebTemplateCreator.create(wtNode, templateId, context.getDefaultLanguage(), context.getLanguages(), nodes, CURRENT_VERSION);
        }
        return null;
    }

    private WebTemplateNode buildNode(String attributeName, AmNode amNode) {
        WebTemplateNode node = createNode(attributeName, amNode);

        buildNodeChildren(amNode, node);

        return node;
    }

    private void buildNodeChildren(AmNode amNode, WebTemplateNode node) {
        segments.push(node);

        List<WebTemplateNode> children = createChildren(amNode, node);
        if (children.isEmpty() && "ELEMENT".equals(node.getRmType())) { // element with any
            children = addAllDataValueChildren(amNode);
        }
        if (children.isEmpty()) {
            addInputs(node);
        }
        if (!node.hasInput()) {
            addRequiredRmAttributes(amNode, children);
        }
        segments.pop();

        processChildren(node, children);

        List<WebTemplateCardinality> cardinalities = getCardinalities(amNode, node);
        if (!cardinalities.isEmpty()) {
            node.setCardinalities(cardinalities);
        }
    }

    private List<WebTemplateNode> addAllDataValueChildren(AmNode amNode) {
        List<WebTemplateNode> children = new ArrayList<>();
        List<AmNode> amChildren = new ArrayList<>();
        for (String type : ANY_DATA_TYPES) {
            AmNode childNode = createAmNode(amNode, type, "value", Element.class);
            amChildren.add(childNode);
            WebTemplateNode customNode = createCustomNode(childNode, amNode.getName(), EXISTENCE_REQUIRED);
            customNode.setJsonId(WebTemplateUtils.getChoiceWebTemplateId(type.substring(3).toLowerCase()));
            children.add(customNode);
        }
        IntervalOfInteger intervalOfInteger = AmUtils.createInterval(1, 1);
        AmAttribute attribute = new AmAttribute(intervalOfInteger, amChildren);
        attribute.setRmOnly(true);

        amNode.getAttributes().put("value", attribute);
        return children;
    }

    private List<WebTemplateNode> createChildren(AmNode amNode, WebTemplateNode node) {
        Class<?> rmType = getRmType(node);
        List<WebTemplateNode> children = new ArrayList<>();
        if (addAttributes(rmType)) {
            amNode.getAttributes().entrySet().stream().filter(ATTRIBUTE_IS_NOT_RM_ONLY).forEach(e -> {
                if ("ACTION".equals(amNode.getRmType()) && ISM_TRANSITION_ATTRIBUTE.equals(e.getKey())) {
                    addIsmTransition(children, amNode);
                } else {
                    addChildren(children, e.getKey(), e.getValue());
                }
            });
            addSpecialAttributes(amNode, children);
        }
        return children;
    }

    private Class<?> getRmType(WebTemplateNode node) {
        Class<?> rmType = null;
        try {
            rmType = RmUtils.getRmClass(node.getRmType());
        } catch (RmClassCastException ignored) {
        }
        return rmType;
    }

    private void addSpecialAttributes(AmNode amNode, List<WebTemplateNode> children) {
        if ("INSTRUCTION".equals(amNode.getRmType())) {
            addChildren(children, "expiry_time", amNode.getAttributes().get("expiry_time"));
        }
    }

    private boolean addAttributes(Class<?> rmType) {
        return rmType == null || DvInterval.class.isAssignableFrom(rmType) || !DataValue.class.isAssignableFrom(rmType);
    }

    private WebTemplateNode createNode(String attributeName, AmNode amNode) {
        WebTemplateNode node = new WebTemplateNode();
        node.setPath(getPath(attributeName, amNode));
        node.setName(amNode.getName());
        if (StringUtils.isNotBlank(amNode.getNodeId())) {
            setLocalizedNames(amNode, node);
        } else if (amNode.getParent() != null && "ELEMENT".equals(amNode.getParent().getRmType())) {
            setLocalizedNames(amNode.getParent(), node);
        }
        node.setRmType(amNode.getRmType());
        node.setNodeId(amNode.getArchetypeNodeId());
        node.setOccurences(new WebTemplateIntegerRange(amNode.getOccurrences()));
        node.setAmNode(amNode);
        if (amNode.getAnnotations() != null) {
            for (Annotation annotation : amNode.getAnnotations()) {
                setNodeAnnotations(node, annotation);
            }
        }
        setArchetypeAnnotations(amNode, node);
        if (amNode.getViewConstraints() != null) {
            setNodeViewAnnotations(node, amNode.getViewConstraints());
        }

        setTermBindings(node);

        return node;
    }

    private void setNodeAnnotations(WebTemplateNode node, Annotation annotation) {
        for (StringDictionaryItem item : annotation.getItems()) {
            node.getAnnotations().put(item.getId(), item.getValue());
        }
    }

    private void setLocalizedNames(AmNode amNode, WebTemplateNode node) {
        String name = StringUtils.defaultString(node.getName(), amNode.getName());
        if (StringUtils.isNotBlank(context.getDefaultLanguage())) {
            if (AmUtils.isNameConstrained(amNode)) {
                if (isConstrainedNameTranslated(amNode, amNode.getNodeId())) {
                    node.setLocalizedName(AmUtils.findText(amNode, context.getDefaultLanguage(), amNode.getNodeId()));
                } else {
                    node.setLocalizedName(name);
                }
            } else {
                node.setLocalizedName(StringUtils.defaultString(AmUtils.findText(amNode, context.getDefaultLanguage(), amNode.getNodeId()), name));
            }
        }
        addLocalizedNames(amNode, amNode.getNodeId(), node, false);
    }

    private void setTermBindings(WebTemplateNode node) {
        AmNode amNode = node.getAmNode();
        String nodeId = amNode.getNodeId();

        if (isNotEmpty(nodeId)) {
            Map<String, TermBindingItem> amTermBindings = AmUtils.findTermBindings(amNode, nodeId);

            for (Map.Entry<String, TermBindingItem> termBindingEntry : amTermBindings.entrySet()) {
                WebTemplateBindingCodedValue codedValue = CodePhraseUtils.getBindingCodedValue(termBindingEntry.getValue());
                if (codedValue != null) {
                    node.getTermBindings().put(termBindingEntry.getKey(), codedValue);
                }
            }
        }
    }

    private void addInputs(WebTemplateNode node) {
        try {
            Class<? extends RmObject> rmClass = RmUtils.getRmClass(node.getRmType());
            if (InputBuilders.isRmTypeWithInputs(rmClass)) {
                InputBuilders.buildByType(node, context);
            }
        } catch (RmClassCastException ignored) {
            InputBuilders.buildByType(node, context);
        }
    }

    private void addRequiredRmAttributes(AmNode parent, List<WebTemplateNode> children) {
        try {
            Class<? extends RmObject> rmClass = RmUtils.getRmClass(parent.getRmType());
            parent.getAttributes().entrySet().stream()
                    .filter(entry -> isRequiredAttribute(rmClass, entry))
                    .forEach(entry -> {
                        String attributeName = entry.getKey();
                        AmNode amNode = entry.getValue().getChildren().get(0);
                        if (children.stream().noneMatch(child -> child.getAmNode().equals(amNode))) {
                            children.add(createCustomNode(amNode, attributeName, EXISTENCE_REQUIRED));
                        }
                    });
        } catch (RmClassCastException ignored) {
        }
    }

    @SuppressWarnings("OverlyComplexBooleanExpression")
    private boolean isRequiredAttribute(Class<? extends RmObject> rmClass, Map.Entry<String, AmAttribute> entry) {
        return OVERRIDE_OPTIONAL.contains(new RmProperty(rmClass, entry.getKey())) ||
                (entry.getValue().getRmOnly() && entry.getValue().getExistence().getLower() == 1 && !isIgnored(entry, rmClass));
    }

    private WebTemplateNode createCustomNode(AmNode amNode, String attributeName, WebTemplateIntegerRange existence) {
        WebTemplateNode node = buildNode(attributeName, amNode);
        node.setName(attributeName.substring(0, 1).toUpperCase() + attributeName.substring(1));
        node.setInContext(true);
        node.setOccurences(existence);
        setTermBindings(node);
        return node;
    }

    private boolean isIgnored(Map.Entry<String, AmAttribute> entry, Class<? extends RmObject> rmClass) {
        for (Class<?> clazz = rmClass; clazz != null && RmObject.class.isAssignableFrom(clazz); clazz = clazz.getSuperclass()) {
            if (IGNORED_RM_PROPERTIES.contains(new RmProperty(clazz, entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    private void addChildren(List<WebTemplateNode> children, String attributeName, AmAttribute attribute) {
        for (AmNode child : attribute.getChildren()) {
            if (!(child.getCObject() instanceof ArchetypeSlot) &&
                    !SKIP_PATHS.contains(attributeName) &&
                    child.isPropertyOnParent()) {
                List<String> nameCodes = getMultipleNames(child);
                if (nameCodes.isEmpty()) {
                    WebTemplateNode wtNode = buildNode(attributeName, child);
                    if (wtNode != null) {
                        children.add(wtNode);
                        //noinspection OverlyBroadCatchBlock
                        try {
                            if (IGNORED_RM_PROPERTIES.contains(new RmProperty(RmUtils.getRmClass(child.getParent().getRmType()), attributeName))) {
                                wtNode.setInContext(true);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } else {
                    addChildrenByNames(children, attributeName, child, nameCodes);
                }
            }
        }
    }

    private void addChildrenByNames(List<WebTemplateNode> children, String attributeName, AmNode child, List<String> nameCodes) {
        for (String nameCode : nameCodes) {
            addChildByName(children, attributeName, child, nameCode);
        }
    }

    private void addChildByName(List<WebTemplateNode> children, String attributeName, AmNode child, String nameCode) {
        WebTemplateNode wtNode = createNode(attributeName, child);
        if (wtNode != null) {
            wtNode.setName(AmUtils.findTermText(child, nameCode));
            if (StringUtils.isNotBlank(context.getDefaultLanguage())) {
                wtNode.setLocalizedName(AmUtils.findText(child, context.getDefaultLanguage(), nameCode));
            }
            wtNode.setNameCodeString(nameCode);
            wtNode.setPath(getPath(attributeName, child, wtNode.getName()));
            addLocalizedNames(child, nameCode, wtNode, true);
            children.add(wtNode);
        }
        buildNodeChildren(child, wtNode);
    }

    private void addLocalizedNames(AmNode amNode, String nameCode, WebTemplateNode wtNode, boolean useConstrainedCode) {
        if (AmUtils.isNameConstrained(amNode) && !useConstrainedCode) {
            if (isConstrainedNameTranslated(amNode, nameCode)) {
                // translations in ADL designer
                context.getLanguages().stream()
                        .map(lang -> Pair.of(lang, AmUtils.findText(amNode, lang, nameCode)))
                        .filter(pair -> StringUtils.isNotBlank(pair.getRight()))
                        .forEach(pair -> wtNode.getLocalizedNames().put(pair.getLeft(), pair.getRight()));
            } else if (StringUtils.isNotBlank(context.getDefaultLanguage())) {
                wtNode.getLocalizedNames().put(context.getDefaultLanguage(), wtNode.getName());
            }

            context.getLanguages()
                    .forEach(lang -> addLocalizedDescription(amNode, nameCode, wtNode, lang));
        } else {
            addLocalizedNamesByCode(amNode, nameCode, wtNode);
        }
        addLocalizedNamesFromAnnotations(wtNode, WebTemplateUtils.getTranslationsFromAnnotations(amNode));
    }

    private boolean isConstrainedNameTranslated(AmNode amNode, String nameCode) {
        return Objects.equals(amNode.getName(), AmUtils.findText(amNode, templateLanguage, nameCode));
    }

    private void addLocalizedNamesByCode(AmNode amNode, String nameCode, WebTemplateNode wtNode) {
        for (String language : context.getLanguages()) {
            String localizedName = AmUtils.findText(amNode, language, nameCode);
            wtNode.getLocalizedNames().put(language, StringUtils.defaultString(localizedName));
            addLocalizedDescription(amNode, nameCode, wtNode, language);
        }
    }

    private void addLocalizedNamesFromAnnotations(WebTemplateNode wtNode, Map<String, String> localizedNamesFromAnnotations) {
        localizedNamesFromAnnotations.entrySet().stream()
                .filter(e -> StringUtils.isNotBlank(e.getValue()))
                .filter(e -> context.getLanguages().contains(e.getKey()))
                .forEach(e -> wtNode.getLocalizedNames().put(e.getKey(), e.getValue()));
    }

    private void addLocalizedDescription(AmNode amNode, String nameCode, WebTemplateNode wtNode, String language) {
        if (context.isAddDescriptions()) {
            String description = AmUtils.findDescription(amNode, language, nameCode);
            if (description != null) {
                wtNode.getLocalizedDescriptions().put(language, description);
            }
        }
    }

    private List<String> getMultipleNames(AmNode child) {
        CCodePhrase nameCodePhrase = AmUtils.getNameCodePhrase(child);
        return nameCodePhrase == null ? Collections.emptyList() : nameCodePhrase.getCodeList();
    }


    private void addIsmTransition(List<WebTemplateNode> children, AmNode amNode) {
        AmNode ismTransition = new AmNode(amNode, "ISM_TRANSITION");
        ismTransition.setName(ISM_TRANSITION_ATTRIBUTE);
        Method getter = RmUtils.getGetterForAttribute(ISM_TRANSITION_ATTRIBUTE, Action.class);
        ismTransition.setGetter(getter);
        ismTransition.setSetter(RmUtils.getSetterForAttribute(ISM_TRANSITION_ATTRIBUTE, Action.class));
        ismTransition.setType(new TypeInfo(getter.getReturnType(), null));

        AmNode currentState = createIsmTransitionAttribute(ismTransition, "current_state", AmUtils.createInterval(1, 1));
        AmNode transition = createIsmTransitionAttribute(ismTransition, "transition", AmUtils.createInterval(0, 1));
        AmNode careflowStep = createIsmTransitionAttribute(ismTransition, "careflow_step", AmUtils.createInterval(0, 1));

        WebTemplateNode webTemplateNode = createCustomNode(ismTransition, ISM_TRANSITION_ATTRIBUTE, EXISTENCE_REQUIRED);
        segments.push(webTemplateNode);

        webTemplateNode.getChildren().clear();

        Set<String> allowedCurrentStates = new HashSet<>();
        WebTemplateNode careflowStepWtNode = createCareFlowStepNode(amNode, careflowStep, allowedCurrentStates);

        webTemplateNode.getChildren().add(createCurrentStateInput(currentState,
                                                                  allowedCurrentStates.isEmpty()
                                                                  ? OpenEhrTerminology.getInstance().getGroupChildren("21")
                                                                  : allowedCurrentStates));
        webTemplateNode.getChildren().add(createCustomNode(transition, "transition", new WebTemplateIntegerRange(0, 1)));
        webTemplateNode.getChildren().add(careflowStepWtNode);

        children.add(webTemplateNode);

        segments.pop();
    }

    private WebTemplateNode createCareFlowStepNode(AmNode amNode, AmNode careflowStep, Set<String> allowedCurrentStates) {
        WebTemplateNode careflowStepWtNode = createCustomNode(careflowStep, "careflow_step", new WebTemplateIntegerRange(0, 1));

        AmAttribute ismTransitionAttribute = amNode.getAttributes().get("ism_transition");
        if (ismTransitionAttribute != null) {
            WebTemplateInput transitionInput = new WebTemplateInput(WebTemplateInputType.CODED_TEXT, WebTemplateConstants.CODE_ATTRIBUTE);
            List<CareflowStepWebTemplateCodedValue> options = ismTransitionAttribute.getChildren().stream()
                    .filter(ismTransitionNode -> StringUtils.isNotBlank(ismTransitionNode.getNodeId()))
                    .map(ismTransitionNode -> {
                        WebTemplateCodedValue codedValue = CodePhraseUtils.getCodedValue(WebTemplateConstants.TERMINOLOGY_LOCAL, ismTransitionNode.getNodeId(), amNode,
                                                                                         context);
                        CCodePhrase currentStateCCodePhrase = AmUtils.getCObjectItem(ismTransitionNode, CCodePhrase.class, "current_state",
                                                                                     "defining_code");
                        CareflowStepWebTemplateCodedValue careflowStepWebTemplateCodedValue = new CareflowStepWebTemplateCodedValue(
                                codedValue,
                                currentStateCCodePhrase != null && currentStateCCodePhrase.getCodeList() != null ? currentStateCCodePhrase.getCodeList() : Collections.emptyList());
                        allowedCurrentStates.addAll(careflowStepWebTemplateCodedValue.getCurrentStates());
                        return careflowStepWebTemplateCodedValue;
                    })
                    .collect(Collectors.toList());
            transitionInput.getList().addAll(options);
            careflowStepWtNode.getInputs().clear();
            careflowStepWtNode.getInputs().add(transitionInput);
        }
        return careflowStepWtNode;
    }

    private WebTemplateNode createCurrentStateInput(AmNode currentState, Collection<String> currentStates) {
        WebTemplateInput currentStateInput = new WebTemplateInput(WebTemplateInputType.CODED_TEXT, WebTemplateConstants.CODE_ATTRIBUTE);
        WebTemplateNode currentStateWtNode = createCustomNode(currentState, "current_state", new WebTemplateIntegerRange(1, 1));
        List<WebTemplateCodedValue> currentStateOptions = currentStates.stream()
                .map(code -> new WebTemplateCodedValue(code, CodePhraseUtils.getOpenEhrTerminologyText(code, WebTemplateConstants.DEFAULT_LANGUAGE)))
                .collect(Collectors.toList());
        currentStateInput.getList().addAll(currentStateOptions);
        currentStateWtNode.getInputs().clear();
        currentStateWtNode.getInputs().add(currentStateInput);

        return currentStateWtNode;
    }

    private String getPath(String attributeName, AmNode amNode) {
        if (attributeName == null) {
            return "";
        } else {
            String path = segments.isEmpty() ? "" : segments.peek().getPath();
            return path + '/' + attributeName + getArchetypePredicate(amNode, AmUtils.isNameConstrained(amNode) ? amNode.getName() : null);
        }
    }

    private String getPath(String attributeName, AmNode amNode, String customName) {
        String path = segments.isEmpty() ? "" : segments.peek().getPath();
        return path + '/' + attributeName + getArchetypePredicate(amNode, customName);
    }

    private String getArchetypePredicate(AmNode amNode, String nameConstraint) {
        String predicate;
        if (StringUtils.isBlank(amNode.getArchetypeNodeId())) {
            predicate = "";
        } else {
            predicate = '[' + amNode.getArchetypeNodeId();
            if (nameConstraint != null) {
                predicate += ",'" + nameConstraint + "']";
            } else {
                predicate += ']';
            }
        }
        return predicate;
    }

    private AmNode createIsmTransitionAttribute(AmNode parent, String attributeName, IntervalOfInteger existence) {
        AmNode childNode = createAmNode(parent, "DV_CODED_TEXT", attributeName, IsmTransition.class);

        AmAttribute attribute = new AmAttribute(existence, Lists.newArrayList(childNode));
        attribute.setRmOnly(true);

        parent.getAttributes().put(attributeName, attribute);
        return childNode;
    }

    private AmNode createAmNode(AmNode parent, String rmType, String attributeName, Class<?> parentClass) {
        AmNode childNode = new AmNode(parent, rmType);
        childNode.setName(attributeName);
        childNode.setSetter(RmUtils.getSetterForAttribute(attributeName, (Class<? extends RmObject>)parentClass));

        Method getter =  RmUtils.getGetterForAttribute(attributeName, (Class<? extends RmObject>)parentClass);
        childNode.setGetter(getter);

        if (getter != null) {
            Class<?> returnType = getter.getReturnType();
            if (Collection.class.isAssignableFrom(returnType)){
                Class<?> fieldType = RmUtils.getFieldType((Class<? extends RmObject>)parentClass, RmUtils.getFieldForAttribute(attributeName));

                if (List.class.isAssignableFrom(returnType)){
                    childNode.setType(new TypeInfo(fieldType, new CollectionInfo(CollectionType.LIST)));
                }else  {
                    childNode.setType(new TypeInfo(fieldType, new CollectionInfo(CollectionType.SET)));
                }
            }else {
                childNode.setType(new TypeInfo(returnType, null));
            }
        }
        return childNode;
    }

    private boolean requiresCardinality(AmAttribute amAttribute) {
        WebTemplateIntegerRange range = new WebTemplateIntegerRange(amAttribute.getCardinality().getInterval());
        boolean requires;
        if (range.isEmpty()) {
            requires = false;
        } else {
            Integer min = range.getMin();
            Integer max = range.getMax();
            if (min == null || min == 0) { // no lower limit
                requires = false;
            } else {
                int childrenCount = amAttribute.getChildren().size();
                if (bothOne(min, max) && childrenCount == 1) { // only one child - required
                    requires = false;
                } else {
                    requires = min > 1 || (max != null && max < childrenCount);
                }
            }
        }
        return requires;
    }

    private List<WebTemplateCardinality> getCardinalities(AmNode amNode, WebTemplateNode node) {
        return amNode.getAttributes().entrySet().stream()
                .filter(entry -> entry.getValue().getCardinality() != null && requiresCardinality(entry.getValue()))
                .map(entry -> {
                    WebTemplateCardinality webTemplateCardinality = new WebTemplateCardinality(new WebTemplateIntegerRange(entry.getValue().getCardinality().getInterval()));
                    webTemplateCardinality.setPath(node.getPath() + '/' + entry.getKey());
                    return webTemplateCardinality;
                })
                .collect(Collectors.toList());
    }

    private void setArchetypeAnnotations(AmNode amNode, WebTemplateNode node) {
        // special annotation from Archetype (extra value on term)
        ArchetypeTerm archetypeTerm = AmUtils.findTerm(amNode.getTerms(), amNode.getNodeId());
        if (archetypeTerm != null) {
            for (StringDictionaryItem dictionaryItem : archetypeTerm.getItems()) {
                if (!AmUtils.TEXT_ID.equals(dictionaryItem.getId()) && !DESCRIPTION_ID.equals(dictionaryItem.getId())) {
                    node.getAnnotations().merge(dictionaryItem.getId(), dictionaryItem.getValue(), (a, b) -> a + ';' + b);
                }
            }
        }
    }

    private void setNodeViewAnnotations(WebTemplateNode node, List<TView.Constraints.Items> viewAnnotations) {
        for (TView.Constraints.Items item : viewAnnotations) {
            Object value = item.getValue();
            if (value instanceof org.w3c.dom.Element) {
                org.w3c.dom.Element element = (org.w3c.dom.Element)value;
                String textContent = element.getTextContent();
                node.getAnnotations().put(VIEW_ANNOTATION_PREFIX + item.getId(), textContent);
            } else {
                node.getAnnotations().put(VIEW_ANNOTATION_PREFIX + item.getId(), value.toString());
            }
        }
    }

    private void processChildren(WebTemplateNode node, List<WebTemplateNode> children) {
        if (!children.isEmpty()) {
            node.getChildren().clear();
            node.getChildren().addAll(children);

            if (postProcess) {
                ChildrenPostProcessor childrenPostProcessor = CHILDREN_POST_PROCESSORS.get(node.getRmType());
                if (childrenPostProcessor != null) {
                    childrenPostProcessor.postProcess(node);
                }
            }
        }
    }

    private boolean bothOne(Integer min, Integer max) {
        return max != null && min == 1 && max == 1;
    }
}
