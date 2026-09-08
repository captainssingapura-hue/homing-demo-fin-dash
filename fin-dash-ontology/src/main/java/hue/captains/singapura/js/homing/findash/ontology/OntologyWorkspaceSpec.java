package hue.captains.singapura.js.homing.findash.ontology;

import hue.captains.singapura.js.homing.findash.core.bus.DeskSecretaryModule;
import hue.captains.singapura.js.homing.studio.workspace.NavigatorSecretaryModule;
import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetGroup;
import hue.captains.singapura.js.homing.workspace.WidgetIcon;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.shell.Arrangement;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements;
import hue.captains.singapura.js.homing.workspace.shell.PaneArrangements.Columns;
import hue.captains.singapura.js.homing.workspace.shell.PartyDecl;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceSpec;

import java.util.List;

/**
 * The Data Ontology workspace (kind {@code "data-ontology"}) — the demo
 * documenting its own data model.
 *
 * <p>Not a persona of the FX desk but a workspace for the people who build it:
 * browse the type catalogue as a tree, select any type, and see which fin-dash
 * widgets require it and in what role. It runs on the same shell, the same
 * tree substrate, and the same desk party as every trading workspace — which
 * is itself the argument that the connection mechanism is general.</p>
 */
public final class OntologyWorkspaceSpec implements WorkspaceSpec {

    public static final OntologyWorkspaceSpec INSTANCE = new OntologyWorkspaceSpec();

    private OntologyWorkspaceSpec() {}

    @Override public String kind()  { return "data-ontology"; }
    @Override public String title() { return "Data Ontology"; }

    @Override
    public List<WidgetEntry> widgetEntries() {
        final WidgetGroup ontology = WidgetGroup.of("Ontology");
        return List.of(
            WidgetEntry.of(DataTypeTreeWidget.class, WidgetLabel.of("Data Types"))
                    .withIcon(new WidgetIcon.Emoji("🧬"))
                    .withGroup(ontology),
            WidgetEntry.of(DataTypeUsageWidget.class, WidgetLabel.of("Type Usage"))
                    .withIcon(new WidgetIcon.Emoji("🔌"))
                    .withGroup(ontology)
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
     * A type browser is a selector and a reading pane, and the selection is live: the
     * tree broadcasts, the usage pane answers. Even halves rather than a narrow rail,
     * because a data type name is long and the usage list is not wide.
     */
    @Override
    public Arrangement arrangement() {
        return PaneArrangements.COLUMNS.allocate()
                .place(Columns.LEFT, DataTypeTreeWidget.class)
                .place(Columns.RIGHT, DataTypeUsageWidget.class)
                .build();
    }
}
