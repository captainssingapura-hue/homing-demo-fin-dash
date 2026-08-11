package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a single fact within a journal, e.g. {@code J-000412}. */
public record JournalEntryId(String value) implements ValueObject {

    public JournalEntryId {
        Objects.requireNonNull(value, "JournalEntryId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("JournalEntryId.value must not be blank");
        }
    }
}
