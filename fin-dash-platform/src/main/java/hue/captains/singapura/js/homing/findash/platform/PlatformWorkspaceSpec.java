package hue.captains.singapura.js.homing.findash.platform;

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
 * The platform operations (SRE) workspace (kind {@code "platform"}) — the
 * machine is healthy, and when it is not, the blast radius is chosen
 * deliberately (study §12): the system console (W5) with SLO budget meters
 * and heavy-framed Ring-3 controls, and the epoch-flow pane with root-cause
 * click-through.
 */
public final class PlatformWorkspaceSpec implements WorkspaceSpec {

    public static final PlatformWorkspaceSpec INSTANCE = new PlatformWorkspaceSpec();

    private PlatformWorkspaceSpec() {}

    @Override public String kind()  { return "platform"; }
    @Override public String title() { return "Platform Operations"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup console  = WidgetGroup.of("Console");
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(PlatformConsoleWidget.class, WidgetLabel.of("Platform Console"))
                    .withIcon(new WidgetIcon.Emoji("🖥"))
                    .withGroup(console),
            WidgetEntry.of(EpochFlowWidget.class, WidgetLabel.of("Epoch Flow"))
                    .withIcon(new WidgetIcon.Emoji("🌀"))
                    .withGroup(console),
            WidgetEntry.of(PlatformHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("🖥"))
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
     * The console is the operator's surface; the epoch flow is the pipeline running
     * underneath it, which is where a wide short strip belongs.
     */
    @Override
    public Arrangement arrangement() {
        return PaneArrangements.MAIN_AND_OUTPUT.allocate()
                .place(MainAndOutput.MAIN, PlatformConsoleWidget.class)
                .place(MainAndOutput.OUTPUT, EpochFlowWidget.class)
                .build();
    }
}
