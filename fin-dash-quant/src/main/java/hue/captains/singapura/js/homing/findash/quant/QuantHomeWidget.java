package hue.captains.singapura.js.homing.findash.quant;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.PersonaCardModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Quant Lab workspace's home card — the persona's mission, ring writes,
 * cadence, and planned screens (UI study), rendered by the shared
 * {@link PersonaCardModule}. Stays as the workspace overview while the real
 * screens land beside it.
 */
public final class QuantHomeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, QuantHomeWidget> {

    public static final QuantHomeWidget INSTANCE = new QuantHomeWidget();

    private QuantHomeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, QuantHomeWidget> {}

    @Override protected _Construct<_None, QuantHomeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Quant Lab"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new PersonaCardModule.personaCard()), PersonaCardModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
                "    var root = branch.createElement('root', 'div');",
                "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;'",
                "        + 'padding:20px;font-family:system-ui,sans-serif;font-size:13px;';",
                "    root.appendChild(personaCard({",
                "        title: 'Quant / methodology owner',",
                "        mission: 'Own calibration quality and evolution.',",
                "        ring: 'Ring 2 — proposals',",
                "        cadence: 'daily / per-change',",
                "        screens: ['Calibration lab', 'Replay lab & champion/challenger']",
                "    }));",
                "    return { root: root, setActive: function (a) {}, partyDeregister: function () {} };");
    }
}
