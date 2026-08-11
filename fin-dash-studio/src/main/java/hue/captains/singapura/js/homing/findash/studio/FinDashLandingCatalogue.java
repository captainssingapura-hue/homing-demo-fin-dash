package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;

import java.util.List;

/**
 * The fin-dash landing (L0) — two branches for orientation-at-a-glance:
 * <ul>
 *   <li><b>{@link FinDashDocCatalogue Documentation}</b> — the design docs
 *       the desk is built from (UI study, engine architecture) and the user
 *       guide;</li>
 *   <li><b>{@link FinDashWorkspacesCatalogue Workspaces}</b> — one workspace
 *       per participant in the UI study's map (§2).</li>
 * </ul>
 * Thin by design — the widgets live in each persona module's
 * {@code WorkspaceSpec}; the docs own their own content.
 */
public record FinDashLandingCatalogue() implements L0_Catalogue<FinDashLandingCatalogue> {

    public static final FinDashLandingCatalogue INSTANCE = new FinDashLandingCatalogue();

    @Override public String name()    { return "FX Options Desk"; }
    @Override public String badge()   { return "FX OPTIONS"; }
    @Override public String summary() { return "Pricing, risk, and governance — the docs and one workspace per participant."; }

    @Override
    public List<? extends L1_Catalogue<FinDashLandingCatalogue, ?>> subCatalogues() {
        return List.of(FinDashDocCatalogue.INSTANCE, FinDashWorkspacesCatalogue.INSTANCE);
    }

    @Override
    public List<Entry<FinDashLandingCatalogue>> leaves() {
        return List.of();
    }
}
