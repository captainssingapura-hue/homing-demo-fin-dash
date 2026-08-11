package hue.captains.singapura.js.homing.findash.conformance;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.js.CoreJsCrate;
import hue.captains.singapura.js.homing.libs.LibsCrate;
import hue.captains.singapura.js.homing.server.ServerCrate;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.studio.workspace.StudioWorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.WorkspaceCrate;
import hue.captains.singapura.js.homing.workspace.codecs.WorkspaceCodecsCrate;
import hue.captains.singapura.js.homing.workspace.persistence.WorkspacePersistenceCrate;
import hue.captains.singapura.js.homing.workspace.shell.WorkspaceShellCrate;

import hue.captains.singapura.js.homing.findash.model.VarModel;
import hue.captains.singapura.js.homing.findash.widget.RiskDashboardPlaceholderWidget;

import java.util.List;

/**
 * The fin-dash's own {@link Crate}: every served JS module {@code
 * homing-demo-fin-dash} ships (one crate per Maven module — {@code OrphanCheck}
 * scans this module's whole build output and fails on any served module this
 * list omits, so add every new widget/model here). This is the demo's
 * "register for serving + register for conformance" leg, and — because the
 * server enforces the runtime crate-gate (see {@code RiskDashboardFixtures}) —
 * a module absent here would be refused at {@code /module}, not silently served.
 *
 * <p>{@link VarModel} is declared as the downstream {@link
 * RiskModuleType#RISK_MODEL} extension type (the declaration wins over structural
 * inference), so the conformance studio shows it held to the {@code risk-model}
 * rule set — the RFC 0044 policy extension, made visible.</p>
 */
public final class FinDashCrate implements Crate {

    public static final FinDashCrate INSTANCE = new FinDashCrate();

    private FinDashCrate() {}

    @Override public String name() { return "homing-demo-fin-dash"; }

    @Override
    public List<Crate> requires() {
        return List.of(
                CoreJsCrate.INSTANCE,
                ServerCrate.INSTANCE,
                StudioBaseCrate.INSTANCE,
                WorkspaceCrate.INSTANCE,
                WorkspaceCodecsCrate.INSTANCE,
                WorkspacePersistenceCrate.INSTANCE,
                WorkspaceShellCrate.INSTANCE,
                StudioWorkspaceCrate.INSTANCE,
                LibsCrate.INSTANCE);
    }

    @Override
    public List<CrateEntry> entries() {
        return List.of(
                CrateEntry.of(RiskDashboardPlaceholderWidget.INSTANCE),
                // The downstream extension type, made visible in the studio:
                CrateEntry.of(VarModel.INSTANCE, RiskModuleType.RISK_MODEL));
    }
}
