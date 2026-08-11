package hue.captains.singapura.js.homing.findash.studio;

import hue.captains.singapura.js.homing.findash.widget.RiskDashboardPlaceholderWidget;
import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The fin-dash workspace (kind {@code "risk-dashboard"}) — the trader dashboard's
 * widget roster on the reused {@code GenericWorkspace} shell (split panes, tabs,
 * layout persistence, party bus, all free). Add each new widget here as an entry.
 *
 * <p>Scaffold: one placeholder widget. Grow the roster toward the UI study's
 * trader screens (Pricer W1, Surface Manager W2, Risk Blotter W4), grouped as
 * they land. The generic {@code NavigatorSecretary} bus is reused verbatim
 * (exposed as {@code navParty}) — swap in a dashboard-specific secretary when
 * cross-widget events (portfolio/scenario selection) need bespoke handling.</p>
 */
public final class RiskDashboardWorkspaceSpec implements WorkspaceSpec {

    public static final RiskDashboardWorkspaceSpec INSTANCE = new RiskDashboardWorkspaceSpec();

    private RiskDashboardWorkspaceSpec() {}

    @Override public String kind()  { return "risk-dashboard"; }
    @Override public String title() { return "Risk Dashboard"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(RiskDashboardPlaceholderWidget.class, WidgetLabel.of("Dashboard"))
                    .withIcon(new WidgetIcon.Emoji("📉"))   // 📉
                    .withGroup(overview)
        );
    }

    @Override
    public List<PartyDecl> parties() {
        return List.of(
            PartyDecl.of("navigation", NavigatorSecretaryModule.INSTANCE, "NavigatorSecretary")
                     .exposedAs("navParty")
                     .build()
        );
    }
}
