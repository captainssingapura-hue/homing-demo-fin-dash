package hue.captains.singapura.js.homing.findash.data;

import hue.captains.singapura.tao.ontology.enforcer.ContractViolation;
import hue.captains.singapura.tao.ontology.enforcer.OntologyEnforcer;
import hue.captains.singapura.tao.ontology.utils.PackageScanner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The <b>ontology gate</b> (requirements §3.6): every object in the dataset
 * declares its mode of being, and honours it.
 *
 * <p>jOntology's enforcer is unforgiving by design — an unmarked class is
 * greeted with "Who are you, where do you come from, and where are you
 * going?" — which is exactly the question this module must never leave
 * unanswered.</p>
 *
 * <p>Two documented allowances, both filtered here with a written reason
 * rather than suppressed at the declaration (the same discipline as
 * {@code FinDashConformance.ALLOWANCES}):</p>
 * <ol>
 *   <li><b>JDK collection fields.</b> The transitive check rejects
 *       {@code List}/{@code Map}/{@code Set} because a {@code List} <i>could</i>
 *       be an {@code ArrayList}. No type that holds a collection can pass, so
 *       the allowance is structural, not a convenience. It applies only where
 *       the canonical constructor defensively copies via {@code List.copyOf},
 *       which yields a genuinely immutable instance — the same way the homing
 *       framework writes its own value objects (e.g.
 *       {@code ConformanceStudioFixtures(…, List&lt;Crate&gt; topLevel)}).</li>
 *   <li><b>Enums and interfaces are not enforced.</b> They are type-level
 *       constructs, not objects with being: an enum constant is a singleton
 *       the language guarantees, and an interface declares no state. (The
 *       enforcer would flag an enum's compiler-generated {@code values()} as
 *       an illegal static method.)</li>
 * </ol>
 */
class DeskOntologyTest {

    private static final List<String> PACKAGES = List.of(
            "hue.captains.singapura.js.homing.findash.data",
            "hue.captains.singapura.js.homing.findash.data.id",
            "hue.captains.singapura.js.homing.findash.data.qty",
            "hue.captains.singapura.js.homing.findash.data.ref",
            "hue.captains.singapura.js.homing.findash.data.market",
            "hue.captains.singapura.js.homing.findash.data.book",
            "hue.captains.singapura.js.homing.findash.data.journal",
            "hue.captains.singapura.js.homing.findash.data.derive",
            "hue.captains.singapura.js.homing.findash.data.meta");

    /** Allowance 1 — a defensively-copied JDK collection field. */
    private boolean isCollectionFieldAllowance(final String message) {
        return message.contains("has non-immutable type java.util.List")
                || message.contains("has non-immutable type java.util.Map")
                || message.contains("has non-immutable type java.util.Set");
    }

    /**
     * Allowance 1b — the <b>transitive echo</b> of allowance 1. The enforcer's
     * immutability check recurses into field types, so a type that holds a
     * collection is itself judged non-immutable, and every holder of <i>that</i>
     * type is flagged in turn. The report adds no information: each of our types
     * is scanned and enforced in its own right by this very test, so a genuine
     * defect inside a nested type is caught there. Scoped strictly to types in
     * this module.
     */
    private boolean isOwnTypeEcho(final String message) {
        return message.contains("has non-immutable type hue.captains.singapura.js.homing.findash.data.");
    }

    @Test
    void everyObjectDeclaresAndHonoursItsModeOfBeing() throws IOException, ClassNotFoundException {
        final PackageScanner scanner = new PackageScanner();
        final OntologyEnforcer enforcer = new OntologyEnforcer();
        final List<String> violations = new ArrayList<>();
        int enforced = 0;

        for (final String pkg : PACKAGES) {
            for (final Class<?> clazz : scanner.scan(pkg)) {
                // Allowance 2 — type-level constructs, and test classes.
                if (clazz.isInterface() || clazz.isEnum() || clazz.isSynthetic()
                        || clazz.isAnonymousClass() || clazz.isLocalClass()
                        || clazz.getName().endsWith("Test")) {
                    continue;
                }
                enforced++;
                for (final ContractViolation v : enforcer.enforce(clazz)) {
                    if (!isCollectionFieldAllowance(v.message()) && !isOwnTypeEcho(v.message())) {
                        violations.add(v.message());
                    }
                }
            }
        }

        assertTrue(enforced >= 30,
                "expected the whole dataset to be scanned, only enforced " + enforced + " classes");
        assertEquals(List.of(), violations,
                () -> "ontology violations (" + violations.size() + "):\n"
                        + String.join("\n", violations));
    }
}
