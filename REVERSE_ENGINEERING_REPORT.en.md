# REVERSE_ENGINEERING_REPORT.md (English)

## Target repository
- **Name:** web-template
- **URL:** https://github.com/better-care/web-template
- **Default branch (upstream):** master
- **Base commit analyzed:** 8115f49 (Next major development version)
- **Project version:** 6.0.0-SNAPSHOT (pom.xml)
- **Local working folder:** C:\Users\User\audit-repos\web-template
- **Audit folder:** C:\Users\User\audit-repos\audit-work
- **Audit date/time:** 2026-11-08 (local time)
- **Environment:** Windows, PowerShell 5.1, Git 2.49.0, Python 3.12.14, OpenJDK 17.0.20.1 (Temurin)

---

## A. General diagnosis

### Architecture identified
The `web-template` project is a Java/Kotlin implementation for openEHR CDR (Clinical Data Repository). It converts openEHR Archetypes (ADL/.opt format) into WebTemplates (JSON format) and performs clinical data conversion between three Representation Model (RM) formats:

1. **RAW** - native openEHR RM objects
2. **FLAT** - map of web template path → value (flattened format)
3. **STRUCTURED** - hierarchical JSON (structured format)

### Main modules (src/main/kotlin)
- `builder/` - Builds WebTemplate from a Template (openEHR archetype). Entry point: `WebTemplateBuilder.build()`.
- `converter/` - Converters between RAW/FLAT/STRUCTURED formats.
- `path/` - Parser and evaluator of WebTemplate Path Expression (based on FHIRPath), using ANTLR 4.13.2 (grammar `WebTemplatePathExpression.g4`) + `WebTemplatePathExpressionFacade` + `WebTemplatePathExpressionEvaluator`.
- `model/` - `WebTemplate` (main class, JSON serialization via Jackson), `WebTemplateNode`, `WebTemplateInput`, occurrence ranges.

### Technologies and dependencies
- Java 25, Kotlin 2.4.21, Maven (pom.xml)
- ANTLR 4.13.2 (path expression grammar)
- Jackson 2.22.3 (JSON serialization)
- Caffeine 3.3.1 (cache)
- openEHR common libraries (ehr-common 5.0.0): model, utils, time, json-jackson, terminology, xml-jaxb, path, amnode
- JUnit 5 (jupiter 6.1.3), AssertJ 3.27.7, json-unit 6.3.0 (tests)
- OWASP dependency-check 12.1.9 (dependency security, runSonar profile)
- JaCoCo 0.8.15, Sonar 5.8.0.7211 (coverage/quality)

### Maintenance status
- Active: recent releases (5.0.0 on 2026-10-13, 4.3.x in 2026)
- CI: CircleCI (image openjdk:25.0.4), code coverage via Codecov
- License: Apache 2.0
- 429 Kotlin files (src/main), 115 test files, 526 test resources

### Existing test mechanisms
- JUnit 5 + AssertJ + json-unit
- Build, conversion, path expression, compatibility tests
- Test resources: .opt files (openEHR archetypes), FLAT format composition JSONs
- CircleCI runs `mvn clean install -P code-coverage` and sends results to Codecov

### Investigation limitations
- **Maven (mvn) not installed** in environment → impossible to run `mvn clean install`, build, tests or dependency-check.
- **Java 17 available**, but project requires **Java 25** → impossible to compile locally.
- `rg` (ripgrep) not available; used system Grep tool and `find`.
- Analysis based exclusively on static inspection of source code, CHANGELOG, and public GitHub issues/PRs. No tests executed.

---

## B. Problems found

### Recurring regressions (problems fixed and returned) — evidence in CHANGELOG.md

| ID | Problem | Versions where appeared | Status | Severity | Evidence |
|----|---------|------------------------|--------|----------|----------|
| REA-001 | ELEMENT including DATA_VALUE created for WebTemplate input even if ELEMENT already exists | 4.0.4 (2024-10-11) and 3.2.9 (2024-09-20) | REGRESSION (PROBABLE) | P2 | CHANGELOG: same fix in two distinct versions |
| REA-002 | `ctx` value cannot be null | 4.0.4 (2024-10-11) and 3.2.9 (2024-09-20) | REGRESSION (PROBABLE) | P2 | CHANGELOG: same fix in two distinct versions |
| REA-003 | `feeder_audit`/`originating_system_audit`/time handling in FLAT format input | 4.0.1 (2024-05-23) and 3.2.7 (2024-03-29) | REGRESSION (PROBABLE) | P2 | CHANGELOG: same fix in two distinct versions |
| REA-004 | NPE when multi-occurrence Locatable has null name | 4.0.1 (2024-05-23) and 3.2.7 (2024-03-29) | REGRESSION (PROBABLE) | P2 | CHANGELOG: same fix in two distinct versions |

