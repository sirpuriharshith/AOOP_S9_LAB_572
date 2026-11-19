package orders;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object (record) combining a carrier tracking number and shipment timestamp.
 * Invariant 5: both exist together if and only if the order has been shipped.
 */
public record Shipment(TrackingNumber trackingNumber, Instant shippedAt) {
    public Shipment {
        Objects.requireNonNull(trackingNumber, "tracking number required");
        Objects.requireNonNull(shippedAt, "shippedAt timestamp required");
    }
}
