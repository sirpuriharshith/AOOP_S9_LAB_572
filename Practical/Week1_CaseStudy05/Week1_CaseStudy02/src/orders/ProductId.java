package orders;

/**
 * Value object wrapping product / SKU identity.
 */
public final class ProductId {
    private final String sku;

    private ProductId(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new DomainException("product SKU required");
        }
        this.sku = sku.trim().toUpperCase();
    }

    public static ProductId of(String sku) {
        return new ProductId(sku);
    }

    public String sku() {
        return sku;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ProductId p && p.sku.equals(this.sku);
    }

    @Override
    public int hashCode() {
        return sku.hashCode();
    }

    @Override
    public String toString() {
        return sku;
    }
}
