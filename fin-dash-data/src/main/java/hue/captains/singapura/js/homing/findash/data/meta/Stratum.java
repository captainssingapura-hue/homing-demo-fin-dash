package hue.captains.singapura.js.homing.findash.data.meta;

/**
 * A layer of the ontology. A type may reference only strata below its own —
 * the rule that keeps the graph acyclic and the module buildable in eras.
 */
public enum Stratum {
    IDENTITY, QUANTITY, REFERENCE, MARKET_STATE, BOOK, JOURNAL, DERIVED, BEHAVIOUR
}
