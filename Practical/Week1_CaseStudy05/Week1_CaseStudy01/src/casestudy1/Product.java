package casestudy1;

import java.util.Objects;

/**
 * ENTITY - a catalog product, identified by its SKU (Q2).
 *
 * Identity survives change: the product is still the same product
 * after a price change, so equals/hashCode use the SKU only.
 * Its price is a Money - so a negative price is unconstructible,
 * and a Refund or a SignedMoney (different types, Q5) can never be
 * assigned as a price: that is a compile error, not a runtime bug.
 */
public final class Product {

    private final String sku;
    private final String name;
    private Money price;

    public Product(String sku, String name, Money price) {
        if (sku == null || sku.isBlank()) {
            throw new DomainException("Product SKU must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new DomainException("Product name must not be blank");
        }
        this.sku = sku.trim();
        this.name = name.trim();
        this.price = Objects.requireNonNull(price, "price");
    }

    public String sku() {
        return sku;
    }

    public String name() {
        return name;
    }

    public Money price() {
        return price;
    }

    /** The ONLY way to change the price - and it takes a Money. */
    public void changePrice(Money newPrice) {
        this.price = Objects.requireNonNull(newPrice, "newPrice");
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Product other && sku.equals(other.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    @Override
    public String toString() {
        return "Product[" + sku + ", " + name + ", " + price + "]";
    }
}
