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

package care.better.platform.web.template.converter.raw.context

import care.better.openehr.rm.RmObject

/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Interface used to visit [RmObject] after it was created.
 */
fun interface RmVisitor<T : RmObject> {
    /**
     * Visits the RM object after it was created.
     *
     * @param rm RM object
     * @param webTemplatePath Web template path
     */
    fun visit(rm: T, webTemplatePath: String)
}
