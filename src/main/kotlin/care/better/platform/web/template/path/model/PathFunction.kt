/* Copyright 2026 Better Ltd (www.better.care)
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

package care.better.platform.web.template.path.model

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Enum representing built-in functions in WebTemplate path expressions.
 */
enum class PathFunction(val functionName: String, val argType: ArgType) {
    COUNT("count", ArgType.NO_ARG),
    EXISTS("exists", ArgType.NO_ARG),
    EMPTY("empty", ArgType.NO_ARG),
    FIRST("first", ArgType.NO_ARG),
    LAST("last", ArgType.NO_ARG),
    SORT("sort", ArgType.NO_ARG),
    DISTINCT("distinct", ArgType.NO_ARG),
    LOWER("lower", ArgType.NO_ARG),
    UPPER("upper", ArgType.NO_ARG),
    SUM("sum", ArgType.NO_ARG),
    MIN("min", ArgType.NO_ARG),
    MAX("max", ArgType.NO_ARG),
    AVG("avg", ArgType.NO_ARG),
    ALL("all", ArgType.NO_ARG),
    ANY("any", ArgType.NO_ARG),
    NONE("none", ArgType.NO_ARG),
    TAIL("tail", ArgType.NO_ARG),
    SINGLE("single", ArgType.NO_ARG),
    LENGTH("length", ArgType.NO_ARG),
    TO_CHARS("toChars", ArgType.NO_ARG),
    NOT("not", ArgType.NO_ARG),
    IS_DISTINCT("isDistinct", ArgType.NO_ARG),
    ABS("abs", ArgType.NO_ARG),
    CEILING("ceiling", ArgType.NO_ARG),
    FLOOR("floor", ArgType.NO_ARG),
    TRUNCATE("truncate", ArgType.NO_ARG),
    SQRT("sqrt", ArgType.NO_ARG),
    LN("ln", ArgType.NO_ARG),
    EXP("exp", ArgType.NO_ARG),
    TO_INTEGER("toInteger", ArgType.NO_ARG),
    TO_DECIMAL("toDecimal", ArgType.NO_ARG),
    TO_BOOLEAN("toBoolean", ArgType.NO_ARG),
    TO_STRING("toString", ArgType.NO_ARG),
    ALL_TRUE("allTrue", ArgType.NO_ARG),
    ANY_TRUE("anyTrue", ArgType.NO_ARG),
    ALL_FALSE("allFalse", ArgType.NO_ARG),
    ANY_FALSE("anyFalse", ArgType.NO_ARG),
    NOW("now", ArgType.NO_ARG),
    TODAY("today", ArgType.NO_ARG),
    TIME_OF_DAY("timeOfDay", ArgType.NO_ARG),
    CHILDREN("children", ArgType.NO_ARG),
    DESCENDANTS("descendants", ArgType.NO_ARG),

    MATCHES("matches", ArgType.SINGLE_STRING_ARG),
    OF_TYPE("ofType", ArgType.SINGLE_STRING_ARG),
    INDEX_OF("indexOf", ArgType.SINGLE_STRING_ARG),
    STARTS_WITH("startsWith", ArgType.SINGLE_STRING_ARG),
    ENDS_WITH("endsWith", ArgType.SINGLE_STRING_ARG),
    CONTAINS("contains", ArgType.SINGLE_STRING_ARG),

    REPLACE_MATCHES("replaceMatches", ArgType.TWO_STRING_ARGS),
    REPLACE("replace", ArgType.TWO_STRING_ARGS),

    SUBSTRING("substring", ArgType.INT_ARGS),

    SKIP("skip", ArgType.SINGLE_INT_ARG),
    TAKE("take", ArgType.SINGLE_INT_ARG),

    ROUND("round", ArgType.OPTIONAL_INT_ARG),

    POWER("power", ArgType.NUMBER_ARG),
    LOG("log", ArgType.NUMBER_ARG),

    SELECT("select", ArgType.PATH_ARG),
    UNION("union", ArgType.PATH_ARG),
    COMBINE("combine", ArgType.PATH_ARG),
    INTERSECT("intersect", ArgType.PATH_ARG),
    EXCLUDE("exclude", ArgType.PATH_ARG),
    SUBSET_OF("subsetOf", ArgType.PATH_ARG),
    SUPERSET_OF("supersetOf", ArgType.PATH_ARG),

    IIF("iif", ArgType.EXPR_ARG),
    AGGREGATE("aggregate", ArgType.EXPR_ARG),
    REPEAT("repeat", ArgType.EXPR_ARG);

    companion object {
        private val byFunctionName = entries.associateBy { it.functionName }

        fun fromFunctionName(name: String): PathFunction =
            byFunctionName[name] ?: throw IllegalArgumentException("Unknown function: $name")
    }
}
