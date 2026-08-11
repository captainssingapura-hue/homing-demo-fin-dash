package hue.captains.singapura.js.homing.findash.data.meta;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stable id of a data type in the ontology, e.g. {@code Position}. */
public record DataTypeId(String value) implements ValueObject {

    public DataTypeId {
        Objects.requireNonNull(value, "DataTypeId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("DataTypeId.value must not be blank");
        }
    }
}
