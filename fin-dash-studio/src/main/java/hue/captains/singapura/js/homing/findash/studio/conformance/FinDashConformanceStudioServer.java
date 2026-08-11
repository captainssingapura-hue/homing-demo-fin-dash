package hue.captains.singapura.js.homing.findash.studio.conformance;

import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudio;
import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudioFixtures;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * Launches the conformance (Crate-)Studio over all of the fin-dash's crates —
 * reuses the framework studio (widgets, GetActions, report codec) pointed at
 * this demo's crates + build-exported report ({@link FinDashConformanceExport}).
 * Landing at {@code /}; workspace at
 * {@code /app?app=genericWorkspace&ws_kind=conformance}. Port defaults to
 * {@code 8101} ({@code -Dconformance.port=...}). The report groups the headless
 * models under the {@code risk-model} type with their rule set — the policy
 * extension made visible — and the crate tree shows one crate per persona
 * module.
 *
 * <pre>mvn -o -pl fin-dash-studio exec:java \
 *   -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.conformance.FinDashConformanceStudioServer</pre>
 */
public final class FinDashConformanceStudioServer {

    private FinDashConformanceStudioServer() {}

    public static void main(String[] args) {
        var umbrella = new Umbrella.Solo<>(ConformanceStudio.INSTANCE);
        int port = Integer.getInteger("conformance.port", 8101);
        System.out.println("[fin-dash crate-studio] serving on port " + port
                + " · " + FinDashConformance.TOP_LEVEL.size() + " crates");
        new Bootstrap<>(new ConformanceStudioFixtures(umbrella, FinDashConformance.TOP_LEVEL),
                new DefaultRuntimeParams(port)).start();
    }
}
