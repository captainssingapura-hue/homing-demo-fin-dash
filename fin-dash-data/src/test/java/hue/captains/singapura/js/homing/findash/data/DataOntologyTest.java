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

    /**
     * <b>The era rule.</b> The desk grew one set of actors at a time, and each
     * layer may build on what came before — never on what came after. So a
     * type may reference only types of its own era or earlier.
     *
     * <p>This is what keeps the growth story honest, and it is why identity is
     * split from entity: Era-1 {@code Lineage} references {@code ModelRef}
     * (Era 1), never the Era-2 {@code Model}. Had lineage pointed at the
     * entity, Era 1 could not exist without governance — which is false, and
     * this test would say so.</p>
     */
    @Test
    void erasDependOnlyOnEarlierEras() {
        final List<String> violations = new ArrayList<>();
        for (final DataType t : ontology.types()) {
            for (final Relation r : t.relations()) {
                final DataType target = ontology.type(r.to()).orElseThrow();
                if (target.era() > t.era()) {
                    violations.add("era " + t.era() + " " + t.id().value() + " --" + r.edge()
                            + "--> era " + target.era() + " " + target.id().value());
                }
            }
        }
        assertEquals(List.of(), violations,
                () -> "types depending on a LATER era:\n" + String.join("\n", violations));
    }

    /** Eras are contiguous from 1 — a gap means a layer was skipped or mislabelled. */
    @Test
    void erasAreContiguousFromOne() {
        final int max = ontology.types().stream().mapToInt(DataType::era).max().orElse(0);
        for (int era = 1; era <= max; era++) {
            assertFalse(ontology.ofEra(era).isEmpty(), "no types declared for era " + era);
        }
    }

    /**
     * The model must be a governed object, not a free-text badge: the entity is
     * owned, its status is explicit, and approval is evidence keyed to a version.
     */
    @Test
    void modelsAreControlledByActorsOtherThanTheDesk() {
        final DataType model = ontology.type(new DataTypeId("Model")).orElseThrow();
        assertTrue(model.relations().stream().anyMatch(r -> r.edge().equals("owner")),
                "a Model must name its owner — an unowned model is nobody's responsibility");
        assertTrue(model.relations().stream().anyMatch(r -> r.edge().equals("status")),
                "a Model must carry a status, or CANDIDATE cannot be distinguished from APPROVED");

        final DataType validation = ontology.type(new DataTypeId("ValidationRecord")).orElseThrow();
        assertTrue(validation.relations().stream()
                        .anyMatch(r -> r.edge().equals("model") && r.to().value().equals("ModelRef")),
                "validation must approve a VERSION (ModelRef), never the entity");
        assertTrue(validation.relations().stream().anyMatch(r -> r.edge().equals("validator")),
                "an approval without an author is an adjective, not evidence");
    }

    /** Lineage may never depend on the governed entity — see {@link #erasDependOnlyOnEarlierEras}. */
    @Test
    void lineagePointsAtTheModelIdentityNotTheEntity() {
        final DataType lineage = ontology.type(new DataTypeId("Lineage")).orElseThrow();
        assertTrue(lineage.relations().stream()
                        .anyMatch(r -> r.edge().equals("model") && r.to().value().equals("ModelRef")),
                "lineage must reference ModelRef so a retired model stays nameable");
        assertFalse(lineage.relations().stream().anyMatch(r -> r.to().value().equals("Model")),
                "lineage must NOT reference the Model entity — old figures outlive it");
    }
}
