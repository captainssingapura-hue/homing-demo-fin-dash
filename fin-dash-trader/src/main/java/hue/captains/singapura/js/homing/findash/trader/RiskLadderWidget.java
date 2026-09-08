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
                        new RiskLadderRowsModule.ladderRows(),
                        new RiskLadderRowsModule.ladderCellValue()),
                        RiskLadderRowsModule.INSTANCE),
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
            "    // Copy status is a line whose TEXT changes, not an element per copy.",
            "    var copyNote = fdk.el(branch, 'copy-note', 'span', [fd_caption, fd_muted]);",
            "    head.appendChild(copyNote);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted],",
            "        'the book down the curve \\u00b7 a subtotal closes each pair, then the book \\u00b7 "
                    + "totals come from the desk, not from summing what you see (P5)'));",
            "",
            "    var host = fdk.el(branch, 'host', 'div', [fd_section, fd_scroll]);",
            "    root.appendChild(host);",
            "",
            "    var state = { focus: null, book: null, folded: {}, rows: [], byPk: {}, asOf: '' };",
            "    var grid = null, gridBranch = null;",
            "",
            "    // The columns, in the order a desk reads them: what it is, then the",
            "    // greeks, then how much to trust the numbers.",
            "    var COLUMNS = ['tenor', 'delta', 'vAtm', 'vRr', 'vBf', 'theta', 'fresh'];",
            "    var LABELS = { tenor: 'Book \\u25b8 tenor', delta: '\\u0394', vAtm: 'vATM',",
            "                   vRr: 'vRR', vBf: 'vBF', theta: '\\u0398', fresh: 'freshness' };",
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
            "            return row ? ladderCellValue(row, col, state.folded) : null;",
            "        },",
            "        // LIVE PATH. The grid hands us fn(pk, col, value) and batches what",
            "        // we push. The feed is a poll of the book: rows whose data changed",
            "        // are re-judged (cellValue) and every cell of them relayed — so",
            "        // aggregates tick with the leaves, which is the P5 obligation a",
            "        // feed that only ticked leaves would silently break. Row-set",
            "        // changes are not a cell update and are not handled here.",
            "        subscribe:   function (fn) {",
            "            state.feed = setInterval(function () {",
            "                fdk.load('/fx/book', { branch: branch, host: host, what: 'risk ladder' },",
            "                    function (d) {",
            "                        var fresh = ladderRows(d), changed = 0;",
            "                        for (var i = 0; i < fresh.length; i++) {",
            "                            var row = state.byPk[fresh[i].pk];",
            "                            if (!row || JSON.stringify(row.data) === JSON.stringify(fresh[i].data)) continue;",
            "                            row.data = fresh[i].data; changed++;",
            "                            for (var c = 0; c < COLUMNS.length; c++) fn(row.pk, COLUMNS[c], ladderCellValue(row, COLUMNS[c], state.folded));",
            "                        }",
            "                        // The batch paints on requestAnimationFrame, which a hidden pane",
            "                        // never gets — the caret learned this first. A tick is data,",
            "                        // and data does not wait for the compositor.",
            "                        if (changed && grid) grid.flushNow();",
            "                    });",
            "            }, 5000);",
            "        },",
            "        unsubscribe: function (fn) { if (state.feed) { clearInterval(state.feed); state.feed = null; } },",
            "        // Read-only, and loudly: an aggregate is derived and a leaf belongs to",
            "        // the book. A stub that returned silently would teach that a contract",
            "        // is something to stub.",
            "        update:      function (pk, col, v) { throw new Error('risk ladder is read-only: ' + pk + '/' + col); },",
            "        deleteRows:  function (pks) { throw new Error('risk ladder is read-only: rows belong to the book'); }",
            "    };",
            "",
            "    // FILTERS. \"Never filtered out\" needs one refinement to be right: an",
            "    // aggregate is exempt from predicates over its MEMBERS' values, but it",
            "    // is still subject to choosing a scope. Filtering to EURUSD must drop",
            "    // the USDJPY subtotal — leaving it would show a total for rows that are",
            "    // not there. So: scope decides whether an aggregate appears at all,",
            "    // and value predicates never do.",
            "    // ONE predicate. Focus and folding are both view state, and filterRows",
            "    // holds a single raw filter — two calls would silently replace each",
            "    // other rather than compose.",
            "    function applyView() {",
            "        if (!grid) return;",
            "        grid.filterRows(function (pk) {",
            "            var row = state.byPk[pk];",
            "            if (!row) return false;",
            "            // The grand total spans every scope, so it survives all three.",
            "            if (row.scope === null) return true;",
            "            // A book narrows to the PAIRS it trades — a membership test on a",
            "            // desk fact, never a guess parsed out of the book's name. It does",
            "            // not turn these into that book's numbers: the pair subtotals are",
            "            // still the desk's, over the whole book (P5). Same relationship",
            "            // the pair focus below already has to an instrument.",
            "            if (state.book && state.book.pairs.indexOf(row.scope) < 0) return false;",
            "            if (state.focus && row.scope !== state.focus) return false;",
            "            // Folding hides the DETAIL and keeps the summary — that is the",
            "            // point of folding a block rather than removing it.",
            "            if (row.kind === 'leaf' && state.folded[row.scope]) return false;",
            "            return true;",
            "        });",
            "    }",
            "",
            "    function toggleSection(scope) {",
            "        if (!scope) return;",
            "        state.folded[scope] = !state.folded[scope];",
            "        applyView();",
            "        // Repaint the caret through the grid's own direct-update path rather",
            "        // than rebuilding: the row is unchanged, only its disclosure is.",
            "        var row = state.byPk[scope];",
            "        if (row && grid) {",
            "            grid.updateCell(scope, 'tenor', ladderCellValue(row, 'tenor', state.folded));",
            "            // updateCell batches on requestAnimationFrame. A fold is a direct",
            "            // answer to a gesture, so it should not wait on the compositor —",
            "            // and rAF does not run at all in a hidden tab, which would leave",
            "            // the caret pointing the wrong way until the pane came forward.",
            "            grid.flushNow();",
            "        }",
            "    }",
            "",
            "    // The chip strip is rebuilt whenever the pair changes, so it owns a",
            "    // dissolvable sub-branch: dissolve() releases the elements AND their",
            "    // names, which is what makes a re-render safe.",
            "    var pairBranch = null;",
            "",
            "    // Enter folds the section under the cursor — the keyboard equivalent of",
            "    // the double-click, so the ladder can be driven without a mouse. The grid",
            "    // leaves Enter alone (its own keydown routes to the edit controller, inert",
            "    // while editable is false) and it tells us where the cursor sits, so this",
            "    // needs no cooperation from it.",
            "    //",
            "    // Bound through setActive rather than to the grid's element: under RFC",
            "    // 0048 the ENTERED PANE holds DOM focus, so a listener hung on our own",
            "    // container would never see the key. Same contract the trade blotter's",
            "    // arrow keys keep.",
            "    var keyHandler = function (ev) {",
            "        if (ev.key !== 'Enter' || !grid) return;",
            "        var tag = ev.target && ev.target.tagName;",
            "        if (tag === 'INPUT' || tag === 'TEXTAREA' || tag === 'SELECT') return;",
            "        var at = null;",
            "        try { at = JSON.parse(grid.cursor()); } catch (e) { return; }",
            "        if (!at || !at.pk) return;",
            "        var row = state.byPk[at.pk];",
            "        if (!row || row.kind !== 'section') return;",
            "        toggleSection(row.scope);",
            "        ev.preventDefault();",
            "    };",
            "",
            "    function renderPairChip() {",
            "        if (pairBranch) pairBranch.dissolve();",
            "        pairBranch = branch.createBranch('pair-strip');",
            "        pairBranch.activate(pairSlot);",
            "        // Each narrowing gets its own chip and its own way out, because they",
            "        // compose: a book can be selected in the tree and a pair focused from",
            "        // an instrument, and one 'whole book' button would have to undo both.",
            "        if (state.book) {",
            "            pairSlot.appendChild(fdk.chip(pairBranch, 'book-chip', 'neutral',",
            "                '\\ud83d\\udcc1 ' + state.book.label));",
            "            var dropBook = fdk.el(pairBranch, 'clear-book', 'button', fd_btn_ghost, 'all books');",
            "            dropBook.onclick = function () { state.book = null; renderPairChip(); applyView(); };",
            "            pairSlot.appendChild(dropBook);",
            "        }",
            "        if (state.focus) {",
            "            pairSlot.appendChild(fdk.chip(pairBranch, 'pair-chip', 'neutral', state.focus));",
            "            var clear = fdk.el(pairBranch, 'clear', 'button', fd_btn_ghost, 'all pairs');",
            "            clear.onclick = function () { state.focus = null; renderPairChip(); applyView(); };",
            "            pairSlot.appendChild(clear);",
            "        }",
            "        if (!state.book && !state.focus) {",
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
            "        if (grid) { grid.destroy(); grid = null; }",
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
            "            cellFactory: function (col, value, meta) {",
            "                return ladderCell(col, value, { onToggleSection: toggleSection });",
            "            },",
            "            editable:    false,",
            "            // Ctrl+C: the grid hands over TSV of the selection (aggregates' numbers",
            "            // already blank, by the cell's own getValueToCopy) and touches no",
            "            // clipboard. That is ours: write it, and say so where the eye is.",
            "            onCopy:      function (tsv) {",
            "                navigator.clipboard.writeText(tsv).then(",
            "                    function ()  { copyNote.textContent = 'copied ' + tsv.split('\\n').length + ' rows'; },",
            "                    function (e) { copyNote.textContent = 'copy failed: ' + (e && e.message ? e.message : e); });",
            "            },",
            "            header:      { show: true, sticky: true, includeInCopy: true, labels: LABELS }",
            "        });",
            "        applyView();",
            "    }",
            "",
            "    function load() {",
            "        fdk.load('/fx/book', { branch: branch, host: host, what: 'risk ladder' }, function (d) {",
            "                state.asOf = d.slice ? 'slice ' + d.slice : '';",
            "                state.rows = ladderRows(d);",
            "                state.byPk = {};",
            "                for (var i = 0; i < state.rows.length; i++) {",
            "                    state.byPk[state.rows[i].pk] = state.rows[i];",
            "                }",
            "                build();",
            "        });",
            "    }",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = fdk.actorId('trader/ladder', branch);",
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
            "                    applyView();",
            "                },",
            "                PortfolioChanged: function (msg) {",
            "                    var pf = msg.portfolio || {};",
            "                    // pairs is what a pair-shaped view can act on; leafIds is",
            "                    // for position-shaped ones. No pairs, no opinion.",
            "                    if (!pf.pairs) return;",
            "                    state.book = { label: pf.label, pairs: pf.pairs };",
            "                    renderPairChip();",
            "                    applyView();",
            "                }",
            "            }",
            "        });",
            "        party.tellFrom(actorId, { kind: 'CurrentInstrumentRequested' });",
            "        // Both, because a ladder opened after the selection was made should",
            "        // still show it — the same courtesy the position list already pays.",
            "        party.tellFrom(actorId, { kind: 'CurrentPortfolioRequested' });",
            "    }",
            "",
            "    load();",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {",
            "            if (active) document.addEventListener('keydown', keyHandler);",
            "            else        document.removeEventListener('keydown', keyHandler);",
            "        },",
            "        partyDeregister: function () {",
            "            document.removeEventListener('keydown', keyHandler);",
            "            if (grid) { grid.destroy(); }",
            "            if (actorId && party) { party.leave(actorId); }",
            "        }",
            "    };");
    }
}
