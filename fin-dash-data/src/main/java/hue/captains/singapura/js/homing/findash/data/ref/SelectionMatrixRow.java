package hue.captains.singapura.js.homing.findash.data.ref;

import hue.captains.singapura.js.homing.findash.data.id.ModelRef;
import hue.captains.singapura.js.homing.findash.data.id.PairId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * Stratum 2, Era 2 — <b>the selection matrix</b>: which model prices which
 * class of instrument, for which purpose, on which pairs.
 *
 * <p>This is the object that answers "what will the desk actually use?", and
 * it is the reason a trader's model badge is a <i>consequence</i> rather than a
 * choice: the pricer resolves (instrument class × purpose) through this row and
 * stamps whatever it finds. Change the row and every subsequent figure changes
 * its lineage — which is precisely why the row may not be edited in place.</p>
 *
 * <p>Ring 2 in the study's terms. Era 2 lets the quant who owns the models set
 * it directly; Era 3 takes that away and routes every edit through a change
 * package with a semantic diff, a golden-replay impact report, four-eyes
 * approval, and activation staged to a named epoch — never "now". The type does
 * not change when that happens; only who is allowed to produce a new one.</p>
 */
public record SelectionMatrixRow(InstrumentClass instrumentClass, PricingPurpose purpose,
                                 ModelRef model, List<PairId> pairs) implements ValueObject {

    public SelectionMatrixRow {
        Objects.requireNonNull(instrumentClass, "SelectionMatrixRow.instrumentClass");
        Objects.requireNonNull(purpose, "SelectionMatrixRow.purpose");
        Objects.requireNonNull(model, "SelectionMatrixRow.model");
        pairs = List.copyOf(Objects.requireNonNull(pairs, "SelectionMatrixRow.pairs"));
        if (pairs.isEmpty()) {
            throw new IllegalArgumentException(
                    "SelectionMatrixRow must name the pairs it governs — an empty row governs nothing");
        }
    }

    public boolean governs(final InstrumentClass klass, final PricingPurpose forPurpose,
                           final PairId pair) {
        return instrumentClass == klass && purpose == forPurpose && pairs.contains(pair);
    }
}
