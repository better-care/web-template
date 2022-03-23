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

package care.better.platform.web.template.builder.utils

import care.better.platform.template.AmNode
import care.better.platform.utils.RmUtils
import care.better.platform.utils.TemplateUtils
import care.better.platform.utils.exception.RmClassCastException
import care.better.platform.web.template.WebTemplate
import care.better.platform.web.template.builder.WebTemplateBuilder
import care.better.platform.web.template.builder.context.WebTemplateBuilderContext
import care.better.platform.web.template.builder.model.WebTemplateNode
import com.google.common.base.Splitter
import org.openehr.am.aom.Template
import org.openehr.rm.common.StringDictionaryItem
import org.openehr.rm.datatypes.DataValue
import java.util.regex.Pattern


/**
 * @author Bostjan Lah
 * @author Primoz Delopst
 * @since 3.1.0
 *
 * Set of utility functions used during the [WebTemplate] building.
 */
object WebTemplateBuilderUtils {
    private val LOCALIZATION_PATTERN = Pattern.compile("L10n=\\{(.+?)(?<!\\\\)}")

    @JvmStatic
    fun <T> getDefaultValue(amNode: AmNode, clazz: Class<T>): T? {
        val node = if (amNode.constraints == null && amNode.parent != null) amNode.parent as AmNode else amNode

        if (node.constraints != null) {
            for (attribute in (node.constraints ?: emptyList())) {
                for (complexObject in attribute.children) {
                    if (clazz.isInstance(complexObject.defaultValue)) {
                        return clazz.cast(complexObject.defaultValue)
                    }
                }
            }
        }
        return null
    }

    @JvmStatic
    internal fun getDataValueClass(amNode: AmNode): Class<*>? =
            sequenceOf(amNode, amNode.parent)
                    .filter { it != null }
                    .mapNotNull {
                        try {
                            val rmClass = RmUtils.getRmClass((it as AmNode).rmType)
                            if (DataValue::class.java.isAssignableFrom(rmClass))
                                rmClass
                            else
                                null

                        } catch (ignored: RmClassCastException) {
                            null
                        }
                    }.firstOrNull()


    @JvmStatic
    internal fun buildChain(node: WebTemplateNode, parent: WebTemplateNode) {
        var amNode: AmNode? = node.amNode
        while (amNode != null) {
            node.chain.add(0, amNode)
            amNode = amNode.parent
            if (amNode == parent.amNode) {
                break
            }
        }
    }

    @JvmStatic
    fun getTranslationsFromAnnotations(amNode: AmNode): Map<String, String?> {
        val localizedNamesFromAnnotations: MutableMap<String, String?> = mutableMapOf()
        amNode.annotations?.forEach {
            it.items.forEach { item ->
                val value = item.value
                val id = item.id
                if (value != null && value.contains("L10n={"))
                    parseTranslations(value, localizedNamesFromAnnotations)
                else if (id != null && id.lowercase().startsWith("l10n."))
                    localizedNamesFromAnnotations[id.substring(5)] = value
            }
        }
        return localizedNamesFromAnnotations.toMap()
    }

    @JvmStatic
    internal fun parseTranslations(value: String, localizedNamesFromAnnotations: MutableMap<String, String?>) {
        val matcher = LOCALIZATION_PATTERN.matcher(value)
        if (matcher.find()) {
            val translations = matcher.group(1)
            for (translation in Splitter.on('|').split(translations)) {
                val i = translation.indexOf('=')
                if (i != -1) {
                    val language = translation.substring(0, i).trim { it <= ' ' }
                    val text = translation.substring(i + 1).trim { it <= ' ' }.replace("\\{", "{").replace("\\}", "}")
                    localizedNamesFromAnnotations[language] = text
                }
            }
        }
    }

    @JvmStatic
    internal fun getChoiceWebTemplateId(typeName: String): String = "${typeName.substring(3).lowercase()}_value"

    @JvmStatic
    fun buildWebTemplate(template: Template): WebTemplate {
        val webTemplateBuilderContext = WebTemplateBuilderContext(
                defaultLanguage = template.language?.codeString,
                languages = TemplateUtils.findTemplateLanguages(template))
        return requireNotNull(WebTemplateBuilder.build(template, webTemplateBuilderContext))
    }

    @JvmStatic
    fun extractOtherDetails(stringDictionaryItems: List<StringDictionaryItem>): Map<String, Any?> =
        stringDictionaryItems.filter { it.id != null }
            .filter {
                it.id == "is_singleton" //Only extract other_details that we find relevant for frontend. At this moment, this is the only attribute we need.
            }
            .associate { it.id!! to it.value }
}
