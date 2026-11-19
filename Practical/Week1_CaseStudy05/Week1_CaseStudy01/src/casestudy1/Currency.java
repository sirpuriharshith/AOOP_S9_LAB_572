package casestudy1;

/**
 * VALUE OBJECT (enum) - a currency and its scale.
 *
 * The scale is the number of minor units per major unit, expressed as
 * decimal digits: EUR/USD have 2 (cents), JPY has 0 (yen is the minor
 * unit itself), KWD has 3 (fils). Money formatting and major-unit
 * conversion both read the scale from here - nowhere else.
 */
public enum Currency {
    EUR(2),
    USD(2),
    JPY(0),
    KWD(3);

    private final int scale;

    Currency(int scale) {
        this.scale = scale;
    }

    /** Minor units per major unit, as a digit count: 2 means 100 cents = 1 euro. */
    public int scale() {
        return scale;
    }

    /** 10^scale: e.g. EUR -> 100, JPY -> 1, KWD -> 1000. */
    public long factor() {
        long f = 1;
        for (int i = 0; i < scale; i++) {
            f *= 10L;
        }
        return f;
    }
}
