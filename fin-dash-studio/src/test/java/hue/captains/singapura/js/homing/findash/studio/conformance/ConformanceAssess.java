package hue.captains.singapura.js.homing.findash.studio.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;
import hue.captains.singapura.js.homing.conformance.rules.Severity;

import java.util.List;
import java.util.TreeMap;

/**
 * Read-only conformance assessment: what the gate sees, broken down by rule,
 * disposition and module. Writes nothing — the baseline is the ratchet and must
 * not move as a side effect of looking at it.
 *
 * <pre>mvn -o -pl fin-dash-studio exec:java -Dexec.classpathScope=test \
 *   -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.conformance.ConformanceAssess</pre>
 */
public final class ConformanceAssess {

    private ConformanceAssess() {}

    public static void main(final String[] args) {
        final List<Finding> raw = new ConformanceEngine(
                FinDashConformance.POLICY, new ServedModuleRenderer())
                .checkCrates(FinDashConformance.TOP_LEVEL);
        final List<GradedFinding> graded = FinDashConformance.grader(true).grade(raw);

        final var byRule = new TreeMap<String, int[]>();          // rule -> {errors, warnings}
        final var errorsByModule = new TreeMap<String, Integer>();
        for (final GradedFinding g : graded) {
            final String rule = g.finding().rule().value();
            final int[] slot = byRule.computeIfAbsent(rule, k -> new int[2]);
            if (g.isError()) {
                slot[0]++;
                final String cls = g.finding().moduleClass();
                errorsByModule.merge(cls.substring(cls.lastIndexOf('.') + 1), 1, Integer::sum);
            } else if (g.severity() == Severity.WARNING) {
                slot[1]++;
            }
        }

        System.out.println("=== conformance assessment ===");
        System.out.printf("raw findings: %d   graded: %d%n", raw.size(), graded.size());
        System.out.println();
        System.out.printf("%-24s %8s %10s%n", "rule", "ERRORS", "baselined");
        int errTotal = 0, warnTotal = 0;
        for (final var e : byRule.entrySet()) {
            System.out.printf("%-24s %8d %10d%n", e.getKey(), e.getValue()[0], e.getValue()[1]);
            errTotal += e.getValue()[0];
            warnTotal += e.getValue()[1];
        }
        System.out.printf("%-24s %8d %10d%n", "TOTAL", errTotal, warnTotal);

        if (!errorsByModule.isEmpty()) {
            System.out.println();
            System.out.println("modules with NEW errors:");
            errorsByModule.entrySet().stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .forEach(e -> System.out.printf("   %-32s %d%n", e.getKey(), e.getValue()));
            System.out.println();
            System.out.println("first 12 new errors:");
            graded.stream().filter(GradedFinding::isError).limit(12).forEach(g ->
                    System.out.println("   [" + g.finding().rule().value() + "] "
                            + g.finding().moduleClass().substring(
                                    g.finding().moduleClass().lastIndexOf('.') + 1)
                            + " — " + g.finding().message()));
        }
    }
}
