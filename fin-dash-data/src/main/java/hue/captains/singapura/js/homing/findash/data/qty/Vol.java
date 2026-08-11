package hue.captains.singapura.js.homing.findash.data.qty;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Stratum 1 — an implied volatility in vol points (7.85 = 7.85%). Distinct from
 * a bare double so a vol can never be silently added to a strike or a delta.
 */
public record Vol(double points) implements ValueObject {

    public Vol plus(final Vol other) { return new Vol(points + other.points); }

    public Vol shifted(final double byPoints) { return new Vol(points + byPoints); }
}
