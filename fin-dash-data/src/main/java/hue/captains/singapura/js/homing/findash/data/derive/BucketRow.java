package hue.captains.singapura.js.homing.findash.data.derive;

import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.js.homing.findash.data.id.TenorId;
import hue.captains.singapura.js.homing.findash.data.qty.Fraction;
import hue.captains.singapura.js.homing.findash.data.qty.Greeks;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stratum 6 — a risk-blotter row: the book bucketed the way the desk thinks
 * (pair, then tenor).
 *
 * <p><b>Derived, never authored</b> (R1). Today's demo hand-writes these rows
 * beside the positions they are supposed to summarise, so nothing stops the
 * two disagreeing; here the row can only come from
 * {@code Derivations.buckets(...)}. A group row is a bucket with no tenor.</p>
 */
public record BucketRow(PairId pair, Optional<TenorId> tenor, Greeks greeks,
                        Fraction worstRevalBudget, long oldestFreshnessSeconds,
                        List<String> positionIds) implements ValueObject {

    public BucketRow {
        Objects.requireNonNull(pair, "BucketRow.pair");
        Objects.requireNonNull(tenor, "BucketRow.tenor (use Optional.empty() for a group row)");
        Objects.requireNonNull(greeks, "BucketRow.greeks");
        Objects.requireNonNull(worstRevalBudget, "BucketRow.worstRevalBudget");
        positionIds = List.copyOf(Objects.requireNonNull(positionIds, "BucketRow.positionIds"));
    }

    public boolean isGroupRow() { return tenor.isEmpty(); }
}
