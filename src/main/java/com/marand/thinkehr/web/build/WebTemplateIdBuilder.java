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

import care.better.platform.template.AmNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.id.IdDeduplicator;
import com.marand.thinkehr.web.build.id.impl.NumericSuffixIdDeduplicator;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Bostjan Lah
 */
public class WebTemplateIdBuilder {
    private static final Pattern ID_PATTERN = Pattern.compile("[^\\p{IsAlphabetic}0-9_.-]");
    private static final Pattern INTERVAL_PATTERN = Pattern.compile("DV_INTERVAL<DV_([^>]+)>");
    private static final Pattern MULTIPLE_UNDERSCORE = Pattern.compile("_{2,}");

    private static final Function<WebTemplateNode, String> TO_JSON_ID = WebTemplateNode::getJsonId;

    private final Deque<WebTemplateNode> segments = new ArrayDeque<>();
    private final IdDeduplicator idDeduplicator = new NumericSuffixIdDeduplicator();

    public WebTemplateNode buildIds(WebTemplateNode node, Multimap<AmNode, WebTemplateNode> nodes) {
        WebTemplateNode parent = segments.peek();
        if (parent != null) {
            WebTemplateUtils.buildChain(node, parent);
        }
        buildId(node);

        updateNodesMap(node, nodes);

        segments.push(node);
        if (!node.getChildren().isEmpty()) {
            handleChildren(node, nodes);
            updateDependsOn(node.getChildren());
        }
        if (node.getCardinalities() != null) {
            updateCardinalities(node);
        }
        segments.pop();
        return node;
    }

    private void handleChildren(WebTemplateNode node, Multimap<AmNode, WebTemplateNode> nodes) {
        boolean isChoice = Objects.equals(node.getRmType(), "ELEMENT") && node.getChildren().size() > 1;
        if (isChoice) {
            // make sure coded text is before text
            fixPolymorphicOrder(node.getChildren());
        }

        int i = 0;
        for (WebTemplateNode childNode : node.getChildren()) {
            if (isChoice) {
                String type = childNode.getRmType();
                if (StringUtils.isNotBlank(type) && type.startsWith("DV_")) {
                    childNode.setJsonId(buildTypedId(type));
                    childNode.setAlternativeId(segments.peek().getId() + WebTemplateConstants.SEGMENT_DELIMITER + "value" + (i > 0 ? String.valueOf(i + 1) : ""));
                    childNode.setAlternativeJsonId("value" + (i > 0 ? String.valueOf(i + 1) : ""));
                }
            }
            buildIds(childNode, nodes);
            i++;
        }
    }

    private String buildTypedId(String type) {
        Matcher matcher = INTERVAL_PATTERN.matcher(type);
        if (matcher.matches()) {
            return "interval_of_" + matcher.group(1).toLowerCase() + "_value";
        }
        return type.substring(3).toLowerCase() + "_value";
    }

    private void fixPolymorphicOrder(List<WebTemplateNode> children) {
        Optional<Integer> codedTextOptional = children.stream().filter(n -> Objects.equals(n.getRmType(), "DV_CODED_TEXT"))
                .findFirst().map(children::indexOf);
        Optional<Integer> textOptional = children.stream().filter(n -> Objects.equals(n.getRmType(), "DV_TEXT"))
                .findFirst().map(children::indexOf);

        // swap if text is before coded_text
        if (codedTextOptional.isPresent() && textOptional.isPresent() && codedTextOptional.get() > textOptional.get()) {
            children.add(textOptional.get(), children.remove((int)codedTextOptional.get()));
        }
    }

    private void updateNodesMap(WebTemplateNode node, Multimap<AmNode, WebTemplateNode> nodes) {
        nodes.put(node.getAmNode(), node);
        for (AmNode amNode : node.getChain()) {
            if (!nodes.containsEntry(amNode, node)) {
                nodes.put(amNode, node);
            }
        }
    }

    private void updateCardinalities(WebTemplateNode node) {
        for (Iterator<WebTemplateCardinality> iterator = node.getCardinalities().iterator(); iterator.hasNext(); ) {
            WebTemplateCardinality cardinality = iterator.next();

            cardinality.setIds(node.getChildren().stream()
                                       .filter(new PathPrefixPredicate(cardinality.getPath()))
                                       .map(TO_JSON_ID)
                                       .collect(Collectors.toList()));
            if (cardinality.getIds().isEmpty()) {
                iterator.remove();
            }
        }
    }

    private void updateDependsOn(List<WebTemplateNode> children) {
        for (WebTemplateNode child : children) {
            if (child.getDependsOn() != null) {
                updateDependsOn(children, child);
            }

        }
    }

    private void updateDependsOn(List<WebTemplateNode> children, WebTemplateNode child) {
        Set<String> dependsOn = new HashSet<>();

        for (String path : child.getDependsOn()) {
            dependsOn.addAll(children.stream()
                                     .filter(new DependsOnPredicate(path))
                                     .map(TO_JSON_ID)
                                     .collect(Collectors.toList()));
        }

        child.setDependsOn(dependsOn.isEmpty() ? null : Lists.newArrayList(dependsOn));
    }

    private void buildId(WebTemplateNode node) {
        String baseId = getBaseId(node);
        if (baseId.isEmpty()) {
            baseId = "id";
        } else if (Character.isDigit(baseId.codePointAt(0))) {
            baseId = 'a' + baseId;
        }

        String parentId = segments.isEmpty()
                ? ""
                : segments.peek().getId() + WebTemplateConstants.SEGMENT_DELIMITER;

        String id = idDeduplicator.getUniqueBaseId(parentId, baseId);
        node.setId(parentId + id);
        node.setJsonId(id);
    }

    private String getBaseId(WebTemplateNode node) {
        String name;
        if (!StringUtils.isBlank(node.getJsonId())) {
            name = getIdForName(node.getJsonId());
        } else if (StringUtils.isBlank(node.getName())) {
            // multiple ored data values
            if ("ELEMENT".equals(segments.peek().getRmType()) && !node.getRmType().startsWith("DV_INTERVAL")) {
                name = "value";
            } else {
                name = getLastPathElement(node);
            }
        } else {
            name = node.getName();
        }
        return getIdForName(name);
    }

    public static String getIdForName(String name) {
        String id = ID_PATTERN.matcher(name).replaceAll("_").toLowerCase();
        id = MULTIPLE_UNDERSCORE.matcher(id).replaceAll("_");
        if (id.startsWith("_")) {
            id = id.substring(1);
        }
        if (id.endsWith("_")) {
            id = id.substring(0, id.length() - 1);
        }
        return id;
    }

    private String getLastPathElement(WebTemplateNode node) {
        int lastSlash = node.getPath().lastIndexOf('/');
        return lastSlash == -1 ? node.getPath() : node.getPath().substring(lastSlash + 1);
    }

    private static final class PathPrefixPredicate implements Predicate<WebTemplateNode> {
        private final String pathPrefix;

        private PathPrefixPredicate(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }

        @Override
        public boolean test(WebTemplateNode node) {
            return node.getPath().startsWith(pathPrefix);
        }
    }

    private static final class DependsOnPredicate implements Predicate<WebTemplateNode> {
        private final String pathPrefix;

        private DependsOnPredicate(String pathPrefix) {
            this.pathPrefix = pathPrefix;
        }

        @Override
        public boolean test(WebTemplateNode node) {
            return !Boolean.TRUE.equals(node.getInContext()) && node.getPath().startsWith(pathPrefix);
        }
    }
}
