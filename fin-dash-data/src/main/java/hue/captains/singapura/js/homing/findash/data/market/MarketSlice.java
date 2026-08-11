package hue.captains.singapura.js.homing.findash.data.market;

import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.js.homing.findash.data.id.SliceId;
import hue.captains.singapura.js.homing.findash.data.id.TenorId;
import hue.captains.singapura.js.homing.findash.data.qty.AsOf;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Stratum 3 — <b>the</b> coherent market snapshot: spots and vol marks that
 * belong together, identified so any figure can name the slice it was priced
 * on (R5).
 *
 * <p>This is the seed of P5. Era 1 has one trader and could have got away with
 * ambient globals — but the moment a second consumer exists, "the market" must
 * be a named, shared thing or two screens can disagree and neither is wrong.
 * The slice is that thing, present from the first era.</p>
 *
 * <p>Collection fields are defensively copied — see the ontology gate's
 * documented allowance (requirements §3.6): {@code List.copyOf} yields a
 * genuinely immutable list, which is what the {@code ValueObject} marker
 * asserts.</p>
 */
public record MarketSlice(SliceId id, AsOf asOf, List<SpotRate> spots, List<VolMark> volMarks)
        implements ValueObject {

    public MarketSlice {
        Objects.requireNonNull(id, "MarketSlice.id");
        Objects.requireNonNull(asOf, "MarketSlice.asOf");
        spots = List.copyOf(Objects.requireNonNull(spots, "MarketSlice.spots"));
        volMarks = List.copyOf(Objects.requireNonNull(volMarks, "MarketSlice.volMarks"));
    }

    public Optional<SpotRate> spot(final PairId pair) {
        return spots.stream().filter(s -> s.pair().equals(pair)).findFirst();
    }

    public Optional<VolMark> volMark(final PairId pair, final TenorId tenor) {
        return volMarks.stream()
                .filter(v -> v.pair().equals(pair) && v.tenor().equals(tenor))
                .findFirst();
    }
}
