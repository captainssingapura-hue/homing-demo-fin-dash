package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a trade-journal entry's subject, e.g. {@code T-4471}. */
public record TradeId(String value) implements ValueObject {

    public TradeId {
        Objects.requireNonNull(value, "TradeId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TradeId.value must not be blank");
        }
    }
}
