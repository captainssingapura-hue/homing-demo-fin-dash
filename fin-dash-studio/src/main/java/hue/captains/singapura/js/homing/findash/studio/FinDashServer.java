package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.findash.studio.conformance.FinDashConformance;
import hue.captains.singapura.js.homing.studio.base.Bootstrap;
import hue.captains.singapura.js.homing.studio.base.DefaultRuntimeParams;
import hue.captains.singapura.js.homing.studio.base.Umbrella;

/**
 * Launches the FX options desk — every persona workspace under one landing.
 * Landing at {@code /} (one tile per persona); a workspace at
 * {@code /app?app=genericWorkspace&ws_kind=<kind>} (open the {@code +} picker
 * to add widgets). Port defaults to {@code 8100} ({@code -Ddashboard.port=...});
 * kept off the conformance studio's 8101.
 *
 * <pre>mvn -o -pl fin-dash-studio exec:java \
 *   -Dexec.mainClass=hue.captains.singapura.js.homing.findash.studio.FinDashServer</pre>
 */
public final class FinDashServer {

    private FinDashServer() {}

    public static void main(String[] args) {
        var umbrella = new Umbrella.Solo<>(FinDashStudio.INSTANCE);
        int port = Integer.getInteger("dashboard.port", 8100);
        System.out.println("[fx-options-desk] serving on port " + port
                + " · workspaces: trader, etrading, sales, market-data, quant, risk,"
                + " governance, middle-office, ipv, platform, audit, summary");
        new Bootstrap<>(new FinDashFixtures(umbrella, FinDashConformance.TOP_LEVEL),
                new DefaultRuntimeParams(port)).start();
    }
}
