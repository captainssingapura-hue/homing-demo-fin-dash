package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a currency pair, e.g. {@code EURUSD}. */
public record PairId(String value) implements ValueObject {

    public PairId {
        Objects.requireNonNull(value, "PairId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PairId.value must not be blank");
        }
    }
}
