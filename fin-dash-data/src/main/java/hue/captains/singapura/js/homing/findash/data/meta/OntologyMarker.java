package hue.captains.singapura.js.homing.findash.data.meta;

/**
 * The mode of being a type declares (requirements §3.2). Mirrors the jOntology
 * markers, plus the two type-level constructs the enforcer does not police
 * because the language already guarantees them.
 */
public enum OntologyMarker {
    VALUE_OBJECT, FUNCTIONAL_OBJECT, STATELESS_FUNCTIONAL_OBJECT, IMMUTABLE,
    SEALED_INTERFACE, ENUM
}
