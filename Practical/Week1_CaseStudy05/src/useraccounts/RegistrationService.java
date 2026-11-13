package useraccounts;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Application service: orchestrates workflows and cross-aggregate rules. */
public final class RegistrationService {
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    private final UserRepository repo;
    private final Clock clock;

    public RegistrationService(UserRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    /** Returns the raw token that would be e-mailed to the user. */
    public String register(String rawEmail) {
        EmailAddress email = EmailAddress.of(rawEmail);                 // I1, I2
        if (repo.existsByEmail(email))                                  // friendly early check
            throw new DomainException("email already registered: " + email);
        String raw = Tokens.generateRaw();
        UserAccount account = UserAccount.register(email, VerificationToken.issue(raw, Instant.now(clock), TTL()));
        repo.save(account);                                             // authoritative check (I3)
        return raw;
    }

    public void verify(String rawEmail, String rawToken) {
        UserAccount a = find(rawEmail);
        a.verifyEmail(rawToken, Instant.now(clock));
        repo.save(a);
    }

    public String resend(String rawEmail) {
        UserAccount a = find(rawEmail);
        String raw = Tokens.generateRaw();
        a.resendVerification(VerificationToken.issue(raw, Instant.now(clock), TTL()));
        repo.save(a);
        return raw;
    }

    public String changeEmail(String currentEmail, String newEmail) {
        UserAccount a = find(currentEmail);
        String raw = Tokens.generateRaw();
        a.changeEmail(EmailAddress.of(newEmail), VerificationToken.issue(raw, Instant.now(clock), TTL()));
        repo.save(a);
        return raw;
    }

    public boolean login(String rawEmail) {
        return repo.findByEmail(EmailAddress.of(rawEmail)).map(UserAccount::canLogin).orElse(false);
    }

    public UserAccount find(String rawEmail) {
        return repo.findByEmail(EmailAddress.of(rawEmail))
                   .orElseThrow(() -> new DomainException("no such user: " + rawEmail));
    }

    private static Duration TTL() { return TOKEN_TTL; }
}
