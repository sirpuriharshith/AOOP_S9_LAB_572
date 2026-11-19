package orders;

import java.util.Objects;

/**
 * Value object representing an individual line in an order.
 * Invariant 6: Lines are unique by product; adding the same product combines quantities.
 */
public final class OrderLine {
    private final ProductId productId;
    private final Money unitPrice;
    private final Quantity quantity;

    public OrderLine(ProductId productId, Money unitPrice, Quantity quantity) {
        this.productId = Objects.requireNonNull(productId, "product ID required");
        this.unitPrice = Objects.requireNonNull(unitPrice, "unit price required");
        this.quantity = Objects.requireNonNull(quantity, "quantity required");
    }

    public ProductId productId() {
        return productId;
    }

    public Money unitPrice() {
        return unitPrice;
    }

    public Quantity quantity() {
        return quantity;
    }

    public Money lineTotal() {
        return unitPrice.times(quantity);
    }

    public OrderLine combine(OrderLine other) {
        Objects.requireNonNull(other, "other line required");
        if (!this.productId.equals(other.productId)) {
            throw new DomainException("cannot combine lines with different products: "
                    + this.productId + " vs " + other.productId);
        }
        if (!this.unitPrice.equals(other.unitPrice)) {
            throw new DomainException("unit price changed for product " + this.productId);
        }
        return new OrderLine(this.productId, this.unitPrice, this.quantity.add(other.quantity));
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof OrderLine line && line.productId.equals(this.productId)
                && line.unitPrice.equals(this.unitPrice) && line.quantity.equals(this.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, unitPrice, quantity);
    }

    @Override
    public String toString() {
        return productId + " x " + quantity + " @ " + unitPrice + " = " + lineTotal();
    }
}
