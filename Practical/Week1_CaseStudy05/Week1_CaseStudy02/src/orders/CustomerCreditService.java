package orders;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Cross-aggregate service managing customer credit limits.
 * Protects against race conditions between concurrent orders via atomic deduction.
 */
public interface CustomerCreditService {
    boolean checkCredit(String customerId, Money amount);
    void reserveCredit(OrderId orderId, String customerId, Money amount);
    void releaseCredit(OrderId orderId);

    class InMemory implements CustomerCreditService {
        private final Map<String, Money> creditLimits = new HashMap<>();
        private final Map<String, Money> usedCredit = new HashMap<>();
        private final Map<OrderId, CreditReservation> reservations = new HashMap<>();

        record CreditReservation(String customerId, Money amount) {}

        public synchronized void setCreditLimit(String customerId, Money limit) {
            creditLimits.put(customerId, limit);
            usedCredit.putIfAbsent(customerId, Money.zero(limit.currency()));
        }

        @Override
        public synchronized boolean checkCredit(String customerId, Money amount) {
            Money limit = creditLimits.get(customerId);
            if (limit == null) return false;
            Money used = usedCredit.getOrDefault(customerId, Money.zero(limit.currency()));
            return !used.plus(amount).isGreaterThan(limit);
        }

        @Override
        public synchronized void reserveCredit(OrderId orderId, String customerId, Money amount) {
            Money limit = creditLimits.get(customerId);
            if (limit == null) {
                throw new DomainException("unknown customer: " + customerId);
            }
            Money used = usedCredit.getOrDefault(customerId, Money.zero(limit.currency()));
            Money newTotal = used.plus(amount);
            if (newTotal.isGreaterThan(limit)) {
                throw new DomainException("credit limit exceeded for customer " + customerId
                        + ": limit " + limit + ", attempted " + newTotal);
            }
            usedCredit.put(customerId, newTotal);
            reservations.put(orderId, new CreditReservation(customerId, amount));
        }

        @Override
        public synchronized void releaseCredit(OrderId orderId) {
            CreditReservation res = reservations.remove(orderId);
            if (res != null) {
                Money current = usedCredit.get(res.customerId());
                if (current != null) {
                    usedCredit.put(res.customerId(), current.minus(res.amount()));
                }
            }
        }
    }
}
