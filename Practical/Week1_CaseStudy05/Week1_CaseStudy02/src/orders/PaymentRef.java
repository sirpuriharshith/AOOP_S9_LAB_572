package orders;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object (record) capturing payment gateway confirmation reference and timestamp.
 */
public record PaymentRef(String referenceId, Instant paidAt) {
    public PaymentRef {
        if (referenceId == null || referenceId.isBlank()) {
            throw new DomainException("payment reference ID required");
        }
        Objects.requireNonNull(paidAt, "payment timestamp required");
    }

    public static PaymentRef of(String ref, Instant paidAt) {
        return new PaymentRef(ref.trim(), paidAt);
    }
}
