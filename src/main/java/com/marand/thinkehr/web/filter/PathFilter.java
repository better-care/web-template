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

package com.marand.thinkehr.web.filter;

/**
 * Used to filter paths to be output in WebTemplateBuilder. This can be used for example to only export a portion
 * of a template - for example only a single section, etc.
 *
 * @author Bostjan Lah
 */
@FunctionalInterface
public interface PathFilter {
    /**
     * Return true to export this path (and all of its subpaths), false otherwise.
     *
     * @param path RM path
     * @return true - export / false - don't export
     */
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean accept(String path);
}
