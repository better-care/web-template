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

package care.better.platform.web.template.path.facade

import care.better.platform.web.template.builder.model.WebTemplateNode
import care.better.platform.web.template.path.model.WebTemplatePathExpressionNode
import care.better.platform.web.template.path.evaluator.WebTemplatePathExpressionEvaluator
import care.better.platform.web.template.path.evaluator.TrackedNode
import care.better.platform.web.template.path.exception.WebTemplatePathExpressionException
import care.better.platform.web.template.path.parser.WebTemplatePathExpressionNodeBuilder
import care.better.platform.web.template.path.parser.WebTemplatePathExpressionLexer
import care.better.platform.web.template.path.parser.WebTemplatePathExpressionParser
import com.fasterxml.jackson.databind.node.ObjectNode
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

/**
 * @author Primoz Delopst
 * @since 5.0.0
 *
 * Facade that wires the lexer, parser, and evaluator layers together.
 * Evaluation is performed by [WebTemplatePathExpressionEvaluator].
 * FLAT and RAW RM objects are converted to structured format before evaluation.
 *
 * @constructor Creates a new instance of [WebTemplatePathExpressionFacade]
 */
internal class WebTemplatePathExpressionFacade private constructor() {
    companion object {

        /**
         * Evaluates WebTemplate path expression against a structured RM object with web template awareness.
         *
         * @param expression WebTemplate path expression
         * @param structuredRmObject Structured RM object
         * @param webTemplateNode [WebTemplateNode] for path validation
         * @return [List] of [TrackedNode]
         */
        internal fun evaluate(expression: String, structuredRmObject: ObjectNode, webTemplateNode: WebTemplateNode): List<TrackedNode> =
            WebTemplatePathExpressionEvaluator.evaluate(parse(expression), structuredRmObject, webTemplateNode)

        /**
         * Parses WebTemplate path expression.
         *
         * @param expression WebTemplate path expression
         * @return [WebTemplatePathExpressionNode]
         */
        internal fun parse(expression: String): WebTemplatePathExpressionNode {
            val errorListener = ThrowingErrorListener(expression)
            val lexer = WebTemplatePathExpressionLexer(CharStreams.fromString(expression)).apply {
                removeErrorListeners()
                addErrorListener(errorListener)
            }
            val parser = WebTemplatePathExpressionParser(CommonTokenStream(lexer)).apply {
                removeErrorListeners()
                addErrorListener(errorListener)
            }

            return WebTemplatePathExpressionNodeBuilder().visit(parser.expression())
        }
    }

    /**
     * Subclass of [BaseErrorListener] that throws [WebTemplatePathExpressionException] in case of error.
     *
     * @constructor Creates a new instance of [ThrowingErrorListener]
     * @param expression WebTemplate path expression
     */
    private class ThrowingErrorListener(private val expression: String) : BaseErrorListener() {
        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?) {
            throw WebTemplatePathExpressionException("Failed to parse expression at position $charPositionInLine: ${msg ?: "unknown error"} in expression: $expression")
        }
    }
}