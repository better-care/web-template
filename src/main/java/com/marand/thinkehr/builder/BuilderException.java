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

package com.marand.thinkehr.builder;

/**
 * User: Bostjan Lah
 */
public class BuilderException extends RuntimeException {
    private final String path;

    public BuilderException(String message) {
        super(message);
        path = null;
    }

    public BuilderException(String message, Throwable cause) {
        super(message, cause);
        path = null;
    }

    public BuilderException(Throwable cause) {
        super(cause);
        path = null;
    }

    public BuilderException(String message, String path) {
        super(formatMessage(message, path));
        this.path = path;
    }

    public BuilderException(String message, Throwable cause, String path) {
        super(formatMessage(message, path), cause);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    private static String formatMessage(String message, String path) {
        return message + " (path: " + path + ')';
    }
}
