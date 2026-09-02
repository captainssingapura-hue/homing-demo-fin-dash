package hue.captains.singapura.js.homing.findash.trader;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.grid.RelationGridModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/**
 * The risk ladder — one pair's exposure down the tenor curve, on RFC 0050's
 * {@code RelationGrid}.
 *
 * <h2>Why one pair</h2>
 *
 * <p>A desk ladder normally reads pair → tenor with a subtotal per pair, and
 * {@code RelationGrid} has no row grouping: {@code GridViewMaps} takes a flat
 * list of primary keys, and {@code GridLayout.render} is given a row
 * <em>count</em>, not row headers. Rather than flatten every pair into
 * "EURUSD · 1W" keys and lose the grouping anyway, the ladder scopes itself to
 * the selected instrument and follows {@code InstrumentChanged} — which is how
 * the rest of this desk already works. With one pair on screen there is exactly
 * one subtotal, and it becomes an ordinary row (Σ) rather than a group header.
 * The limitation stops mattering instead of being worked around.</p>
 *
 * <h2>What each piece owns</h2>
 *
 * <ul>
 *   <li>The <b>adapter</b> is the seam onto the domain: it holds the rows for
 *       the current pair, answers {@code get(pk, column)}, and — because a cell
 *       never learns its row — makes the per-row judgements (is this reading
 *       stale? is this residual over its budget?) and ships them inside the
 *       value.</li>
 *   <li>The <b>cell</b> ({@link RiskLadderCellModule}) renders that judgement.</li>
 *   <li>The <b>grid</b> owns structure, selection, keyboard and copy — including
 *       TSV copy of a selected block, which is a real desk gesture.</li>
 * </ul>
 *
 * <p>Read-only by construction ({@code editable: false}): the ladder reports
 * risk the book already owns, and a UI is a consumer, not a calculator (P5).</p>
 */
