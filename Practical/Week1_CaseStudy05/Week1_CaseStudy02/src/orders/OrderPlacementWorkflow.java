package orders;

import java.util.Objects;

/**
 * Application-layer workflow coordinating cross-aggregate transactions with compensation (Saga pattern).
 * Demonstrates eventual consistency across Order, Inventory, and Customer Credit aggregates.
 */
public final class OrderPlacementWorkflow {
    private final InventoryService inventoryService;
    private final CustomerCreditService creditService;

    public OrderPlacementWorkflow(InventoryService inventoryService, CustomerCreditService creditService) {
        this.inventoryService = Objects.requireNonNull(inventoryService);
        this.creditService = Objects.requireNonNull(creditService);
    }

    public void placeOrder(Order order, String customerId) {
        Objects.requireNonNull(order, "order required");
        Objects.requireNonNull(customerId, "customer ID required");

        // 1. Reserve credit first
        Money total = order.total();
        creditService.reserveCredit(order.id(), customerId, total);

        try {
            // 2. Reserve stock for each line item
            for (OrderLine line : order.lines()) {
                inventoryService.reserveStock(order.id(), line.productId(), line.quantity());
            }

            // 3. Transition order to PLACED
            order.place();
        } catch (DomainException ex) {
            // Compensation on failure
            inventoryService.releaseStock(order.id());
            creditService.releaseCredit(order.id());
            throw ex;
        }
    }
}
