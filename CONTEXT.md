# Shopping Mall

An online shopping mall built as a Spring Modulith. The system is one deployable
application split into bounded contexts (modules) that communicate by events for
writes and by published APIs for reads.

## Modules

The application is a single context split into five modules. Each is a top-level
package under `com.example.shoppingmall`; its `internal` sub-package is private to
the module, its `api` sub-package is the only thing other modules may call.

**Catalog**:
Products and the prices shown to shoppers.
_Avoid_: Inventory (that's stock counts, a separate module), Product service.

**Order**:
The cart-to-checkout-to-fulfillment lifecycle of a customer's purchase.
_Avoid_: Purchase, Transaction, Basket.

**Payment**:
Charging and refunding money against an Order.
_Avoid_: Billing, Transaction.

**Member**:
Customer accounts and authentication identity.
_Avoid_: User, Account, Client, Buyer.

**Inventory**:
Stock counts per product and their reservation/decrement.
_Avoid_: Catalog, Stock service, Warehouse.

## Language

**Module**:
A bounded context inside the modulith — one top-level package, verified by
`ModularityTests`. Not a Gradle subproject.

**Published API**:
A module's `api` sub-package — the only types other modules may depend on.
Returns DTOs, never entities.
_Avoid_: Public interface, facade.

**Application Event**:
A domain event published by one module and consumed by another via
`@ApplicationModuleListener`. The only sanctioned way to trigger a write in
another module.
_Avoid_: Message, signal.
