package hue.captains.singapura.js.homing.findash.marketdata;

import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The Market data operations workspace (kind {@code "market-data"}) on the reused
 * {@code GenericWorkspace} shell. Scaffold: the home card; the persona's real
 * screens (UI study) land beside it, grouped as they arrive.
 *
 * <p>The generic {@code NavigatorSecretary} bus is reused (exposed as
 * {@code navParty}) until this persona's cross-widget events need a bespoke
 * secretary.</p>
 */
public final class MarketDataWorkspaceSpec implements WorkspaceSpec {

    public static final MarketDataWorkspaceSpec INSTANCE = new MarketDataWorkspaceSpec();

    private MarketDataWorkspaceSpec() {}

    @Override public String kind()  { return "market-data"; }
    @Override public String title() { return "Market Data Operations"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
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
                     .build()
        );
    }
}
