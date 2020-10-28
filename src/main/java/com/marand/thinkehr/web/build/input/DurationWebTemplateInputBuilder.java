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
import care.better.platform.utils.JodaConversionUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import com.marand.thinkehr.web.build.input.range.WebTemplateValidationIntegerRange;
import org.joda.time.Period;
import org.openehr.am.aom.CDuration;
import org.openehr.rm.datatypes.DvDuration;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Bostjan Lah
 */
public class DurationWebTemplateInputBuilder implements WebTemplateInputBuilder<CDuration> {
    public static final Set<WebTemplateDurationField> FULL_DURATION =
            ImmutableSet.of(
                    WebTemplateDurationField.YEAR,
                    WebTemplateDurationField.MONTH,
                    WebTemplateDurationField.DAY,
                    WebTemplateDurationField.WEEK,
                    WebTemplateDurationField.HOUR,
                    WebTemplateDurationField.MINUTE,
                    WebTemplateDurationField.SECOND);

    @Override
    public WebTemplateInput build(AmNode amNode, CDuration item, WebTemplateBuilderContext context) {
        WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.DURATION);
        Set<WebTemplateDurationField> fields;
        Period min;
        Period max;
        if (item == null) {
            fields = FULL_DURATION;
            min = Period.ZERO;
            max = Period.ZERO;
        } else {
            fields = item.getPattern() == null ? FULL_DURATION : getAllowedFields(item.getPattern());
            min = item.getRange() != null && item.getRange().getLower() != null
                    ? JodaConversionUtils.toPeriod(item.getRange().getLower())
                    : Period.ZERO;
            max = item.getRange() != null && item.getRange().getUpper() != null
                    ? JodaConversionUtils.toPeriod(item.getRange().getUpper())
                    : Period.ZERO;
            if (item.getAssumedValue() != null) {
                input.setDefaultValue(item.getAssumedValue());
            }
        }
        for (WebTemplateDurationField field : fields) {
            WebTemplateCodedValue codedValue = new WebTemplateCodedValue(field.name().toLowerCase(), field.name().toLowerCase());
            codedValue.setValidation(new WebTemplateValidation());
            int fieldMin = min.get(field.getDurationFieldType());
            int fieldMax = max.get(field.getDurationFieldType());
            codedValue.getValidation().setRange(new WebTemplateValidationIntegerRange(fieldMin, fieldMax == 0 ? null : fieldMax));
            input.getList().add(codedValue);
        }
        input.setFixed(!min.equals(Period.ZERO) && min.equals(max));
        DvDuration defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvDuration.class);
        if (defaultValue != null) {
            input.setDefaultValue(defaultValue.getValue());
        }
        return input;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CDuration item = AmUtils.getPrimitiveItem(node.getAmNode(), CDuration.class, "value");

        DvDuration defaultDuration = WebTemplateUtils.getDefaultValue(node.getAmNode(), DvDuration.class);
        String defaultValue = defaultDuration == null ? null : defaultDuration.getValue();

        Set<WebTemplateDurationField> fields;
        Period min;
        Period max;
        if (item == null) {
            fields = FULL_DURATION;
            min = Period.ZERO;
            max = Period.ZERO;
        } else {
            if (defaultValue == null && item.getAssumedValue() != null) {
                defaultValue = item.getAssumedValue();
            }

            fields = item.getPattern() == null ? FULL_DURATION : getAllowedFields(item.getPattern());
            min = getMin(item);
            max = getMax(item);
        }

        Period defaultPeriod = defaultValue == null ? null : Period.parse(defaultValue);
        for (WebTemplateDurationField field : fields) {
            WebTemplateInput input = new WebTemplateInput(WebTemplateInputType.INTEGER, field.name().toLowerCase());
            input.setValidation(new WebTemplateValidation());
            int fieldMin = min.get(field.getDurationFieldType());
            int fieldMax = max.get(field.getDurationFieldType());
            input.getValidation().setRange(new WebTemplateValidationIntegerRange(fieldMin, fieldMax == 0 ? null : fieldMax));
            input.setFixed(!min.equals(Period.ZERO) && min.equals(max));
            if (defaultPeriod != null) {
                input.setDefaultValue(defaultPeriod.get(field.getDurationFieldType()));
            }
            node.getInputs().add(input);
        }
    }

    public static Set<WebTemplateDurationField> getAllowedFields(String pattern) {
        Iterator<String> iterator = Splitter.on("T").split(pattern).iterator();
        Set<WebTemplateDurationField> allowedFields = EnumSet.noneOf(WebTemplateDurationField.class);
        if (iterator.hasNext()) {
            String ymdw = iterator.next();
            if (ymdw.contains("Y")) {
                allowedFields.add(WebTemplateDurationField.YEAR);
            }
            if (ymdw.contains("M")) {
                allowedFields.add(WebTemplateDurationField.MONTH);
            }
            if (ymdw.contains("D")) {
                allowedFields.add(WebTemplateDurationField.DAY);
            }
            if (ymdw.contains("W")) {
                allowedFields.add(WebTemplateDurationField.WEEK);
            }
        }
        if (iterator.hasNext()) {
            String hms = iterator.next();
            if (hms.contains("H")) {
                allowedFields.add(WebTemplateDurationField.HOUR);
            }
            if (hms.contains("M")) {
                allowedFields.add(WebTemplateDurationField.MINUTE);
            }
            if (hms.contains("S")) {
                allowedFields.add(WebTemplateDurationField.SECOND);
            }
        }
        return allowedFields;
    }

    public static Period getMin(CDuration item) {
        return item.getRange() != null && item.getRange().getLower() != null
                ? JodaConversionUtils.toPeriod(item.getRange().getLower())
                : Period.ZERO;
    }

    public static Period getMax(CDuration item) {
        return item.getRange() != null && item.getRange().getUpper() != null
                ? JodaConversionUtils.toPeriod(item.getRange().getUpper())
                : Period.ZERO;
    }

    public static Period getAssumedValue(CDuration item) {
        return item.getAssumedValue() != null
                ? JodaConversionUtils.toPeriod(item.getAssumedValue())
                : Period.ZERO;
    }
}
