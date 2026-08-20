package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a contract definition (what was traded), e.g. {@code I-1104}. */
public record InstrumentId(String value) implements ValueObject {

    public InstrumentId {
        Objects.requireNonNull(value, "InstrumentId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("InstrumentId.value must not be blank");
        }
    }
}
