package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import hue.captains.singapura.js.homing.workspace.shell.GenericWorkspace;

import java.util.ArrayList;
import java.util.List;

/**
 * The workspaces branch of the fin-dash landing (L1 under
 * {@link FinDashLandingCatalogue}): one tile per persona workspace — the UI
 * study's participant map (§2) rendered as tiles. Each tile opens
 * {@code GenericWorkspace} with that persona's kind; the widgets live in each
 * persona module's {@code WorkspaceSpec}.
 */
public record FinDashWorkspacesCatalogue()
        implements L1_Catalogue<FinDashLandingCatalogue, FinDashWorkspacesCatalogue> {

    public static final FinDashWorkspacesCatalogue INSTANCE = new FinDashWorkspacesCatalogue();

    @Override public FinDashLandingCatalogue parent() { return FinDashLandingCatalogue.INSTANCE; }
    @Override public String name()    { return "Workspaces"; }
    @Override public String summary() { return "One workspace per participant — trader to auditor."; }
    @Override public String badge()   { return "WORKSPACES"; }
    @Override public String icon()    { return "🖥"; }

    /** kind → (title, blurb): the participant map, one row per persona. */
    private record Persona(String kind, String title, String blurb) {}

    private static final List<Persona> PERSONAS = List.of(
        new Persona("trader",        "Trader Desk",            "Run the book: price, mark, hedge. Pricer, surface manager, risk blotter."),
        new Persona("etrading",      "e-Trading Supervision",  "Keep auto-quoting safe and competitive. Quoting console, RFQ tape."),
        new Persona("sales",         "Sales & Structuring",    "Price for clients, capture margin. Client pricer, RFQ workflow."),
        new Persona("market-data",   "Market Data Operations", "Guarantee clean inputs. Feed health, quote quality, override inventory."),
        new Persona("quant",         "Quant Lab",              "Own calibration quality and evolution. Calibration lab, replay lab."),
        new Persona("risk",          "Market Risk",            "Independent view of exposures vs limits. Risk views, scenarios, limits."),
        new Persona("governance",    "Model Governance",       "Approve what may price and mark. Change console, model inventory."),
        new Persona("middle-office", "Middle Office",          "Every trade correct through its lifecycle. Blotter, lifecycle events, breaks."),
        new Persona("ipv",           "Product Control & IPV",  "P&L is right and marks are independent. P&L explain, IPV workbench."),
        new Persona("platform",      "Platform Operations",    "The system itself is healthy. System console, epochs, failover."),
        new Persona("audit",         "Audit Explorer",         "Reconstruct and attest anything. Cross-journal search, time travel."),
        new Persona("summary",       "Management Summary",     "Situational awareness, escalation. Every tile a door."));

    @Override
    public List<Entry<FinDashWorkspacesCatalogue>> leaves() {
        var entries = new ArrayList<Entry<FinDashWorkspacesCatalogue>>();
        for (Persona p : PERSONAS) {
            entries.add(Entry.of(this, new Navigable<>(GenericWorkspace.INSTANCE,
                    new GenericWorkspace.Params(p.kind()), p.title(), p.blurb())));
        }
        return List.copyOf(entries);
    }
}
