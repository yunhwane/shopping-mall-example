# Contributing

Solo project, GitHub Flow. Humans and AI agents follow the same loop.

## The loop

1. **Issue first.** Every change starts as a GitHub issue in
   `yunhwane/shopping-mall-example`. Triage labels decide who picks it up:
   - `ready-for-agent` — a Claude Code agent can take it.
   - `ready-for-human` — you take it.
   - (`needs-triage`, `needs-info`, `wontfix` per `docs/agents/triage-labels.md`.)
2. **Branch off `main`**, named `<type>/<issue#>-<slug>`
   (e.g. `feat/12-order-checkout`). Types: `feat`, `fix`, `chore`, `docs`, `refactor`.
3. **Open a PR** with `Closes #<issue>` in the body.
4. **CI must be green** (`./gradlew build` — compiles, runs all tests including
   `ModularityTests` boundary checks).
5. **Human merges.** Squash-merge only; branch auto-deletes. Squash closes the issue.

## Rules

- `main` is protected: no direct pushes, CI must pass before merge.
- No review-approval gate (solo), but the **merge button is always pressed by a human**.
- Architecture is enforced by tests, not review — keep module boundaries
  (see `docs/adr/0001-spring-modulith-architecture.md`). Writes cross modules via
  events; reads via a module's `api` package.

## Local check before pushing

```
./gradlew build
```
