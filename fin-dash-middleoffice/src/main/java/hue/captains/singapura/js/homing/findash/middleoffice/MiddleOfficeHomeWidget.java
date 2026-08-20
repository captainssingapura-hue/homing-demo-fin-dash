package hue.captains.singapura.js.homing.findash.middleoffice;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.kit.PersonaCardModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Middle Office workspace's home card — the persona's mission, ring writes,
 * cadence, and planned screens (UI study), rendered by the shared
 * {@link PersonaCardModule}. Stays as the workspace overview while the real
 * screens land beside it.
 */
public final class MiddleOfficeHomeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, MiddleOfficeHomeWidget> {

    public static final MiddleOfficeHomeWidget INSTANCE = new MiddleOfficeHomeWidget();

    private MiddleOfficeHomeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, MiddleOfficeHomeWidget> {}

    @Override protected _Construct<_None, MiddleOfficeHomeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Middle Office"; }
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
                "        title: 'Middle office / trade support',",
                "        mission: 'Every trade correct through its lifecycle.',",
                "        ring: 'Trade amendments — journaled',",
                "        cadence: 'intraday',",
                "        screens: ['Trade blotter', 'Lifecycle workstation (expiries, barriers, fixings)', 'Breaks dashboard']",
                "    }));",
                "    return { root: root, setActive: function (a) {}, partyDeregister: function () {} };");
    }
}
