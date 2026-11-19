package orders;

/**
 * Order lifecycle states.
 * Draft -> Placed -> Paid -> Shipped -> Delivered.
 * Draft, Placed, or Paid may transition to Cancelled.
 * Shipped and Delivered cannot be cancelled.
 */
public enum OrderStatus {
    DRAFT,
    PLACED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean isCancellable() {
        return this == DRAFT || this == PLACED || this == PAID;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
}
