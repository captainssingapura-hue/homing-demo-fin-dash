package hue.captains.singapura.js.homing.findash.data.book;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 4 — a barrier feature of an instrument. Proximity to spot is NEVER
 * stored here: it is derived from this level, the slice's spot, and the pair's
 * pip size (R1), which is why three different screens can show the same
 * distance without any of them owning it.
 */
public record Barrier(BarrierType type, double level, Monitoring monitoring) implements ValueObject {

    public Barrier {
        Objects.requireNonNull(type, "Barrier.type");
        Objects.requireNonNull(monitoring, "Barrier.monitoring");
        if (level <= 0) {
            throw new IllegalArgumentException("Barrier.level must be positive");
        }
    }
}
