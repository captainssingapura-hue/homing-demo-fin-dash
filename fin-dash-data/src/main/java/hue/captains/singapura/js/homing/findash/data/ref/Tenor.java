package hue.captains.singapura.js.homing.findash.data.ref;

import hue.captains.singapura.js.homing.findash.data.id.TenorId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 2 — a point on the standard tenor ladder. {@code days} is the maths; the id is the label. */
public record Tenor(TenorId id, int days) implements ValueObject {

    public Tenor {
        Objects.requireNonNull(id, "Tenor.id");
        if (days <= 0) {
            throw new IllegalArgumentException("Tenor.days must be positive");
        }
    }

    public double years() { return days / 365.0; }
}
