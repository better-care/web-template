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

/**
 * @author Bostjan Lah
 */
public enum WebTemplateProportionType {
    // num and den can be any type
    RATIO,
    // den must be 1
    UNITARY,
    // den must be 100
    PERCENT,
    // num and den are integral
    FRACTION,
    // num and den are integral, presentation is "x y/z" (so 3/2 would be 1 1/2)
    INTEGER_FRACTION
}
