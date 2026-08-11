package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The Portfolio view — the position-level anchor shared across workspaces
 * (trader, risk, product control): the middle rung of the universal drill
 * chain, aggregate → <b>position</b> → trade → explain. It both <b>drives</b>
 * (clicking a position broadcasts its pair/tenor on the desk party — pricer
 * and surface follow) and <b>follows</b> (an {@code InstrumentChanged} from
 * the risk blotter's bucket rows filters this view to that bucket). Every
 * row carries freshness (P2) and its trade-journal door.
 */
public final class PortfolioWidget
        extends WorkspaceWidget<WorkspaceWidget._None, PortfolioWidget> {

    public static final PortfolioWidget INSTANCE = new PortfolioWidget();

    private PortfolioWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, PortfolioWidget> {}

    @Override protected _Construct<_None, PortfolioWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Portfolio"; }
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
            "    var state = { all: [], filter: null, portfolio: null, grid: null };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    var head = fdk.el('div', 'display:flex;align-items:center;gap:10px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el('span', 'font-weight:700;font-size:14px;letter-spacing:0.3px;',",
            "        'PORTFOLIO'));",
            "    var stampSlot = fdk.el('span', '');",
            "    head.appendChild(stampSlot);",
            "    var filterSlot = fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;');",
            "    head.appendChild(filterSlot);",
            "    root.appendChild(head);",
            "",
            "    var gridSlot = fdk.el('div', 'margin-top:8px;');",
            "    root.appendChild(gridSlot);",
            "    root.appendChild(fdk.el('div', 'color:#898781;font-size:11px;margin-top:4px;',",
            "        'drill: position \\u2192 trade (id column) \\u2192 pricing explain (P1) \\u00b7 '",
            "        + 'click a row to broadcast \\u00b7 blotter bucket clicks filter this view'));",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "",
            "    function renderFilter() {",
            "        clear(filterSlot);",
            "        if (!state.filter && !state.portfolio) return;",
            "        if (state.portfolio) {",
            "            filterSlot.appendChild(fdk.chip('neutral', '\\ud83d\\udcc1 ' + state.portfolio.label));",
            "        }",
            "        if (state.filter) {",
            "            var f = state.filter;",
            "            filterSlot.appendChild(fdk.chip('neutral',",
            "                f.pair + (f.tenor ? ' ' + f.tenor : '')));",
            "        }",
            "        var x = document.createElement('button');",
            "        x.textContent = 'clear';",
            "        x.style.cssText = 'padding:1px 8px;font-size:10.5px;border:1px solid #c3c2b7;'",
            "            + 'border-radius:4px;background:#fcfcfb;color:#52514e;cursor:pointer;';",
            "        x.onclick = function () { state.filter = null; state.portfolio = null; render(); };",
            "        filterSlot.appendChild(x);",
            "    }",
            "",
            "    function rows() {",
            "        var out = [];",
            "        for (var i = 0; i < state.all.length; i++) {",
            "            var p = state.all[i];",
            "            if (state.portfolio && state.portfolio.leafIds.indexOf(p.portfolioId) < 0) continue;",
            "            if (state.filter) {",
            "                if (p.pair !== state.filter.pair) continue;",
            "                if (state.filter.tenor && p.tenor !== state.filter.tenor) continue;",
            "            }",
            "            out.push(p);",
            "        }",
            "        return out;",
            "    }",
            "",
            "    function render() {",
            "        renderFilter();",
            "        clear(gridSlot);",
            "        state.grid = fdGrid({",
            "            compact: true,",
            "            columns: [",
            "                { key: 'id', label: 'Pos' },",
            "                { key: 'instrument', label: 'Instrument', render: function (v, row) {",
            "                    var cell = fdk.el('span', '');",
            "                    cell.appendChild(fdk.el('span', 'font-weight:600;', row.pair + ' ' + row.tenor + '  '));",
            "                    cell.appendChild(fdk.el('span', 'color:#52514e;', v));",
            "                    return cell;",
            "                } },",
            "                { key: 'notional', label: 'Notional', align: 'right' },",
            "                { key: 'pv', label: 'PV', align: 'right' },",
            "                { key: 'delta', label: '\\u0394', align: 'right' },",
            "                { key: 'vega', label: 'Vega', align: 'right' },",
            "                { key: 'barrierDist', label: 'Barrier', render: function (v) {",
            "                    return v ? fdk.chip(v.indexOf('12 pips') >= 0 ? 'serious' : 'warn', v)",
            "                             : fdk.el('span', 'color:#898781;', '\\u2014');",
            "                } },",
            "                { key: 'freshSecs', label: 'freshness', render: function (v, row) {",
            "                    var cell = fdk.el('span', 'display:inline-flex;gap:6px;align-items:center;');",
            "                    cell.appendChild(fdk.meter(row.budgetFrac, { width: 44 }));",
            "                    cell.appendChild(fdk.el('span', 'font-size:11px;color:'",
            "                        + (v > 10 ? '#9a6b1f' : '#898781') + ';', v + 's'));",
            "                    return cell;",
            "                } },",
            "                { key: 'tradeId', label: 'Trade' }",
            "            ],",
            "            rows: rows(),",
            "            onRowClick: function (row) {",
            "                if (party && actorId) {",
            "                    party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                        instrument: { pair: row.pair, tenor: row.tenor, ref: row.tradeId } });",
            "                }",
            "            }",
            "        });",
            "        gridSlot.appendChild(state.grid.root);",
            "    }",
            "",
            "    if (party) {",
            "        actorId = 'book/portfolio-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (!ins.pair) return;",
            "                    // A position row's own broadcast carries a ref — don't self-filter on it.",
            "                    if (ins.ref) return;",
            "                    state.filter = { pair: ins.pair, tenor: ins.tenor || null };",
            "                    render();",
            "                },",
            "                PortfolioChanged: function (msg) {",
            "                    var pf = msg.portfolio || {};",
            "                    if (!pf.leafIds) return;",
            "                    state.portfolio = pf;",
            "                    render();",
            "                }",
            "            }",
            "        });",
            "        party.tellFrom(actorId, { kind: 'CurrentPortfolioRequested' });",
            "    }",
            "",
            "    fetch('/fx/positions')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            state.all = d.positions;",
            "            stampSlot.appendChild(fdk.stamp({ slice: d.slice, model: d.model }));",
            "            render();",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el('div', 'color:#a8502a;',",
            "                'positions load failed: ' + (e && e.message ? e.message : e)));",
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
