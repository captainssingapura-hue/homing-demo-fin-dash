package hue.captains.singapura.js.homing.findash.data.id;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 0 — a pricing model <b>at a version</b>, e.g. {@code VV-2.3}. Both
 * halves are identity: "priced by VV" is not an answer an auditor accepts, so
 * the version travels with the name everywhere a figure carries lineage.
 *
 * <p>Era 1 (the lone trader) already stamps this: the desk has exactly one
 * model, but a figure that cannot say which model produced it cannot be
 * explained. Era 4 makes the reverse query — "everything priced by model X
 * v Y" — a governance requirement; the field is already there.</p>
 */
public record ModelRef(String model, String version) implements ValueObject {

    public ModelRef {
        Objects.requireNonNull(model, "ModelRef.model");
        Objects.requireNonNull(version, "ModelRef.version");
    }

    /** Display-neutral join key, e.g. {@code VV-2.3}. */
    public String key() {
        return model + "-" + version;
    }
}
