package orders;

import java.util.HashMap;
import java.util.Map;

/**
 * Cross-aggregate domain service for inventory reservation.
 * Invariant: Stock availability cannot be enforced by Order alone since it spans aggregates.
 */
public interface InventoryService {
    boolean hasSufficientStock(ProductId product, Quantity quantity);
    void reserveStock(OrderId orderId, ProductId product, Quantity quantity);
    void releaseStock(OrderId orderId);

    class InMemory implements InventoryService {
        private final Map<ProductId, Integer> inventory = new HashMap<>();
        private final Map<OrderId, Map<ProductId, Integer>> reservations = new HashMap<>();

        public synchronized void setStock(ProductId product, int quantity) {
            inventory.put(product, quantity);
        }

        public synchronized int getStock(ProductId product) {
            return inventory.getOrDefault(product, 0);
        }

        @Override
        public synchronized boolean hasSufficientStock(ProductId product, Quantity quantity) {
            return inventory.getOrDefault(product, 0) >= quantity.value();
        }

        @Override
        public synchronized void reserveStock(OrderId orderId, ProductId product, Quantity quantity) {
            int available = inventory.getOrDefault(product, 0);
            if (available < quantity.value()) {
                throw new DomainException("insufficient inventory for product " + product
                        + ": requested " + quantity + ", available " + available);
            }
            inventory.put(product, available - quantity.value());
            reservations.computeIfAbsent(orderId, k -> new HashMap<>())
                    .merge(product, quantity.value(), Integer::sum);
        }

        @Override
        public synchronized void releaseStock(OrderId orderId) {
            Map<ProductId, Integer> reserved = reservations.remove(orderId);
            if (reserved != null) {
                reserved.forEach((prod, qty) -> inventory.merge(prod, qty, Integer::sum));
            }
        }
    }
}
