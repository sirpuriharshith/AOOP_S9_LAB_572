package useraccounts;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Value object. Invariant I5: usable only before expiry. The raw token is never stored. */
public final class VerificationToken {
    private final String hash;
    private final Instant expiresAt;

    public VerificationToken(String hash, Instant expiresAt) {
        this.hash = Objects.requireNonNull(hash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    /** Creates a token for a raw value, valid for the given time-to-live. */
    public static VerificationToken issue(String rawToken, Instant now, Duration ttl) {
        return new VerificationToken(Tokens.sha256Hex(rawToken), now.plus(ttl));
    }

    public boolean isExpired(Instant now) { return !now.isBefore(expiresAt); }

    public boolean matches(String rawToken) {
        return rawToken != null && MessageDigest.isEqual(
            hash.getBytes(StandardCharsets.UTF_8),
            Tokens.sha256Hex(rawToken).getBytes(StandardCharsets.UTF_8));
    }

    public Instant expiresAt() { return expiresAt; }
}
