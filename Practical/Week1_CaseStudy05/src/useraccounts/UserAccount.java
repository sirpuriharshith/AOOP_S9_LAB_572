package useraccounts;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import useraccounts.EmailVerification.Unverified;
import useraccounts.EmailVerification.Verified;

/**
 * Entity and aggregate root (identity = UserId). No public setters:
 * every change goes through a method that enforces the invariants.
 */
public final class UserAccount {
    private final UserId id;
    private EmailAddress email;
    private EmailVerification verification;
    private AccountStatus status = AccountStatus.PENDING;
    private Optional<PhoneNumber> phone = Optional.empty();
    private Optional<PostalAddress> address = Optional.empty();

    private UserAccount(UserId id, EmailAddress email, VerificationToken token) {
        this.id = id;
        this.email = Objects.requireNonNull(email);
        this.verification = new Unverified(token);
    }

    public static UserAccount register(EmailAddress email, VerificationToken token) {
        return new UserAccount(UserId.newId(), email, token);
    }

    // ---- email verification (I4, I5, I10) ----

    public void verifyEmail(String rawToken, Instant now) {
        requireNotDeleted();
        if (!(verification instanceof Unverified u))
            throw new DomainException("email already verified");
        if (u.pending().isExpired(now)) throw new DomainException("verification token expired");
        if (!u.pending().matches(rawToken)) throw new DomainException("invalid verification token");
        verification = new Verified(now);
        if (status == AccountStatus.PENDING) status = AccountStatus.ACTIVE;   // activation
    }

    public void resendVerification(VerificationToken fresh) {
        requireNotDeleted();
        if (verification instanceof Verified) throw new DomainException("email already verified");
        verification = new Unverified(fresh);   // old token is replaced, so it is invalidated
    }

    public void changeEmail(EmailAddress newEmail, VerificationToken token) {
        requireNotDeleted();
        this.email = Objects.requireNonNull(newEmail);
        this.verification = new Unverified(token);   // new address is never "verified" automatically
    }

    // ---- lifecycle (I8) ----

    public void suspend() {
        if (status != AccountStatus.ACTIVE) throw new DomainException("only ACTIVE accounts can be suspended");
        status = AccountStatus.SUSPENDED;
    }

    public void reinstate() {
        if (status != AccountStatus.SUSPENDED) throw new DomainException("account is not suspended");
        status = AccountStatus.ACTIVE;
    }

    public void delete() {
        requireNotDeleted();
        status = AccountStatus.DELETED;
        phone = Optional.empty();      // erase personal data
        address = Optional.empty();
    }

    // ---- optional contact details ----

    public void setPhone(PhoneNumber p) { requireNotDeleted(); phone = Optional.of(Objects.requireNonNull(p)); }
    public void clearPhone() { requireNotDeleted(); phone = Optional.empty(); }
    public void setAddress(PostalAddress a) { requireNotDeleted(); address = Optional.of(Objects.requireNonNull(a)); }
    public void clearAddress() { requireNotDeleted(); address = Optional.empty(); }

    // ---- login rule (I9) ----

    public boolean canLogin() {
        return status == AccountStatus.ACTIVE && verification instanceof Verified;
    }

    private void requireNotDeleted() {
        if (status == AccountStatus.DELETED) throw new DomainException("account is deleted");
    }

    // ---- read-only access ----

    public UserId id() { return id; }
    public EmailAddress email() { return email; }
    public AccountStatus status() { return status; }
    public EmailVerification verification() { return verification; }
    public Optional<PhoneNumber> phone() { return phone; }
    public Optional<PostalAddress> address() { return address; }
    public boolean isEmailVerified() { return verification instanceof Verified; }

    @Override public boolean equals(Object o) { return o instanceof UserAccount u && u.id.equals(id); }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() {
        return "UserAccount[" + email + ", " + status + ", verified=" + isEmailVerified() + "]";
    }
}
