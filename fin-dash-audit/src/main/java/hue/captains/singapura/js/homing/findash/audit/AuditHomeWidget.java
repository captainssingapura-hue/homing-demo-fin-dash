package hue.captains.singapura.js.homing.findash.audit;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.kit.PersonaCardModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Audit Explorer workspace's home card — the persona's mission, ring writes,
 * cadence, and planned screens (UI study), rendered by the shared
 * {@link PersonaCardModule}. Stays as the workspace overview while the real
 * screens land beside it.
 */
public final class AuditHomeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, AuditHomeWidget> {

    public static final AuditHomeWidget INSTANCE = new AuditHomeWidget();

    private AuditHomeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, AuditHomeWidget> {}

    @Override protected _Construct<_None, AuditHomeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Audit Explorer"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new PersonaCardModule.personaCard()),
                        PersonaCardModule.INSTANCE),
                new ModuleImports<>(List.of(new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    css.setClass(root, fd_widget_root);",
                "    root.appendChild(personaCard(branch, {",
                "        title: 'Audit, compliance & mgmt',",
                "        mission: 'Reconstruct and attest anything.',",
                "        ring: 'None — read-only',",
                "        cadence: 'ad hoc',",
                "        screens: ['Audit explorer (cross-journal, time-travel)', 'Entitlement reporting']",
                "    }));",
                "    return { root: root, setActive: function (a) {}, partyDeregister: function () {} };");
    }
}
