package useraccounts;

import java.util.Locale;
import java.util.regex.Pattern;

/** Value object. Invariants I1, I2: well formed and always stored trimmed + lower-case. */
public final class EmailAddress {
    private static final Pattern FORMAT =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    private final String value;

    private EmailAddress(String value) { this.value = value; }

    public static EmailAddress of(String raw) {
        if (raw == null) throw new DomainException("email required");
        String canonical = raw.trim().toLowerCase(Locale.ROOT);
        if (canonical.length() > 254 || !FORMAT.matcher(canonical).matches())
            throw new DomainException("invalid email: '" + raw + "'");
        return new EmailAddress(canonical);
    }

    public String value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof EmailAddress e && e.value.equals(value); }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value; }
}
