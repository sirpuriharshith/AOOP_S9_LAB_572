package casestudy1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * VALUE OBJECT - a percentage in 0..100% (Q3, exactly as written).
 *
 * Stored as basis points: 1% = 100 bp, so 100% = 10_000 bp.
 * Basis points keep fractional percentages (e.g. 12.5% = 1250 bp)
 * exact in an int.
 *
 * The constructor is private: the ONLY way in is through a validating
 * factory, so the invariant 0..10_000 bp can never be bypassed.
 * Rejection is via DomainException, which extends
 * IllegalArgumentException - the exception the written answer names.
 */
public final class Percentage {

    private final int basisPoints;              // invariant: 0 .. 10_000

    private Percentage(int basisPoints) {
        if (basisPoints < 0 || basisPoints > 10_000) {
            throw new DomainException(
                    "Percentage out of range 0..100%: " + basisPoints + " bp");
        }
        this.basisPoints = basisPoints;
    }

    public static Percentage ofBasisPoints(int bp) {
        return new Percentage(bp);              // constructor validates
    }

    public static Percentage ofPercent(int percent) {
        if (percent < 0 || percent > 100) {
            throw new DomainException("Percent out of range: " + percent);
        }
        return new Percentage(percent * 100);
    }

    public int basisPoints() {
        return basisPoints;
    }

    /**
     * Returns the price AFTER this discount is applied.
     * Rounding happens here, in exactly one place, with one documented
     * rule (HALF_EVEN), and the result can never go below zero.
     */
    public Money applyTo(Money price) {
        Objects.requireNonNull(price, "price");
        long discount = BigDecimal.valueOf(price.minorUnits())
                .multiply(BigDecimal.valueOf(basisPoints))
                .divide(BigDecimal.valueOf(10_000), 0, RoundingMode.HALF_EVEN)
                .longValueExact();
        long discounted = price.minorUnits() - discount;   // >= 0, since bp <= 10_000
        return Money.ofMinor(discounted, price.currency());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Percentage other && basisPoints == other.basisPoints;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(basisPoints);
    }

    @Override
    public String toString() {
        return (basisPoints / 100.0) + "%";
    }
}
