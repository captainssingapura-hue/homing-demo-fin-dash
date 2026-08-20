package hue.captains.singapura.js.homing.findash.data.ref;

/**
 * Stratum 2 — who an actor is. <b>Grows one era at a time</b>: Era 1 is a desk
 * of traders and nothing else; every later persona is a value added when that
 * role actually arrives (Era 2 adds QUANT + MARKET_DATA_OPS, and so on). The
 * enum is therefore a readable history of the desk's growth.
 */
public enum Persona {
    /** Era 1 — runs the book: prices, marks, hedges. */
    TRADER,
    /** Era 1 — an automation, not a human: rules that act carry an actor too. */
    AUTOMATION,
    /** Era 2 — owns calibration quality and model methodology; owns Models. */
    QUANT,
    /** Era 3 — decides what may price and mark, with evidence. Owns ValidationRecords. */
    MODEL_VALIDATION
}
