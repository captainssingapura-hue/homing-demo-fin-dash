package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;

import java.util.List;

/**
 * The fin-dash landing: one tile that opens the {@code "risk-dashboard"}
 * workspace. Thin by design — it satisfies the Bootstrap harness (a studio needs
 * a home) and brands the entry page; the widgets live in the workspace
 * ({@link RiskDashboardWorkspaceSpec}), not as catalogue content.
 */
public record RiskDashboardLandingCatalogue() implements L0_Catalogue<RiskDashboardLandingCatalogue> {

    public static final RiskDashboardLandingCatalogue INSTANCE = new RiskDashboardLandingCatalogue();

    @Override public String name()    { return "Risk Dashboard"; }
    @Override public String badge()   { return "FX OPTIONS"; }
    @Override public String summary() { return "Live pricing & risk for the FX options book."; }

    @Override
    public List<Entry<RiskDashboardLandingCatalogue>> leaves() {
        Navigable<GenericWorkspace.Params, GenericWorkspace> workspace =
                new Navigable<>(GenericWorkspace.INSTANCE,
                        new GenericWorkspace.Params("risk-dashboard"),
                        "Risk Dashboard",
                        "Pricer, surface manager, and position & risk blotter.");
        return List.of(Entry.of(this, workspace));
    }
}
