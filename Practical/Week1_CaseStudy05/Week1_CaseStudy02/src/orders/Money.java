package orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value object representing non-negative monetary amounts stored in exact minor units (e.g. cents).
 * Floating-point representation is prohibited to avoid rounding errors.
 */
public final class Money implements Comparable<Money> {
    private final long minorUnits;
    private final Currency currency;

    private Money(long minorUnits, Currency currency) {
        if (minorUnits < 0) {
            throw new DomainException("Money cannot be negative: " + minorUnits);
        }
        this.minorUnits = minorUnits;
        this.currency = Objects.requireNonNull(currency, "currency required");
    }

    public static Money ofMinor(long minorUnits, Currency currency) {
        return new Money(minorUnits, currency);
    }

    public static Money of(String majorString, Currency currency) {
        Objects.requireNonNull(majorString, "amount string required");
        Objects.requireNonNull(currency, "currency required");
        BigDecimal bd = new BigDecimal(majorString.trim());
        if (bd.signum() < 0) {
            throw new DomainException("amount cannot be negative: " + majorString);
        }
        BigDecimal scaled = bd.movePointRight(currency.scale());
        try {
            long minor = scaled.setScale(0, RoundingMode.UNNECESSARY).longValueExact();
            return new Money(minor, currency);
        } catch (ArithmeticException e) {
            throw new DomainException("too many decimal places for currency " + currency + ": " + majorString);
        }
    }

    public static Money zero(Currency currency) {
        return new Money(0L, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(Math.addExact(this.minorUnits, other.minorUnits), this.currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        if (this.minorUnits < other.minorUnits) {
            throw new DomainException("insufficient funds: " + this + " < " + other);
        }
        return new Money(this.minorUnits - other.minorUnits, this.currency);
    }

    public Money times(Quantity quantity) {
        Objects.requireNonNull(quantity, "quantity required");
        return new Money(Math.multiplyExact(this.minorUnits, quantity.value()), this.currency);
    }

    public long minorUnits() {
        return minorUnits;
    }

    public Currency currency() {
        return currency;
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return this.minorUnits > other.minorUnits;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money required");
        if (this.currency != other.currency) {
            throw new DomainException("currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public int compareTo(Money o) {
        requireSameCurrency(o);
        return Long.compare(this.minorUnits, o.minorUnits);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money m && m.minorUnits == this.minorUnits && m.currency == this.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minorUnits, currency);
    }

    @Override
    public String toString() {
        if (currency.scale() == 0) {
            return minorUnits + " " + currency;
        }
        long factor = (long) Math.pow(10, currency.scale());
        long whole = minorUnits / factor;
        long part = minorUnits % factor;
        String format = "%d.%0" + currency.scale() + "d %s";
        return String.format(format, whole, part, currency);
    }
}
