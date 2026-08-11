package hue.captains.singapura.js.homing.findash.data.ref;

/**
 * Stratum 2, Era 2 — where a model stands.
 *
 * <p>Era 1 has no need of this: one desk, one model, nothing to decide. The
 * status only becomes meaningful once someone other than the trader has a say —
 * which is the whole point of the value {@link #CANDIDATE}: a model that exists,
 * runs, and produces numbers that <b>may not be published</b>.</p>
 */
public enum ModelStatus {
    /** Runs in shadow (challenger). Its output is never published. */
    CANDIDATE,
    /** Approved for the uses the selection matrix assigns it. */
    APPROVED,
    /** No longer selectable; retained because old figures still name it (P1). */
    RETIRED
}