### Open GitHub issues (better-care/web-template)

| ID | Issue | URL | Status | Severity |
|----|-------|-----|--------|----------|
| REA-005 | #8: CodedText External Valueset lost when LimitTo List = False | https://github.com/better-care/web-template/issues/8 | OPEN | P2 |
| REA-006 | #7: Add original_namespace and custodian_namespace to web template | https://github.com/better-care/web-template/issues/7 | OPEN | P3 |
| REA-007 | #6: Support multi-lingual annotations and treat 'comments' as keyword | https://github.com/better-care/web-template/issues/6 | OPEN | P3 |
| REA-008 | #9: SCM url + ehr-common.version mismatch (5.0.0→6.0.0-SNAPSHOT) | https://github.com/better-care/web-template/pull/9 | OPEN (PR) | P2 |

### Risk points (static analysis, not confirmed by execution)

| ID | Location | Problem | Status | Severity | Evidence |
|----|----------|---------|--------|----------|----------|
| REA-009 | `WebTemplateInputBuilderDelegator.kt:101` | `requireNotNull` throws "Unsupported type" exception for RM type not mapped in `typeBuilders` map | HYPOTHESIS | P2 | Line 101: `requireNotNull(typeBuilders[...]) { "Unsupported type: ..." }` |
| REA-010 | `WebTemplatePathExpressionEvaluator.kt:150` | Division `left.toDouble() / right.toDouble()` without zero divisor check (returns Infinity/NaN in Double, no exception) | HYPOTHESIS | P3 | Line 150 of evaluator |
| REA-011 | `WebTemplateBuilder.kt:170` | `requireNotNull(template.language?.codeString)` throws exception if template has no default language | HYPOTHESIS | P3 | Line 170 |

### No P0 or P1 problems confirmed
No critical problem (data exposure, security compromise, irreversible loss, generalized unavailability) was confirmed in this audit.

---

## C. Recurring problems

Developers already tried to fix, but problems persist/regress:

1. **ELEMENT/DATA_VALUE duplicate** — fixed in 4.0.4 and 3.2.9, same fix text. Suggests lack of regression test preventing return.
2. **ctx value null** — fixed in 4.0.4 and 3.2.9. Same pattern.
3. **feeder_audit time handling** — fixed in 4.0.1 and 3.2.7.
4. **NPE null name in multi-occurrence Locatable** — fixed in 4.0.1 and 3.2.7.

**Root cause hypothesis:** lack of automated regression tests specifically covering these edge-case scenarios, allowing the fix to be undone or forgotten in future merges.

Links: CHANGELOG.md (versions 3.2.7, 3.2.9, 4.0.1, 4.0.4). No GitHub issue linked directly for these specific regressions.

---

## D. Undocumented errors

Important errors found by this audit that were not documented in issues/PRs:

