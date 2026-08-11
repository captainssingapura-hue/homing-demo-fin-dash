package hue.captains.singapura.js.homing.findash.data;

import hue.captains.singapura.js.homing.findash.data.book.Instrument;
import hue.captains.singapura.js.homing.findash.data.book.Position;
import hue.captains.singapura.js.homing.findash.data.derive.BucketRow;
import hue.captains.singapura.js.homing.findash.data.derive.DeskTotals;
import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.js.homing.findash.data.id.TenorId;
import hue.captains.singapura.js.homing.findash.data.qty.Ccy;
import hue.captains.singapura.js.homing.findash.data.qty.Fraction;
import hue.captains.singapura.js.homing.findash.data.qty.Greeks;
import hue.captains.singapura.js.homing.findash.data.qty.Money;
import hue.captains.singapura.js.homing.findash.data.qty.Pips;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stratum 7 — <b>every aggregate the screens show</b>, computed from positions.
 * Zero instance fields: a {@code StatelessFunctionalObject} taking the dataset
 * as a parameter, so there is exactly one implementation of "the desk's vega"
 * and no screen can hold a private opinion (R1, and P5 at the data layer).
 */
public final class Derivations implements StatelessFunctionalObject {

    /** Bucket the given positions pair ▸ tenor: a group row per pair, then a row per tenor. */
    public List<BucketRow> buckets(final DeskDataset ds, final List<Position> scope) {
        final Map<PairId, List<Position>> byPair = new LinkedHashMap<>();
        for (final Position p : scope) {
            final Optional<Instrument> i = ds.instrument(p.instrument());
            if (i.isEmpty()) {
                continue;
            }
            byPair.computeIfAbsent(i.get().pair(), k -> new ArrayList<>()).add(p);
        }
        final List<BucketRow> rows = new ArrayList<>();
        for (final Map.Entry<PairId, List<Position>> pairEntry : byPair.entrySet()) {
            rows.add(row(ds, pairEntry.getKey(), Optional.empty(), pairEntry.getValue()));
            final Map<TenorId, List<Position>> byTenor = new LinkedHashMap<>();
            for (final Position p : pairEntry.getValue()) {
                final TenorId t = ds.instrument(p.instrument()).orElseThrow().tenor();
                byTenor.computeIfAbsent(t, k -> new ArrayList<>()).add(p);
            }
            for (final Map.Entry<TenorId, List<Position>> tenorEntry : byTenor.entrySet()) {
                rows.add(row(ds, pairEntry.getKey(), Optional.of(tenorEntry.getKey()),
                        tenorEntry.getValue()));
            }
        }
        return List.copyOf(rows);
    }

    private BucketRow row(final DeskDataset ds, final PairId pair,
                          final Optional<TenorId> tenor, final List<Position> members) {
        final List<String> ids = members.stream().map(p -> p.id().value()).toList();
        return new BucketRow(pair, tenor, sumGreeks(members), worstBudget(members),
                oldestFreshness(ds, members), ids);
    }

    /** Totals over a scope. The same summation the buckets use — one implementation. */
    public DeskTotals totals(final DeskDataset ds, final List<Position> scope) {
        return new DeskTotals(scope.size(), sumPv(scope), sumGreeks(scope), worstBudget(scope));
    }

    public Money sumPv(final List<Position> scope) {
        Money acc = new Money(BigDecimal.ZERO, Ccy.EUR);
        for (final Position p : scope) {
            acc = acc.plus(p.pv());
        }
        return acc;
    }

    public Greeks sumGreeks(final List<Position> scope) {
        Greeks acc = zeroGreeks();
        for (final Position p : scope) {
            acc = acc.plus(p.greeks());
        }
        return acc;
    }

    public Greeks zeroGreeks() {
        final Money z = new Money(BigDecimal.ZERO, Ccy.EUR);
        return new Greeks(0, 0, z, z, z, z, z, z);
    }

    /** The worst (highest) reval-budget consumption in the scope — the honest headline. */
    public Fraction worstBudget(final List<Position> scope) {
        double worst = 0;
        for (final Position p : scope) {
            worst = Math.max(worst, p.revalBudget().value());
        }
        return new Fraction(worst);
    }

    /** Seconds since the oldest full reval in the scope, against the slice's as-of (never a clock). */
    public long oldestFreshness(final DeskDataset ds, final List<Position> scope) {
        long oldest = 0;
        for (final Position p : scope) {
            oldest = Math.max(oldest, ds.slice().asOf().secondsSince(p.lastFullReval()));
        }
        return oldest;
    }

    /**
     * Distance from spot to a position's barrier, in pips — derived from the
     * barrier level, the slice's spot, and the pair's pip size. No screen may
     * author this number; three of them display it.
     */
    public Optional<Pips> barrierDistance(final DeskDataset ds, final Position position) {
        final Optional<Instrument> instrument = ds.instrument(position.instrument());
        if (instrument.isEmpty() || instrument.get().barrier().isEmpty()) {
            return Optional.empty();
        }
        final Instrument i = instrument.get();
        return ds.pair(i.pair()).flatMap(pair ->
                ds.slice().spot(i.pair()).map(spot ->
                        pair.pipsBetween(i.barrier().orElseThrow().level(), spot.rate())));
    }
}
