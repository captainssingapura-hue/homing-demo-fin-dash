package hue.captains.singapura.js.homing.findash.data.meta;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * <b>A type, described as data.</b> One entry in the ontology catalogue: what
 * it is, what mode of being it declares, which era introduced it, what it
 * points at, and which widgets need it.
 *
 * <p>This is the metamodel — the ontology of the ontology. Writing it as
 * {@code ValueObject}s rather than prose is what lets the demo browse its own
 * data model, and lets the question "which screens break if I change this
 * type?" be answered by a query instead of a search.</p>
 *
 * @param era the era that introduced the type (1 = the trader's world). The
 *            field is a readable history of how the desk grew, and the check
 *            that each era is genuinely additive.
 */
public record DataType(DataTypeId id, String javaType, Stratum stratum,
                       OntologyMarker marker, int era, String summary,
                       List<Relation> relations, List<WidgetUsage> usages)
        implements ValueObject {

    public DataType {
        Objects.requireNonNull(id, "DataType.id");
        Objects.requireNonNull(javaType, "DataType.javaType");
        Objects.requireNonNull(stratum, "DataType.stratum");
        Objects.requireNonNull(marker, "DataType.marker");
        Objects.requireNonNull(summary, "DataType.summary");
        relations = List.copyOf(Objects.requireNonNull(relations, "DataType.relations"));
        usages = List.copyOf(Objects.requireNonNull(usages, "DataType.usages"));
        if (era < 1) {
            throw new IllegalArgumentException("DataType.era starts at 1");
        }
    }

    /** The widgets that publish selections of this type — the drivers. */
    public List<WidgetUsage> drivers() {
        return usages.stream().filter(u -> u.role() == UsageRole.DRIVES).toList();
    }

    /** The widgets that re-scope when this type is selected — the followers. */
    public List<WidgetUsage> followers() {
        return usages.stream().filter(u -> u.role() == UsageRole.FOLLOWS).toList();
    }

    /** Distinct workspaces that touch this type at all — the reach of a change. */
    public List<String> workspaces() {
        return usages.stream().map(WidgetUsage::workspaceKind).distinct().sorted().toList();
    }
}
