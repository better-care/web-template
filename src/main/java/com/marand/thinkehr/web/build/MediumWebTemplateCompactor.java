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

import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
public class MediumWebTemplateCompactor extends MinimalWebTemplateCompactor {
    private static final Set<String> ALWAYS_COMPACTABLE_CLASSES =
            ImmutableSet.<String>builder()
                    .add("ITEM_TREE")
                    .add("ITEM_LIST")
                    .add("ITEM_SINGLE")
                    .add("ITEM_TABLE")
                    .add("ITEM_STRUCTURE")
                    .add("HISTORY")
                    .build();
    private static final Set<String> SINGLE_COMPACTABLE_CLASSES =
            ImmutableSet.<String>builder()
                    .add("POINT_EVENT")
                    .add("INTERVAL_EVENT")
                    .add("EVENT")
                    .build();

    @Override
    protected WebTemplateNode compactNode(WebTemplateNode node) {
        List<WebTemplateNode> children = getCompacted(node, node.getChildren());
        node.getChildren().clear();
        node.getChildren().addAll(children);

        return super.compactNode(node);
    }

    private List<WebTemplateNode> getCompacted(WebTemplateNode node, List<WebTemplateNode> children) {
        List<WebTemplateNode> compacted = new ArrayList<>();
        for (WebTemplateNode child : children) {
            if (isCompactable(child, children)) {
                if (child.getDependsOn() != null) {
                    copyDependsOnToChildren(child);
                }
                if (child.getCardinalities() != null) {
                    copyCardinalities(child.getCardinalities(), node);
                }
                compacted.addAll(getCompacted(child, child.getChildren()));
            } else {
                compacted.add(child);
            }
        }
        return compacted;
    }

    private void copyDependsOnToChildren(WebTemplateNode node) {
        for (WebTemplateNode child : node.getChildren()) {
            copyDependsOn(node, child);
        }
    }

    private boolean isCompactable(WebTemplateNode child, List<WebTemplateNode> children) {
        boolean compactable;
        if (ALWAYS_COMPACTABLE_CLASSES.contains(child.getRmType())) {
            compactable = true;
        } else {
            compactable = child.getOccurences().getJsonMax() == 1 &&
                    SINGLE_COMPACTABLE_CLASSES.contains(child.getRmType()) &&
                    children.stream().noneMatch(webTemplateNode -> !webTemplateNode.equals(child) && typesMatch(child, webTemplateNode));
        }
        return compactable;
    }

    private boolean typesMatch(WebTemplateNode node1, WebTemplateNode node2) {
        return node1.getRmType().equals(node2.getRmType()) || (node1.getRmType().endsWith("EVENT") && node2.getRmType().endsWith("EVENT"));
    }

    @Override
    protected boolean isSkippable(WebTemplateNode node) {
        return super.isSkippable(node) || "ELEMENT".equals(node.getRmType());
    }
}
