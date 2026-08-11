package hue.captains.singapura.js.homing.findash.data.qty;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Stratum 1 — a bounded fraction in [0,1]: reval error-budget consumption,
 * limit utilisation, composite weights. The bound is the invariant a UI meter
 * relies on, so it is checked here rather than hoped for at render time.
 */
public record Fraction(double value) implements ValueObject {

    public Fraction {
        if (value < 0.0 || value > 1.0 || Double.isNaN(value)) {
            throw new IllegalArgumentException("Fraction out of [0,1]: " + value);
        }
    }

    public boolean exceeds(final Fraction threshold) { return value > threshold.value; }
}
