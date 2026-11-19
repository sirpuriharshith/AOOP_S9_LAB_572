package orders;

/**
 * Currency enumeration defining code and minor-unit scale.
 */
public enum Currency {
    USD(2),
    EUR(2),
    INR(2),
    JPY(0);

    private final int scale;

    Currency(int scale) {
        this.scale = scale;
    }

    public int scale() {
        return scale;
    }
}
