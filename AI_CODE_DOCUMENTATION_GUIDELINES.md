# AI Code Documentation Guidelines

This note is a project-specific guide for AI-assisted code documentation.
Use it to produce consistent, useful, low-noise comments.

## 1. Goal

Documentation must answer, quickly:

1. What this code does.
2. When it triggers.
3. What contract it returns or enforces.
4. Why this behavior exists (only when not obvious).

Write for maintainers who did not author the code.

## 2. Non-Negotiable Principles

1. Clarity over cleverness.
2. Intent over implementation narration.
3. Contract over internal trivia.
4. Stable wording over noisy, changing details.
5. Keep comments synchronized with behavior changes.

## 3. Preferred Comment Architecture

Use all three layers in medium/large classes.

### 3.1 Section banners (grouping)

Use multiline decorated banners to partition related logic.

```java
/*============================================================
  VALIDATION EXCEPTIONS
  Bean validation and request parsing failures
============================================================*/
```

### 3.2 Scanner line before handlers

Required for exception handlers.

Format:

```java
// -> Triggers: <concise trigger> || Returns: <Status Name> (<code>)
```

Example:

```java
// -> Triggers: malformed JSON / unreadable body || Returns: Bad Request (400)
```

### 3.3 Javadoc for methods

Use Javadoc for public methods and non-trivial private helpers.

Minimum shape:

```java
/**
 * One-line purpose with domain meaning.
 *
 * @param x what matters about this input
 * @param y what matters about this input
 * @return contract-level output
 */
```

## 4. Style Rules (strict)

Direct, explicit, concise. Active voice. Domain vocabulary only. No vague terms ("stuff", "things", "some logic"). No jokes in permanent docs. Avoid repeating the method name in sentence form.

## 5. AI Workflow: How to Document a File

When an AI updates documentation, follow this sequence:

1. Detect responsibilities in the class.
2. Group methods into meaningful sections.
3. Add/normalize section banners.
4. For each handler, add scanner trigger/status line.
5. Add or tighten Javadoc with purpose, params that affect behavior, return contract, and throws only when part of the contract.
6. Remove filler comments that restate obvious syntax.
7. Validate that comments still match behavior.

## 6. What to Document

1. Business intent of methods.
2. Input conditions that change behavior.
3. Output contract (status, payload shape, side effects).
4. Security-sensitive constraints.
5. Observability behavior (structured logs, metrics tags).

## 7. What Not to Document

1. Obvious control flow.
2. Java syntax semantics.
3. Temporary implementation detail likely to churn.
4. Comments with no actionable meaning.

## 8. Exception Handler Documentation Standard

For each `@ExceptionHandler`:

1. Add scanner line in this exact format: `// -> Triggers: ... || Returns: Name (code)`.
2. Add Javadoc that states failure category, key params (`ex`, `request`), and response contract (`ProblemDetail`, status).
3. Keep user-facing messages safe (no secrets/internal traces).
4. Ensure status mapping is explicit and stable.

## 9. Javadoc Quality Bar

Good Javadoc is:

1. Specific to domain behavior.
2. Short (usually 1-3 lines of description).
3. Focused on contract, not line-by-line mechanics.
4. Updated when method behavior changes.

Bad Javadoc examples:

1. "This method handles exceptions".
2. "Sets variable value".
3. Copy-paste text that does not match current logic.

## 10. Presentation Improvements AI Should Apply

When possible, AI should improve readability by:

1. Ordering sections from external contract to internal helpers.
2. Keeping consistent wording across similar handlers.
3. Using one terminology set (`request body`, `validation`, `conflict`, etc.).
4. Keeping scanner comments parallel in shape for quick visual parsing.

## 11. Maintenance Rule

If behavior changes, update related comments in the same commit.
If code and comment diverge, fix the comment immediately.
