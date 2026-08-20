package hue.captains.singapura.js.homing.findash.data.market;

import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/** Stratum 3 — spot for one pair, as of the slice that carries it. */
public record SpotRate(PairId pair, double rate) implements ValueObject {

    public SpotRate {
        Objects.requireNonNull(pair, "SpotRate.pair");
    }
}
