package hue.captains.singapura.js.homing.findash.quant;

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
 * The Quant / methodology owner workspace (kind {@code "quant"}) on the reused
 * {@code GenericWorkspace} shell. Scaffold: the home card; the persona's real
 * screens (UI study) land beside it, grouped as they arrive.
 *
 * <p>The generic {@code NavigatorSecretary} bus is reused (exposed as
 * {@code navParty}) until this persona's cross-widget events need a bespoke
 * secretary.</p>
 */
public final class QuantWorkspaceSpec implements WorkspaceSpec {

    public static final QuantWorkspaceSpec INSTANCE = new QuantWorkspaceSpec();

    private QuantWorkspaceSpec() {}

    @Override public String kind()  { return "quant"; }
    @Override public String title() { return "Quant Lab"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        WidgetGroup overview = WidgetGroup.of("Overview");
        return List.of(
            WidgetEntry.of(CalibrationLabWidget.class, WidgetLabel.of("Calibration Lab"))
                    .withIcon(new WidgetIcon.Emoji("🧪"))
                    .withGroup(WidgetGroup.of("Console")),
            WidgetEntry.of(QuantHomeWidget.class, WidgetLabel.of("Overview"))
                    .withIcon(new WidgetIcon.Emoji("🧪"))
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
     * The calibration lab is the desk; the persona card beside it says whose desk and
     * what it is for, which is worth a first visit while the rest of the screens land.
     */
    @Override
    public Arrangement arrangement() {
        return PaneArrangements.MAIN_AND_SIDE.allocate()
                .place(MainAndSide.MAIN, CalibrationLabWidget.class)
                .place(MainAndSide.SIDE, QuantHomeWidget.class)
                .build();
    }
}
