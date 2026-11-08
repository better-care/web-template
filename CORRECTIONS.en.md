# CORRECTIONS.md (English)

## Target repository
- **Name:** web-template
- **URL:** https://github.com/better-care/web-template
- **Default branch (upstream):** master
- **Base commit analyzed:** 8115f49
- **Project version:** 6.0.0-SNAPSHOT
- **Date of preparation:** 2026-11-08 (local time)
- **Author:** admeduardovieira (Kilo, REA 6.7.0)

---

## Correction proposals

---

### REA-008

**Title:** Fix SCM url and ehr-common.version mismatch in pom.xml  
**Severity:** P2  
**Status:** PROPOSAL (not implemented)  
**Location:** `pom.xml` (lines 36, 583-587)  
**Problem:** The `pom.xml` declares `version 6.0.0-SNAPSHOT` but `ehr-common.version` is `5.0.0`, and the SCM URL (`<url>https://github.com/better-care/web-template/tree/master/</url>`) points to `master/` without alignment with the development SNAPSHOT state. This may cause confusion in releases and builds.  
**Evidence:** PR #9 open on GitHub ("fix: SCM url + ehr-common.version mismatch (5.0.0→6.0.0-SNAPSHOT)"), lines 36 and 583-587 of pom.xml.  
**Root cause:** Misalignment between artifact version, dependency version, and SCM URL at the time of bump to 6.0.0-SNAPSHOT.  
**Impact:** Users and release tools may resolve the wrong ehr-common version; SCM points to stable branch while code is SNAPSHOT.  
**Proposed correction:** Update `ehr-common.version` to a version compatible with 6.0.0-SNAPSHOT (or adjust the artifact version) and align the SCM URL per project convention.  
**Files involved:** `pom.xml`  
**Validation test:** `mvn validate` (or `mvn dependency:resolve`) to confirm dependency resolution without version error. Not executable locally (Maven not installed, Java 17 vs Java 25 required).  
**Risk of change:** Low (build metadata change, not code logic).  
**Benefit:** Build/release consistency, avoids incorrect dependency resolution.  
**Suggested PR:** `fix: align ehr-common version and SCM url for 6.0.0-SNAPSHOT`  
**Priority:** Immediate  

---

### REA-009

**Title:** Graceful handling of unmapped RM type in WebTemplateInputBuilderDelegator  
**Severity:** P2  
**Status:** PROPOSAL (not implemented)  
**Location:** `src/main/kotlin/care/better/platform/web/template/builder/input/WebTemplateInputBuilderDelegator.kt` (lines 100-103 and 106-110)  
**Problem:** `delegate()` uses `requireNotNull(typeBuilders[...]) { "Unsupported type: ..." }`, throwing an unhandled exception when an RM type is not mapped in the `typeBuilders` map. No fallback nor graceful return.  
**Evidence:** Lines 100-103: `with(requireNotNull(typeBuilders[RmUtils.getNonGenericRmNamePart(amNode.rmType)]) { "Unsupported type: ${amNode.rmType}" })`.  
**Root cause:** The `typeBuilders` map does not cover all possible RM types; absence of handling for unknown types.  
**Impact:** Web template build fails abruptly for unmapped RM types, without orienting message nor alternative.  
**Proposed correction:** Replace `requireNotNull` with `null` return (or Optional-like) when the type is not mapped, allowing the caller to decide (skip input, log warning, or throw controlled exception).  
**Files involved:** `WebTemplateInputBuilderDelegator.kt`, possibly `WebTemplateBuilder.kt` (caller in `addInputs`).  
**Validation test:** Unit test passing an `AmNode` with `rmType` not present in `typeBuilders` and verifying no unhandled exception is thrown. Not executable locally (no build).  
**Risk of change:** Low (localized change in one delegator method).  
**Benefit:** Builder robustness for future or extended RM types; avoids abrupt failure.  
**Suggested PR:** `fix: handle unmapped RM type gracefully in WebTemplateInputBuilderDelegator`  
**Priority:** Immediate  

---

### REA-010

**Title:** Validate divisor zero in path expression arithmetic operations  
**Severity:** P3  
**Status:** PROPOSAL (not implemented)  
**Location:** `src/main/kotlin/care/better/platform/web/template/path/evaluator/WebTemplatePathExpressionEvaluator.kt` (line 150)  
**Problem:** `DIV` operation (`left.toDouble() / right.toDouble()`) and `MOD` (`left.toDouble() % right.toDouble()`) do not check if divisor is zero. In Kotlin/Double, division by zero returns `Infinity` or `NaN` without exception, producing silently incorrect results in clinical path expressions.  
**Evidence:** Line 150: `PathOperator.DIV -> left.toDouble() / right.toDouble()` and line 151: `PathOperator.MOD -> left.toDouble() % right.toDouble()`.  
**Root cause:** Absence of zero divisor validation before arithmetic operation.  
**Impact:** Path expressions with zero divisor return `Infinity`/`NaN` without explicit error, potentially generating incorrect clinical data without the user noticing.  
**Proposed correction:** Add check `if (right == 0) throw WebTemplatePathExpressionException("Division by zero")` before `DIV` and `MOD` operations.  
**Files involved:** `WebTemplatePathExpressionEvaluator.kt`  
**Validation test:** Unit test evaluating expression `10 div 0` and verifying `WebTemplatePathExpressionException` is thrown. Not executable locally (no build).  
**Risk of change:** Low (addition of validation at one arithmetic operation point).  
**Benefit:** Predictability and correctness of clinical path expressions; avoids silently incorrect results.  
**Suggested PR:** `fix: validate divisor zero in path expression arithmetic`  
**Priority:** Low  

---

### REA-001 to REA-004 (regression tests)

**Title:** Add regression tests for recurring bugs (ELEMENT/DATA_VALUE, ctx null, feeder_audit time, NPE null name)  
**Severity:** P2  
**Status:** PROPOSAL (not implemented)  
**Location:** `src/test/kotlin/care/better/platform/web/template/` (new test files)  
**Problem:** Problems fixed in previous versions reappeared (regressions), indicating absence of regression tests preventing their return.  
**Evidence:** CHANGELOG.md — same fix applied in distinct versions (4.0.4 and 3.2.9; 4.0.1 and 3.2.7).  
**Root cause:** Absence of automated tests specifically covering edge-case scenarios already fixed.  
**Impact:** Risk of already-fixed bugs returning in future releases.  
**Proposed correction:** Add unit tests reproducing each regression scenario identified.  
**Files involved:** New test files in `src/test/kotlin/care/better/platform/web/template/build/` and `src/test/kotlin/care/better/platform/web/template/converter/`.  
**Validation test:** Execution of new tests with `mvn test`. Not executable locally.  
**Risk of change:** Low (addition of tests, no production code change).  
**Benefit:** Prevents regression of already fixed problems; increases release confidence.  
**Suggested PR:** `test: add regression tests for recurring bugs`  
**Priority:** High  

---

## Corrections already performed in this execution

No corrections were implemented in this execution (Phase 1 completed, Phase 2 pending). Reason: environment without Maven and without Java 25, preventing build, compilation and test execution to validate any code change.

---

## Uncorrected problems and remaining risks

- All proposed corrections (REA-008, REA-009, REA-010, regression tests REA-001 to REA-004) remain as proposals awaiting implementation and validation in an environment with Maven/Java 25.
- Functionality improvements (GitHub issues #6, #7) were not addressed; require maintainer discussion.
- OWASP dependency-check analysis not executed (Maven unavailable).

---

*This document will be updated in Phase 2 as corrections are implemented, validated and published.*
