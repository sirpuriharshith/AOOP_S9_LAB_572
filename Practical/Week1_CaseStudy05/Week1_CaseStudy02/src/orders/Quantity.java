package orders;

import java.util.Objects;

/**
 * Value object representing an item quantity. Strictly positive (>= 1).
 */
public final class Quantity {
    private final int value;

    private Quantity(int value) {
        if (value <= 0) {
            throw new DomainException("quantity must be strictly positive (got " + value + ")");
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public int value() {
        return value;
    }

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "quantity required");
        return new Quantity(Math.addExact(this.value, other.value));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Quantity q && q.value == this.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
