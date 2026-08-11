package hue.captains.singapura.js.homing.findash.studio.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.CrateConformance;
import hue.captains.singapura.js.homing.conformance.rules.CrateCoverage;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import hue.captains.singapura.js.homing.core.Crate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The fin-dash conformance gate — Crate model + the extended {@link
 * FinDashConformance#POLICY} (framework rules plus the {@code risk-model} rule
 * set), across every fin-dash Maven module. Three checks:
 * <ol>
 *   <li><b>Crate integrity</b> — no orphans / illegal imports for any crate.</li>
 *   <li><b>Coverage</b> — no served module in any fin-dash Maven module escapes
 *       a crate (build-time half of the serve-without-conformance guard; the
 *       runtime crate-gate in {@code FinDashFixtures} is the other half).</li>
 *   <li><b>Rule conformance</b> — every crate module's served artifact grades
 *       clean; pre-existing patterns are grandfathered in the baseline.</li>
 * </ol>
 */
class FinDashConformanceTest {

    private static final boolean ALLOW_PRE_EXISTING =
            Boolean.parseBoolean(System.getProperty("conformance.allowPreExisting", "true"));

    @Test
    void everyCrateIsStructurallyComplete() {
        CrateConformance.Result result = CrateConformance.evaluate(CrateClosure.of(FinDashConformance.TOP_LEVEL));
        for (Crate crate : FinDashConformance.TOP_LEVEL) {
            CrateConformance.CrateResult r = result.crates().get(crate.name());
            assertNotNull(r, "crate " + crate.name() + " must be present in the evaluation");
            assertEquals(List.of(), r.orphans(),
                    "every served module in " + crate.name() + "'s Maven module must be declared in its crate");
            assertEquals(List.of(), r.illegalImports(),
                    "every cross-crate import in " + crate.name() + " must be declared in requires()");
        }
    }

    @Test
    void everyServedModuleIsCrated() {
        // One anchor class per fin-dash Maven module: its crate class.
        List<Class<?>> anchors = FinDashConformance.TOP_LEVEL.stream()
                .<Class<?>>map(Crate::getClass).toList();
        List<String> gaps = CrateCoverage.check(
                CrateClosure.of(FinDashConformance.TOP_LEVEL), anchors);
        assertEquals(List.of(), gaps,
                () -> "served modules missing from every crate:\n" + String.join("\n", gaps));
    }

    @Test
    void servedModulesAreConformant() {
        List<Finding> raw = new ConformanceEngine(FinDashConformance.POLICY, new ServedModuleRenderer())
                .checkCrates(FinDashConformance.TOP_LEVEL);
        List<GradedFinding> graded = FinDashConformance.grader(ALLOW_PRE_EXISTING).grade(raw);

        List<GradedFinding> errors = graded.stream().filter(GradedFinding::isError).toList();
        graded.stream().filter(g -> g.severity() == Severity.WARNING)
                .forEach(g -> System.out.println("[fin-dash-conformance] WARN " + describe(g)));

        assertEquals(List.of(), errors, () -> "fin-dash conformance ERRORS (" + errors.size() + "):\n"
                + errors.stream().map(FinDashConformanceTest::describe).collect(Collectors.joining("\n")));
    }

    private static String describe(GradedFinding g) {
        Finding f = g.finding();
        return f.moduleClass() + " [" + f.rule().value() + "] " + f.message()
                + (g.note().isBlank() ? "" : "  (" + g.note() + ")");
    }
}