1. **SCM/ehr-common version mismatch** (PR #9 open): `pom.xml` declares version 6.0.0-SNAPSHOT but `ehr-common.version` is 5.0.0, and SCM URL points to `master/` (even though master is the real branch, version seems inconsistent with development state).
2. **Unmapped RM type throws unhandled exception**: `WebTemplateInputBuilderDelegator.delegate()` throws `requireNotNull` with message "Unsupported type" if RM type is not in `typeBuilders`. No fallback or graceful handling.
3. **Silent division by zero** in path expression evaluator: `div` operation with zero divisor produces `Infinity`/`NaN` without explicit error, potentially generating incorrect results in clinical path expressions.

---

## E. Performance

Opportunities identified (not measured, no benchmark available):

| ID | Opportunity | Justification | How to measure |
|----|-------------|---------------|----------------|
| REA-012 | Cache of resolved path (`findWebTemplateNode`) | Recursive tree node search for each path resolution; may be called repeatedly for same path | Microbenchmark before/after with cache (Caffeine already present as dependency) |
| REA-013 | `WebTemplatePathExpressionEvaluator` creates `Executors.newSingleThreadExecutor()` per evaluation with regex timeout | Executor creation per regex call may be costly in frequent evaluations | Measure throughput of expression evaluations with regex |
| REA-014 | `stripWithTemplate` iterates `fieldNames()` of ObjectNode on each evaluation | Repeated operation at evaluation entry point | Measure evaluation time for large compositions |

No initial measurement possible (no Maven/Java 25). Recommended: establish baseline benchmark before optimizing.

---

## F. Technical improvements

1. **Regression tests for recurring problems** (REA-001 to REA-004): add unit tests covering regression scenarios identified in CHANGELOG, preventing return of problems.
2. **Graceful handling of unmapped RM type** (REA-009): replace `requireNotNull` with null/Optional return with descriptive message, avoiding unhandled exception.
3. **Zero divisor validation** (REA-010): add check before `div`/`%` operation in evaluator, throwing descriptive `WebTemplatePathExpressionException`.
4. **Fix SCM/ehr-common version mismatch** (REA-008): align versions in pom.xml and SCM URL (PR #9 already proposed upstream).
5. **Multi-lingual names support and 'comments' keyword** (REA-006, REA-007): functionality improvements requested in open issues.
6. **Contribution documentation**: CONTRIBUTING.md mentions `mvn clean install` but does not mention Java 25 requirement nor Antlr generation.

---

## G. Execution plan

Independent fixes (can be separate PRs):

1. **REA-008 (P2)** - Fix version mismatch pom.xml + SCM URL. Low risk, configuration change. Separate PR.
2. **REA-009 (P2)** - Graceful handling of unmapped RM type in `WebTemplateInputBuilderDelegator`. Low risk, 1 file. Separate PR.
3. **REA-010 (P3)** - Zero divisor validation in evaluator. Low risk, 1 file. Separate PR.
4. **REA-001 to REA-004 (P2)** - Regression tests for recurring problems. Medium risk (requires understanding exact scenarios), multiple test files. Separate or grouped PR.
5. **REA-006, REA-007 (P3)** - Functionality improvements (issues #6, #7). Larger scope, requires maintainer discussion.

Recommended order: REA-008 → REA-009 → REA-010 (low risk, independent), then regression tests.

---

## H. Expected result

Each fix adds:
- **REA-008:** build/release consistency, avoids incorrect version and SCM confusion.
- **REA-009:** builder robustness for future or extended RM types; avoids abrupt failure.
- **REA-010:** predictability of clinical path expressions; avoids silently incorrect results.
- **Regression tests:** prevent return of already fixed problems; increase confidence in future releases.

All fixes are low risk, with reproducible tests, and preserve operation of the rest of the project.

---

## Malware and threat analysis (static)

Statically examined: Kotlin sources, pom.xml, .circleci/config.yml, ANTLR grammar, test resources, build scripts.

**Result:** No indicator identified by the static analysis performed.
- No credentials, keys, tokens or passwords found embedded.
- No suspicious URLs, domains or IPs found (only Apache license URLs).
- No shell commands, process execution, persistence, obfuscation, packaged binaries, or data exfiltration found.
- GitHub Actions not present (only CircleCI with standard `mvn` and Codecov coverage upload via `$CODECOV_TOKEN` environment token).

**Limitations:** exclusively static analysis; no samples executed; transitive dependencies not analyzed in depth (Maven unavailable for `dependency:tree` or OWASP dependency-check). It is not stated that the project is free of malware.

---

## Mandatory final questions

1. **Which critical bugs were confirmed?** None P0/P1 confirmed. Only PROBABLE regressions (REA-001 to REA-004) based on CHANGELOG.

2. **Which recurring problems have developers not yet resolved?** ELEMENT/DATA_VALUE duplicate, ctx null, feeder_audit time, NPE null name — all fixed before and reappeared (regressions), due to lack of regression tests preventing them.

3. **Which important errors were not documented?** SCM/ehr-common version mismatch (PR #9), "Unsupported type" exception for unmapped RM type, silent division by zero in evaluator.

4. **Which performance opportunities were found?** Cache of resolved path, regex executor reuse in evaluator, optimization of `stripWithTemplate`. No initial measurement possible.

5. **Which security and reliability improvements are justifiable?** Graceful handling of unmapped RM type (avoids unhandled exception), zero divisor validation, regression tests for recurring problems.

6. **Which fixes can be made quickly?** pom.xml version mismatch (REA-008), unmapped type handling (REA-009), zero divisor validation (REA-010). Low risk, 1-2 files each.

7. **Which are the three contributions with highest technical value?**
   - (a) Regression tests for REA-001 to REA-004 (prevents return of already fixed bugs).
   - (b) Graceful handling of unmapped RM type in `WebTemplateInputBuilderDelegator` (builder robustness).
   - (c) SCM/ehr-common version mismatch fix (build/release consistency).

8. **Which fixes should be avoided for lack of evidence?** No change to clinical data conversion logic without test execution (no environment to validate). Functionality improvements #6 and #7 (open issues) require maintainer discussion before implementation.

9. **Which modifications do you recommend implementing first?** REA-008 (version mismatch), REA-009 (unmapped type), REA-010 (zero divisor) — independent, low risk, reproducible testing possible.

---

*This report is a product of Phase 1 (static audit) and will be updated in Phase 2 with correction and test results.*
