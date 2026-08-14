package hue.captains.singapura.js.homing.findash.book;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

import java.util.List;

/**
 * The Trade Blotter — the trade-journal anchor shared across workspaces
 * (middle office, trader, product control, audit): the journal rendered as a
 * blotter (study §10 — the journal IS the audit trail; the UI just renders
 * it). Every trade carries the slice it was priced on (P1), and amended
 * trades unfold their full journaled amendment history (who, what, P&amp;L
 * impact, four-eyes). Follows {@code InstrumentChanged} (filters by pair) and
 * drives — clicking a trade broadcasts its pair.
 *
 * <p><b>Aggregated view:</b> when the portfolio selection spans more than one
 * leaf (a book or the desk root), the blotter switches to per-portfolio
 * groups — a Σ header with trade count and net PV at booking, then each
 * child portfolio with its own subtotal. A leaf selection keeps the flat
 * journal. Presentation-only arithmetic (sorting/bucketing per P5).</p>
 */
public final class TradeBlotterWidget
        extends WorkspaceWidget<WorkspaceWidget._None, TradeBlotterWidget> {

    public static final TradeBlotterWidget INSTANCE = new TradeBlotterWidget();

    private TradeBlotterWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, TradeBlotterWidget> {}

    @Override protected _Construct<_None, TradeBlotterWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Trade Blotter"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        // One entry per CssGroup: ImportsFor is keyed on the source module, so a
        // second entry for the same group replaces the first rather than adding
        // to it — split entries would silently drop the earlier classes.
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()),
                        FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_mono(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_strongest(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text(),
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_status_warn_bg()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn_ghost(),
                        new FdControlCss.fd_clickable()),
                        FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_panel(),
                        new FdSurfaceCss.fd_rule(),
                        new FdSurfaceCss.fd_rule_strong()),
                        FdSurfaceCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var state = { all: [], filter: null, portfolio: null, open: {}, pfIndex: {} };",
            "",
            "    // PV-at-booking strings are uniform EUR ('+\\u20ac142,399' / '\\u2212\\u20ac9,100') —",
            "    // parse for presentation-only subtotals (bucketing for display, P5).",
            "    function parsePv(s) {",
            "        var neg = s.indexOf('\\u2212') >= 0 || s.indexOf('-') >= 0;",
            "        var v = parseInt(String(s).replace(/[^0-9]/g, '') || '0', 10);",
            "        return neg ? -v : v;",
            "    }",
            "    function fmtPv(v) {",
            "        return (v < 0 ? '\\u2212' : '+') + '\\u20ac' + fdk.fmt.amount(Math.abs(v));",
            "    }",
            "",
            "    // Both the filter strip and the journal list are rebuilt on every",
            "    // selection change. A branch element name is unique for the branch's",
            "    // life, so each region owns a sub-branch that is dissolved and",
            "    // re-created per render — dissolve() detaches the elements too, which",
            "    // is what retires the old clear() loops.",
            "    var filterBranch = null, listBranch = null;",
            "",
            "    var head = fdk.el(branch, 'head', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'TRADE BLOTTER'));",
            "    head.appendChild(fdk.el(branch, 'cap-1', 'span', fd_caption, ",
            "        'the journal, rendered \\u00b7 amendments unfold'));",
            "    var filterSlot = fdk.el(branch, 'cluster-1', 'span', fd_cluster);",
            "    head.appendChild(filterSlot);",
            "    root.appendChild(head);",
            "",
            "    var list = fdk.el(branch, 'sect-1', 'div', fd_section);",
            "    root.appendChild(list);",
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
            "            filterSlot.appendChild(fdk.chip(filterBranch, 'ins', 'neutral', state.filter));",
            "        }",
            "        var x = fdk.el(filterBranch, 'clear', 'button', fd_btn_ghost, 'clear');",
            "        x.onclick = function () { state.filter = null; state.portfolio = null; render(); };",
            "        filterSlot.appendChild(x);",
            "    }",
            "",
            "    // k is the row's unique key within this render pass — every element",
            "    // below is named from it, so two trades never claim the same name.",
            "    function tradeRow(b, k, t) {",
            "        var wrap = fdk.el(b, 'tr-' + k, 'div', fd_rule);",
            "        var line = fdk.el(b, 'tr-line-' + k, 'div', [fd_row, fd_clickable]);",
            "        var amended = t.amendments.length > 0;",
            "        line.appendChild(amended",
            "            ? fdk.chip(b, 'tr-chip-' + k, 'warn', 'amended (' + t.amendments.length + ')')",
            "            : fdk.chip(b, 'tr-chip-' + k, 'good', 'booked'));",
            "        var main = fdk.el(b, 'tr-main-' + k, 'div', fd_spacer);",
            "        var top = fdk.el(b, 'tr-top-' + k, 'div', null);",
            "        top.appendChild(fdk.el(b, 'tr-meta-' + k, 'span', [fd_caption, fd_muted, fd_num],",
            "            t.time + ' \\u00b7 ' + t.id + ' \\u00b7 ' + t.trader + '  '));",
            "        top.appendChild(fdk.el(b, 'tr-ticket-' + k, 'span', [fd_strong, fd_mono], t.ticket));",
            "        main.appendChild(top);",
            "        var sub = fdk.el(b, 'tr-sub-' + k, 'div', fd_header_row);",
            "        sub.appendChild(fdk.el(b, 'tr-pv-' + k, 'span', fd_caption,",
            "            t.notional + ' \\u00b7 PV at booking ' + t.pvAtBooking));",
            "        sub.appendChild(fdk.stamp(b, 'tr-stamp-' + k, { slice: t.stamp }));",
            "        main.appendChild(sub);",
            "        line.appendChild(main);",
            "        if (amended) {",
            "            line.appendChild(fdk.el(b, 'tr-hist-' + k, 'span', [fd_caption, fd_muted],",
            "                state.open[t.id] ? '\\u25be history' : '\\u25b8 history'));",
            "        }",
            "        line.onclick = function () {",
            "            if (amended) { state.open[t.id] = !state.open[t.id]; render(); }",
            "            if (party && actorId) {",
            "                party.tellFrom(actorId, { kind: 'InstrumentSelected',",
            "                    instrument: { pair: t.pair, ref: t.id } });",
            "            }",
            "        };",
            "        wrap.appendChild(line);",
            "        if (amended && state.open[t.id]) {",
            "            for (var a = 0; a < t.amendments.length; a++) {",
            "                var am = t.amendments[a];",
            "                var box = fdk.el(b, 'am-' + k + '-' + a, 'div',",
            "                    [fd_panel, fd_status_warn_bg, fd_caption]);",
            "                box.appendChild(fdk.el(b, 'am-who-' + k + '-' + a, 'div',",
            "                    [fd_strong, fd_status_warn], am.when + ' \\u00b7 ' + am.who));",
            "                box.appendChild(fdk.el(b, 'am-what-' + k + '-' + a, 'div', null, am.what));",
            "                box.appendChild(fdk.el(b, 'am-pnl-' + k + '-' + a, 'div', null,",
            "                    am.pnlImpact + ' \\u00b7 ' + am.approval));",
            "                wrap.appendChild(box);",
            "            }",
            "        }",
            "        return wrap;",
            "    }",
            "",
            "    function visibleTrades() {",
            "        var out = [];",
            "        for (var i = 0; i < state.all.length; i++) {",
            "            var t = state.all[i];",
            "            if (state.portfolio && state.portfolio.leafIds.indexOf(t.portfolioId) < 0) continue;",
            "            if (state.filter && t.pair !== state.filter) continue;",
            "            out.push(t);",
            "        }",
            "        return out;",
            "    }",
            "",
            "    function render() {",
            "        renderFilter();",
            "        if (listBranch) listBranch.dissolve();",
            "        listBranch = branch.createBranch('journal');",
            "        listBranch.activate(root);",
            "        var b = listBranch;",
            "        var shown = visibleTrades();",
            "        if (!shown.length) {",
            "            list.appendChild(fdk.el(b, 'empty', 'div', [fd_caption, fd_muted],",
            "                'no trades for this filter'));",
            "            return;",
            "        }",
            "        // Aggregated view: a non-leaf selection (more than one leaf portfolio)",
            "        // groups the journal by child portfolio with subtotals.",
            "        var aggregated = state.portfolio && state.portfolio.leafIds.length > 1;",
            "        if (!aggregated) {",
            "            for (var i = 0; i < shown.length; i++) list.appendChild(tradeRow(b, i, shown[i]));",
            "            return;",
            "        }",
            "        var net = 0;",
            "        for (i = 0; i < shown.length; i++) net += parsePv(shown[i].pvAtBooking);",
            "        var total = fdk.el(b, 'total', 'div', [fd_panel, fd_row]);",
            "        total.appendChild(fdk.el(b, 'total-label', 'span', fd_strongest,",
            "            '\\u03a3 ' + state.portfolio.label));",
            "        total.appendChild(fdk.el(b, 'total-count', 'span', null,",
            "            shown.length + (shown.length === 1 ? ' trade' : ' trades')));",
            "        total.appendChild(fdk.el(b, 'total-pv', 'span', [fd_strong, fd_num],",
            "            'net PV at booking ' + fmtPv(net)));",
            "        total.appendChild(fdk.el(b, 'total-note', 'span', [fd_caption, fd_muted],",
            "            'presentation-only aggregation (P5)'));",
            "        list.appendChild(total);",
            "",
            "        var leafIds = state.portfolio.leafIds;",
            "        for (var l = 0; l < leafIds.length; l++) {",
            "            var group = [];",
            "            for (i = 0; i < shown.length; i++) {",
            "                if (shown[i].portfolioId === leafIds[l]) group.push(shown[i]);",
            "            }",
            "            if (!group.length) continue;",
            "            var sub = 0;",
            "            for (i = 0; i < group.length; i++) sub += parsePv(group[i].pvAtBooking);",
            "            var meta = state.pfIndex[leafIds[l]];",
            "            var gh = fdk.el(b, 'g-' + l, 'div', [fd_row, fd_rule_strong]);",
            "            gh.appendChild(fdk.el(b, 'g-label-' + l, 'span', fd_strongest,",
            "                '\\u25b8 ' + (meta ? meta.label : leafIds[l])));",
            "            gh.appendChild(fdk.el(b, 'g-count-' + l, 'span', null,",
            "                group.length + (group.length === 1 ? ' trade' : ' trades')));",
            "            gh.appendChild(fdk.el(b, 'g-pv-' + l, 'span', fd_num, fmtPv(sub)));",
            "            list.appendChild(gh);",
            "            // key the rows by group so two groups' rows never collide",
            "            for (i = 0; i < group.length; i++) {",
            "                list.appendChild(tradeRow(b, l + '-' + i, group[i]));",
            "            }",
            "        }",
            "    }",
            "",
            "    if (party) {",
            "        actorId = 'book/trades-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({",
            "            id: actorId,",
            "            parentSecretary: 'desk',",
            "            reactors: {",
            "                InstrumentChanged: function (msg) {",
            "                    var ins = msg.instrument || {};",
            "                    if (!ins.pair || ins.ref) return;   // ignore our own trade broadcasts",
            "                    state.filter = ins.pair;",
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
            "    Promise.all([",
            "        fetch('/fx/trades').then(function (r) {",
            "            if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); }),",
            "        fetch('/fx/portfolios').then(function (r) {",
            "            if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "    ]).then(function (res) {",
            "        state.all = res[0].trades;",
            "        state.pfIndex = res[1].index;   // id -> {label, leafIds} for group headers",
            "        render();",
            "    }).catch(function (e) {",
            "        list.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "            'trades load failed: ' + (e && e.message ? e.message : e)));",
            "    });",
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
