package hue.captains.singapura.js.homing.findash.data.derive;

import hue.captains.singapura.js.homing.findash.data.qty.Fraction;
import hue.captains.singapura.js.homing.findash.data.qty.Greeks;
import hue.captains.singapura.js.homing.findash.data.qty.Money;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 6 — the desk's totals over a scope (all positions, or the positions
 * of a selected portfolio subtree).
 *
 * <p>Derived (R1): the invariant that position greeks sum to bucket greeks and
 * bucket greeks sum to these totals is enforced by the integrity gate, not by
 * an author remembering to update three places.</p>
 */
public record DeskTotals(int positionCount, Money pv, Greeks greeks,
                         Fraction worstRevalBudget) implements ValueObject {

    public DeskTotals {
        Objects.requireNonNull(pv, "DeskTotals.pv");
        Objects.requireNonNull(greeks, "DeskTotals.greeks");
        Objects.requireNonNull(worstRevalBudget, "DeskTotals.worstRevalBudget");
    }
}
