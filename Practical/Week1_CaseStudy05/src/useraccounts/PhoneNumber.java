package useraccounts;

import java.util.regex.Pattern;

/** Value object. Invariant I6: always one normalised international (E.164) format. */
public final class PhoneNumber {
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final String e164;

    private PhoneNumber(String e164) { this.e164 = e164; }

    public static PhoneNumber of(String raw) {
        if (raw == null) throw new DomainException("phone required");
        String cleaned = raw.replaceAll("[\\s().-]", "");   // strip formatting characters only
        if (!E164.matcher(cleaned).matches())
            throw new DomainException("phone must be international, e.g. +919876543210 (got '" + raw + "')");
        return new PhoneNumber(cleaned);
    }

    public String e164() { return e164; }

    @Override public boolean equals(Object o) { return o instanceof PhoneNumber p && p.e164.equals(e164); }
    @Override public int hashCode() { return e164.hashCode(); }
    @Override public String toString() { return e164; }
}
