package hue.captains.singapura.js.homing.findash.marketdata;

import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements.MainAndOutput;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The market data operations workspace (kind {@code "market-data"}) —
 * guarantee clean inputs, or the right people know they are not (study §6):
 * the feed &amp; quality console (source × kind grid, monitors, quarantine
 * queue) and the override inventory (every live Ring-3 action, one view).
 *
 * <p>Two parties: navigation plus the desk bus — quarantine rows publish pair
 * selections the trader widgets follow.</p>
 */
public final class MarketDataWorkspaceSpec implements WorkspaceSpec {

    public static final MarketDataWorkspaceSpec INSTANCE = new MarketDataWorkspaceSpec();

    private MarketDataWorkspaceSpec() {}

    @Override public String kind()  { return "market-data"; }
    @Override public String title() { return "Market Data Operations"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup console  = WidgetGroup.of("Console");
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(FeedHealthWidget.class, WidgetLabel.of("Feed & Quality"))
                    .withIcon(new WidgetIcon.Emoji("📡"))
                    .withGroup(console),
            WidgetEntry.of(OverrideInventoryWidget.class, WidgetLabel.of("Override Inventory"))
                    .withIcon(new WidgetIcon.Emoji("🗃"))
                    .withGroup(console),
            WidgetEntry.of(MarketDataHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("📡"))
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

    /**
     * Feed health is the watch surface and holds the eye; the override inventory is
     * the record of what was done about it, which is what a strip underneath is for.
     */
    @Override
    public Arrangement arrangement() {
        return PaneArrangements.MAIN_AND_OUTPUT.allocate()
                .place(MainAndOutput.MAIN, FeedHealthWidget.class)
                .place(MainAndOutput.OUTPUT, OverrideInventoryWidget.class)
                .build();
    }
}
