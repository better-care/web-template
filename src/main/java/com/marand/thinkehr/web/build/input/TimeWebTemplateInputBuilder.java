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
import org.joda.time.LocalTime;
import org.joda.time.format.ISODateTimeFormat;
import org.openehr.am.aom.CTime;
import org.openehr.rm.datatypes.DvTime;

import javax.annotation.Nullable;

/**
 * @author Bostjan Lah
 */
public class TimeWebTemplateInputBuilder extends TemporalWebTemplateInputBuilder<CTime> {
    @Override
    public WebTemplateInput build(AmNode amNode, @Nullable CTime item, WebTemplateBuilderContext context) {
        WebTemplateInput input = item == null
                ? build(WebTemplateInputType.TIME)
                : build(WebTemplateInputType.TIME, new WebTemplateTemporalRange(item.getRange()), item.getPattern());
        input.setDefaultValue(getDefaultValue(amNode, item));
        return input;
    }

    protected String getDefaultValue(AmNode amNode, CTime item) {
        if (item != null && StringUtils.isNotBlank(item.getAssumedValue())) {
            return item.getAssumedValue();
        }
        DvTime defaultValue = WebTemplateUtils.getDefaultValue(amNode, DvTime.class);
        return defaultValue == null ? null : defaultValue.getValue();
    }

    @Override
    public void build(WebTemplateNode node, WebTemplateBuilderContext context) {
        CTime cTime = AmUtils.getPrimitiveItem(node.getAmNode(), CTime.class, "value");
        WebTemplateInput input = build(node.getAmNode(), cTime, context);
        input.setDefaultValue(getTypedDefaultValue(node.getAmNode(), cTime == null ? null : cTime.getAssumedValue()));
        node.getInputs().add(input);
    }

    protected LocalTime getTypedDefaultValue(AmNode amNode, String assumedValue) {
        LocalTime defaultValue = null;
        if (assumedValue != null) {
            defaultValue = ISODateTimeFormat.time().parseLocalTime(assumedValue);
        }
        DvTime time = WebTemplateUtils.getDefaultValue(amNode, DvTime.class);
        if (time != null) {
            defaultValue = ISODateTimeFormat.timeParser().parseLocalTime(time.getValue());
        }
        return defaultValue;
    }
}
