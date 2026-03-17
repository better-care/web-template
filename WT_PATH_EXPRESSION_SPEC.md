# Web Template path expression

## 1. Background

Web Template path expression is a path-based navigation and extraction language for openEHR RM data.
It is based on [FHIRPath](https://hl7.org/fhirpath/) — a platform-independent, model-agnostic language for traversing and extracting data from hierarchical structures.

While FHIRPath was designed for FHIR resources, Web Template Path adapts its core concepts — collection-based evaluation, function chaining, operators, and type system — to work with openEHR RM objects in **FLAT**, **STRUCTURED**, and **RAW** formats.

### Key differences

| Aspect              | FHIRPath       | Web Template Path                         |
|---------------------|----------------|-------------------------------------------|
| Data model          | FHIR resources | openEHR RM data (flat, structured, raw)   |
| Path separator      | `.` (dot)      | `/` (slash)                               |
| Occurrence indexing | N/A            | `:index` syntax (e.g., `any_event:0`)     |
| Attribute access    | `.` (dot)      | `\|` (pipe, e.g., `systolic\|magnitude`)  |
| Predicate filters   | `.where(expr)` | `.where(\|attr op value)`                 |
| Function chaining   | `.` (dot)      | `.` (e.g., `systolic\|magnitude.count()`) |
| Division operator   | `/`            | `div` keyword                             |

## 2. Navigation Model

Web Template path expression operates on an openEHR RM object using web template. 
Expressions navigate this data and return **collections** — ordered, non-unique lists of nodes or values. Even a single value is treated as a collection of one element.
FLAT and RAW inputs are automatically converted to STRUCTURED format before evaluation, and results are converted to the desired output format.

## 3. Path Expressions

### 3.1 Path navigation

Paths navigate the composition tree using `/`:

```
blood_pressure                              
blood_pressure/any_event/systolic    
```

### 3.2 Occurrence indexing

Steps can include a zero-based occurrence index using `:` syntax to select a specific repetition:

```
blood_pressure:0                          
blood_pressure:0/any_event:0/systolic       
```

### 3.3 Attribute access

Attributes are accessed using the `|` (pipe) operator:

```
blood_pressure/any_event/systolic|magnitude
blood_pressure/any_event/systolic|unit
```

### 3.4 Predicate filtering (where clause)

Predicates filter nodes by attribute value. All comparison operators are supported, and values can be string, numeric or boolean literals:

**Supported operators:** `=`, `!=`, `>`, `>=`, `<`, `<=`

```
any_event.where(|code = 'at0004')
any_event.where(|code != 'at0004')
blood_pressure/any_event.where(|code = 'at0004')/systolic|magnitude
any_event:0.where(|code = 'at0004')
systolic.where(|count > 5)
systolic.where(|score >= 3.5)
systolic.where(|active = true)
```

Values can be string literals (`'...'`), numeric literals (`5`, `3.5`), or boolean literals (`true`, `false`).
Boolean values only support `=` and `!=` operators.

## 4. Expressions

### 4.1 Literals

The language supports the following literal types:

| Type    | Examples             | Description                       |
|---------|----------------------|-----------------------------------|
| Integer | `42`, `0`            | Whole numbers                     |
| Decimal | `1.5`, `3.14`        | Floating-point numbers            |
| String  | `'hello'`, `'it\'s'` | Single-quoted, backslash escaping |
| Boolean | `true`, `false`      | Boolean values                    |

### 4.2 Context variables

Special variables provide access to iteration context:

| Variable | Description                             |
|----------|-----------------------------------------|
| `$this`  | Current item in iteration context       |
| `$index` | Current index in iteration context      |
| `$total` | Accumulator value (in `aggregate` only) |

### 4.3 Parenthesized expressions

Any expression can be wrapped in parentheses for grouping or to enable function chaining on computed values:

```
(-5).abs()
(2 + 3).toString()
```

## 5. Operators

### 5.1 Arithmetic operators

| Operator | Description                                                      |
|----------|------------------------------------------------------------------|
| `*`      | Multiplication                                                   |
| `div`    | Division (keyword, e.g., `a div b`)                              |
| `%`      | Modulo                                                           |
| `+`      | Addition (or string concatenation if either operand is a string) |
| `-`      | Subtraction (also unary negation)                                |

### 5.2 Comparison operators

| Operator | Description           |
|----------|-----------------------|
| `>`      | Greater than          |
| `>=`     | Greater than or equal |
| `<`      | Less than             |
| `<=`     | Less than or equal    |

### 5.3 Equality operators

| Operator | Description |
|----------|-------------|
| `=`      | Equal       |
| `!=`     | Not equal   |

### 5.4 Boolean operators

| Operator  | Description                            |
|-----------|----------------------------------------|
| `and`     | Logical AND                            |
| `or`      | Logical OR                             |
| `xor`     | Logical XOR                            |
| `implies` | Logical implication (`!left or right`) |

### 5.5 Operator precedence (highest to lowest)

1. `*`, `%`, `div` (multiplicative)
2. `+`, `-` (additive)
3. `>`, `>=`, `<`, `<=` (comparison)
4. `=`, `!=` (equality)
5. `and`
6. `or`
7. `xor`
8. `implies`

## 6. Functions

Functions are invoked using `.functionName(args)` syntax. Multiple functions can be chained, and functions can be called on any expression including literals:

```
blood_pressure/any_event.first().count()
42.toString()
true.not()
```

### 6.1 Existence

| Function   | Description                                  |
|------------|----------------------------------------------|
| `exists()` | `true` if collection is not empty            |
| `empty()`  | `true` if collection is empty                |
| `count()`  | Number of items in the collection            |
| `single()` | Assert exactly one element (error otherwise) |

### 6.2 Filtering and projection

| Function       | Description                                    |
|----------------|------------------------------------------------|
| `select(expr)` | Evaluate expression for each element (flatMap) |
| `repeat(expr)` | Recursive projection until stable              |

### 6.3 Subsetting

| Function  | Description               |
|-----------|---------------------------|
| `first()` | First element             |
| `last()`  | Last element              |
| `tail()`  | All elements except first |
| `skip(n)` | Skip first N elements     |
| `take(n)` | Take first N elements     |

### 6.4 Combining

| Function          | Description                                   |
|-------------------|-----------------------------------------------|
| `union(expr)`     | Combine current + other (deduplicated)        |
| `combine(expr)`   | Merge current + other (preserving duplicates) |
| `intersect(expr)` | Items in both collections                     |
| `exclude(expr)`   | Items in current but not in other             |

### 6.5 Collection testing

| Function           | Description                      |
|--------------------|----------------------------------|
| `sort()`           | Sort by natural order            |
| `distinct()`       | Remove duplicate values          |
| `isDistinct()`     | `true` if all values are unique  |
| `subsetOf(expr)`   | `true` if current values ⊆ other |
| `supersetOf(expr)` | `true` if current values ⊇ other |

### 6.6 Boolean aggregate functions

| Function     | Description                                |
|--------------|--------------------------------------------|
| `all()`      | `true` if all values are evaluated to true |
| `any()`      | `true` if any value is evaluated to true   |
| `none()`     | `true` if no value is evaluated to true    |
| `allTrue()`  | `true` if all values are exactly `true`    |
| `anyTrue()`  | `true` if any value is exactly `true`      |
| `allFalse()` | `true` if all values are exactly `false`   |
| `anyFalse()` | `true` if any value is exactly `false`     |

### 6.7 Math aggregate functions

| Function | Description               |
|----------|---------------------------|
| `sum()`  | Sum of all numeric values |
| `min()`  | Minimum value             |
| `max()`  | Maximum value             |
| `avg()`  | Average of all values     |

### 6.8 Math functions

| Function           | Description                   |
|--------------------|-------------------------------|
| `abs()`            | Absolute value                |
| `ceiling()`        | Round up to nearest integer   |
| `floor()`          | Round down to nearest integer |
| `truncate()`       | Truncate toward zero          |
| `round()`          | Round to nearest integer      |
| `round(precision)` | Round to N decimal places     |
| `sqrt()`           | Square root                   |
| `power(exp)`       | Raise to exponent             |
| `ln()`             | Natural logarithm             |
| `log(base)`        | Logarithm with given base     |
| `exp()`            | e raised to the value         |

### 6.9 String functions 

| Function                      | Description                             |
|-------------------------------|-----------------------------------------|
| `lower()`                     | Lowercase                               |
| `upper()`                     | Uppercase                               |
| `length()`                    | Character count                         |
| `indexOf(str)`                | Position of substring (-1 if not found) |
| `startsWith(str)`             | Test prefix                             |
| `endsWith(str)`               | Test suffix                             |
| `contains(str)`               | Test containment                        |
| `substring(start)`            | Substring from index                    |
| `substring(start, length)`    | Substring with max length               |
| `matches(regex)`              | Test against regex                      |
| `replace(old, new)`           | Literal replacement                     |
| `replaceMatches(regex, repl)` | Regex replacement                       |
| `toChars()`                   | Split into characters                   |

### 6.10 Type conversion functions

| Function      | Description        |
|---------------|--------------------|
| `toInteger()` | Convert to integer |
| `toDecimal()` | Convert to decimal |
| `toBoolean()` | Convert to boolean |
| `toString()`  | Convert to string  |

### 6.11 Boolean functions

| Function | Description          |
|----------|----------------------|
| `not()`  | Negate boolean value |

### 6.12 Type functions

| Function       | Description                   |
|----------------|-------------------------------|
| `ofType(type)` | Filter elements by type       |

Supported type names for `ofType`:
- Primitive types: `string`, `number`, `decimal`, `integer`, `boolean`
- openEHR RM types: `OBSERVATION`, `EVALUATION`, `INSTRUCTION`, `ACTION`, `CLUSTER`, `DV_QUANTITY`, `DV_CODED_TEXT`, etc. (case-insensitive)

### 6.13 Date/Time functions

| Function      | Description                    |
|---------------|--------------------------------|
| `now()`       | Current DateTime as ISO string |
| `today()`     | Current Date as ISO string     |
| `timeOfDay()` | Current Time as ISO string     |

### 6.14 Tree navigation

| Function        | Description                     |
|-----------------|---------------------------------|
| `children()`    | Immediate child fields/elements |
| `descendants()` | All descendants recursively     |

### 6.15 Expression-based functions

| Function                       | Description                         |
|--------------------------------|-------------------------------------|
| `iif(criterion, true, false?)` | Conditional evaluation              |
| `aggregate(expr, init?)`       | Fold with `$total`/`$this`/`$index` |
| `repeat(expr)`                 | Recursive projection until stable   |

## 7. Result Types

### 7.2 Output formats

The evaluation of Web Template path expression on `WebTemplate` return `List<Any?>`. The actual types depend on the requested format:

| Format       | Primitives                            | Complex nodes       |
|--------------|---------------------------------------|---------------------|
| `STRUCTURED` | `Number`, `String`, `Boolean`, `null` | `JsonNode`          |
| `FLAT`       | `Number`, `String`, `Boolean`, `null` | `Map<String, Any?>` |
| `RAW`        | `Number`, `String`, `Boolean`, `null` | `RmObject`          |

There is no separate scalar type — even single values like `count()` or `exists()` are returned as a list containing one element.

FLAT and RAW inputs are automatically converted to STRUCTURED format before evaluation. Results are then converted to the requested output format.

### 7.3 RM type filtering with `ofType()`

`ofType()` supports both primitive types (`string`, `number`, `integer`, `boolean`) and openEHR RM types (`OBSERVATION`, `EVALUATION`, `CLUSTER`, `DV_QUANTITY`, etc.):

```
blood_pressure.ofType('OBSERVATION')     // filter by RM type
systolic|magnitude.ofType('number')      // filter by JSON type
```

## 8. API Usage

### 8.1 EvaluationContext

`EvaluationContext` controls the scope and conversion settings for expression evaluation.
It allows evaluating expressions against any RM object (not just full compositions) by specifying the WebTemplate of AQL path.

```kotlin
data class EvaluationContext(
    val webTemplatePath: String? = null,    // e.g., "blood_pressure/any_event"
    val aqlPath: String? = null,            // e.g., "/content[openEHR-EHR-OBSERVATION.blood_pressure.v2]"
    val valueConverter: ValueConverter? = null,
    val objectMapper: ObjectMapper? = null
)
```

| Field             | Description                                       |
|-------------------|---------------------------------------------------|
| `webTemplatePath` | Web template path to the root node for evaluation |
| `aqlPath`         | AQL path to the root node for evaluation          |
| `valueConverter`  | `ValueConverter` for RAW conversion               |
| `objectMapper`    | `ObjectMapper` for RAW conversion                 |

Only one of `webTemplatePath` or `aqlPath` may be set. When neither is set, the composition root is used.

Factory methods are provided for convenience:

```kotlin
EvaluationContext.ofWebTemplatePath("blood_pressure")
EvaluationContext.ofAqlPath("/content[openEHR-EHR-OBSERVATION.blood_pressure.v2]")
```

For RAW input, `EvaluationContext` is used to construct the `FromRawConversion` internally.

### 8.2 Kotlin / Java

```kotlin
// FLAT input (default output: FLAT)
val result = webTemplate.evaluateExpression("blood_pressure/any_event/systolic|magnitude", flatRmObject)

// STRUCTURED input (default output: STRUCTURED)
val result = webTemplate.evaluateExpression("blood_pressure.count()", structuredRmObject)

// RAW input (default output: STRUCTURED)
val result = webTemplate.evaluateExpression("blood_pressure.count()", rmObject)

// FLAT input → RAW output
val result = webTemplate.evaluateExpression("blood_pressure", flatRmObject, RmObjectFormat.RAW)

// STRUCTURED input → FLAT output
val result = webTemplate.evaluateExpression("blood_pressure/any_event/systolic", structuredRmObject, RmObjectFormat.FLAT)

// Evaluation on a subtree RM object
val result = webTemplate.evaluateExpression(
    "any_event/systolic|magnitude",
    bloodPressureNode,
    evaluationContext = EvaluationContext(webTemplatePath = "vital_signs/blood_pressure"))

val result = webTemplate.evaluateExpression(
    "any_event/systolic|magnitude",
    bloodPressureNode,
    evaluationContext = EvaluationContext.ofAqlPath("/content[openEHR-EHR-OBSERVATION.blood_pressure.v2]"))

// Parse without evaluating
val expression = webTemplate.parseExpression("blood_pressure.count()")
```

### 8.3 Input formats

#### Flat format

Keys follow the pattern `root/segment:index/...|attribute`:

```json
{
  "vital_signs/blood_pressure:0/any_event:0/systolic|magnitude": 120,
  "vital_signs/blood_pressure:0/any_event:0/systolic|unit": "mm[Hg]"
}
```

#### Structured format

```json
{
  "vital_signs": {
    "blood_pressure": [{
      "any_event": [{
        "systolic": [{ "|magnitude": 120, "|unit": "mm[Hg]" }]
      }]
    }]
  }
}
```

Metadata keys (e.g., `ctx`) are excluded.

#### Raw format

Standard openEHR RM objects (`Composition`, `Observation`, etc.) are accepted directly. They are converted to STRUCTURED format internally before evaluation.

## 9. References

- [FHIRPath Specification](https://hl7.org/fhirpath/) — the foundation for this language's design
- [FHIR](https://hl7.org/fhir/) — the healthcare data standard that FHIRPath was originally designed for
- [openEHR](https://www.openehr.org/) — the clinical modeling standard used by web template
