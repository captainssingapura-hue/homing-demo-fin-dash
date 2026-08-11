package hue.captains.singapura.js.homing.findash.studio.conformance;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.engine.ServedModuleRenderer;
import hue.captains.singapura.js.homing.conformance.export.ConformanceReportWriter;
import hue.captains.singapura.js.homing.conformance.rules.report.ConformanceRun;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Build-time entry point for the fin-dash conformance studio: assemble the
 * demo's conformance report with the <b>extended</b> policy ({@link
 * FinDashConformance#POLICY}, via the policy-taking engine constructor) and write
 * it (arg 0) for the studio's {@code ConformanceReportSource} to read back. Wired
 * into the Maven {@code exec} plugin at {@code process-classes}.
 */
public final class FinDashConformanceExport {

    private FinDashConformanceExport() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: FinDashConformanceExport <output-directory>");
        }
        Path dir = Paths.get(args[0]);

        ConformanceRun run = new ConformanceEngine(FinDashConformance.POLICY, new ServedModuleRenderer())
                .assemble(FinDashConformance.TOP_LEVEL, FinDashConformance.grader(true));
        new ConformanceReportWriter().write(dir, run);

        System.out.println("[FinDashConformanceExport] wrote report to " + dir
                + " (" + run.modules().size() + " modules, "
                + run.summary().errorCount() + " errors, "
                + run.summary().warningCount() + " warnings)");
    }
}
