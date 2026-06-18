# Test coverage: signal now, per-module ratcheting gate later

Jacoco is configured to **measure and report, not to gate**. The build emits a
single aggregate XML+HTML report; nothing fails on a low number. This is
deliberate for the current greenfield state — there is almost no domain code, so
any threshold would be a meaningless number that only generates ceremony.

## The plan

- **Now (signal):** report only, build never fails on coverage. Watch the trend.
- **When the first real module lands (gate):** add
  `jacocoTestCoverageVerification` and wire it into `check`.

### Update — first module (`catalog`) landed

The gate is on, but as **one repo-wide floor** (instruction coverage ≥ 0.70,
bootstrap class excluded), *not* a per-module rule. With a single module a
per-package rule and a repo-wide rule are identical, so the lazier form wins.
The split to **per-module absolute thresholds** (one `rule` per top-level
package, ratcheted upward) happens when the **second** module lands — that's the
first point a per-module rule does anything a repo-wide floor doesn't.

Because every module is greenfield, a per-module *absolute* threshold is, in
practice, new-code coverage — there is no legacy to drag the number down. That
lets us get "don't merge undertested new code" without diff-coverage tooling.

## What counts

Coverage measures logic we wrote, not framework wiring. Today the only exclusion
is the `@SpringBootApplication` main class (pure bootstrap). Config classes and
DTO/entity boilerplate will be excluded when the gate lands and the number starts
to matter — not before.

## Considered and rejected

- **Absolute global threshold from day one** — rejected: meaningless on an empty
  codebase, and later it would penalize the whole repo for one weak module.
- **External diff-coverage service (Codecov/Coveralls)** — rejected: adds a
  token, an integration, and a data-sharing surface not justified for a solo
  learning repo; greenfield per-module absolute thresholds achieve the same
  "test your new code" intent with zero external dependency.
- **Per-module split reports** — rejected: the HTML report already shows
  per-package percentages, and clean module boundaries make a per-module `rule` a
  one-line change later. Splitting now is premature.
