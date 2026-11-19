package orders;

import java.time.Instant;

/**
 * Demonstration runner for Case Study 2: Order Lifecycle and State Invariants.
 * Demonstrates live enforcement of all 6 invariants and answers to exam scenarios.
 */
public class Main {

    static void attempt(String title, Runnable action) {
        try {
            action.run();
            System.out.println("  OK       " + title);
        } catch (DomainException e) {
            System.out.println("  REJECTED " + title + "  ->  " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("AOOP (25CS2205E) - CO-1 Case Study 2: Order Lifecycle & State Machine");
        System.out.println("Name: S.Harshith | Roll No: 2520030572");
        System.out.println("================================================================================\n");

        ProductId book = ProductId.of("BOOK-101");
        ProductId pen = ProductId.of("PEN-202");
        Money bookPrice = Money.of("25.00", Currency.USD);
        Money penPrice = Money.of("5.50", Currency.USD);

        // ====================================================================
        // Scenario 1: Invariant 1 - Placed order requires at least one line
        // ====================================================================
        System.out.println("== 1. Invariant 1: Placed order requires >= 1 line ==");
        Order emptyOrder = Order.create(Currency.USD);
        attempt("place empty order", emptyOrder::place);
        emptyOrder.addLine(book, bookPrice, Quantity.of(1));
        attempt("place order after adding line", emptyOrder::place);
        System.out.println("  order status: " + emptyOrder.status());

        // ====================================================================
        // Scenario 2: Invariant 2 - Total equals sum of line totals (no drift)
        // ====================================================================
        System.out.println("\n== 2. Invariant 2: Total derived dynamically (no drift) ==");
        Order order = Order.create(Currency.USD);
        order.addLine(book, bookPrice, Quantity.of(2)); // 50.00
        order.addLine(pen, penPrice, Quantity.of(4));   // 22.00
        System.out.println("  lines: " + order.lines());
        System.out.println("  derived total: " + order.total() + " (expected: 72.00 USD)");
        order.removeLine(pen);
        System.out.println("  total after removing pen: " + order.total() + " (expected: 50.00 USD)");

        // ====================================================================
        // Scenario 3: Invariant 3 - Lines editable only in DRAFT status
        // ====================================================================
        System.out.println("\n== 3. Invariant 3: Line edits forbidden after DRAFT ==");
        order.place();
        attempt("add line to PLACED order", () -> order.addLine(pen, penPrice, Quantity.of(1)));
        attempt("remove line from PLACED order", () -> order.removeLine(book));

        // ====================================================================
        // Scenario 4: Invariant 4 - Valid state machine transitions
        // ====================================================================
        System.out.println("\n== 4. Invariant 4: Lifecycle transitions (Draft->Placed->Paid->Shipped->Delivered) ==");
        Order liveOrder = Order.create(Currency.USD);
        liveOrder.addLine(book, bookPrice, Quantity.of(1));
        liveOrder.place();
        System.out.println("  status after place: " + liveOrder.status());
        attempt("ship directly from PLACED (skipping PAID)", () ->
                liveOrder.ship(TrackingNumber.of("TRK-123456"), Instant.now()));
        liveOrder.pay(PaymentRef.of("PAY-999888", Instant.now()));
        System.out.println("  status after pay: " + liveOrder.status());
        liveOrder.ship(TrackingNumber.of("TRK-123456"), Instant.now());
        System.out.println("  status after ship: " + liveOrder.status());
        liveOrder.deliver(Instant.now());
        System.out.println("  status after deliver: " + liveOrder.status());

        // ====================================================================
        // Scenario 5: Invariant 4 & Q2 - Cancelled order cannot be shipped
        // ====================================================================
        System.out.println("\n== 5. Invariant 4 & Q2: Cancelled order cannot be shipped ==");
        Order orderToCancel = Order.create(Currency.USD);
        orderToCancel.addLine(book, bookPrice, Quantity.of(1));
        orderToCancel.place();
        orderToCancel.cancel("Customer requested cancellation");
        System.out.println("  cancelled status: " + orderToCancel.status());
        attempt("ship cancelled order", () ->
                orderToCancel.ship(TrackingNumber.of("TRK-999000"), Instant.now()));

        // ====================================================================
        // Scenario 6: Invariant 4 - Shipped and Delivered cannot be cancelled
        // ====================================================================
        System.out.println("\n== 6. Invariant 4: Shipped/Delivered orders cannot be cancelled ==");
        attempt("cancel delivered order", () -> liveOrder.cancel("Want refund"));

        // ====================================================================
        // Scenario 7: Invariant 5 - Shipment data exists iff order is shipped
        // ====================================================================
        System.out.println("\n== 7. Invariant 5: Shipment tracking exists iff shipped ==");
        Order draftTest = Order.create(Currency.USD);
        draftTest.addLine(book, bookPrice, Quantity.of(1));
        System.out.println("  draft shipment present? " + draftTest.shipment().isPresent());
        draftTest.place();
        draftTest.pay(PaymentRef.of("PAY-111222", Instant.now()));
        TrackingNumber trk = TrackingNumber.of("TRK-888777");
        draftTest.ship(trk, Instant.now());
        System.out.println("  shipped tracking number: " + draftTest.shipment().orElseThrow().trackingNumber());

        // ====================================================================
        // Scenario 8: Invariant 6 - Duplicate products combine quantities
        // ====================================================================
        System.out.println("\n== 8. Invariant 6: Lines unique by product (merging quantities) ==");
        Order mergeOrder = Order.create(Currency.USD);
        mergeOrder.addLine(book, bookPrice, Quantity.of(2));
        mergeOrder.addLine(book, bookPrice, Quantity.of(3));
        System.out.println("  distinct lines count: " + mergeOrder.lineCount());
        System.out.println("  merged quantity: " + mergeOrder.lines().get(0).quantity() + " (expected: 5)");

        // ====================================================================
        // Scenario 9: Model B Type-Safety Demonstration
        // ====================================================================
        System.out.println("\n== 9. Model B: Sealed State Types (Compile-time Safety) ==");
        OrderState.DraftOrder dOrder = new OrderState.DraftOrder(OrderId.generate(), Currency.USD)
                .addLine(book, bookPrice, Quantity.of(1));
        OrderState.PlacedOrder pOrder = dOrder.place();
        OrderState.PaidOrder paidState = pOrder.pay(PaymentRef.of("PAY-B-100", Instant.now()));
        OrderState.ShippedOrder shippedState = paidState.ship(TrackingNumber.of("TRK-B-555"), Instant.now());
        System.out.println("  Model B shipped total: " + shippedState.total());
        System.out.println("  Model B tracking: " + shippedState.shipment().trackingNumber());
        // Note: shippedState has NO cancel() method in Java - compiler prevents calling it!

        // ====================================================================
        // Scenario 10: Cross-Aggregate Rules (Inventory & Credit Limits)
        // ====================================================================
        System.out.println("\n== 10. Cross-Aggregate Workflow: Stock Reservation & Credit Limit ==");
        InventoryService.InMemory inv = new InventoryService.InMemory();
        inv.setStock(book, 5); // 5 books in stock

        CustomerCreditService.InMemory credit = new CustomerCreditService.InMemory();
        credit.setCreditLimit("CUST-1", Money.of("100.00", Currency.USD));

        OrderPlacementWorkflow workflow = new OrderPlacementWorkflow(inv, credit);

        // Success placement
        Order goodOrder = Order.create(Currency.USD);
        goodOrder.addLine(book, bookPrice, Quantity.of(2)); // total 50.00 USD <= 100.00 limit
        attempt("workflow place order (2 books, $50)", () -> workflow.placeOrder(goodOrder, "CUST-1"));
        System.out.println("  remaining stock: " + inv.getStock(book));

        // Excess credit limit failure
        Order excessCreditOrder = Order.create(Currency.USD);
        excessCreditOrder.addLine(book, bookPrice, Quantity.of(3)); // 75.00 USD + 50.00 used = 125.00 > 100.00 limit
        attempt("workflow place order exceeding credit limit ($75 + $50 used > $100)", () ->
                workflow.placeOrder(excessCreditOrder, "CUST-1"));

        // Excess stock failure with rollback
        Order excessStockOrder = Order.create(Currency.USD);
        credit.setCreditLimit("CUST-2", Money.of("500.00", Currency.USD));
        excessStockOrder.addLine(book, bookPrice, Quantity.of(10)); // requested 10 books, only 3 available
        attempt("workflow place order exceeding available stock (10 books > 3 available)", () ->
                workflow.placeOrder(excessStockOrder, "CUST-2"));

        System.out.println("\n================================================================================");
        System.out.println("ALL DEMONSTRATION SCENARIOS COMPLETED SUCCESSFULLY");
        System.out.println("================================================================================");
    }
}
