package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a coherent market snapshot every consumer prices against, e.g. {@code C204}. */
public record SliceId(String value) implements ValueObject {

    public SliceId {
        Objects.requireNonNull(value, "SliceId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("SliceId.value must not be blank");
        }
    }
}
