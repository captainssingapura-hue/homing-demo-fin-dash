package hue.captains.singapura.js.homing.findash.conformance;

import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudio;
import hue.captains.singapura.js.homing.conformance.studio.ConformanceStudioFixtures;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * Launches the conformance (Crate-)Studio over the fin-dash's own {@link
 * FinDashCrate} — reuses the framework studio (widgets, GetActions, report codec)
 * pointed at this demo's crate + build-exported report ({@link
 * FinDashConformanceExport}). Landing at {@code /}; workspace at
 * {@code /app?app=genericWorkspace&ws_kind=conformance}. Port defaults to
 * {@code 8101} ({@code -Dconformance.port=...}). The report groups {@code
 * VarModel} under the {@code risk-model} type with its rule set — the policy
 * extension made visible.
 */
public final class FinDashConformanceStudioServer {

    private FinDashConformanceStudioServer() {}

    public static void main(String[] args) {
        var umbrella = new Umbrella.Solo<>(ConformanceStudio.INSTANCE);
        int port = Integer.getInteger("conformance.port", 8101);
        System.out.println("[fin-dash crate-studio] serving on port " + port);
        new Bootstrap<>(new ConformanceStudioFixtures(umbrella, FinDashConformance.TOP_LEVEL),
                new DefaultRuntimeParams(port)).start();
    }
}
