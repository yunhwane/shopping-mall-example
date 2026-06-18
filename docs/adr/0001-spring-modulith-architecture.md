# Spring Modulith architecture: events for writes, APIs for reads

The shopping mall ships as one Spring Boot application split into five modules
(`catalog`, `order`, `payment`, `member`, `inventory`), each a top-level package
under `com.example.shoppingmall`. We chose a modulith over microservices because
the system is solo-maintained and one deployable keeps operational cost near zero
while `ModularityTests.verify()` still enforces the boundaries a microservice
split would give us — without the network, the distributed transactions, or the
ops. If a module ever needs independent scaling, the enforced boundaries make
extracting it cheap.

## Module rules (enforced by `ModularityTests`)

- Each module exposes an `api` sub-package; everything else lives in `internal`
  and is invisible to other modules.
- **Writes across modules go through application events**, not direct calls.
  e.g. `order` publishes `OrderPlaced`; `inventory` decrements stock and
  `payment` initiates the charge via `@ApplicationModuleListener`.
- **Reads across modules call the target module's `api` package** and get DTOs
  back — never entities, never repositories.
- Direct access to another module's `internal` package, entities, or
  repositories fails the build.

## Reliable events

We enable the Spring Modulith Event Publication Registry (already provided by
`spring-modulith-starter-jpa`) with
`spring.modulith.events.republish-outstanding-events-on-restart: true`. Rationale:
a dropped `OrderPlaced` event means a lost stock decrement or a lost charge — a
data-integrity bug, not a cosmetic one. Persisting publications and re-delivering
on restart costs ~2 lines of config since the dependency is already present.

## Considered and rejected

- **Microservices** — rejected: operational cost unjustified for a solo project;
  modulith boundaries give the same decoupling and can be split later.
- **Events-only (no synchronous reads)** — rejected: forces read-model
  duplication across modules for simple lookups (e.g. a product price); `api`
  calls are simpler for queries.
- **In-memory fire-and-forget events** — rejected: lossy on crash, unacceptable
  for money/stock side-effects.
