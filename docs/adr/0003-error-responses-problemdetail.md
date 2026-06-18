# Error responses are RFC 9457 ProblemDetail; no success envelope

Every module's HTTP error responses use Spring's built-in `ProblemDetail`
(RFC 9457, `application/problem+json`). Success responses return the DTO (or
`Page<DTO>`) directly — there is **no** common `{ code, message, data }` success
envelope.

## Why

- **Errors need one consistent shape**, and Spring Boot already ships it.
  `ProblemDetail` gives `{ type, title, status, detail, instance }` with zero
  custom classes. We enable it for framework-thrown exceptions with
  `spring.mvc.problemdetails.enabled: true`, so a `ResponseStatusException`
  (e.g. 404) and a `@ExceptionHandler`-mapped `IllegalArgumentException` (400)
  render the same way. Native platform feature beats a hand-rolled error type.
- **A success envelope is ceremony.** HTTP status already signals success and
  the DTO already carries the payload. Wrapping every 200 in `{ data: ... }`
  only forces every controller to wrap and every client to unwrap. We add one
  only if a client contract ever mandates a fixed wrapper — not before.

## How (the convention modules follow)

- Validation/domain-guard failures (`require(...)` → `IllegalArgumentException`)
  → `400` via a module `@RestControllerAdvice` returning
  `ProblemDetail.forStatusAndDetail(BAD_REQUEST, message)`.
- Expected misses (entity not found, etc.) → throw
  `ResponseStatusException(NOT_FOUND, detail)`; Spring renders the ProblemDetail.
- Reference implementation: `catalog.internal.CatalogExceptionHandler`.

## Considered and rejected

- **Custom `ApiResponse<T>` / `CommonResponse` envelope on every response** —
  rejected: duplicates what HTTP status + DTO already convey, couples every
  client to an unwrap step, and is the canonical over-engineering trap. No
  client requires it today.
- **A custom error DTO** instead of `ProblemDetail` — rejected: reinvents a
  standardized, framework-provided structure (RFC 9457) for no gain.
- **Global `@RestControllerAdvice` shared across modules** — deferred: each
  module owns its advice in `internal` for now; promote to a shared one only if
  duplication becomes real. (`IllegalArgumentException` → 400 is currently the
  only rule, and it lives with catalog.)
