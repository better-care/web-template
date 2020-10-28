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

import care.better.platform.utils.RmUtils;
import care.better.platform.utils.exception.RmClassCastException;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.input.WebTemplateInput;
import com.marand.thinkehr.web.build.input.range.WebTemplateIntegerRange;
import org.apache.commons.lang3.StringUtils;
import org.openehr.rm.datatypes.DataValue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
public class MinimalWebTemplateCompactor implements WebTemplateCompactor {
    private final Deque<WebTemplateNode> segments = new ArrayDeque<>();

    private static final Set<String> SKIP_IF_EMPTY =
            ImmutableSet.<String>builder()
                    .add("LIST")
                    .add("CLUSTER")
                    .add("ELEMENT")
                    .add("ITEM_TREE")
                    .add("ITEM_LIST")
                    .add("ITEM_SINGLE")
                    .add("ITEM_TABLE")
                    .add("ITEM_STRUCTURE")
                    .add("HISTORY")
                    .add("POINT_EVENT")
                    .add("INTERVAL_EVENT")
                    .add("EVENT")
                    .add("ITEM")
                    .build();

    @Override
    public WebTemplateNode compact(WebTemplateNode node) {
        segments.push(node);
        WebTemplateNode compactedNode = compactNode(node);
        segments.pop();
        return compactedNode;
    }

    protected WebTemplateNode compactNode(WebTemplateNode node) {
        ListIterator<WebTemplateNode> iterator = node.getChildren().listIterator();
        while (iterator.hasNext()) {
            WebTemplateNode compactedNode = compact(iterator.next());
            if (compactedNode == null) {
                iterator.remove();
            } else {
                iterator.set(compactedNode);
            }
        }

        return processChildren(node);
    }

    @SuppressWarnings("OverlyComplexBooleanExpression")
    private WebTemplateNode processChildren(WebTemplateNode node) {
        compactChildren(node.getChildren());

        WebTemplateNode compactedNode;
        //noinspection OverlyComplexBooleanExpression
        if (!node.hasInput() && node.getChildren().size() == 1 && segments.size() > 1 && isSkippable(node)) {
            if (node.getCardinalities() != null) {
                copyCardinalities(node.getCardinalities(), Iterables.get(segments, 2));
            }
            WebTemplateNode childNode = node.getChildren().get(0);
            // make sure ELEMENT.value is 1..? if it is the only child in the ELEMENT
            if (Objects.equals("ELEMENT", node.getRmType()) &&
                    (childNode.getOccurences().getMin() == null || childNode.getOccurences().getMin() == 0)) {
                childNode.getOccurences().setMin(1);
            }
            copyValues(node, childNode);
            compactedNode = childNode;
        } else if (node.getChildren().isEmpty() && SKIP_IF_EMPTY.contains(node.getRmType())) {
            compactedNode = null;
        } else {
            compactedNode = node;
        }
        return compactedNode;
    }

    protected boolean isSkippable(WebTemplateNode node) {
        try {
            return DataValue.class.isAssignableFrom(RmUtils.getRmClass(node.getRmType()));
        } catch (RmClassCastException ignored) {
        }
        return false;
    }

    private void copyValues(WebTemplateNode from, WebTemplateNode to) {
        to.setName(from.getName());
        to.setLocalizedName(from.getLocalizedName());
        to.getLocalizedNames().clear();
        to.getLocalizedNames().putAll(from.getLocalizedNames());
        to.getLocalizedDescriptions().putAll(from.getLocalizedDescriptions());
        to.setNodeId(from.getNodeId());
        to.setNameCodeString(from.getNameCodeString());

        try {
            if (!DataValue.class.isAssignableFrom(RmUtils.getRmClass(to.getRmType()))) {
                to.setRmType(from.getRmType());
            }
        } catch (RmClassCastException ignored) {
            to.setRmType(from.getRmType());
        }

        WebTemplateIntegerRange fromOccurences = from.getOccurences();
        WebTemplateIntegerRange toOccurences = to.getOccurences();
        if (toOccurences.getMin() == null || fromMinGtTo(fromOccurences, toOccurences) || fromElementToDataValue(from, to)) {
            toOccurences.setMin(fromOccurences.getMin());
        }
        if (fromOccurences.getMax() == null || fromMaxLtTo(fromOccurences, toOccurences)) {
            toOccurences.setMax(fromOccurences.getMax());
        }
        if (from.getDependsOn() != null) {
            copyDependsOn(from, to);
        }
        if (from.hasInput() && !to.hasInput()) {
            to.getInputs().clear();
            to.getInputs().addAll(from.getInputs());
        }
        to.getAnnotations().putAll(from.getAnnotations());
        to.getTermBindings().putAll(from.getTermBindings());
    }

    private boolean fromElementToDataValue(WebTemplateNode from, WebTemplateNode to) {
        // ELEMENT.value is always 1..1, so copy from ELEMENT occurences
        return to.getRmType().startsWith("DV_") && Objects.equals(to.getOccurences().getMin(), 1);
    }

