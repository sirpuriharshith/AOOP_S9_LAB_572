package casestudy1;

import java.util.Objects;

/**
 * VALUE OBJECT, LEDGER-ONLY - an amount that MAY be negative (Q5).
 *
 * Credits and debits genuinely net off inside a ledger/account
 * balance, so the signed type exists - but it is fenced off from
 * the catalog: Product.price is a Money, and a SignedMoney is not
 * a Money, so no price can ever be set from one (compile error).
 *
 * The only way back to Money is requireNonNegative(), which THROWS
 * if the amount is negative - there is no silent conversion that
 * drops a sign.
 */
public final class SignedMoney {

    private final long minorUnits;      // may be negative - that is the point
    private final Currency currency;

    private SignedMoney(long minorUnits, Currency currency) {
        this.minorUnits = minorUnits;
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public static SignedMoney ofMinor(long minorUnits, Currency currency) {
        return new SignedMoney(minorUnits, currency);
    }

    public static SignedMoney fromMoney(Money money) {
        Objects.requireNonNull(money, "money");
        return new SignedMoney(money.minorUnits(), money.currency());
    }

    public static SignedMoney zero(Currency currency) {
        return new SignedMoney(0, currency);
    }

    public long minorUnits() {
        return minorUnits;
    }

    public Currency currency() {
        return currency;
    }

    public boolean isNegative() {
        return minorUnits < 0;
    }

    /** Ledger addition. Same currency only - the check still applies. */
    public SignedMoney plus(SignedMoney other) {
        Objects.requireNonNull(other, "other");
        if (currency != other.currency) {
            throw new DomainException("Currency mismatch: cannot combine "
                    + currency + " with " + other.currency);
        }
        return new SignedMoney(Math.addExact(minorUnits, other.minorUnits), currency);
    }

    /**
     * The ONLY way back to Money - explicit, and it refuses negatives.
     */
    public Money requireNonNegative() {
        if (minorUnits < 0) {
            throw new DomainException("SignedMoney is negative (" + this
                    + ") and cannot be used as a Money/price");
        }
        return Money.ofMinor(minorUnits, currency);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SignedMoney other
                && minorUnits == other.minorUnits
                && currency == other.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minorUnits, currency);
    }

    @Override
    public String toString() {
        long abs = Math.abs(minorUnits);
        String sign = minorUnits < 0 ? "-" : "";
        if (currency.scale() == 0) {
            return sign + abs + " " + currency;
        }
        long factor = currency.factor();
        long major = abs / factor;
        long minor = abs % factor;
        return sign + major + "." + String.format("%0" + currency.scale() + "d", minor)
                + " " + currency;
    }
}
