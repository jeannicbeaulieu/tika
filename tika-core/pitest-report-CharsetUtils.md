# PIT Mutation Testing Report — `CharsetUtils`

**Date:** 2026-10-10  
**Tool:** PIT 1.25.7 + pitest-junit5-plugin  
**Class under test:** `org.apache.tika.utils.CharsetUtils`  
**Test class:** `org.apache.tika.utils.CharsetUtilsTest`  
**Module:** `tika-core`

---

## Summary

| Metric              | Value          |
|---------------------|----------------|
| Number of classes   | 1              |
| Line coverage       | 78% (63 / 81)  |
| Mutation coverage   | 80% (20 / 25)  |
| Test strength       | 100% (20 / 20) |

> **Test strength** measures the proportion of detected mutants among those that were actually covered by tests (i.e., excluding NO_COVERAGE mutants). A score of 100% means every mutation that was reachable by a test was successfully killed.

---

## Mutation Results by Line

### ✅ KILLED mutations (detected — tests are effective)

| Line | Code | Mutation | Status |
|------|------|----------|--------|
| 107 | `if (isSupportedICU != null && (Boolean) isSupportedICU.invoke(null, charsetName))` | negated conditional | KILLED |
| 110 | `return Charset.isSupported(charsetName);` | replaced boolean return with `false` | KILLED |
| 110 | `return Charset.isSupported(charsetName);` | replaced boolean return with `true` | KILLED |
| 112 | `return false;` | replaced boolean return with `true` | KILLED |
| 115 | `return false;` | replaced boolean return with `true` | KILLED |
| 131 | `return forName(charsetName).name();` | replaced return value with `""` | KILLED |
| 133 | `return null;` | replaced return value with `""` | KILLED |
| 144 | `if (name == null)` | negated conditional | KILLED |
| 150 | `if (!m.matches())` | negated conditional | KILLED |
| 157 | `if (charset != null)` | negated conditional | KILLED |
| 158 | `return charset;` | replaced return value with `null` | KILLED |
| 159 | `} else if ("none".equals(lower) \|\| "no".equals(lower))` | negated conditional (×2) | KILLED |
| 165 | `if (iso.matches())` | negated conditional | KILLED |
| 169 | `} else if (cp.matches())` | negated conditional | KILLED |
| 173 | `} else if (win.matches())` | negated conditional | KILLED |
| 178 | `if (charset != null)` | negated conditional | KILLED |
| 179 | `return charset;` | replaced return value with `null` | KILLED |
| 183 | `if (getCharsetICU != null)` | negated conditional | KILLED |
| 195 | `return Charset.forName(name);` | replaced return value with `null` | KILLED |

---

### ⚠️ SURVIVED mutations (not detected — potential test gaps)

| Line | Code | Mutation | Status | Reason |
|------|------|----------|--------|--------|
| 107 | `if (isSupportedICU != null && ...)` | negated conditional | NO_COVERAGE | ICU4J branch not exercised |
| 108 | `return true;` (inside ICU branch) | replaced boolean return with `false` | NO_COVERAGE | ICU4J branch not exercised |
| 118 | `return false;` (in catch block) | replaced boolean return with `true` | NO_COVERAGE | Exception path in `isSupported` not tested |
| 186 | `if (cs != null)` | negated conditional | NO_COVERAGE | ICU4J code path in `forName` not exercised |
| 187 | `return cs;` | replaced return value with `null` | NO_COVERAGE | ICU4J code path in `forName` not exercised |

---

## Uncovered Lines (0% line coverage)

These lines were never executed by any test. They all fall in two categories:

### 1. ICU4J integration code (lines 69–77, 108, 185–192)
Code that loads and uses `com.ibm.icu.charset.CharsetICU` via reflection. ICU4J is an optional dependency not present in the test classpath, so the entire ICU branch is unreachable during testing.

```
Line 69:  getCharsetICU = icuCharset.getMethod("forNameICU", String.class);
Line 74:  isSupportedICU = icuCharset.getMethod("isSupported", String.class);
Line 108: return true;           // ICU isSupported branch
Line 185: Charset cs = (Charset) getCharsetICU.invoke(null, name);
Line 186: if (cs != null) {
Line 187: return cs;
```

### 2. Exception-handling paths (lines 70–72, 75–77, 92, 116–118, 189–192)
Catch blocks for `IllegalArgumentException`, `IllegalCharsetNameException`, and ICU-related reflection exceptions. These require either invalid input or deliberate fault injection to reach.

```
Line 92:  } catch (IllegalArgumentException e) {
Line 116: } catch (Exception e) {
Line 118: return false;
```

---

## Analysis Notes

1. **Mutation coverage is strong (80%) for standard code paths.** All conditional branches and return values in the main `forName()` and `clean()` logic are effectively killed by the existing tests.

2. **The 5 surviving mutants are all `NO_COVERAGE`**, not true survivors. This means none of them were reached by any test — they are not surviving in the presence of tests, they are simply unreachable code paths (ICU4J absence + unexercised exception handlers).

3. **Test strength is 100%**: every mutation the tests did reach was caught. There are no "weak" assertions.

4. **To improve mutation coverage to 100%**, two things would be needed:
   - Tests for exception-handling paths in `isSupported` and `forName` (inject `IllegalArgumentException` etc.).
   - ICU4J-dependent paths are structurally untestable without adding the ICU4J dependency to the test scope, or mocking the reflection calls.

---

## Methods Covered

| Method | Covered | Notes |
|--------|---------|-------|
| `isSupported(String)` | Partial | ICU branch and its catch block uncovered |
| `clean(String)` | Full | All mutations killed |
| `forName(String)` | Partial | ICU `forNameICU` branch uncovered |
| `initCommonCharsets(...)` | N/A | Static initializer, not directly mutated |
