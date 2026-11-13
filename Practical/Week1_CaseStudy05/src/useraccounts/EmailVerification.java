package useraccounts;

import java.time.Instant;

/**
 * Sum type replacing emailVerified + verifiedAt + verificationToken + tokenExpiry.
 * Exactly one state at a time, each carrying only the data that belongs to it.
 */
public sealed interface EmailVerification permits EmailVerification.Unverified, EmailVerification.Verified {

    /** Not yet verified: always has a pending token, never a timestamp. */
    record Unverified(VerificationToken pending) implements EmailVerification {
        public Unverified {
            if (pending == null) throw new DomainException("unverified state needs a token");
        }
    }

    /** Verified: always has a timestamp, never a token. */
    record Verified(Instant at) implements EmailVerification {
        public Verified {
            if (at == null) throw new DomainException("verified state needs a timestamp");
        }
    }
}
