package hue.captains.singapura.js.homing.findash.data.qty;

/**
 * Stratum 1 — a settlement currency. An enum rather than {@code java.util.Currency}
 * so the value is transitively immutable by construction (and so the demo's
 * universe stays closed — R4 self-containment).
 */
public enum Ccy { EUR, USD, JPY, GBP, AUD, CHF, NZD }
