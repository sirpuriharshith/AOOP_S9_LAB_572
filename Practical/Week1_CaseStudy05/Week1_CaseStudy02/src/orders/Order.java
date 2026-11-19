package orders;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root: Order (Refined Model A - Single Entity with Guarded State Machine).
 * Enforces all 6 business invariants:
 *   I1: Placed orders must have >= 1 line.
 *   I2: Total is dynamically derived from line items, preventing drift.
 *   I3: Lines can be modified only in DRAFT status.
 *   I4: Strict state machine transitions (Draft -> Placed -> Paid -> Shipped -> Delivered).
 *   I5: Shipment data (tracking number, shippedAt) exists iff order is SHIPPED or DELIVERED.
 *   I6: Lines unique by product; adding existing product merges quantity.
 */
public final class Order {
    private final OrderId id;
    private final Currency currency;
    private OrderStatus status = OrderStatus.DRAFT;
    private final Map<ProductId, OrderLine> lines = new LinkedHashMap<>();
    private Optional<PaymentRef> payment = Optional.empty();
    private Optional<Shipment> shipment = Optional.empty();
    private Optional<Instant> deliveredAt = Optional.empty();
    private Optional<String> cancellationReason = Optional.empty();

    private Order(OrderId id, Currency currency) {
        this.id = Objects.requireNonNull(id, "order ID required");
        this.currency = Objects.requireNonNull(currency, "currency required");
    }

    public static Order create(Currency currency) {
        return new Order(OrderId.generate(), currency);
    }

    public static Order create(OrderId id, Currency currency) {
        return new Order(id, currency);
    }

    // ---- Line item manipulation (I3, I6) ----

    public void addLine(ProductId productId, Money unitPrice, Quantity quantity) {
        requireStatus(OrderStatus.DRAFT, "Lines can only be added while order is DRAFT");
        Objects.requireNonNull(productId, "product ID required");
        Objects.requireNonNull(unitPrice, "unit price required");
        Objects.requireNonNull(quantity, "quantity required");

        if (unitPrice.currency() != this.currency) {
            throw new DomainException("currency mismatch: line price in "
                    + unitPrice.currency() + " but order is in " + this.currency);
        }

        OrderLine newLine = new OrderLine(productId, unitPrice, quantity);
        lines.merge(productId, newLine, OrderLine::combine);
    }

    public void removeLine(ProductId productId) {
        requireStatus(OrderStatus.DRAFT, "Lines can only be removed while order is DRAFT");
        Objects.requireNonNull(productId, "product ID required");
        if (!lines.containsKey(productId)) {
            throw new DomainException("cannot remove absent product line: " + productId);
        }
        lines.remove(productId);
    }

    // ---- Lifecycle state transitions (I1, I4, I5) ----

    public void place() {
        requireStatus(OrderStatus.DRAFT, "Only DRAFT orders can be placed");
        if (lines.isEmpty()) {
            throw new DomainException("cannot place an empty order: at least one line item is required");
        }
        this.status = OrderStatus.PLACED;
    }

    public void pay(PaymentRef paymentRef) {
        requireStatus(OrderStatus.PLACED, "Only PLACED orders can be paid");
        this.payment = Optional.of(Objects.requireNonNull(paymentRef, "payment reference required"));
        this.status = OrderStatus.PAID;
    }

    /**
     * Ships the order.
     * Guard enforces status is PAID.
     * This strictly prevents cancelled, draft, or already-shipped orders from being shipped.
     */
    public void ship(TrackingNumber trackingNumber, Instant shippedAt) {
        requireStatus(OrderStatus.PAID, "Only PAID orders can be shipped");
        Objects.requireNonNull(trackingNumber, "tracking number required");
        Objects.requireNonNull(shippedAt, "shippedAt timestamp required");

        this.shipment = Optional.of(new Shipment(trackingNumber, shippedAt));
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver(Instant at) {
        requireStatus(OrderStatus.SHIPPED, "Only SHIPPED orders can be delivered");
        this.deliveredAt = Optional.of(Objects.requireNonNull(at, "delivered timestamp required"));
        this.status = OrderStatus.DELIVERED;
    }

    public void cancel(String reason) {
        if (!status.isCancellable()) {
            throw new DomainException("cannot cancel order in status " + status
                    + ": SHIPPED and DELIVERED orders cannot be cancelled");
        }
        if (reason == null || reason.isBlank()) {
            throw new DomainException("cancellation reason required");
        }
        this.cancellationReason = Optional.of(reason.trim());
        this.status = OrderStatus.CANCELLED;
    }

    // ---- Derived calculations (I2) ----

    /**
     * Dynamic derived total: always equals the sum of line totals.
     * Never stored as a field, completely eliminating drift bugs.
     */
    public Money total() {
        return lines.values().stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.zero(currency), Money::plus);
    }

    // ---- Read-only queries ----

    public OrderId id() {
        return id;
    }

    public Currency currency() {
        return currency;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderLine> lines() {
        return List.copyOf(lines.values());
    }

    public int lineCount() {
        return lines.size();
    }

    public Optional<PaymentRef> payment() {
        return payment;
    }

    public Optional<Shipment> shipment() {
        return shipment;
    }

    public Optional<Instant> deliveredAt() {
        return deliveredAt;
    }

    public Optional<String> cancellationReason() {
        return cancellationReason;
    }

    private void requireStatus(OrderStatus expected, String message) {
        if (this.status != expected) {
            throw new DomainException(message + " (current status: " + this.status + ")");
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Order other && other.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Order[" + id + ", status=" + status + ", lines=" + lines.size() + ", total=" + total() + "]";
    }
}
