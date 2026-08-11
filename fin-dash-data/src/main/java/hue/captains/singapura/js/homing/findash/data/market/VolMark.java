package hue.captains.singapura.js.homing.findash.data.market;

import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.js.homing.findash.data.id.TenorId;
import hue.captains.singapura.js.homing.findash.data.qty.Vol;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 3 — <b>the trader's own mark</b> for one pair/tenor: ATM, 25-delta
 * risk reversal, 25-delta butterfly.
 *
 * <p>Era 1 has nothing better. There is no fitted surface, no epoch, no quote
 * provenance — the desk marks where it thinks the market is, and everything
 * prices off that. Era 2 is precisely the argument that this is not enough:
 * when quants and market-data ops arrive, the mark becomes a <i>fit</i> over
 * sourced quotes, versioned as an epoch. This record survives that change as
 * the trader's override input.</p>
 */
public record VolMark(PairId pair, TenorId tenor, Vol atm, Vol rr25, Vol bf25)
        implements ValueObject {

    public VolMark {
        Objects.requireNonNull(pair, "VolMark.pair");
        Objects.requireNonNull(tenor, "VolMark.tenor");
        Objects.requireNonNull(atm, "VolMark.atm");
        Objects.requireNonNull(rr25, "VolMark.rr25");
        Objects.requireNonNull(bf25, "VolMark.bf25");
    }

    /** The 25-delta call/put wings implied by (atm, rr, bf) — derived, never authored. */
    public Vol call25() { return new Vol(atm.points() + bf25.points() + rr25.points() / 2); }

    public Vol put25()  { return new Vol(atm.points() + bf25.points() - rr25.points() / 2); }
}
