package hue.captains.singapura.js.homing.findash.data.meta;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * A typed edge between two data types: {@code Position --instrument--> Instrument}.
 *
 * <p>Edges are declared here as data so the graph can be traversed, rendered,
 * and checked without reflection — and so the requirement that every edge is
 * <b>by typed id</b> (R2) is inspectable rather than merely intended.</p>
 */
public record Relation(DataTypeId from, String edge, DataTypeId to,
                       Cardinality cardinality, String purpose) implements ValueObject {

    public Relation {
        Objects.requireNonNull(from, "Relation.from");
        Objects.requireNonNull(edge, "Relation.edge");
        Objects.requireNonNull(to, "Relation.to");
        Objects.requireNonNull(cardinality, "Relation.cardinality");
        Objects.requireNonNull(purpose, "Relation.purpose");
    }
}
