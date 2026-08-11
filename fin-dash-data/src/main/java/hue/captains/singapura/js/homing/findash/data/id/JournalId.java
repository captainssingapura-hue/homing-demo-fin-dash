package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — one append-only journal, e.g. {@code trades}. */
public record JournalId(String value) implements ValueObject {

    public JournalId {
        Objects.requireNonNull(value, "JournalId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("JournalId.value must not be blank");
        }
    }
}
