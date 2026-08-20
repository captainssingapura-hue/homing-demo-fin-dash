package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;

import java.util.List;

/**
 * The fin-dash landing (L0) — one branch and one door:
 * <ul>
 *   <li><b>{@link FinDashDocCatalogue Documentation}</b> — the design docs the
 *       desk is built from, the user guide, and (under
 *       {@link WorkspaceIntrosCatalogue}) one introduction per persona;</li>
 *   <li><b>the workspace itself</b> — a single leaf, opening the Trader Desk.</li>
 * </ul>
 *
 * <h2>Why one workspace leaf and not thirteen</h2>
 *
 * <p>There used to be a <i>Workspaces</i> catalogue holding one tile per
 * persona. All thirteen opened the same {@code AppModule} and differed only by
 * {@code ws_kind}, which had two costs. Navigationally they were thirteen doors
 * with nothing written on them. Technically they broke the framework's
 * {@code /app-refs} lookup, which keys {@code AppDoc}s by app id: thirteen
 * registrations collapsed to one arbitrary winner, so every workspace page
 * showed a breadcrumb naming a different workspace (see
 * {@code docs/defect-app-refs-ambiguous.md}).</p>
 *
 * <p>Both costs come from modelling app state as navigation. Choosing a
 * persona is not a change of page — the shell switches kind in place, from the
 * workspace title. So the workspace registers once, the breadcrumb resolves
 * unambiguously, and the thirteen persona pages became documents that can
 * actually say something.</p>
 */
public record FinDashLandingCatalogue() implements L0_Catalogue<FinDashLandingCatalogue> {

    public static final FinDashLandingCatalogue INSTANCE = new FinDashLandingCatalogue();

    /**
     * The kind the single leaf opens. The desk's anchor persona: everything
     * else is one click away from the workspace title, and the trader is the
     * one participant whose screens the others are all reacting to.
     */
    private static final String LANDING_KIND = "trader";

    @Override public String name()    { return "FX Options Desk"; }
    @Override public String badge()   { return "FX OPTIONS"; }
    @Override public String summary() { return "Pricing, risk, and governance — the docs and one workspace per participant."; }

    @Override
    public List<? extends L1_Catalogue<FinDashLandingCatalogue, ?>> subCatalogues() {
        return List.of(FinDashDocCatalogue.INSTANCE);
    }

    @Override
    public List<Entry<FinDashLandingCatalogue>> leaves() {
        return List.of(Entry.of(this, new Navigable<>(
                GenericWorkspace.INSTANCE,
                new GenericWorkspace.Params(LANDING_KIND),
                "Workspace",
                "Open the desk. Every participant's workspace — trader to auditor — "
                        + "switched in place from the workspace title.")));
    }
}
