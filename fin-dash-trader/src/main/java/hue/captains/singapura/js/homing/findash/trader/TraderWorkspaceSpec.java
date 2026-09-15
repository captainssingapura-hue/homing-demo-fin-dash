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
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneDirection;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.ShapePane;
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
            // RFC 0050 · Episode 2 — the same ladder on the grid GROUP, docked
            // beside the one above until the comparison is settled.
            WidgetEntry.of(RiskLadderGroupWidget.class, WidgetLabel.of("Risk Ladder (group)"))
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

    /**
     * The desk's own shape, written out because none of the eight shipped ones
     * is a trading desk: a full-height book rail, risk and positions side by
     * side above, the tape across the bottom.
     *
     * <p>Read as the sequence of splits a trader would have performed. Each
     * ratio is the share the pane being split <b>keeps</b>, and it is local to
     * that split — so the ladder's 0.60 is 60% of the 80% that is left of the
     * 70% above the tape, not 60% of the screen.</p>
     */
    private static final PaneArrangement DESK =
            PaneArrangement.named("fx-desk")
                    .root("ladder")
                    .splitWithRatio("ladder", PaneDirection.LEFT, "books",     0.80)
                    .splitWithRatio("ladder", PaneDirection.DOWN, "tape",      0.70)
                    .splitWithRatio("ladder", PaneDirection.RIGHT, "positions", 0.60)
                    .build();

    private static final ShapePane BOOKS     = DESK.pane("books");
    private static final ShapePane LADDER    = DESK.pane("ladder");
    private static final ShapePane POSITIONS = DESK.pane("positions");
    private static final ShapePane TAPE      = DESK.pane("tape");

    /**
     * What the desk opens with on a first visit: the book rail on the left, the
     * risk ladder and the positions it selects side by side, the trades behind
     * them along the bottom.
     *
     * <p>The allocation is not a guess about taste — it is <b>the desk bus
     * drawn as geometry</b>. {@code PortfolioTreeWidget} is the only widget
     * here that broadcasts {@code PortfolioSelected}, and every other pane
     * answers the {@code PortfolioChanged} the secretary turns it into. One
     * selection in the rail moves all three, so the trader learns the desk's
     * central gesture by making it rather than by assembling three widgets from
     * the picker first.</p>
     *
     * <p>The ladder takes the largest pane because it is the desk's standing
     * question — where the risk sits down the curve. It narrows to the pairs
     * the selected book trades, which is a narrowing of the <i>view</i>: the
     * subtotals stay the desk's own, over the whole book (P5).</p>
     *
     * <p>The tape earns the bottom strip twice over: it reads well wide and
     * short, and a row click there emits {@code InstrumentSelected}, the second
     * gesture — the one the ladder, the pricer and the surface manager all
     * answer, so the first visit has somewhere to go next.</p>
     */
    @Override
    public Arrangement arrangement() {
        return DESK.allocate()
                .place(BOOKS,     PortfolioTreeWidget.class)
                .place(LADDER,    RiskLadderWidget.class)
                .place(POSITIONS, PortfolioWidget.class)
                .place(TAPE,      TradeBlotterWidget.class)
                .build();
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
