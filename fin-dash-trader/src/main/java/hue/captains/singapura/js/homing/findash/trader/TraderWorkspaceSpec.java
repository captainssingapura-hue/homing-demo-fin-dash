package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.findash.book.PortfolioTreeWidget;
import hue.captains.singapura.js.homing.findash.book.PortfolioWidget;
import hue.captains.singapura.js.homing.findash.book.TradeBlotterWidget;
import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
import hue.captains.singapura.js.homing.findash.core.bus.TradeSecretaryModule;
import hue.captains.singapura.js.homing.findash.viz.VolSurfaceWidget;
import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The trader / market-maker workspace (kind {@code "trader"}) — the desk's
 * screens on the reused {@code GenericWorkspace} shell: Pricer W1, Surface
 * Manager W2, Position &amp; Risk Blotter W4 (UI study §3).
 *
 * <p>Two parties: the generic navigation bus, and the fin-dash <b>desk</b> bus
 * ({@code DeskSecretary}, exposed as {@code deskParty}) carrying the
 * cross-widget selection flow — blotter row click → {@code InstrumentSelected}
 * → pricer prefills, surface manager switches pair.</p>
 */
public final class TraderWorkspaceSpec implements WorkspaceSpec {

    public static final TraderWorkspaceSpec INSTANCE = new TraderWorkspaceSpec();

    private TraderWorkspaceSpec() {}

    @Override public String kind()  { return "trader"; }
    @Override public String title() { return "Trader Desk"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup trading  = WidgetGroup.of("Trading");
        WidgetGroup risk     = WidgetGroup.of("Risk");
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(PortfolioTreeWidget.class, WidgetLabel.of("Portfolios"))
                    .withIcon(new WidgetIcon.Emoji("🌳"))
                    .withGroup(WidgetGroup.of("Book")),
            WidgetEntry.of(PricerWidget.class, WidgetLabel.of("Pricer"))
                    .withIcon(new WidgetIcon.Emoji("🎯"))
                    .withGroup(trading),
            WidgetEntry.of(SurfaceManagerWidget.class, WidgetLabel.of("Surface Manager"))
                    .withIcon(new WidgetIcon.Emoji("🌊"))
                    .withGroup(trading),
            WidgetEntry.of(VolSurfaceWidget.class, WidgetLabel.of("Vol Surface 3D"))
                    .withIcon(new WidgetIcon.Emoji("🧊"))
                    .withGroup(trading),
            WidgetEntry.of(RiskBlotterWidget.class, WidgetLabel.of("Risk Blotter"))
                    .withIcon(new WidgetIcon.Emoji("📋"))
                    .withGroup(risk),
            WidgetEntry.of(RiskLadderWidget.class, WidgetLabel.of("Risk Ladder"))
                    .withIcon(new WidgetIcon.Emoji("🪜"))
                    .withGroup(risk),
            WidgetEntry.of(PortfolioWidget.class, WidgetLabel.of("Portfolio"))
                    .withIcon(new WidgetIcon.Emoji("📁"))
                    .withGroup(risk),
            WidgetEntry.of(TradeBlotterWidget.class, WidgetLabel.of("Trade Blotter"))
                    .withIcon(new WidgetIcon.Emoji("🧾"))
                    .withGroup(trading),
            WidgetEntry.of(BarrierWatchWidget.class, WidgetLabel.of("Barrier Watch"))
                    .withIcon(new WidgetIcon.Emoji("🚨"))
                    .withGroup(risk),
            WidgetEntry.of(ExpiryClustersWidget.class, WidgetLabel.of("Expiry / Pins"))
                    .withIcon(new WidgetIcon.Emoji("📌"))
                    .withGroup(risk),
            WidgetEntry.of(TraderHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("📈"))
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
                     .build(),
            PartyDecl.of("trade", TradeSecretaryModule.INSTANCE, "TradeSecretary")
                     .exposedAs("tradeParty")
                     .build()
        );
    }
}
