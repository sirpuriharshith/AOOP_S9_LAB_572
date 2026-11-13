package useraccounts;

import java.util.Objects;
import java.util.UUID;

/** Value object wrapping the identity of a user so it cannot be mixed up with other ids. */
public final class UserId {
    private final UUID value;

    private UserId(UUID value) { this.value = Objects.requireNonNull(value, "id required"); }

    public static UserId newId() { return new UserId(UUID.randomUUID()); }
    public static UserId of(UUID value) { return new UserId(value); }

    public UUID value() { return value; }

    @Override public boolean equals(Object o) { return o instanceof UserId u && u.value.equals(value); }
    @Override public int hashCode() { return value.hashCode(); }
    @Override public String toString() { return value.toString(); }
}
