package hue.captains.singapura.js.homing.findash.studio.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.rules.Finding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Regenerates {@code risk-conformance-baseline.txt} — the grandfathered list of
 * pre-existing findings.
 *
 * <p>Deliberately a Java writer rather than a shell redirect: on Windows the
 * console mangles UTF-8 (an em-dash in a rule message becomes a lone 0x97),
 * which makes {@code Files.readAllLines} throw and the WHOLE baseline load as
 * empty — every grandfathered finding silently becomes a new error. KT.md §9.3
 * records that this cost real debugging time; this class is the safe path.</p>
 *
 * <pre>mvn -o -pl fin-dash-studio exec:java -Dexec.classpathScope=test \
 *   -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.conformance.BaselineRegen</pre>
 */
public final class BaselineRegen {

    private BaselineRegen() {}

    public static void main(final String[] args) throws IOException {
        final List<Finding> raw = new ConformanceEngine(
                FinDashConformance.POLICY, new ServedModuleRenderer())
                .checkCrates(FinDashConformance.TOP_LEVEL);

        final var byRule = new TreeMap<String, Integer>();
        final var lines = new ArrayList<String>();
        for (final Finding f : raw) {
            byRule.merge(f.rule().value(), 1, Integer::sum);
            lines.add(f.moduleClass() + " [" + f.rule().value() + "] " + f.message());
        }
        lines.sort(String::compareTo);

        final var out = new ArrayList<String>();
        out.add("# RFC 0044 conformance baseline — homing-demo-fin-dash.");
        out.add("# Pre-existing violations, grandfathered to warnings. Anything NOT here");
        out.add("# (or allowlisted) is a NEW violation and fails the build.");
        out.add("#");
        out.add("# The no-inline-style entries are a MIGRATION RATCHET, not an exemption:");
        out.add("# every widget still styles inline, and each one converted to typed CSS");
        out.add("# classes removes its lines from here. This list may only shrink.");
        out.add("#");
        for (final var e : byRule.entrySet()) {
            out.add("#   " + e.getValue() + " x " + e.getKey());
        }
        out.add("#");
        out.addAll(lines);

        final Path target = Path.of(args.length > 0 ? args[0]
                : "src/main/resources/risk-conformance-baseline.txt");
        Files.writeString(target, String.join("\n", out) + "\n", StandardCharsets.UTF_8);
        System.out.println("[BaselineRegen] wrote " + lines.size() + " fingerprints to " + target);
        byRule.forEach((rule, n) -> System.out.println("    " + n + " x " + rule));
    }
}
