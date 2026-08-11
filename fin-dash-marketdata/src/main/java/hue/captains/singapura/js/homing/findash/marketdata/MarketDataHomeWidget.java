package hue.captains.singapura.js.homing.findash.marketdata;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.PersonaCardModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Market Data Operations workspace's home card — the persona's mission, ring writes,
 * cadence, and planned screens (UI study), rendered by the shared
 * {@link PersonaCardModule}. Stays as the workspace overview while the real
 * screens land beside it.
 */
public final class MarketDataHomeWidget
        extends WorkspaceWidget<WorkspaceWidget._None, MarketDataHomeWidget> {

    public static final MarketDataHomeWidget INSTANCE = new MarketDataHomeWidget();

    private MarketDataHomeWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, MarketDataHomeWidget> {}

    @Override protected _Construct<_None, MarketDataHomeWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Market Data Operations"; }
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
                "        title: 'Market data operations',",
                "        mission: 'Guarantee clean inputs.',",
                "        ring: 'Ring 3 — source switches',",
                "        cadence: 'ticking',",
                "        screens: ['Feed & quality console (W5 pattern)', 'Override inventory', 'Quarantine review queue']",
                "    }));",
                "    return { root: root, setActive: function (a) {}, partyDeregister: function () {} };");
    }
}
