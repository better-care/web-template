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
import care.better.platform.template.AmUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.marand.thinkehr.web.build.input.WebTemplateBindingCodedValue;
import com.marand.thinkehr.web.build.input.WebTemplateInput;
import com.marand.thinkehr.web.build.input.range.WebTemplateIntegerRange;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Web template node
 *
 * @author Bostjan Lah
 */
@JsonPropertyOrder({"id", "name", "localizedName", "rmType", "nodeId", "occurences", "cardinalities", "dependsOn", "localizedNames", "localizedDescriptions", "annotations", "path", "proportionTypes", "inputs", "children"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebTemplateNode {
    private AmNode amNode;
    private String name;
    private String localizedName;
    private final Map<String, String> localizedNames = new HashMap<>();
    private final Map<String, String> localizedDescriptions = new HashMap<>();
    private String path;
    private String id;
    private String jsonId;
    private String alternativeId;
    private String alternativeJsonId;
    private String rmType;
    private String nodeId;
    private String nameCodeString;
    private Boolean inContext;
    private WebTemplateIntegerRange occurences;
    private List<WebTemplateCardinality> cardinalities;
    private List<String> dependsOn;
    private final List<WebTemplateNode> children = new ArrayList<>();
    private final List<AmNode> chain = new ArrayList<>();
    private final Map<String, String> annotations = new HashMap<>();
    private final Map<String, WebTemplateBindingCodedValue> termBindings = new LinkedHashMap<>(1);
    private final Set<String> proportionTypes = new HashSet<>();

    // data entry
    private final List<WebTemplateInput> inputs = new ArrayList<>();

    @JsonIgnore
    public AmNode getAmNode() {
        return amNode;
    }

    public void setAmNode(AmNode amNode) {
        this.amNode = amNode;
    }

    @JsonIgnore
    public List<AmNode> getChain() {
        return chain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getLocalizedNames() {
        return localizedNames;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getLocalizedDescriptions() {
        return localizedDescriptions;
    }

    @JsonProperty("aqlPath")
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @JsonIgnore
    public String getSubPath(int index, ArchetypePredicateProvider archetypePredicateProvider) {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        AmNode parent = chain.get(0).getParent();
        for (AmNode node : chain) {
            String attribute = AmUtils.attributeNameOf(parent, node);
            builder.append(getPath(attribute, node, i == chain.size() - 1 ? index : -1, archetypePredicateProvider));
            parent = node;
            i++;
            if ("ELEMENT".equals(node.getRmType())) {
                break;
            }
        }
        return builder.toString();
    }

    private String getPath(String attributeName, AmNode amNode, int index, ArchetypePredicateProvider archetypePredicateProvider) {
        if (attributeName == null) {
            return "";
        } else {
            return '/' + attributeName + archetypePredicateProvider.getPredicate(amNode, index);
        }
    }

    /**
     * Gets complete id (including all parent ids).
     *
     * @return complete id
     */
    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets id for this node/leaf only (doesn't prepend parent node ids).
     *
     * @return id
     */
    @JsonProperty("id")
    public String getJsonId() {
        return jsonId;
    }

    public void setJsonId(String jsonId) {
        this.jsonId = jsonId;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getRmType() {
        return rmType;
    }

    public void setRmType(String rmType) {
        this.rmType = rmType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Boolean getInContext() {
        return inContext;
    }

    public void setInContext(Boolean inContext) {
        this.inContext = inContext;
    }

    @JsonUnwrapped
    public WebTemplateIntegerRange getOccurences() {
        return occurences;
    }

    public void setOccurences(WebTemplateIntegerRange occurences) {
        this.occurences = occurences;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<WebTemplateCardinality> getCardinalities() {
        return cardinalities;
    }

    public void setCardinalities(List<WebTemplateCardinality> cardinalities) {
        this.cardinalities = cardinalities;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, WebTemplateBindingCodedValue> getTermBindings() {
        return termBindings;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<WebTemplateNode> getChildren() {
        return children;
    }

    @JsonIgnore
    public WebTemplateInput getInput() {
        return inputs.isEmpty() ? null : inputs.get(0);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<WebTemplateInput> getInputs() {
        return inputs;
    }

    @JsonIgnore
    public WebTemplateInput getInput(String suffix) {
        return inputs.stream().filter(input -> suffix.equals(input.getSuffix())).findAny().orElse(null);
    }

    public void setInput(WebTemplateInput input) {
        if (inputs.isEmpty()) {
            inputs.add(input);
        } else {
            inputs.set(0, input);
        }
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    @JsonIgnore
    public boolean isRepeating() {
        return occurences != null && (occurences.getMax() == null || occurences.getMax() > 1);
    }

    @JsonIgnore
    public boolean hasInput() {
        return !inputs.isEmpty();
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, String> getAnnotations() {
        return annotations;
    }

    @JsonIgnore
    public String getNameCodeString() {
        return nameCodeString;
    }

    public void setNameCodeString(String nameCodeString) {
        this.nameCodeString = nameCodeString;
    }

    @JsonIgnore
    public String getAlternativeId() {
        return alternativeId;
    }

    public void setAlternativeId(String alternativeId) {
        this.alternativeId = alternativeId;
    }

    @JsonIgnore
    public String getAlternativeJsonId() {
        return alternativeJsonId;
    }

    public void setAlternativeJsonId(String alternativeJsonId) {
        this.alternativeJsonId = alternativeJsonId;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Set<String> getProportionTypes() {
        return proportionTypes;
    }

    @JsonIgnore
    public boolean jsonIdMatches(String id) {
        return Objects.equals(id, jsonId) || Objects.equals(id, alternativeJsonId);
    }

    @Override
    public String toString() {
        return rmType + '[' + StringUtils.defaultString(jsonId, "") + ':' + StringUtils.defaultString(name, "") + ']';
    }
}
