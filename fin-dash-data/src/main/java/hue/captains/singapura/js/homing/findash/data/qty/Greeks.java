package hue.captains.singapura.js.homing.findash.data.qty;

import hue.captains.singapura.tao.ontology.ValueObject;

/**
 * Stratum 1 — the risk of a position or trade, in the desk's units: delta and
 * gamma in base-currency millions equivalent, vega/vanna/volga/theta as Money.
 * Smile-bucket vega (ATM / RR / BF) is carried separately because the blotter
 * thinks in buckets — and because summing them must reproduce total vega
 * (a §4 invariant).
 */
public record Greeks(double delta, double gamma,
                     Money vegaAtm, Money vegaRr, Money vegaBf,
                     Money vanna, Money volga, Money theta) implements ValueObject {

    /** Total vega = the smile buckets, summed. Never stored — always this. */
    public Money vega() {
        return vegaAtm.plus(vegaRr).plus(vegaBf);
    }

    public Greeks plus(final Greeks other) {
        return new Greeks(delta + other.delta, gamma + other.gamma,
                vegaAtm.plus(other.vegaAtm), vegaRr.plus(other.vegaRr),
                vegaBf.plus(other.vegaBf), vanna.plus(other.vanna),
                volga.plus(other.volga), theta.plus(other.theta));
    }
}
