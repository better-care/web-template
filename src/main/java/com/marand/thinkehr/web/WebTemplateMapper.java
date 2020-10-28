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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;

/**
 * @author Bostjan Lah
 */
public final class WebTemplateMapper {
    private final ObjectMapper mapper;
    private final ObjectWriter prettyObjectWriter;
    private final ObjectWriter objectWriter;

    private static class Holder {
        private static final WebTemplateMapper mapper = new WebTemplateMapper();
    }

    private WebTemplateMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        prettyObjectWriter = mapper.writerWithDefaultPrettyPrinter();
        objectWriter = mapper.writer();
    }

    public static WebTemplateMapper getInstance() {
        return Holder.mapper;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public ObjectWriter getWriter() {
        return objectWriter;
    }

    public ObjectWriter getWriter(boolean pretty) {
        return pretty ? prettyObjectWriter : objectWriter;
    }
}
