package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a human or automated actor, e.g. {@code trader-a} or {@code rule-qw3}. */
public record ActorId(String value) implements ValueObject {

    public ActorId {
        Objects.requireNonNull(value, "ActorId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ActorId.value must not be blank");
        }
    }
}
