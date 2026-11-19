package casestudy1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ENTITY / AGGREGATE ROOT - a shopping cart (Q2).
 *
 * Rules owned by the cart:
 *  - lines are unique by product: re-adding a product MERGES into
 *    its existing line, summing the quantity (never a second line)
 *  - lines are encapsulated: the internal map is private, and
 *    lines() returns an unmodifiable snapshot - outside code cannot
 *    add or remove lines behind the cart's back
 *  - total() is DERIVED: recomputed from the lines on every call,
 *    never stored - so cart and invoice, both calling total() on
 *    the same lines, can never disagree by a cent (Q1)
 *  - all lines share the cart's currency; a product priced in a
 *    different currency is rejected on add (Q4's invariant, again
 *    enforced at the aggregate boundary)
 */
public final class Cart {

    /** One cart line: a product and how many of it. Immutable snapshot. */
    public record Line(Product product, Quantity quantity) {
        public Money lineTotal() {
            return product.price().times(quantity);
        }
    }

    private final String id;
    private final Currency currency;
    private final Map<String, Line> linesBySku = new LinkedHashMap<>();

    public Cart(String id, Currency currency) {
        if (id == null || id.isBlank()) {
            throw new DomainException("Cart id must not be blank");
        }
        this.id = id.trim();
        this.currency = Objects.requireNonNull(currency, "currency");
    }

    public String id() {
        return id;
    }

    public Currency currency() {
        return currency;
    }

    public void add(Product product, Quantity quantity) {
        Objects.requireNonNull(product, "product");
        Objects.requireNonNull(quantity, "quantity");
        if (product.price().currency() != currency) {
            throw new DomainException("Product " + product.sku() + " is priced in "
                    + product.price().currency() + " but this cart is in " + currency);
        }
        Line existing = linesBySku.get(product.sku());
        if (existing == null) {
            linesBySku.put(product.sku(), new Line(product, quantity));
        } else {
            // Merge: same product, quantities add - still ONE line.
            linesBySku.put(product.sku(),
                    new Line(existing.product(), existing.quantity().plus(quantity)));
        }
    }

    public void remove(Product product) {
        Objects.requireNonNull(product, "product");
        linesBySku.remove(product.sku());
    }

    /** Unmodifiable snapshot - mutating it cannot affect the cart. */
    public List<Line> lines() {
        return Collections.unmodifiableList(new ArrayList<>(linesBySku.values()));
    }

    public int lineCount() {
        return linesBySku.size();
    }

    public Quantity quantityOf(Product product) {
        Line line = linesBySku.get(product.sku());
        return line == null ? null : line.quantity();
    }

    /** DERIVED total - the sum of the line totals, computed now. */
    public Money total() {
        Money sum = Money.zero(currency);
        for (Line line : linesBySku.values()) {
            sum = sum.plus(line.lineTotal());
        }
        return sum;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cart other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cart[" + id + ", " + linesBySku.size() + " lines, total " + total() + "]";
    }
}
