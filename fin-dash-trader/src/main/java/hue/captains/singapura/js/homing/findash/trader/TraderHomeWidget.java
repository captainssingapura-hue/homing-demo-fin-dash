package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.kit.PersonaCardModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The trader workspace's home card — the persona's mission, ring writes,
 * cadence, and planned screens (UI study §3), rendered by the shared
 * {@link PersonaCardModule}. Stays as the workspace overview while the real
 * screens (Pricer W1, Surface Manager W2, Risk Blotter W4) land beside it.
 */
public final class TraderHomeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, TraderHomeWidget> {

    public static final TraderHomeWidget INSTANCE = new TraderHomeWidget();

    private TraderHomeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, TraderHomeWidget> {}

    @Override protected _Construct<_None, TraderHomeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Trader Desk"; }
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
                "        title: 'Trader / market-maker',",
                "        mission: 'Run the book: price, mark, hedge.',",
                "        ring: 'Ring 3 — marks, overrides',",
                "        cadence: 'ticking (sub-second; keyboard-first is non-negotiable)',",
                "        screens: ['Pricer / deal ticket (W1)',",
                "                  'Surface manager (W2)',",
                "                  'Position & risk blotter (W4)']",
                "    }));",
                "    return { root: root, setActive: function (a) {}, partyDeregister: function () {} };");
    }
}
