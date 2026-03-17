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

// Author: Primoz Delopst (primoz.delopst@better.care)
// (c) Copyright, Better, https://www.better.care

grammar WebTemplatePathExpression;

expression
    : expr EOF
    ;

expr
    : expr (STAR | PERCENT | DIV) expr
    | expr (PLUS | MINUS) expr
    | expr (GT | GTE | LT | LTE) expr
    | expr (EQUALS | NEQ) expr
    | expr AND expr
    | expr OR expr
    | expr XOR expr
    | expr IMPLIES expr
    | atom
    ;

atom
    : MINUS atom
    | primary functionInvocation*
    ;

primary
    : pathExpr
    | DECIMAL
    | INT
    | STRING_LITERAL
    | TRUE
    | FALSE
    | DOLLAR_THIS
    | DOLLAR_INDEX
    | DOLLAR_TOTAL
    | LEFT_PAREN expr RIGHT_PAREN
    ;

pathExpr
    : step (SLASH step)* attributeAccess?
    ;

step
    : IDENTIFIER COLON INT whereClause?
    | IDENTIFIER whereClause
    | IDENTIFIER
    ;

whereClause
    : FUNC_SEP WHERE LEFT_PAREN PIPE IDENTIFIER whereOp whereValue RIGHT_PAREN
    ;

whereOp
    : EQUALS | NEQ | GT | GTE | LT | LTE
    ;

whereValue
    : STRING_LITERAL | DECIMAL | INT | TRUE | FALSE
    ;

attributeAccess
    : PIPE IDENTIFIER
    ;

functionInvocation
    : FUNC_SEP IDENTIFIER LEFT_PAREN functionArgs? RIGHT_PAREN
    ;

functionArgs
    : functionArg (COMMA functionArg)*
    ;

functionArg
    : expr
    ;

WHERE              : 'where' ;
AND                : 'and' ;
OR                 : 'or' ;
XOR                : 'xor' ;
IMPLIES            : 'implies' ;
DIV                : 'div' ;
TRUE               : 'true' ;
FALSE              : 'false' ;

DOLLAR_THIS        : '$this' ;
DOLLAR_INDEX       : '$index' ;
DOLLAR_TOTAL       : '$total' ;

FUNC_SEP           : '.' ;
GTE                : '>=' ;
LTE                : '<=' ;
NEQ                : '!=' ;
SLASH              : '/' ;
PIPE               : '|' ;
COLON              : ':' ;
LEFT_PAREN         : '(' ;
RIGHT_PAREN        : ')' ;
EQUALS             : '=' ;
COMMA              : ',' ;
PLUS               : '+' ;
MINUS              : '-' ;
STAR               : '*' ;
PERCENT            : '%' ;
GT                 : '>' ;
LT                 : '<' ;

STRING_LITERAL     : '\'' ( '\\' . | ~['\\] )* '\'' ;
DECIMAL            : [0-9]+ '.' [0-9]+ ;
INT                : [0-9]+ ;

IDENTIFIER         : [a-zA-Z_] [a-zA-Z0-9_-]* ;

WS                 : [ \t\r\n]+ -> skip ;
