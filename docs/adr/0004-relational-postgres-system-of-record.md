# Relational Postgres as the single system of record

The shopping mall is a money-and-integrity workload — orders, line items,
payments, and inventory reservations are multi-entity transactions that want
ACID and constraints. We ratify **relational** as the persistence model and
**PostgreSQL** as the production engine. No requirement today pushes any part of
the system to NoSQL, so we don't adopt one. The codebase already leaned this way
(Spring Data JPA + Hibernate); this ADR makes it a deliberate choice rather than
an accident of the starter.

## Decisions

- **Engine.** Postgres in prod. It grows into the NoSQL-ish needs we deferred
  (JSONB, full-text search) without a second datastore.
- **Test against the real engine.** Integration tests run on **Testcontainers
  Postgres**; H2 is retired. We stop trusting an emulator for money paths. Pure
  unit specs stay off the database entirely.
- **Schema is owned by Flyway.** Versioned `V*.sql` migrations are the system of
  record for schema; Hibernate runs `ddl-auto: validate` and never mutates.
  Testcontainers applies the same migrations, so tests and prod share one schema
  definition. Flyway over Liquibase because we are Postgres-only — plain SQL
  beats a DB-agnostic changelog abstraction we don't need.
- **One shared schema, module-respecting data.** Single database, single schema.
  Foreign keys are allowed **within** a module (e.g. Order → OrderLine) but
  **never across** modules. Cross-module references are by bare ID (an `Order`
  stores `productId: Long`, not an FK into Catalog's table), enforced by
  `ModularityTests`, not by the DB. This keeps the data boundary aligned with the
  event/published-API boundary and keeps later module extraction cheap.
- **Money on disk.** Monetary columns are `numeric(12,2)` (`@Column(precision =
  12, scale = 2)`): two decimals, ~9.9 billion ceiling, enforced by the DB.
  **Single currency** — no currency column. Multi-currency is deferred; adding a
  `currency` column later is a cheap additive migration.

## When to revisit NoSQL

We chose relational; we did not ban NoSQL. Postgres stays the system of record —
any NoSQL store joins only as a **derived / cache layer**, never a replacement.
Named triggers:

- **Product search** outgrows the current `LIKE` full-scan → Postgres full-text
  search first, a dedicated search index (e.g. Elasticsearch) only if FTS isn't
  enough.
- **Cart / session** state needs TTL and high churn → Redis as a cache.
- **A read model** gets hot enough to need denormalization → materialized view or
  a projection store.

## Considered and rejected

- **NoSQL as system of record** — rejected: no access pattern, scale number, or
  schema-flexibility need justifies giving up ACID and constraints on money data.
- **MySQL** — fine, but Postgres has the edge on constraint/transaction semantics,
  which is the entire reason we picked relational.
- **H2 for tests** — rejected: dialect gap with Postgres lets bugs pass green in
  CI and break in prod. Worth the Docker dependency and slower integration tests.
- **Hibernate `ddl-auto` managing prod schema** — rejected: unreviewed,
  un-rollback-able schema mutation on a database that holds money.
- **Schema-per-module / datasource-per-module** — rejected as premature: buys
  nothing for a single deployable today, and no-cross-module-FK already delivers
  the decoupling that would make extraction cheap.
