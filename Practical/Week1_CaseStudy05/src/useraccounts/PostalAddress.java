package useraccounts;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** Value object. Invariant I7: all four parts present; incomplete addresses cannot exist. */
public final class PostalAddress {
    private static final Set<String> COUNTRIES = Set.of(Locale.getISOCountries());

    private final String street, city, postalCode, country;

    private PostalAddress(String street, String city, String postalCode, String country) {
        this.street = street; this.city = city; this.postalCode = postalCode; this.country = country;
    }

    public static PostalAddress of(String street, String city, String postalCode, String country) {
        return new PostalAddress(required(street, "street"), required(city, "city"),
                                 required(postalCode, "postal code"), isoCountry(country));
    }

    private static String required(String v, String name) {
        if (v == null || v.isBlank()) throw new DomainException(name + " required");
        return v.trim();
    }

    private static String isoCountry(String v) {
        String c = required(v, "country").toUpperCase(Locale.ROOT);
        if (!COUNTRIES.contains(c)) throw new DomainException("unknown country code: " + v);
        return c;
    }

    public String street() { return street; }
    public String city() { return city; }
    public String postalCode() { return postalCode; }
    public String country() { return country; }

    @Override public boolean equals(Object o) {
        return o instanceof PostalAddress a && a.street.equals(street) && a.city.equals(city)
            && a.postalCode.equals(postalCode) && a.country.equals(country);
    }
    @Override public int hashCode() { return Objects.hash(street, city, postalCode, country); }
    @Override public String toString() { return street + ", " + city + " " + postalCode + ", " + country; }
}
