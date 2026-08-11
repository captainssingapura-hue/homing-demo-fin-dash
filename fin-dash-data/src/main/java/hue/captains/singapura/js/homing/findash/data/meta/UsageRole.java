package hue.captains.singapura.js.homing.findash.data.meta;

/**
 * What a widget does with a type — the vocabulary of the connection map
 * (requirements §3.5).
 */
public enum UsageRole {
    /** Publishes a selection of this type onto the bus. */
    DRIVES,
    /** Filters or re-scopes itself when this type is selected elsewhere. */
    FOLLOWS,
    /** Displays instances of this type. */
    RENDERS,
    /** Computes a projection from this type. */
    DERIVES_FROM
}
