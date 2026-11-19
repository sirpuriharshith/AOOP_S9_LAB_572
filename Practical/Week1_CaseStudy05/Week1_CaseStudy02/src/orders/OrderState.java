package orders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Model B: State-Dependent Types using Java 17 Sealed Interfaces.
 * Each state is represented by its own type, holding only the data applicable to that state.
 * Illegal state transitions (e.g. cancelling a shipped order) are impossible at compile time.
 */
public sealed interface OrderState permits
        OrderState.DraftOrder,
        OrderState.PlacedOrder,
        OrderState.PaidOrder,
        OrderState.ShippedOrder,
        OrderState.DeliveredOrder,
        OrderState.CancelledOrder {

    OrderId id();
    Currency currency();
    List<OrderLine> lines();

    default Money total() {
        return lines().stream()
                .map(OrderLine::lineTotal)
                .reduce(Money.zero(currency()), Money::plus);
    }

    // ---- 1. DRAFT ----
    final class DraftOrder implements OrderState {
        private final OrderId id;
        private final Currency currency;
        private final List<OrderLine> lines;

        public DraftOrder(OrderId id, Currency currency) {
            this(id, currency, List.of());
        }

        private DraftOrder(OrderId id, Currency currency, List<OrderLine> lines) {
            this.id = Objects.requireNonNull(id);
            this.currency = Objects.requireNonNull(currency);
            this.lines = List.copyOf(lines);
        }

        public DraftOrder addLine(ProductId product, Money unitPrice, Quantity quantity) {
            List<OrderLine> updated = new ArrayList<>(this.lines);
            OrderLine newLine = new OrderLine(product, unitPrice, quantity);
            boolean merged = false;
            for (int i = 0; i < updated.size(); i++) {
                if (updated.get(i).productId().equals(product)) {
                    updated.set(i, updated.get(i).combine(newLine));
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                updated.add(newLine);
            }
            return new DraftOrder(id, currency, updated);
        }

        public PlacedOrder place() {
            if (lines.isEmpty()) {
                throw new DomainException("Model B: cannot place empty draft order");
            }
            return new PlacedOrder(id, currency, lines);
        }

        public CancelledOrder cancel(String reason) {
            return new CancelledOrder(id, currency, lines, reason);
        }

        @Override public OrderId id() { return id; }
        @Override public Currency currency() { return currency; }
        @Override public List<OrderLine> lines() { return lines; }
    }

    // ---- 2. PLACED ----
    final class PlacedOrder implements OrderState {
        private final OrderId id;
        private final Currency currency;
        private final List<OrderLine> lines;

        private PlacedOrder(OrderId id, Currency currency, List<OrderLine> lines) {
            this.id = id;
            this.currency = currency;
            this.lines = List.copyOf(lines);
        }

        public PaidOrder pay(PaymentRef paymentRef) {
            return new PaidOrder(id, currency, lines, paymentRef);
        }

        public CancelledOrder cancel(String reason) {
            return new CancelledOrder(id, currency, lines, reason);
        }

        @Override public OrderId id() { return id; }
        @Override public Currency currency() { return currency; }
        @Override public List<OrderLine> lines() { return lines; }
    }

    // ---- 3. PAID ----
    final class PaidOrder implements OrderState {
        private final OrderId id;
        private final Currency currency;
        private final List<OrderLine> lines;
        private final PaymentRef payment;

        private PaidOrder(OrderId id, Currency currency, List<OrderLine> lines, PaymentRef payment) {
            this.id = id;
            this.currency = currency;
            this.lines = List.copyOf(lines);
            this.payment = Objects.requireNonNull(payment);
        }

        public ShippedOrder ship(TrackingNumber tracking, Instant at) {
            return new ShippedOrder(id, currency, lines, payment, new Shipment(tracking, at));
        }

        public CancelledOrder cancel(String reason) {
            return new CancelledOrder(id, currency, lines, reason);
        }

        public PaymentRef payment() { return payment; }
        @Override public OrderId id() { return id; }
        @Override public Currency currency() { return currency; }
        @Override public List<OrderLine> lines() { return lines; }
    }

    // ---- 4. SHIPPED (Has NO cancel() method!) ----
    final class ShippedOrder implements OrderState {
        private final OrderId id;
        private final Currency currency;
        private final List<OrderLine> lines;
        private final PaymentRef payment;
        private final Shipment shipment;

        private ShippedOrder(OrderId id, Currency currency, List<OrderLine> lines, PaymentRef payment, Shipment shipment) {
            this.id = id;
            this.currency = currency;
            this.lines = List.copyOf(lines);
            this.payment = payment;
            this.shipment = Objects.requireNonNull(shipment);
        }

        public DeliveredOrder deliver(Instant deliveredAt) {
            return new DeliveredOrder(id, currency, lines, payment, shipment, deliveredAt);
        }

        public Shipment shipment() { return shipment; }
        public PaymentRef payment() { return payment; }
        @Override public OrderId id() { return id; }
        @Override public Currency currency() { return currency; }
        @Override public List<OrderLine> lines() { return lines; }
    }

    // ---- 5. DELIVERED (Terminal) ----
    final class DeliveredOrder implements OrderState {
        private final OrderId id;
        private final Currency currency;
        private final List<OrderLine> lines;
        private final PaymentRef payment;
        private final Shipment shipment;
        private final Instant deliveredAt;

        private DeliveredOrder(OrderId id, Currency currency, List<OrderLine> lines,
                               PaymentRef payment, Shipment shipment, Instant deliveredAt) {
            this.id = id;
            this.currency = currency;
            this.lines = List.copyOf(lines);
            this.payment = payment;
            this.shipment = shipment;
            this.deliveredAt = Objects.requireNonNull(deliveredAt);
        }

        public Instant deliveredAt() { return deliveredAt; }
        public Shipment shipment() { return shipment; }
        public PaymentRef payment() { return payment; }
        @Override public OrderId id() { return id; }
        @Override public Currency currency() { return currency; }
        @Override public List<OrderLine> lines() { return lines; }
    }

    // ---- 6. CANCELLED (Terminal) ----
    final class CancelledOrder implements OrderState {
        private final OrderId id;
        private final Currency currency;
        private final List<OrderLine> lines;
        private final String cancellationReason;

        private CancelledOrder(OrderId id, Currency currency, List<OrderLine> lines, String reason) {
            this.id = id;
            this.currency = currency;
            this.lines = List.copyOf(lines);
            this.cancellationReason = Objects.requireNonNull(reason);
        }

        public String cancellationReason() { return cancellationReason; }
        @Override public OrderId id() { return id; }
        @Override public Currency currency() { return currency; }
        @Override public List<OrderLine> lines() { return lines; }
    }
}
