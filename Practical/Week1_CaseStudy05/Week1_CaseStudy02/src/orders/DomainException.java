package orders;

/**
 * Thrown whenever a business invariant or domain rule would be violated.
 */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
