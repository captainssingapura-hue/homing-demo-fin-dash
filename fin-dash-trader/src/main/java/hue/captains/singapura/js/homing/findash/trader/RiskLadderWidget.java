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
            "        'the book down the curve \\u00b7 \\u03a3 closes each pair, then the book \\u00b7 "
                    + "totals come from the desk, not from summing what you see (P5)'));",
            "",
            "    var host = fdk.el(branch, 'host', 'div', [fd_section, fd_scroll]);",
            "    root.appendChild(host);",
            "",
            "    var state = { focus: null, rows: [], byPk: {}, asOf: '' };",
            "    var grid = null, gridBranch = null;",
            "",
            "    // The columns, in the order a desk reads them: what it is, then the",
            "    // greeks, then how much to trust the numbers.",
            "    var COLUMNS = ['tenor', 'delta', 'vAtm', 'vRr', 'vBf', 'theta', 'fresh'];",
            "    var LABELS = { tenor: 'Book \\u25b8 tenor', delta: '\\u0394', vAtm: 'vATM',",
            "                   vRr: 'vRR', vBf: 'vBF', theta: '\\u0398', fresh: 'freshness' };",
            "",
            "    // ── ROW KINDS ────────────────────────────────────────────────────────",
            "    // A row is a pk, and what a pk MEANS is the domain's business, not the",
            "    // grid's. Three kinds, each with a scope:",
            "    //",
            "    //   leaf      one tenor of one pair          scope: that pair",
            "    //   subtotal  one pair, all tenors           scope: that pair",
            "    //   grand     the book                       scope: null (everything)",
            "    //",
            "    // The kind rides on the row record — never parsed back out of the pk",
            "    // string. A pk is an identity; asking it what sort of thing it is would",
            "    // be the same mistake as reading a machine field out of a display slot.",
            "    function leaf(pair, r)     { return { pk: pair + '/' + r.tenor, kind: 'leaf',",
            "                                          scope: pair, label: r.tenor, data: r }; }",
            "    function subtotal(pair, r) { return { pk: pair + '/\\u03a3', kind: 'subtotal',",
            "                                          scope: pair, label: '\\u03a3 ' + pair, data: r }; }",
            "    function grand(r)          { return { pk: '\\u03a3\\u03a3', kind: 'grand',",
            "                                          scope: null, label: '\\u03a3 FXO book', data: r }; }",
            "",
            "    function isAggregate(row) { return row.kind !== 'leaf'; }",
            "",
            "    // ORDER. Aggregates sort last WITHIN THEIR SCOPE — a pair's \\u03a3 closes",
            "    // its own block, the grand total closes the table. Last-overall would",
            "    // tear each subtotal away from the rows it totals, which reads as a",
            "    // mistake rather than a convention.",
            "    function order(d) {",
            "        var pairs = [], seen = {}, all = d.rows || [], i;",
            "        for (i = 0; i < all.length; i++) {",
            "            if (!seen[all[i].pair]) { seen[all[i].pair] = 1; pairs.push(all[i].pair); }",
            "        }",
            "        var out = [];",
            "        for (var p = 0; p < pairs.length; p++) {",
            "            var pair = pairs[p], sub = null;",
            "            for (i = 0; i < all.length; i++) {",
            "                if (all[i].pair !== pair) continue;",
            "                if (all[i].group) sub = all[i]; else out.push(leaf(pair, all[i]));",
            "            }",
            "            if (sub) out.push(subtotal(pair, sub));   // last within its scope",
            "        }",
            "        if (d.totals) out.push(grand(d.totals));      // last overall",
            "        return out;",
            "    }",
            "",
            "    // Every per-row judgement is made HERE, because a cell is never told",
            "    // which row it is in. The value carries the verdict — and the row kind,",
            "    // so the cell can render weight and refuse to be copied as data.",
            "    function cellValue(row, col) {",
            "        var r = row.data, agg = isAggregate(row);",
            "        if (col === 'tenor') {",
            "            return { kind: 'label', text: row.label, agg: agg, leaf: !agg };",
            "        }",
            "        if (col === 'fresh') {",
            "            var secs = r.freshSecs;",
            "            if (secs == null) return null;",
            "            return { kind: 'fresh', n: secs, text: secs + 's', agg: agg,",
            "                     state: secs > 10 ? 'over' : (secs > 5 ? 'warn' : 'ok') };",
            "        }",
            "        var n = r[col];",
            "        if (n == null) return null;",
            "        // Reval budget is the desk's own trust metric: past it, the number",
            "        // on screen is Taylor-tracked rather than freshly revalued.",
            "        var st = 'ok';",
            "        if (r.budgetFrac != null && r.budgetFrac >= 0.90) st = 'over';",
            "        else if (r.budgetFrac != null && r.budgetFrac >= 0.75) st = 'warn';",
            "        return { kind: 'num', n: n, text: fdk.fmt.compact(n), state: st, agg: agg };",
            "    }",
            "",
            "    // RelationAdapterContract. Every invariant the row kinds imply is",
            "    // enforced on THIS side — the grid needs to learn nothing about totals,",
            "    // because each seam it exposes (the filter predicate, the cell's copy",
            "    // value, these mutators) is already ours.",
            "    var adapter = {",
            "        pks:     function () { return state.rows.map(function (r) { return r.pk; }); },",
            "        columns: function () { return COLUMNS; },",
            "        get:     function (pk, col) {",
            "            var row = state.byPk[pk];",
            "            return row ? cellValue(row, col) : null;",
            "        },",
            "        subscribe:   function (fn) {},",
            "        unsubscribe: function (fn) {},",
            "        // An aggregate is derived, so it is not writable and not deletable —",
            "        // refused here rather than hoped about. (The ladder is read-only",
            "        // anyway; this is the invariant, not the current configuration.)",
            "        update:      function (pk, col, v) {",
            "            var row = state.byPk[pk];",
            "            if (!row || isAggregate(row)) return;",
            "        },",
            "        deleteRows:  function (pks) {}",
            "    };",
            "",
            "    // FILTERS. \"Never filtered out\" needs one refinement to be right: an",
            "    // aggregate is exempt from predicates over its MEMBERS' values, but it",
            "    // is still subject to choosing a scope. Filtering to EURUSD must drop",
            "    // the USDJPY subtotal — leaving it would show a total for rows that are",
            "    // not there. So: scope decides whether an aggregate appears at all,",
            "    // and value predicates never do.",
            "    function applyFocus() {",
            "        if (!grid) return;",
            "        var focus = state.focus;",
            "        if (!focus) { grid.clearFilter(); return; }",
            "        grid.filterRows(function (pk) {",
            "            var row = state.byPk[pk];",
            "            if (!row) return false;",
            "            if (row.scope === null) return true;      // the grand total spans every scope",
            "            return row.scope === focus;",
            "        });",
            "    }",
            "",
            "    // The chip strip is rebuilt whenever the pair changes, so it owns a",
            "    // dissolvable sub-branch: dissolve() releases the elements AND their",
            "    // names, which is what makes a re-render safe.",
            "    var pairBranch = null;",
            "    function renderPairChip() {",
            "        if (pairBranch) pairBranch.dissolve();",
            "        pairBranch = branch.createBranch('pair-strip');",
            "        pairBranch.activate(pairSlot);",
            "        if (state.focus) {",
            "            pairSlot.appendChild(fdk.chip(pairBranch, 'pair-chip', 'neutral', state.focus));",
            "            var clear = fdk.el(pairBranch, 'clear', 'button', fd_btn_ghost, 'whole book');",
            "            clear.onclick = function () { state.focus = null; renderPairChip(); applyFocus(); };",
            "            pairSlot.appendChild(clear);",
            "        } else {",
            "            pairSlot.appendChild(fdk.chip(pairBranch, 'pair-chip', 'neutral', 'whole book'));",
            "        }",
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
            "                'no risk rows'));",
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
            "        applyFocus();",
            "    }",
            "",
            "    function load() {",
            "        fetch('/fx/book')",
            "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "            .then(function (d) {",
            "                state.asOf = d.slice ? 'slice ' + d.slice : '';",
            "                state.rows = order(d);",
            "                state.byPk = {};",
            "                for (var i = 0; i < state.rows.length; i++) {",
            "                    state.byPk[state.rows[i].pk] = state.rows[i];",
            "                }",
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
            "                    if (!ins.pair || ins.pair === state.focus) return;",
            "                    // A pair selection FOCUSES the ladder rather than reloading",
            "                    // it: the whole book stays loaded and the filter narrows the",
            "                    // view, so the grand total keeps spanning what it always",
            "                    // spanned and 'whole book' is one click back.",
            "                    state.focus = ins.pair;",
            "                    renderPairChip();",
            "                    applyFocus();",
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
