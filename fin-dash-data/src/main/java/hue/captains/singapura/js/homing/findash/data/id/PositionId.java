package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — an aggregated holding, e.g. {@code P-1104}. */
public record PositionId(String value) implements ValueObject {

    public PositionId {
        Objects.requireNonNull(value, "PositionId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PositionId.value must not be blank");
        }
    }
}
