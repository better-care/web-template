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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * @author matijak
 * @since 18.03.2016
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebTemplateBindingCodedValue extends WebTemplateCodedValue {

    private final String terminologyId;

    public WebTemplateBindingCodedValue(String value, String terminologyId) {
        super(value, null);
        this.terminologyId = terminologyId;
    }

    @Override
    @JsonIgnore
    public String getLabel() {
        return null;
    }

    public String getTerminologyId() {
        return terminologyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        WebTemplateBindingCodedValue that = (WebTemplateBindingCodedValue)o;
        return Objects.equals(terminologyId, that.terminologyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), terminologyId);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("value", getValue())
                .append("terminology", terminologyId)
                .toString();
    }
}
