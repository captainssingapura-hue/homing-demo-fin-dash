package hue.captains.singapura.js.homing.findash.risk;

import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The market risk manager workspace (kind {@code "risk"}) — an independent,
 * complete view of exposures against limits, on risk's own cadence (study
 * §8): limits &amp; utilization with the breach worklist, the governed
 * scenario workbench, and the concentration views (barrier density,
 * event-date vega). No mark or model controls — independence is the point.
 */
public final class RiskWorkspaceSpec implements WorkspaceSpec {

    public static final RiskWorkspaceSpec INSTANCE = new RiskWorkspaceSpec();

    private RiskWorkspaceSpec() {}

    @Override public String kind()  { return "risk"; }
    @Override public String title() { return "Market Risk"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup views    = WidgetGroup.of("Views");
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(RiskViewsWidget.class, WidgetLabel.of("Risk Views"))
                    .withIcon(new WidgetIcon.Emoji("🛡"))
                    .withGroup(views),
            WidgetEntry.of(ScenarioWorkbenchWidget.class, WidgetLabel.of("Scenarios"))
                    .withIcon(new WidgetIcon.Emoji("🧪"))
                    .withGroup(views),
            WidgetEntry.of(ConcentrationWidget.class, WidgetLabel.of("Concentrations"))
                    .withIcon(new WidgetIcon.Emoji("🧲"))
                    .withGroup(views),
            WidgetEntry.of(RiskHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("🛡"))
                    .withGroup(overview)
        );
    }

    @Override
    public List<PartyDecl> parties() {
        return List.of(
            PartyDecl.of("navigation", NavigatorSecretaryModule.INSTANCE, "NavigatorSecretary")
                     .exposedAs("navParty")
                     .build(),
            PartyDecl.of("desk", DeskSecretaryModule.INSTANCE, "DeskSecretary")
                     .exposedAs("deskParty")
                     .build()
        );
    }
}
