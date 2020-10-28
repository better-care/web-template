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
import care.better.platform.template.AmUtils;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.WebTemplateConstants;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import com.marand.thinkehr.web.build.input.range.WebTemplateDecimalRange;
import com.marand.thinkehr.web.build.input.range.WebTemplateValidationIntegerRange;
import org.apache.commons.lang3.tuple.Pair;
import org.openehr.am.aom.CDvQuantity;
import org.openehr.am.aom.CQuantityItem;
import org.openehr.rm.datatypes.DvQuantity;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author Bostjan Lah
 */
public class QuantityWebTemplateInputBuilder implements WebTemplateInputBuilder<CDvQuantity> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CDvQuantity dvQuantity, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.QUANTITY);
        if (dvQuantity != null) {
            if (!dvQuantity.getList().isEmpty()) {
                buildFromList(input, dvQuantity.getList(), amNode, context.getLanguages());
            }
            if (dvQuantity.getAssumedValue() != null) {
                input.setDefaultValue(
                        String.valueOf(dvQuantity.getAssumedValue().getMagnitude()) + ' ' + dvQuantity.getAssumedValue().getUnits());
            }
        }

        DvQuantity defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvQuantity.class);
        if (defaultValue != null) {
            input.setDefaultValue(String.valueOf(defaultValue.getMagnitude()) + ' ' + defaultValue.getUnits());
        }

        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        WebTemplateInput magnitude = new WebTemplateInput(WebTemplateInputType.DECIMAL, WebTemplateConstants.MAGNITUDE_ATTRIBUTE);

        WebTemplateInput units;
        if (node.getAmNode().getCObject() instanceof CDvQuantity) {
            units = new WebTemplateInput(WebTemplateInputType.CODED_TEXT, WebTemplateConstants.UNIT_ATTRIBUTE);
            CDvQuantity dvQuantity = (CDvQuantity)node.getAmNode().getCObject();
            if (!dvQuantity.getList().isEmpty()) {
                buildFromList(units, dvQuantity.getList(), node.getAmNode(), context.getLanguages());
                if (units.getList().size() == 1) {
                    magnitude.setValidation(units.getList().get(0).getValidation());
                }
            }
            if (dvQuantity.getAssumedValue() != null) {
                magnitude.setDefaultValue(dvQuantity.getAssumedValue().getMagnitude());
                units.setDefaultValue(dvQuantity.getAssumedValue().getUnits());
            }

            DvQuantity defaultValue = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvQuantity.class);
            if (defaultValue != null) {
                magnitude.setDefaultValue(defaultValue.getMagnitude());
                units.setDefaultValue(defaultValue.getUnits());
            }
        } else {
            units = new WebTemplateInput(WebTemplateInputType.TEXT, WebTemplateConstants.UNIT_ATTRIBUTE);
        }

        node.getInputs().add(magnitude);
        node.getInputs().add(units);
    }

    private void buildFromList(WebTemplateInput input, List<CQuantityItem> quantityItems, AmNode amNode, Collection<String> languages) {
        for (CQuantityItem item : quantityItems) {
            WebTemplateValidationIntegerRange precision = new WebTemplateValidationIntegerRange(item.getPrecision());
            WebTemplateDecimalRange range = new WebTemplateDecimalRange(item.getMagnitude());
            WebTemplateCodedValue value = new WebTemplateCodedValue(item.getUnits(), item.getUnits());
            if (!precision.isEmpty() || !range.isEmpty()) {
                value.setValidation(new WebTemplateValidation());
                if (!precision.isEmpty()) {
                    value.getValidation().setPrecision(precision);
                }
                if (!range.isEmpty()) {
                    value.getValidation().setRange(range);
                }
            }
            languages.stream()
                    .map(language -> Pair.of(language, AmUtils.findText(amNode, language, item.getUnits())))
                    .filter(p -> p.getRight() != null)
                    .forEach(p -> value.getLocalizedLabels().put(p.getLeft(), p.getRight()));
            input.getList().add(value);
        }
    }
}
