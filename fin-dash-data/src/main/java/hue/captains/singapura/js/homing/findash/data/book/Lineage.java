package hue.captains.singapura.js.homing.findash.data.book;

import hue.captains.singapura.js.homing.findash.data.id.ModelRef;
import hue.captains.singapura.js.homing.findash.data.id.SliceId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 4 — <b>P1 made structural</b>: what a figure was produced from.
 * Era 1 carries the slice and the model; that is already enough to answer
 * "why is this number what it is" and to make the reverse query
 * ("everything priced by model X") a join rather than a hope.
 *
 * <p>Era 2 widens this record with the surface epoch, because once surfaces
 * are fitted and versioned, naming the slice is no longer sufficient to
 * reproduce a price. That widening is the demo's own argument for why lineage
 * belongs in the data model instead of in a display string.</p>
 */
public record Lineage(SliceId slice, ModelRef model) implements ValueObject {

    public Lineage {
        Objects.requireNonNull(slice, "Lineage.slice");
        Objects.requireNonNull(model, "Lineage.model");
    }
}