public final class RiskLadderWidget
        extends WorkspaceWidget<WorkspaceWidget._None, RiskLadderWidget> {

    public static final RiskLadderWidget INSTANCE = new RiskLadderWidget();

    private RiskLadderWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RiskLadderWidget> {}

    @Override protected _Construct<_None, RiskLadderWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Risk Ladder"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()),
                        FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new RelationGridModule.RelationGrid()),
                        RelationGridModule.INSTANCE),
                new ModuleImports<>(List.of(new RiskLadderCellModule.ladderCell()),
                        RiskLadderCellModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_scroll(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn_ghost()),
                        FdControlCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, 'RISK LADDER'));",
            "    var pairSlot = fdk.el(branch, 'pair', 'span', fd_cluster);",
            "    head.appendChild(pairSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted],",
            "        'one pair down the curve \\u00b7 \\u03a3 is the pair subtotal \\u00b7 "
                    + "select a block and copy'));",
            "",
            "    var host = fdk.el(branch, 'host', 'div', [fd_section, fd_scroll]);",
            "    root.appendChild(host);",
            "",
            "    var state = { pair: 'EURUSD', rows: [], asOf: '' };",
            "    var grid = null, gridBranch = null;",
            "",
            "    // The columns, in the order a desk reads them: what it is, then the",
            "    // greeks, then how much to trust the numbers.",
            "    var COLUMNS = ['tenor', 'delta', 'vAtm', 'vRr', 'vBf', 'theta', 'fresh'];",
            "    var LABELS = { tenor: 'Tenor', delta: '\\u0394', vAtm: 'vATM', vRr: 'vRR',",
            "                   vBf: 'vBF', theta: '\\u0398', fresh: 'freshness' };",
            "",
            "    function rowFor(pk) {",
            "        for (var i = 0; i < state.rows.length; i++) {",
            "            if (keyOf(state.rows[i]) === pk) return state.rows[i];",
            "        }",
            "        return null;",
            "    }",
            "",
            "    // The subtotal row carries no tenor, so it keys as \\u03a3 — one pair on",
            "    // screen means one subtotal, and it can simply be a row.",
            "    function keyOf(r) { return r.group ? '\\u03a3' : r.tenor; }",
            "",
            "    // Every per-row judgement is made HERE, because a cell is never told",
            "    // which row it is in. The value carries the verdict.",
            "    function cellValue(r, col) {",
            "        if (col === 'tenor') {",
            "            return { kind: 'label', text: keyOf(r), emphasis: !!r.group };",
            "        }",
            "        if (col === 'fresh') {",
            "            var secs = r.freshSecs;",
            "            return { kind: 'fresh', n: secs, text: secs + 's',",
            "                     state: secs > 10 ? 'over' : (secs > 5 ? 'warn' : 'ok') };",
            "        }",
            "        var n = r[col];",
            "        if (n == null) return null;",
            "        // Reval budget is the desk's own trust metric: past it, the number",
            "        // on screen is Taylor-tracked rather than freshly revalued.",
            "        var st = 'ok';",
            "        if (r.budgetFrac != null && r.budgetFrac >= 0.90) st = 'over';",
            "        else if (r.budgetFrac != null && r.budgetFrac >= 0.75) st = 'warn';",
            "        return { kind: 'num', n: n, text: fdk.fmt.compact(n), state: st,",
            "                 emphasis: !!r.group };",
            "    }",
            "",
            "    // RelationAdapterContract. The mutators are inert: this ladder reports",
            "    // risk the book already owns (P5), so it is read-only by construction.",
            "    var adapter = {",
            "        pks:     function () { return state.rows.map(keyOf); },",
            "        columns: function () { return COLUMNS; },",
            "        get:     function (pk, col) { var r = rowFor(pk); return r ? cellValue(r, col) : null; },",
            "        subscribe:   function (fn) {},",
            "        unsubscribe: function (fn) {},",
            "        update:      function (pk, col, v) {},",
            "        deleteRows:  function (pks) {}",
            "    };",
            "",
            "    // The chip strip is rebuilt whenever the pair changes, so it owns a",
            "    // dissolvable sub-branch: dissolve() releases the elements AND their",
            "    // names, which is what makes a re-render safe.",
            "    var pairBranch = null;",
            "    function renderPairChip() {",
            "        if (pairBranch) pairBranch.dissolve();",
            "        pairBranch = branch.createBranch('pair-strip');",
            "        pairBranch.activate(pairSlot);",
            "        pairSlot.appendChild(fdk.chip(pairBranch, 'pair-chip', 'neutral', state.pair));",
            "        if (state.asOf) {",
            "            pairSlot.appendChild(fdk.el(pairBranch, 'asof', 'span', [fd_caption, fd_muted],",
            "                'as-of ' + state.asOf));",
            "        }",
            "    }",
            "",
            "    function build() {",
            "        // The grid owns its cells' elements, so it gets a branch of its own",
            "        // that is dissolved whole when the pair changes — the same discipline",
            "        // every re-rendering region here follows.",
            "        if (grid) { try { grid.destroy(); } catch (e) {} grid = null; }",
            "        if (gridBranch) gridBranch.dissolve();",
            "        gridBranch = branch.createBranch('grid');",
            "        gridBranch.activate(host);",
            "        renderPairChip();",
            "        if (!state.rows.length) {",
            "            host.appendChild(fdk.el(gridBranch, 'empty', 'div', [fd_caption, fd_muted],",
            "                'no risk rows for ' + state.pair));",
            "            return;",
            "        }",
            "        grid = new RelationGrid({",
            "            container:   host,",
            "            branch:      gridBranch,",
            "            adapter:     adapter,",
            "            cellFactory: ladderCell,",
            "            editable:    false,",
            "            header:      { show: true, sticky: true, includeInCopy: true, labels: LABELS }",
            "        });",
            "    }",
            "",
            "    function load() {",
            "        fetch('/fx/book')",
            "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "            .then(function (d) {",
            "                state.asOf = d.slice ? 'slice ' + d.slice : '';",
            "                var all = d.rows || [];",
            "                var out = [];",
            "                for (var i = 0; i < all.length; i++) {",
            "                    if (all[i].pair === state.pair) out.push(all[i]);",
            "                }",
            "                state.rows = out;",
            "                build();",
            "            })",
            "            .catch(function (e) {",
            "                if (gridBranch) gridBranch.dissolve();",
            "                gridBranch = branch.createBranch('grid');",
            "                gridBranch.activate(host);",
            "                host.appendChild(fdk.el(gridBranch, 'err', 'div', fd_error_text,",
            "                    'risk ladder load failed: ' + (e && e.message ? e.message : e)));",
            "            });",
            "    }",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'trader/ladder-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (!ins.pair || ins.pair === state.pair) return;",
            "                    state.pair = ins.pair;",
            "                    load();",
            "                }",
            "            }",
            "        });",
            "        party.tellFrom(actorId, { kind: 'CurrentInstrumentRequested' });",
            "    }",
            "",
            "    load();",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {",
            "            if (grid) { try { grid.destroy(); } catch (e) {} }",
            "            if (actorId && party) { try { party.leave(actorId); } catch (e) {} }",
            "        }",
            "    };");
    }
}
