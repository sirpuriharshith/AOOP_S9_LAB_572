package casestudy1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * VALUE OBJECT - an exact, non-negative amount of money (Q1/Q2).
 *
 * Stored as a long counting MINOR units (cents / yen / fils) plus a
 * Currency. Integer arithmetic is exact: 10c + 20c == 30c, always -
 * unlike double, where 0.1 + 0.2 != 0.3.
 *
 * Invariants, enforced in the one private constructor:
 *  - minorUnits >= 0 (a negative Money cannot be constructed)
 *  - currency is never null
 * plus/minus require the SAME currency and throw otherwise (Q4) -
 * conversion is NOT done here; see ExchangeService.
 */
public final class Money {

    private final long minorUnits;
    private final Currency currency;

    private Money(long minorUnits, Currency currency) {
        if (minorUnits < 0) {
            throw new DomainException("Money must not be negative: " + minorUnits
                    + " minor units");
        }
        this.minorUnits = minorUnits;
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    /** Factory from minor units, e.g. Money.ofMinor(1050, EUR) = 10.50 EUR. */
    public static Money ofMinor(long minorUnits, Currency currency) {
        return new Money(minorUnits, currency);
    }

    /** Factory from whole major units, e.g. Money.ofMajor(10, EUR) = 10.00 EUR. */
    public static Money ofMajor(long majorUnits, Currency currency) {
        Objects.requireNonNull(currency, "currency");
        if (majorUnits < 0) {
            throw new DomainException("Money must not be negative: " + majorUnits);
        }
        return new Money(Math.multiplyExact(majorUnits, currency.factor()), currency);
    }

    /**
     * Factory from a decimal major amount, e.g. new BigDecimal("10.50") EUR.
     * The amount must be exactly representable in the currency's minor
     * units - 10.505 EUR (half a cent) is rejected, not silently rounded.
     */
    public static Money ofMajor(BigDecimal majorAmount, Currency currency) {
        Objects.requireNonNull(majorAmount, "amount");
        Objects.requireNonNull(currency, "currency");
        try {
            long minor = majorAmount.movePointRight(currency.scale())
                    .setScale(0, RoundingMode.UNNECESSARY)
                    .longValueExact();
            return new Money(minor, currency);
        } catch (ArithmeticException e) {
            throw new DomainException("Amount " + majorAmount + " is not exactly "
                    + "representable in " + currency + " minor units");
        }
    }

    public static Money zero(Currency currency) {
        return new Money(0, currency);
    }

    public long minorUnits() {
        return minorUnits;
    }

    public Currency currency() {
        return currency;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other");
        if (currency != other.currency) {
            throw new DomainException("Currency mismatch: cannot combine "
                    + currency + " with " + other.currency
                    + " - convert explicitly via ExchangeService first");
        }
    }

    /** Exact addition. Same currency only - see Q4. */
    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(minorUnits, other.minorUnits), currency);
    }

    /** Exact subtraction. The result may not go negative. */
    public Money minus(Money other) {
        requireSameCurrency(other);
        long result = Math.subtractExact(minorUnits, other.minorUnits);
        if (result < 0) {
            throw new DomainException("Money.minus would go negative: "
                    + this + " - " + other);
        }
        return new Money(result, currency);
    }

    /** Multiply by a Quantity (Q2) - exact integer multiplication. */
    public Money times(Quantity quantity) {
        Objects.requireNonNull(quantity, "quantity");
        return new Money(Math.multiplyExact(minorUnits, quantity.value()), currency);
    }

    /** Multiply by a plain positive count (used for derived totals). */
    public Money times(long factor) {
        if (factor < 0) {
            throw new DomainException("Money.times factor must not be negative: " + factor);
        }
        return new Money(Math.multiplyExact(minorUnits, factor), currency);
    }

    /** Widen into the ledger world (Q5) - one-way, explicit. */
    public SignedMoney toSigned() {
        return SignedMoney.ofMinor(minorUnits, currency);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money other
                && minorUnits == other.minorUnits
                && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minorUnits, currency);
    }

    /**
     * Readable form respecting the currency scale:
     * 1050 EUR -> "10.50 EUR", 500 JPY -> "500 JPY", 1250 KWD -> "1.250 KWD".
     */
    @Override
    public String toString() {
        if (currency.scale() == 0) {
            return minorUnits + " " + currency;
        }
        long factor = currency.factor();
        long major = minorUnits / factor;
        long minor = minorUnits % factor;
        return major + "." + String.format("%0" + currency.scale() + "d", minor)
                + " " + currency;
    }
}
