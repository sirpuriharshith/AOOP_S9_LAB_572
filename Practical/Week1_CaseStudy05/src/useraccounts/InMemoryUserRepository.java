package useraccounts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** In-memory stand-in for a database. The synchronized check-and-insert models a UNIQUE index. */
public final class InMemoryUserRepository implements UserRepository {
    private final Map<UserId, UserAccount> byId = new HashMap<>();
    private final Map<EmailAddress, UserId> emailIndex = new HashMap<>();

    @Override public synchronized boolean existsByEmail(EmailAddress email) {
        return emailIndex.containsKey(email);
    }

    @Override public synchronized Optional<UserAccount> findByEmail(EmailAddress email) {
        UserId id = emailIndex.get(email);
        return id == null ? Optional.empty() : Optional.of(byId.get(id));
    }

    @Override public synchronized void save(UserAccount account) {
        UserId owner = emailIndex.get(account.email());
        if (owner != null && !owner.equals(account.id()))
            throw new DomainException("email already registered: " + account.email());
        // remove an old index entry if the user changed email
        emailIndex.values().removeIf(id -> id.equals(account.id()));
        byId.put(account.id(), account);
        emailIndex.put(account.email(), account.id());
    }
}
