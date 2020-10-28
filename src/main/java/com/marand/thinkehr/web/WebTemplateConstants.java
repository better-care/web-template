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

package com.marand.thinkehr.web;

/**
 * @author Bostjan Lah
 */
public interface WebTemplateConstants {
    String SEGMENT_DELIMITER = "/";
    String SUBFIELD_DELIMITER = "|";

    int PROPORTION_TYPE_PERCENTAGE = 2;

    String TEXT_ID = "text";
    String TERMINOLOGY_LOCAL = "local";
    String TERMINOLOGY_OPENEHR = "openehr";

    String DEFAULT_LANGUAGE = "en";

    String NUMERATOR = "numerator";
    String DENOMINATOR = "denominator";

    String CODE_ATTRIBUTE = "code";
    String VALUE_ATTRIBUTE = "value";
    String TERMINOLOGY_ATTRIBUTE = "terminology";
    String OTHER_ATTRIBUTE = "other";
    String UNIT_ATTRIBUTE = "unit";
    String MAGNITUDE_ATTRIBUTE = "magnitude";
    String NAME_ATTRIBUTE = "name";
    String ID_ATTRIBUTE = "id";
    String ID_SCHEME_ATTRIBUTE = "id_scheme";
    String ID_NAMESPACE_ATTRIBUTE = "id_namespace";
    String TYPE_ATTRIBUTE = "type";
    String FORMALISM_ATTRIBUTE = "formalism";
}
