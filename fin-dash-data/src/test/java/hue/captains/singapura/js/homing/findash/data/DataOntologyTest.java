package hue.captains.singapura.js.homing.findash.data;

import hue.captains.singapura.js.homing.findash.data.meta.DataOntology;
import hue.captains.singapura.js.homing.findash.data.meta.DataType;
import hue.captains.singapura.js.homing.findash.data.meta.DataTypeId;
import hue.captains.singapura.js.homing.findash.data.meta.Relation;
import hue.captains.singapura.js.homing.findash.data.meta.Stratum;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The <b>catalogue gate</b>: the declared type graph must be closed and
 * layered. These are the checks that make the catalogue trustworthy enough to
 * browse — and to populate against, later.
 */
class DataOntologyTest {

    private final DataOntology ontology = new DataOntology();

    @Test
    void everyRelationTargetIsADeclaredType() {
        final Set<String> declared = new HashSet<>();
        for (final DataType t : ontology.types()) {
            declared.add(t.id().value());
        }
        final List<String> dangling = new ArrayList<>();
        for (final DataType t : ontology.types()) {
            for (final Relation r : t.relations()) {
                if (!declared.contains(r.to().value())) {
                    dangling.add(t.id().value() + " --" + r.edge() + "--> " + r.to().value());
                }
            }
        }
        assertEquals(List.of(), dangling,
                () -> "relations pointing at undeclared types:\n" + String.join("\n", dangling));
    }

    @Test
    void typeIdsAreUnique() {
        final Set<String> seen = new HashSet<>();
        final List<String> duplicates = new ArrayList<>();
        for (final DataType t : ontology.types()) {
            if (!seen.add(t.id().value())) {
                duplicates.add(t.id().value());
            }
        }
        assertEquals(List.of(), duplicates, () -> "duplicate type ids: " + duplicates);
    }

    /**
     * The layering rule: a type may reference only strata at or below its own.
     * This is what keeps the graph acyclic and lets the module grow in eras.
     */
    @Test
    void relationsPointDownwardOnly() {
        final List<String> violations = new ArrayList<>();
        for (final DataType t : ontology.types()) {
            for (final Relation r : t.relations()) {
                final DataType target = ontology.type(r.to()).orElseThrow();
                if (target.stratum().ordinal() > t.stratum().ordinal()) {
                    violations.add(t.id().value() + " (" + t.stratum() + ") --" + r.edge()
                            + "--> " + target.id().value() + " (" + target.stratum() + ")");
                }
            }
        }
        assertEquals(List.of(), violations,
                () -> "relations pointing UP a stratum:\n" + String.join("\n", violations));
    }

    @Test
    void everyStratumIsPopulated() {
        for (final Stratum s : Stratum.values()) {
            assertFalse(ontology.inStratum(s).isEmpty(), "no types declared in stratum " + s);
        }
    }

    /** The connection map must actually connect: the anchors carry drivers and followers. */
    @Test
    void thePortfolioSelectionIsDrivenAndFollowed() {
        final DataType node = ontology.type(new DataTypeId("PortfolioNodeId")).orElseThrow();
        assertFalse(node.drivers().isEmpty(), "nothing drives a portfolio selection");
        assertTrue(node.followers().size() >= 2,
                "a portfolio selection must be followed by at least the portfolio and the blotter");
    }

    /** The inverse query is what the usage widget will ask — prove it resolves. */
    @Test
    void typesUsedByAWidgetResolve() {
        final List<DataType> used = ontology.typesUsedBy("trade-blotter");
        assertTrue(used.size() >= 4,
                "the trade blotter should depend on several types, found " + used.size());
    }

    @Test
    void era1IsTheWholeCatalogueForNow() {
        assertEquals(ontology.types().size(), ontology.ofEra(1).size(),
                "only Era 1 (the trader's world) is declared so far");
    }
}
