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
        out.add("# Grandfathered violations. Anything NOT here (or allowlisted) is a NEW");
        out.add("# violation and fails the build. This list may only shrink.");
        out.add("#");
        out.add("# The typed-CSS migration is COMPLETE: every widget and kit module now");
        out.add("# builds branch-owned elements and styles through the CssGroups in");
        out.add("# findash.core.css. What remains is a single upstream gap —");
        out.add("# SmileChartModule cannot be made conformant downstream, because");
        out.add("# branch.createElement has no namespace support while");
        out.add("# UseDomOpsPartyRule forbids document.createElementNS, so conformant");
        out.add("# code cannot create SVG at all. These clear when core adds");
        out.add("# branch.createElementNS; FdChartCss is already written and waiting.");
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