    private boolean fromMaxLtTo(WebTemplateIntegerRange fromOccurences, WebTemplateIntegerRange toOccurences) {
        return toOccurences.getMax() != null && toOccurences.getMax() < fromOccurences.getMax();
    }

    private boolean fromMinGtTo(WebTemplateIntegerRange fromOccurences, WebTemplateIntegerRange toOccurences) {
        return fromOccurences.getMin() != null && toOccurences.getMin() > fromOccurences.getMin();
    }

    protected void copyDependsOn(WebTemplateNode from, WebTemplateNode to) {
        if (to.getDependsOn() == null) {
            to.setDependsOn(from.getDependsOn());
        } else {
            to.getDependsOn().addAll(from.getDependsOn());
        }
    }

    private void compactChildren(List<WebTemplateNode> currentChildren) {
        compactCodedTextWithOther(currentChildren);
        compactMultipleCodedTexts(currentChildren);
    }

    protected void copyCardinalities(List<WebTemplateCardinality> cardinalities, WebTemplateNode node) {
        if (node.getCardinalities() == null) {
            node.setCardinalities(cardinalities);
        } else {
            node.getCardinalities().addAll(cardinalities);
        }
    }

    private void compactCodedTextWithOther(List<WebTemplateNode> children) {
        if (children.size() == 2) {
            WebTemplateNode node0 = children.get(0);
            WebTemplateNode node1 = children.get(1);
            if (ImmutableSet.of("DV_TEXT", "DV_CODED_TEXT").equals(ImmutableSet.of(node0.getRmType(), node1.getRmType()))) {
                String difference = StringUtils.difference(node0.getPath(), node1.getPath());
                if ("/defining_code".equals(difference)) {
                    node1.getInputs().add(new WebTemplateInput(WebTemplateInputType.TEXT, WebTemplateConstants.OTHER_ATTRIBUTE));
                    node1.getInput().setListOpen(true);
                    children.remove(0);
                } else if (difference.isEmpty()) {
                    node0.getInputs().add(new WebTemplateInput(WebTemplateInputType.TEXT, WebTemplateConstants.OTHER_ATTRIBUTE));
                    node0.getInput().setListOpen(true);
                    children.remove(1);
                }
            }
        }
    }

    private void compactMultipleCodedTexts(List<WebTemplateNode> children) {
        Map<String, List<WebTemplateNode>> matchingChildren = mergeChildrenWithMatchingPaths(children);
        for (List<WebTemplateNode> webTemplateNodes : matchingChildren.values()) {
            // are all for defining_code
            if (webTemplateNodes.size() == 2 && webTemplateNodes.get(0).getPath().endsWith("defining_code")) {
                WebTemplateNode child0 = webTemplateNodes.get(0);
                boolean constrained0 = isConstrained(child0);
                WebTemplateNode child1 = webTemplateNodes.get(1);
                boolean constrained1 = isConstrained(child1);
                if (constrained0 && !constrained1) {
                    children.remove(child1);
                } else if (constrained1 && !constrained0) {
                    children.remove(child0);
                } else {
                    WebTemplateInput input = mergeInputs(child0.getInput(), child1.getInput());
                    if (input != null) {
                        child0.setInput(input);
                        children.remove(child1);
                    }
                }
            }
        }
    }

    private boolean isConstrained(WebTemplateNode child) {
        return child.hasInput() && child.getInput().getValidation() != null;
    }

    private Map<String, List<WebTemplateNode>> mergeChildrenWithMatchingPaths(List<WebTemplateNode> children) {
        Map<String, List<WebTemplateNode>> matchingChildren = new HashMap<>();
        for (WebTemplateNode child : children) {
            String rmPath = child.getPath();
            if (!matchingChildren.containsKey(rmPath)) {
                matchingChildren.put(rmPath, new ArrayList<>());
            }
            matchingChildren.get(rmPath).add(child);
        }
        return matchingChildren;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    private WebTemplateInput mergeInputs(WebTemplateInput input1, WebTemplateInput input2) {
        WebTemplateInput input = new WebTemplateInput(input1.getType());
        if (input1.getList().isEmpty()) {
            input.getList().addAll(input2.getList());
        } else if (input2.getList().isEmpty()) {
            input.getList().addAll(input1.getList());
        } else {
            input.getList().addAll(input1.getList());
            input.getList().addAll(input2.getList());
        }

        if (input1.getValidation() == null) {
            input.setValidation(input2.getValidation());
        } else if (input2.getValidation() == null) {
            input.setValidation(input1.getValidation());
        } else {
            return null;
        }

        input.setFixed(false);
        if (!input.getList().isEmpty() && input.getValidation() != null) {
            input.setListOpen(true);
        } else if (!Boolean.TRUE.equals(input1.getListOpen()) && input2.getListOpen() != null) {
            input.setListOpen(input2.getListOpen());
        } else if (!Boolean.TRUE.equals(input2.getListOpen()) && input1.getListOpen() != null) {
            input.setListOpen(input1.getListOpen());
        } else {
            return null;
        }

        return input;
    }
}
