package hue.captains.singapura.js.homing.findash.data.ref;

import hue.captains.singapura.js.homing.findash.data.id.ActorId;
import hue.captains.singapura.js.homing.findash.data.id.ModelRef;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * Stratum 2, Era 2 — <b>a named pricing model, as an object the desk does not
 * own.</b>
 *
 * <p>Era 1 needed only {@link ModelRef}: a stamp, so a figure could say what
 * produced it. That is the identity. This is the <i>entity</i>, and it appears
 * exactly when someone other than the trader has authority over it — the quant
 * who owns the methodology, and later the validator who decides whether it may
 * price at all.</p>
 *
 * <p>The split is deliberate and load-bearing. A retired model must still be
 * nameable, because ten-year-old trades carry its stamp; so lineage points at
 * the {@code ModelRef}, never at this record. A model can therefore be retired,
 * re-approved, or re-owned without touching a single historical figure.</p>
 *
 * <p>Ring 1 in the study's terms: the trader <b>selects</b> from what the matrix
 * allows and can see the badge, but cannot change any of this. The UI renders
 * it read-only — structurally, because there is no write path here at all.</p>
 */
public record Model(ModelRef ref, String displayName, ModelStatus status,
                    ActorId owner) implements ValueObject {

    public Model {
        Objects.requireNonNull(ref, "Model.ref");
        Objects.requireNonNull(displayName, "Model.displayName");
        Objects.requireNonNull(status, "Model.status");
        Objects.requireNonNull(owner, "Model.owner");
    }

    /** A candidate runs in shadow: its numbers exist, and may never be published. */
    public boolean mayBePublished() {
        return status == ModelStatus.APPROVED;
    }
}
