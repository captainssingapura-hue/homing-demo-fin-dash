package hue.captains.singapura.js.homing.findash.sales;

import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements.MainAndSide;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The Sales / structuring workspace (kind {@code "sales"}) on the reused
 * {@code GenericWorkspace} shell. Scaffold: the home card; the persona's real
 * screens (UI study) land beside it, grouped as they arrive.
 *
 * <p>The generic {@code NavigatorSecretary} bus is reused (exposed as
 * {@code navParty}) until this persona's cross-widget events need a bespoke
 * secretary.</p>
 */
public final class SalesWorkspaceSpec implements WorkspaceSpec {

    public static final SalesWorkspaceSpec INSTANCE = new SalesWorkspaceSpec();

    private SalesWorkspaceSpec() {}

    @Override public String kind()  { return "sales"; }
    @Override public String title() { return "Sales & Structuring"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(ClientPricerWidget.class, WidgetLabel.of("Client Pricer"))
                    .withIcon(new WidgetIcon.Emoji("🤝"))
                    .withGroup(WidgetGroup.of("Console")),
            WidgetEntry.of(SalesHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("🤝"))
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

    /**
     * The client pricer is the whole job; the persona card beside it carries the
     * mission and the cadence while the rest of the sales screens land.
     */
    @Override
    public Arrangement arrangement() {
        return PaneArrangements.MAIN_AND_SIDE.allocate()
                .place(MainAndSide.MAIN, ClientPricerWidget.class)
                .place(MainAndSide.SIDE, SalesHomeWidget.class)
                .build();
    }
}
