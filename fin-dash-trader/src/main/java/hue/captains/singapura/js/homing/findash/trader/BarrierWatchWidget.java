package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The barrier watch — W4's first-class barrier display as its own widget:
 * positions within the configured band of their trigger sorted by proximity,
 * with distance-to-level, notional, and the tightened budget state (P2 chips).
 * Clicking a row publishes {@code InstrumentSelected} on the desk party — the
 * pricer and surface manager jump to that pair.
 */
public final class BarrierWatchWidget
        extends WorkspaceWidget<WorkspaceWidget._None, BarrierWatchWidget> {

    public static final BarrierWatchWidget INSTANCE = new BarrierWatchWidget();

    private BarrierWatchWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, BarrierWatchWidget> {}

    @Override protected _Construct<_None, BarrierWatchWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Barrier Watch"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    root.style.cssText = 'height:100%;overflow:auto;box-sizing:border-box;padding:14px;'",
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:#0b0b0b;';",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'trader/barriers-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'BARRIER WATCH'));",
            "    head.appendChild(fdk.el('span', 'color:#52514e;font-size:11.5px;', 'sorted by proximity'));",
            "    var stampSlot = fdk.el('span', '');",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var list = fdk.el('div', 'margin-top:8px;');",
            "    root.appendChild(list);",
            "    root.appendChild(fdk.el('div', 'color:#898781;font-size:11px;margin-top:8px;',",
            "        'positions inside their band tighten the reval error budget (two-speed risk) \\u00b7 '",
            "        + 'click a row to broadcast the selection'));",
            "",
            "    function render(d) {",
            "        stampSlot.appendChild(fdk.stamp({ slice: d.slice, model: d.model }));",
            "        var bs = d.barriers.slice().sort(function (a, b) { return a.pips - b.pips; });",
            "        for (var i = 0; i < bs.length; i++) {",
            "            (function (b) {",
            "                var line = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:7px 4px;'",
            "                    + 'border-bottom:1px solid #e1e0d9;font-size:12.5px;cursor:pointer;');",
            "                line.appendChild(fdk.chip(b.severity === 'good' ? 'good' : b.severity,",
            "                    b.pips + ' pips'));",
            "                var main = fdk.el('div', '');",
            "                main.appendChild(fdk.el('div', 'font-weight:600;',",
            "                    b.pair + ' ' + b.type + ' ' + b.level));",
            "                main.appendChild(fdk.el('div', 'color:#52514e;font-size:11.5px;',",
            "                    b.notional + ' notional \\u00b7 ' + b.note));",
            "                line.appendChild(main);",
            "                line.onclick = function () {",
            "                    if (party && actorId) {",
            "                        party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                            instrument: { pair: b.pair } });",
            "                    }",
            "                };",
            "                list.appendChild(line);",
            "            })(bs[i]);",
            "        }",
            "    }",
            "",
            "    fetch('/fx/barriers')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(render)",
            "        .catch(function (e) {",
            "            list.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'barriers load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {",
            "            if (actorId && party) { try { party.leave(actorId); } catch (e) {} }",
            "        }",
            "    };");
    }
}
