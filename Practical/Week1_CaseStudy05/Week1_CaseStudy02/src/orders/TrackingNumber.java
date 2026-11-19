package orders;

import java.util.regex.Pattern;

/**
 * Value object representing a shipment tracking number.
 * Ensures non-blank, formatted carrier tracking code.
 */
public final class TrackingNumber {
    private static final Pattern FORMAT = Pattern.compile("^[A-Z0-9-]{6,32}$");
    private final String value;

    private TrackingNumber(String value) {
        this.value = value;
    }

    public static TrackingNumber of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DomainException("tracking number required");
        }
        String clean = raw.trim().toUpperCase();
        if (!FORMAT.matcher(clean).matches()) {
            throw new DomainException("invalid tracking number format: '" + raw + "'");
        }
        return new TrackingNumber(clean);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TrackingNumber t && t.value.equals(this.value);
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
