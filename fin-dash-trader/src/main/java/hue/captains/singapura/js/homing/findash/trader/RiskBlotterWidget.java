package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * W4 — the position &amp; risk blotter: the book bucketed the way the desk
 * thinks (pair ▸ tenor), smile-bucket vega columns, and the two-speed
 * machinery made honest (P2): every row shows its revaluation freshness —
 * error-budget consumption as a meter, last full reval age. Clicking a row
 * publishes {@code InstrumentSelected} on the desk party — the pricer and
 * surface manager follow the selection (the UI study's cross-widget flow).
 * Barrier proximity and expiry/pin clusters live in their own widgets
 * ({@link BarrierWatchWidget}, {@link ExpiryClustersWidget}).
 */
public final class RiskBlotterWidget
        extends WorkspaceWidget<WorkspaceWidget._None, RiskBlotterWidget> {

    public static final RiskBlotterWidget INSTANCE = new RiskBlotterWidget();

    private RiskBlotterWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RiskBlotterWidget> {}

    @Override protected _Construct<_None, RiskBlotterWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Risk Blotter"; }
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
            "        + 'font-family:system-ui,sans-serif;font-size:13px;color:var(--color-text-primary);';",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'trader/blotter-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:baseline;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;', 'RISK \\u2014 FXO BOOK'));",
            "    head.appendChild(fdk.el('span', 'color:var(--color-text-primary);font-size:11.5px;', 'as-of: live'));",
            "    var stampSlot = fdk.el('span', '');",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "",
            "    var totals = fdk.el('div', 'display:flex;gap:16px;align-items:center;flex-wrap:wrap;'",
            "        + 'margin:8px 0;padding:8px 10px;background:var(--color-surface-raised);border-radius:8px;font-size:12.5px;');",
            "    root.appendChild(totals);",
            "",
            "    var gridSlot = fdk.el('div', '');",
            "    root.appendChild(gridSlot);",
            "    root.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;margin:4px 0 0;',",
            "        'drill: bucket \\u2192 position \\u2192 trade \\u2192 pricing explain (P1) \\u00b7 click a row to broadcast the selection'",
            "        + ' \\u00b7 barriers + expiries in their own widgets'));",
            "",
            "    function totalsItem(label, valueNode) {",
            "        var box = fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;');",
            "        box.appendChild(fdk.el('span', 'color:var(--color-text-muted);', label));",
            "        box.appendChild(valueNode);",
            "        return box;",
            "    }",
            "",
            "    function render(d) {",
            "        stampSlot.appendChild(fdk.stamp({ slice: d.slice, model: d.model }));",
            "        var t = d.totals;",
            "        totals.appendChild(totalsItem('\\u0394', fdk.el('span', 'font-weight:600;', '\\u20ac' + fdk.fmt.compact(t.delta))));",
            "        totals.appendChild(totalsItem('Vega', fdk.el('span', 'font-weight:600;', '\\u20ac' + fdk.fmt.compact(t.vega))));",
            "        totals.appendChild(totalsItem('\\u0398', fdk.el('span', 'font-weight:600;', '\\u2212\\u20ac' + fdk.fmt.compact(Math.abs(t.theta)))));",
            "        totals.appendChild(totalsItem('vega limit ' + Math.round(t.vegaLimitFrac * 100) + '%', fdk.meter(t.vegaLimitFrac)));",
            "        totals.appendChild(totalsItem('reval wave ' + Math.round(t.revalWaveFrac * 100) + '%', fdk.meter(t.revalWaveFrac, { warnAt: 2 })));",
            "",
            "        var rows = [];",
            "        for (var i = 0; i < d.rows.length; i++) {",
            "            var r = d.rows[i];",
            "            rows.push({",
            "                book: r.group ? r.pair : r.tenor, _group: r.group, _indent: r.group ? 0 : 1,",
            "                pair: r.pair, tenor: r.tenor,",
            "                delta: fdk.fmt.compact(r.delta), vAtm: fdk.fmt.compact(r.vAtm),",
            "                vRr: fdk.fmt.compact(r.vRr), vBf: fdk.fmt.compact(r.vBf),",
            "                theta: fdk.fmt.compact(r.theta),",
            "                freshSecs: r.freshSecs, budgetFrac: r.budgetFrac, note: r.note",
            "            });",
            "        }",
            "        var grid = fdGrid({",
            "            compact: true,",
            "            columns: [",
            "                { key: 'book', label: 'Book (pair \\u25b8 tenor)' },",
            "                { key: 'delta', label: '\\u0394', align: 'right' },",
            "                { key: 'vAtm', label: 'vATM', align: 'right' },",
            "                { key: 'vRr', label: 'vRR', align: 'right' },",
            "                { key: 'vBf', label: 'vBF', align: 'right' },",
            "                { key: 'theta', label: '\\u0398', align: 'right' },",
            "                { key: 'freshSecs', label: 'freshness', render: function (v, row) {",
            "                    var cell = fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;');",
            "                    cell.appendChild(fdk.meter(row.budgetFrac, { width: 44 }));",
            "                    cell.appendChild(fdk.el('span', 'font-size:11px;color:'",
            "                        + (v > 10 ? '#9a6b1f' : 'var(--color-text-muted)') + ';', v + 's'));",
            "                    if (row.note) cell.appendChild(fdk.chip('warn', row.note));",
            "                    return cell;",
            "                } }",
            "            ],",
            "            rows: rows,",
            "            onRowClick: function (row) {",
            "                if (party && actorId) {",
            "                    party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                        instrument: { pair: row.pair, tenor: row.tenor } });",
            "                }",
            "            }",
            "        });",
            "        gridSlot.appendChild(grid.root);",
            "    }",
            "",
            "    fetch('/fx/book')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(render)",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el('div', 'color:#a8502a;', 'book load failed: ' + (e && e.message ? e.message : e)));",
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
