package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

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
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text(),
                        new FdStatusCss.fd_status_warn()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(new FdControlCss.fd_btn_ghost()),
                        FdControlCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var state = { all: [], filter: null, portfolio: null, grid: null };",
            "",
            "    var head = fdk.el(branch, 'head', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, 'PORTFOLIO'));",
            "    var stampSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(stampSlot);",
            "    var filterSlot = fdk.el(branch, 'cluster-1', 'span', fd_cluster);",
            "    head.appendChild(filterSlot);",
            "    root.appendChild(head);",
            "",
            "    var gridSlot = fdk.el(branch, 'sect-1', 'div', fd_section);",
            "    root.appendChild(gridSlot);",
            "    var footer = fdk.el(branch, 'footer', 'div', [fd_caption, fd_muted],",
            "        'drill: position \\u2192 trade (id column) \\u2192 pricing explain (P1) \\u00b7 '",
            "        + 'click a row to broadcast \\u00b7 blotter bucket clicks filter this view');",
            "    root.appendChild(footer);",
            "",
            "    // Both regions below are rebuilt on every filter change, and a branch",
            "    // element name is unique for the branch's life -- so each gets its own",
            "    // sub-branch that is dissolved and re-created per render. dissolve()",
            "    // detaches the elements too, which is what retires the old clear() loop.",
            "    var filterBranch = null, gridBranch = null;",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "",
            "    function renderFilter() {",
            "        if (filterBranch) filterBranch.dissolve();",
            "        filterBranch = branch.createBranch('filter');",
            "        filterBranch.activate(root);",
            "        if (!state.filter && !state.portfolio) return;",
            "        if (state.portfolio) {",
            "            filterSlot.appendChild(fdk.chip(filterBranch, 'pf', 'neutral',",
            "                '\\ud83d\\udcc1 ' + state.portfolio.label));",
            "        }",
            "        if (state.filter) {",
            "            var f = state.filter;",
            "            filterSlot.appendChild(fdk.chip(filterBranch, 'ins', 'neutral',",
            "                f.pair + (f.tenor ? ' ' + f.tenor : '')));",
            "        }",
            "        var x = fdk.el(filterBranch, 'clear', 'button', fd_btn_ghost, 'clear');",
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
            "        if (gridBranch) gridBranch.dissolve();",
            "        gridBranch = branch.createBranch('grid');",
            "        gridBranch.activate(root);",
            "        state.grid = fdGrid(gridBranch, {",
            "            compact: true,",
            "            columns: [",
            "                { key: 'id', label: 'Pos' },",
            "                // Cells take the grid's ROW branch (b) and the row index —",
            "                // one render callback runs once per row, so a fixed name",
            "                // would collide on the second row.",
            "                { key: 'instrument', label: 'Instrument', render: function (v, row, b, i) {",
            "                    var cell = fdk.el(b, 'ins-' + i, 'span', null);",
            "                    cell.appendChild(fdk.el(b, 'ins-pair-' + i, 'span', fd_strong,",
            "                        row.pair + ' ' + row.tenor + '  '));",
            "                    cell.appendChild(fdk.el(b, 'ins-kind-' + i, 'span', null, v));",
            "                    return cell;",
            "                } },",
            "                { key: 'notional', label: 'Notional', align: 'right' },",
            "                { key: 'pv', label: 'PV', align: 'right' },",
            "                { key: 'delta', label: '\\u0394', align: 'right' },",
            "                { key: 'vega', label: 'Vega', align: 'right' },",
            "                { key: 'barrierDist', label: 'Barrier', render: function (v, row, b, i) {",
            "                    return v ? fdk.chip(b, 'barrier-' + i,",
            "                                   v.indexOf('12 pips') >= 0 ? 'serious' : 'warn', v)",
            "                             : fdk.el(b, 'barrier-' + i, 'span', fd_muted, '\\u2014');",
            "                } },",
            "                { key: 'freshSecs', label: 'freshness', render: function (v, row, b, i) {",
            "                    var cell = fdk.el(b, 'fresh-' + i, 'span', fd_cluster);",
            "                    cell.appendChild(fdk.meter(b, 'fresh-meter-' + i, row.budgetFrac));",
            "                    var age = fdk.el(b, 'fresh-age-' + i, 'span', fd_caption, v + 's');",
            "                    // Past 10s is a state, not decoration (P2).",
            "                    css.addClass(age, v > 10 ? fd_status_warn : fd_muted);",
            "                    cell.appendChild(age);",
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
            "            stampSlot.appendChild(fdk.stamp(branch, 'stamp-1', { slice: d.slice, model: d.model }));",
            "            render();",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'positions load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {",
            "            if (actorId && party) { party.leave(actorId); }",
            "        }",
            "    };");
    }
}
