package useraccounts;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

/** Demo for the team presentation: each step shows an invariant being enforced. */
public class Main {

    /** Clock we can move forward to simulate token expiry. */
    static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T10:00:00Z");
        void advance(Duration d) { now = now.plus(d); }
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId z) { return this; }
        @Override public Instant instant() { return now; }
    }

    static void attempt(String title, Runnable action) {
        try {
            action.run();
            System.out.println("  OK       " + title);
        } catch (DomainException e) {
            System.out.println("  REJECTED " + title + "  ->  " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        MutableClock clock = new MutableClock();
        UserRepository repo = new InMemoryUserRepository();
        RegistrationService service = new RegistrationService(repo, clock);

        System.out.println("== 1. Email validity and canonical form (I1, I2) ==");
        attempt("bad email 'not-an-email'", () -> service.register("not-an-email"));
        attempt("bad email 'a@b'", () -> service.register("a@b"));
        String[] token = new String[1];
        attempt("register ' Asha@Example.COM '", () -> token[0] = service.register(" Asha@Example.COM "));
        System.out.println("  stored as: " + service.find("asha@example.com").email());

        System.out.println("\n== 2. Uniqueness, case-insensitive (I3) ==");
        attempt("register 'ASHA@example.com' again", () -> service.register("ASHA@example.com"));

        System.out.println("\n== 3. Cannot log in before verification (I9) ==");
        System.out.println("  canLogin = " + service.login("asha@example.com"));

        System.out.println("\n== 4. Wrong and expired tokens (I5) ==");
        attempt("verify with wrong token", () -> service.verify("asha@example.com", "wrong-token"));
        clock.advance(Duration.ofHours(25));
        attempt("verify with correct token after 25h", () -> service.verify("asha@example.com", token[0]));
        attempt("resend a fresh token", () -> token[0] = service.resend("asha@example.com"));
        attempt("verify with fresh token", () -> service.verify("asha@example.com", token[0]));
        System.out.println("  state: " + service.find("asha@example.com"));
        System.out.println("  canLogin = " + service.login("asha@example.com"));
        attempt("verify again (already verified)", () -> service.verify("asha@example.com", token[0]));

        System.out.println("\n== 5. Phone and address (I6, I7) ==");
        UserAccount asha = service.find("asha@example.com");
        attempt("phone '98765 43210' (no country code)", () -> asha.setPhone(PhoneNumber.of("98765 43210")));
        attempt("phone '+91 98765-43210'", () -> asha.setPhone(PhoneNumber.of("+91 98765-43210")));
        System.out.println("  stored phone: " + asha.phone().orElseThrow());
        attempt("address with blank city", () -> PostalAddress.of("12 MG Road", " ", "500055", "IN"));
        attempt("address with country 'XX'", () -> PostalAddress.of("12 MG Road", "Hyderabad", "500055", "XX"));
        attempt("complete address", () -> asha.setAddress(PostalAddress.of("12 MG Road", "Hyderabad", "500055", "in")));
        System.out.println("  stored address: " + asha.address().orElseThrow());

        System.out.println("\n== 6. Suspension (I8, I9) ==");
        attempt("suspend", asha::suspend);
        System.out.println("  canLogin = " + service.login("asha@example.com"));
        attempt("suspend again", asha::suspend);
        attempt("reinstate", asha::reinstate);
        System.out.println("  canLogin = " + service.login("asha@example.com"));

        System.out.println("\n== 7. Verified user changes email (I10) ==");
        attempt("change email to new@example.com", () -> token[0] = service.changeEmail("asha@example.com", "new@example.com"));
        System.out.println("  verified now? " + service.find("new@example.com").isEmailVerified()
            + ", canLogin = " + service.login("new@example.com"));
        attempt("verify new email", () -> service.verify("new@example.com", token[0]));
        System.out.println("  canLogin = " + service.login("new@example.com"));

        System.out.println("\n== 8. Deletion is terminal (I8) ==");
        UserAccount user = service.find("new@example.com");
        attempt("delete", user::delete);
        System.out.println("  canLogin = " + service.login("new@example.com"));
        attempt("reinstate deleted account", user::reinstate);
        attempt("set phone on deleted account", () -> user.setPhone(PhoneNumber.of("+919876543210")));
        System.out.println("  personal data erased? phone=" + user.phone().isPresent()
            + ", address=" + user.address().isPresent());
    }
}
