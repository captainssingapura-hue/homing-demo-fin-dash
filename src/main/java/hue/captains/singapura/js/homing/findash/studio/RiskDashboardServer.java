package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.findash.conformance.FinDashConformance;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * Launches the FX options risk dashboard. Landing at {@code /}; workspace at
 * {@code /app?app=genericWorkspace&ws_kind=risk-dashboard} (open the {@code +}
 * picker to add widgets). Port defaults to {@code 8100}
 * ({@code -Ddashboard.port=...}); kept off the conformance studio's 8101.
 *
 * <pre>mvn -o -pl homing-demo-fin-dash exec:java \
 *   -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.RiskDashboardServer</pre>
 */
public final class RiskDashboardServer {

    private RiskDashboardServer() {}

    public static void main(String[] args) {
        var umbrella = new Umbrella.Solo<>(RiskDashboardStudio.INSTANCE);
        int port = Integer.getInteger("dashboard.port", 8100);
        System.out.println("[risk-dashboard] serving on port " + port
                + " · workspace ws_kind=risk-dashboard");
        new Bootstrap<>(new RiskDashboardFixtures(umbrella, FinDashConformance.TOP_LEVEL),
                new DefaultRuntimeParams(port)).start();
    }
}
