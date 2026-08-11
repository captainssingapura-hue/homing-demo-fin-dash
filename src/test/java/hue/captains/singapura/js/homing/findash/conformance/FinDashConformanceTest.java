package hue.captains.singapura.js.homing.findash.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.CrateClosure;
import hue.captains.singapura.js.homing.conformance.rules.CrateConformance;
import hue.captains.singapura.js.homing.conformance.rules.CrateCoverage;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.Severity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The fin-dash conformance gate — Crate model + the extended {@link
 * FinDashConformance#POLICY} (framework rules plus the {@code risk-model} rule
 * set). Three checks:
 * <ol>
 *   <li><b>Crate integrity</b> — no orphans / illegal imports for the crate.</li>
 *   <li><b>Coverage</b> — no served module in this Maven module escapes a crate
 *       (build-time half of the serve-without-conformance guard; the runtime
 *       crate-gate in {@code RiskDashboardFixtures} is the other half).</li>
 *   <li><b>Rule conformance</b> — every crate module's served artifact grades
 *       clean; pre-existing patterns are grandfathered in the baseline.</li>
 * </ol>
 */
class FinDashConformanceTest {

    private static final boolean ALLOW_PRE_EXISTING =
            Boolean.parseBoolean(System.getProperty("conformance.allowPreExisting", "true"));

    @Test
    void crateIsStructurallyComplete() {
        CrateConformance.Result result = CrateConformance.evaluate(CrateClosure.of(FinDashConformance.TOP_LEVEL));
        CrateConformance.CrateResult crate = result.crates().get(FinDashCrate.INSTANCE.name());
        assertNotNull(crate, "the fin-dash crate must be present in the evaluation");
        assertEquals(List.of(), crate.orphans(),
                "every served fin-dash module must be declared in FinDashCrate");
        assertEquals(List.of(), crate.illegalImports(),
                "every cross-crate import must be declared in FinDashCrate.requires()");
    }

    @Test
    void everyServedModuleIsCrated() {
        List<String> gaps = CrateCoverage.check(
                CrateClosure.of(FinDashConformance.TOP_LEVEL), List.of(FinDashCrate.class));
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
