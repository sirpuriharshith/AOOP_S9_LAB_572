package useraccounts;

import java.util.Optional;

/** Persistence port. Email uniqueness (I3) is a rule over ALL users, so it lives here, not in UserAccount. */
public interface UserRepository {
    boolean existsByEmail(EmailAddress email);
    Optional<UserAccount> findByEmail(EmailAddress email);

    /** Must fail atomically if the email is already taken (models a DB UNIQUE constraint). */
    void save(UserAccount account);
}
