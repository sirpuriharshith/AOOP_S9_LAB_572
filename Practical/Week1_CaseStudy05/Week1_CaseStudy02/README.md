# CO-1 Case Study 2: Order Lifecycle, Guarded State Machine and State Types

Requires JDK 17 or newer (uses sealed interfaces, records, pattern matching).

## Run in VS Code
1. Install the "Extension Pack for Java" (Microsoft).
2. File > Open Folder > select this `Week1_CaseStudy02` folder.
3. Open `src/orders/Main.java` and click "Run" above `main`.

## Run from a terminal
    javac -d out src/orders/*.java
    java -cp out orders.Main

## Files (all in src/orders)
- Value objects: OrderId, ProductId, Quantity, Money, Currency, TrackingNumber, Shipment, PaymentRef, OrderLine
- State types: OrderStatus (Enum), OrderState (Sealed Interface Model B: DraftOrder | PlacedOrder | PaidOrder | ShippedOrder | DeliveredOrder | CancelledOrder)
- Entity / aggregate root: Order (Model A - Guarded State Machine)
- Cross-aggregate rules: InventoryService, CustomerCreditService
- Application workflow: OrderPlacementWorkflow (Saga compensation pattern)
- Exception: DomainException
- Demo: Main (10 comprehensive scenarios demonstrating all invariants and exam questions)
