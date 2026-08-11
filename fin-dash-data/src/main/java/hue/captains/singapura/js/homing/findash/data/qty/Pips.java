package hue.captains.singapura.js.homing.findash.data.qty;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Stratum 1 — a distance in pips. Derived from a level difference and the
 * pair's pip size (never authored, per R1): barrier proximity, spread width.
 */
public record Pips(double value) implements ValueObject {}
