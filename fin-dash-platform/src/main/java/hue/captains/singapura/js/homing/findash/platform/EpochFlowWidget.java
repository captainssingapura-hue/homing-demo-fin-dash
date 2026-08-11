package hue.captains.singapura.js.homing.findash.platform;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The epoch-flow pane (W5): per-pair surface epoch age vs expected cadence,
 * with click-through to the root cause and its affected consumers — the
 * study's "click-through: waiting on 1M BF quarantine → affected: USDJPY
 * surface, EURJPY cross". Clicking a row also publishes the pair on the desk
 * party.
 */
public final class EpochFlowWidget
        extends WorkspaceWidget<WorkspaceWidget._None, EpochFlowWidget> {

    public static final EpochFlowWidget INSTANCE = new EpochFlowWidget();

    private EpochFlowWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, EpochFlowWidget> {}

    @Override protected _Construct<_None, EpochFlowWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Epoch Flow"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE));
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
            "        actorId = 'platform/epochs-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    root.appendChild(fdk.el('div', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'EPOCH FLOW'));",
            "    root.appendChild(fdk.el('div', 'color:#52514e;font-size:11.5px;margin:2px 0 8px;',",
            "        'surface epoch age vs expected cadence \\u00b7 click a row for root cause'));",
            "",
            "    var gridSlot = fdk.el('div', '');",
            "    root.appendChild(gridSlot);",
            "    var curve = fdk.el('div', 'color:#52514e;font-size:12px;margin-top:6px;');",
            "    root.appendChild(curve);",
            "    var cause = fdk.el('div', 'margin-top:6px;font-size:12px;color:#898781;min-height:18px;',",
            "        'root cause appears here');",
            "    root.appendChild(cause);",
            "",
            "    fetch('/fx/platform')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            var grid = fdGrid({",
            "                compact: true,",
            "                columns: [",
            "                    { key: 'pair', label: 'Pair', render: function (v) {",
            "                        return fdk.el('span', 'font-weight:600;', v); } },",
            "                    { key: 'epoch', label: 'Surface', align: 'right' },",
            "                    { key: 'age', label: 'Age', align: 'right' },",
            "                    { key: 'expected', label: 'Expected', align: 'right' },",
            "                    { key: 'state', label: 'State', render: function (v, row) {",
            "                        return v === 'ok' ? fdk.chip('good', row.note || 'on cadence')",
            "                            : fdk.chip('warn', 'behind cadence'); } }",
            "                ],",
            "                rows: d.epochs,",
            "                onRowClick: function (row) {",
            "                    cause.style.color = row.note ? '#52514e' : '#898781';",
            "                    cause.textContent = row.note",
            "                        ? row.pair + ': ' + row.note",
            "                        : row.pair + ': on cadence \\u2014 nothing waiting';",
            "                    if (party && actorId) party.tellFrom(actorId,",
            "                        { kind: 'InstrumentSelected', instrument: { pair: row.pair } });",
            "                }",
            "            });",
            "            gridSlot.appendChild(grid.root);",
            "            curve.textContent = d.curveSet;",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'epoch flow load failed: ' + (e && e.message ? e.message : e)));",
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
