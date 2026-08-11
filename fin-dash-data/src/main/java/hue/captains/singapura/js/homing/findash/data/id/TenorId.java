package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a tenor on the standard ladder, e.g. {@code 3M}. */
public record TenorId(String value) implements ValueObject {

    public TenorId {
        Objects.requireNonNull(value, "TenorId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("TenorId.value must not be blank");
        }
    }
}
