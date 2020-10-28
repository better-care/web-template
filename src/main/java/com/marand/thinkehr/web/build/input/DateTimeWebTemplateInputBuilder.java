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
import care.better.platform.utils.DateTimeConversionUtils;
import care.better.platform.utils.JSR310ConversionUtils;
import com.marand.thinkehr.web.WebTemplateBuilderContext;
import com.marand.thinkehr.web.build.WebTemplateInputType;
import com.marand.thinkehr.web.build.WebTemplateNode;
import com.marand.thinkehr.web.build.WebTemplateUtils;
import com.marand.thinkehr.web.build.input.range.WebTemplateTemporalRange;
import org.apache.commons.lang3.StringUtils;
import org.openehr.am.aom.CDateTime;
import org.openehr.rm.datatypes.DvDateTime;

import java.time.OffsetDateTime;

/**
 * @author Bostjan Lah
 */
public class DateTimeWebTemplateInputBuilder extends TemporalWebTemplateInputBuilder<CDateTime> {
    @Override
    public WebTemplateInput build(AmNode amNode, CDateTime item, WebTemplateBuilderContext context) {
        WebTemplateInput input = item == null
                ? build(WebTemplateInputType.DATETIME)
                : build(WebTemplateInputType.DATETIME, new WebTemplateTemporalRange(item.getRange()), item.getPattern());
        input.setDefaultValue(getDefaultValue(amNode, item));
        return input;
    }

    protected String getDefaultValue(AmNode amNode, CDateTime item) {
        String defaultValue = null;
        if (item != null && StringUtils.isNotBlank(item.getAssumedValue())) {
            defaultValue = item.getAssumedValue();
        }
        DvDateTime dateTime = WebTemplateUtils.getDefaultValue(amNode, DvDateTime.class);
        if (dateTime != null) {
            defaultValue = dateTime.getValue();
        }
        return defaultValue;
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CDateTime cDateTime = AmUtils.getPrimitiveItem(node.getAmNode(), CDateTime.class, "value");
        WebTemplateInput input = build(node.getAmNode(), cDateTime, context);
        input.setDefaultValue(getTypedDefaultValue(node.getAmNode(), cDateTime == null ? null : cDateTime.getAssumedValue()));
        node.getInputs().add(input);
    }

    protected OffsetDateTime getTypedDefaultValue(AmNode amNode, String assumedValue) {
        OffsetDateTime defaultValue = null;
        if (assumedValue != null) {
            defaultValue = DateTimeConversionUtils.toOffsetDateTime(assumedValue);
        }
        DvDateTime dateTime = WebTemplateUtils.getDefaultValue(amNode, DvDateTime.class);
        if (dateTime != null) {
            defaultValue = JSR310ConversionUtils.toOffsetDateTime(dateTime);
        }
        return defaultValue;
    }
}
