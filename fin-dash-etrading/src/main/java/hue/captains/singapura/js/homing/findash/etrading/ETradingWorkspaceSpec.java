package hue.captains.singapura.js.homing.findash.etrading;

import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
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
 * The e-Trading supervisor workspace (kind {@code "etrading"}) — keep
 * auto-quoting safe and competitive (UI study §4): the quoting console (W3)
 * with its Ring-3 controls and one-gesture master kill, and the RFQ tape with
 * per-quote slice stamps.
 *
 * <p>Two parties: the generic navigation bus and the fin-dash desk bus
 * ({@code deskParty}) — console and tape rows publish pair selections that
 * the trader widgets follow when mounted alongside.</p>
 */
public final class ETradingWorkspaceSpec implements WorkspaceSpec {

    public static final ETradingWorkspaceSpec INSTANCE = new ETradingWorkspaceSpec();

    private ETradingWorkspaceSpec() {}

    @Override public String kind()  { return "etrading"; }
    @Override public String title() { return "e-Trading Supervision"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup console  = WidgetGroup.of("Console");
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(QuotingConsoleWidget.class, WidgetLabel.of("Quoting Console"))
                    .withIcon(new WidgetIcon.Emoji("⚡"))
                    .withGroup(console),
            WidgetEntry.of(RfqTapeWidget.class, WidgetLabel.of("RFQ Tape"))
                    .withIcon(new WidgetIcon.Emoji("🧾"))
                    .withGroup(console),
            WidgetEntry.of(ETradingHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("⚡"))
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
     * The console is where a quote is made; the tape is the inbound flow it answers.
     * A feed reads well as a narrow column beside the work, not underneath it.
     */
    @Override
    public Arrangement arrangement() {
        return PaneArrangements.MAIN_AND_SIDE.allocate()
                .place(MainAndSide.MAIN, QuotingConsoleWidget.class)
                .place(MainAndSide.SIDE, RfqTapeWidget.class)
                .build();
    }
}
