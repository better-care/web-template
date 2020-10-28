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
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import com.marand.thinkehr.web.build.input.range.WebTemplateTemporalRange;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.am.aom.CDate;
import org.openehr.rm.datatypes.DvDate;

import javax.annotation.Nullable;

/**
 * @author Bostjan Lah
 */
public class DateWebTemplateInputBuilder extends TemporalWebTemplateInputBuilder<CDate> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CDate item, WebTemplateBuilderContext context) {
        WebTemplateInput input = item == null
                ? build(WebTemplateInputType.DATE)
                : build(WebTemplateInputType.DATE, new WebTemplateTemporalRange(item.getRange()), item.getPattern());
        input.setDefaultValue(getDefaultValue(amNode, item));
        return input;
    }

    protected String getDefaultValue(AmNode amNode, CDate item) {
        if (item != null && StringUtils.isNotBlank(item.getAssumedValue())) {
            return item.getAssumedValue();
        }
        DvDate defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvDate.class);
        return defaultValue == null ? null : defaultValue.getValue();
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CDate cDate = AmUtils.getPrimitiveItem(node.getAmNode(), CDate.class, "value");
        WebTemplateInput input = build(node.getAmNode(), cDate, context);
        input.setDefaultValue(getTypedDefaultValue(node.getAmNode(), cDate == null ? null : cDate.getAssumedValue()));
        node.getInputs().add(input);
    }

    protected LocalDate getTypedDefaultValue(AmNode amNode, String assumedValue) {
        LocalDate defaultValue = null;
        if (assumedValue != null) {
            defaultValue = ISODateTimeFormat.dateParser().parseLocalDate(assumedValue);
        }
        DvDate date = WebTemplateUtils.getDefaultValue(amNode, DvDate.class);
        if (date != null) {
            defaultValue = ISODateTimeFormat.dateParser().parseLocalDate(date.getValue());
        }
        return defaultValue;
    }
}
