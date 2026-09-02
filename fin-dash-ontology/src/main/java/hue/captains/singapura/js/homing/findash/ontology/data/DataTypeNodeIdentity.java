package hue.captains.singapura.js.homing.findash.ontology.data;

import hue.captains.singapura.js.homing.tree.NodeIdentity;

/**
 * What a data-ontology tree node <b>is</b> (RFC 0053): the root, one stratum, or
 * one data type.
 *
 * <p>Two kinds share one record because the tree is small and the alternative —
 * a sealed hierarchy of three singletons — buys nothing here. What matters is
 * the pair of rules the RFC sets: the value is <b>intrinsic and global</b>
 * (a type's identity is its id, wherever the type is listed) and <b>equality is
 * the contract</b>, since this is a map key.</p>
 *
 * <p>These values were already being computed — they travelled in the node's
 * {@code Summary} display slot as {@code "stratum:ALL"}, {@code "stratum:BOOK"}
 * and the bare type id, because a display slot was the only channel available
 * for a machine field. RFC 0053 gives them their own.</p>
 */
public record DataTypeNodeIdentity(String kind, String value) implements NodeIdentity {

    public DataTypeNodeIdentity {
        if (kind == null || kind.isBlank())   throw new IllegalArgumentException("kind must not be blank");
        if (value == null || value.isBlank()) throw new IllegalArgumentException("value must not be blank");
    }

    /** The whole ontology. */
    public static DataTypeNodeIdentity root() {
        return new DataTypeNodeIdentity("stratum", "ALL");
    }

    /** One stratum, by its enum name. */
    public static DataTypeNodeIdentity stratum(final String name) {
        return new DataTypeNodeIdentity("stratum", name);
    }

    /** One data type, by its id. */
    public static DataTypeNodeIdentity type(final String id) {
        return new DataTypeNodeIdentity("type", id);
    }
}
