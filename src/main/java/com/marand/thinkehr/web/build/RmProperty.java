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

package com.marand.thinkehr.web.build;

/**
 * @author Bostjan Lah
 */
public class RmProperty {
    private final Class<?> clazz;
    private final String property;

    public RmProperty(Class<?> clazz, String property) {
        this.clazz = clazz;
        this.property = property;
    }

    @SuppressWarnings({"QuestionableName", "AccessingNonPublicFieldOfAnotherObject", "RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RmProperty that = (RmProperty)o;

        if (!clazz.equals(that.clazz)) {
            return false;
        }
        if (!property.equals(that.property)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = clazz.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }
}
