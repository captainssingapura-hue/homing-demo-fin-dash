package hue.captains.singapura.js.homing.findash.data.ref;

/**
 * Stratum 2, Era 3 — the result of an independent validation.
 *
 * <p>{@link #APPROVED_WITH_CONDITIONS} exists because real validation rarely
 * says yes or no: it says yes, within these bounds. A model whose conditions
 * are invisible is a model whose limits get forgotten.</p>
 */
public enum ValidationOutcome { PENDING, APPROVED, APPROVED_WITH_CONDITIONS, REJECTED }
