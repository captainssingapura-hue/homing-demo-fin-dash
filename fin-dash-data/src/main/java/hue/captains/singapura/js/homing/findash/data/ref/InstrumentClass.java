package hue.captains.singapura.js.homing.findash.data.ref;

/**
 * Stratum 2, Era 2 — the granularity the <b>selection matrix</b> works at.
 *
 * <p>Deliberately coarser than {@code OptionType}: methodology is chosen per
 * class of payoff, not per contract shape. Keeping them separate is what lets
 * the matrix say "barriers use SLV" without enumerating KO, KI, NT and OT.</p>
 */
public enum InstrumentClass { VANILLA, BARRIER, DIGITAL, STRUCTURED }
