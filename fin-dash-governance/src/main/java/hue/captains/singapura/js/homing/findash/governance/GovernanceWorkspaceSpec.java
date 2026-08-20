package hue.captains.singapura.js.homing.findash.governance;

import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The model validation &amp; governance workspace (kind {@code "governance"})
 * — approve what may price and mark, with evidence (study §9): the change
 * console (W6, Ring-2 promotions as packages) and the model inventory with
 * the revalidation worklist and the auditors' reverse query.
 */
public final class GovernanceWorkspaceSpec implements WorkspaceSpec {

    public static final GovernanceWorkspaceSpec INSTANCE = new GovernanceWorkspaceSpec();

    private GovernanceWorkspaceSpec() {}

    @Override public String kind()  { return "governance"; }
    @Override public String title() { return "Model Governance"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup console  = WidgetGroup.of("Console");
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(ChangeConsoleWidget.class, WidgetLabel.of("Change Console"))
                    .withIcon(new WidgetIcon.Emoji("⚖"))
                    .withGroup(console),
            WidgetEntry.of(ModelInventoryWidget.class, WidgetLabel.of("Model Inventory"))
                    .withIcon(new WidgetIcon.Emoji("📚"))
                    .withGroup(console),
            WidgetEntry.of(GovernanceHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("⚖"))
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
