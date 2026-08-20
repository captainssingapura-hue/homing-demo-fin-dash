package hue.captains.singapura.js.homing.findash.data.qty;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Stratum 1 — an amount <b>with its currency</b>. R3: never a formatted string,
 * never a bare double. Formatting (compaction, grouping, the euro sign) belongs
 * to the UI kit; the dataset only knows the quantity.
 */
public record Money(BigDecimal amount, Ccy ccy) implements ValueObject {

    public Money {
        Objects.requireNonNull(amount, "Money.amount");
        Objects.requireNonNull(ccy, "Money.ccy");
    }

    public Money plus(final Money other) {
        if (other.ccy != ccy) {
            throw new IllegalArgumentException("cannot add " + other.ccy + " to " + ccy);
        }
        return new Money(amount.add(other.amount), ccy);
    }

    public Money scaled(final double factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), ccy);
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }
}
