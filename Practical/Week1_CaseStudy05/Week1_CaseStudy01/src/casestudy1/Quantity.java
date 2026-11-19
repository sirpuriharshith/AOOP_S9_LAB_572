package casestudy1;

/**
 * VALUE OBJECT - a positive count of items (Q2).
 * Zero and negative quantities cannot be constructed.
 */
public final class Quantity {

    private final int value;                    // invariant: value > 0

    private Quantity(int value) {
        if (value <= 0) {
            throw new DomainException("Quantity must be positive: " + value);
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public int value() {
        return value;
    }

    public Quantity plus(Quantity other) {
        return new Quantity(Math.addExact(value, other.value));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Quantity other && value == other.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
