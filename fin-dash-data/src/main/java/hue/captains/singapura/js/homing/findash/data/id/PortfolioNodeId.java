package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 0 — a node at any level of the book tree, e.g. {@code pf-eur-exo}. */
public record PortfolioNodeId(String value) implements ValueObject {

    public PortfolioNodeId {
        Objects.requireNonNull(value, "PortfolioNodeId.value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PortfolioNodeId.value must not be blank");
        }
    }
}
