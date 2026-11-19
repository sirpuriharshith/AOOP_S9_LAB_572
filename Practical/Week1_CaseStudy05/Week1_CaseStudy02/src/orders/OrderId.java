package orders;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object wrapping the identity of an Order.
 * Strongly typed to prevent accidental confusion with product, user, or payment IDs.
 */
public final class OrderId {
    private final String value;

    private OrderId(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainException("order ID required");
        }
        this.value = value.trim();
    }

    public static OrderId generate() {
        return new OrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    }

    public static OrderId of(String value) {
        return new OrderId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OrderId id && id.value.equals(this.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
