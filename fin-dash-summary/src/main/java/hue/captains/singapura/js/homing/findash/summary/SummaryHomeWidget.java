package hue.captains.singapura.js.homing.findash.summary;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.kit.PersonaCardModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Management Summary workspace's home card — the persona's mission, ring writes,
 * cadence, and planned screens (UI study), rendered by the shared
 * {@link PersonaCardModule}. Stays as the workspace overview while the real
 * screens land beside it.
 */
public final class SummaryHomeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, SummaryHomeWidget> {

    public static final SummaryHomeWidget INSTANCE = new SummaryHomeWidget();

    private SummaryHomeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, SummaryHomeWidget> {}

    @Override protected _Construct<_None, SummaryHomeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Management Summary"; }
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
                "        title: 'Desk head / management',",
                "        mission: 'Situational awareness, escalation.',",
                "        ring: 'Escalation approvals',",
                "        cadence: 'continuous glance',",
                "        screens: ['Summary dashboard — every tile a door into the owning persona']",
                "    }));",
                "    return { root: root, setActive: function (a) {}, partyDeregister: function () {} };");
    }
}
