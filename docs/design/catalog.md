# Catalog module — design

Status: agreed, not yet implemented. First real domain module in the modulith.

## Scope

Catalog owns **Products and the prices shown to shoppers** — nothing else.
Iteration one is *only* `Product`: id, name, price, active. No categories,
brands, images, variants/SKUs, price history, or search filters. Those land
when a concrete consumer asks for them.

Out of scope (per `CONTEXT.md`): inventory/stock, multi-currency.

## Domain model

```kotlin
@Entity
class Product(
    var name: String,
    var price: BigDecimal,   // KRW, no currency field until a 2nd currency exists
    var active: Boolean = true,
) {
    @Id @GeneratedValue(strategy = IDENTITY)
    val id: Long = 0
    init {
        require(name.isNotBlank())
        require(price >= BigDecimal.ZERO)
    }
}
```

- **Id**: `Long` IDENTITY auto-increment. Not UUID, not Snowflake — single
  H2 instance, single id generator (the DB), no distribution problem to solve.
  Revisit only if a module is extracted to its own service (ADR-0001).
- **Price**: `BigDecimal`. Single currency (KRW); add a currency field the day
  a second currency appears.
- Guards live in `init` — single source of truth for validation.

## Published API (`catalog.api`)

Consumed by `Order` at checkout to snapshot name + price onto order lines.

```kotlin
data class ProductView(val id: Long, val name: String, val price: BigDecimal, val active: Boolean)

interface CatalogApi {
    fun findProduct(id: Long): ProductView?            // null when absent
    // add findProducts(ids) only when a cart-sized batch lookup actually exists
}
```

- Returns the DTO `ProductView`, never the entity (ADR-0001 rule).
- `findProduct` is **active-agnostic**: it returns deactivated products too, so
  `Order` can still resolve a product deactivated after it entered a cart and
  decide for itself. Nullable return, no exception for an expected miss.

## Storefront read side (`catalog.internal`)

A customer-facing listing endpoint — **not** the cross-module api.

```
GET /products?keyword=&page=&size=   ->  Page<ProductView>
```

- **Query mechanism: Spring Data derived query**, not JPQL, not native:
  `findByNameContainingIgnoreCaseAndActiveTrue(keyword, pageable)`. No query
  string to get wrong; it's parsed and validated at application startup.
  Escalation ladder if the search ever outgrows this: derived → Specifications /
  JPQL `@Query` (both boot-validated, portable) → native SQL only when forced by
  a DB-specific feature (full-text, window fn) or a hand-tuned plan the ORM
  won't generate. Native is last because it's validated only at execution, needs
  a manual `countQuery` for paging, and couples to the DB dialect.
- Name-only search; a leading-wildcard `LIKE` is a full scan by definition —
  acceptable at this scale, no index/EXPLAIN work needed.
- Paging is Spring Data `Pageable`/`Page` — no custom code.
- Lists **active products only**. No category/price-range filters, no sort
  options, no full-text until asked.

## Write side (`catalog.internal`)

Same controller. No auth yet.

```
POST /products          { name, price }         -> create
PUT  /products/{id}     { name, price, active } -> update / deactivate
```

- No `DELETE`. Deactivation = `active = false` via update, so products ever
  referenced by an order never vanish.
- No auth — admin-auth gate arrives with the `Member` module
  (`// ponytail:` comment marks the spot).

## Events

**None.** Catalog neither publishes nor consumes events in iteration one — it's
a pure read-source. Nothing writes into it from another module, and no module
needs to react to catalog changes. Add an event the day one actually does.

## Error mapping

- Missing product on `PUT` → `ResponseStatusException(NOT_FOUND)` at the lookup.
- Entity guard failures (`require`) → one `@RestControllerAdvice` with
  `@ExceptionHandler(IllegalArgumentException::class)` → 400.
- No Bean Validation annotations on top of the `init` guards — one validation
  system, not two.

## Coverage gate

Catalog is the first real module, so it trips ADR-0002's commitment to turn on
a gate. Decision: **one repo-wide floor now**, split per-module when module #2
lands.

- Add `jacocoTestCoverageVerification` (instruction coverage ≥ 0.70, a floor the
  real tests clear), wired into `check`, over `classDirectories` minus the
  already-excluded `ShoppingMallApplication`.
- Amend ADR-0002 to record: repo-wide floor for one module, splits to
  per-module absolute thresholds at the second module.

## Package layout

```
com.example.shoppingmall.catalog
├── api
│   ├── CatalogApi.kt
│   └── ProductView.kt
└── internal
    ├── Product.kt                  (entity)
    ├── ProductRepository.kt        (Spring Data JpaRepository, derived query)
    ├── CatalogService.kt           (implements CatalogApi; create/update logic)
    ├── ProductController.kt        (listing + write endpoints)
    └── CatalogExceptionHandler.kt  (@RestControllerAdvice)
```

## Implementation steps

1. Entity + repository + `ProductView` + `CatalogApi`.
2. `CatalogService` (api impl + create/update).
3. `ProductController` (list/search/page + create/update) + exception advice.
4. Tests: entity guards, service create/update + not-found, api lookup,
   listing search/paging. `ModularityTests.verify()` must stay green.
5. Add repo-wide jacoco verification + amend ADR-0002.
```
