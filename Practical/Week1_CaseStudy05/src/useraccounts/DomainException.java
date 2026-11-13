package useraccounts;

/** Thrown whenever a business rule (invariant) would be broken. */
public class DomainException extends RuntimeException {
    public DomainException(String message) { super(message); }
}
