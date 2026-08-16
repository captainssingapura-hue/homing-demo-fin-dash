package hue.captains.singapura.js.homing.findash.middleoffice;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;

import java.util.List;

/**
 * The middle-office lifecycle workstation (study §10): expiries with cut
 * countdowns and exercise decisions (auto vs manual), barrier/touch watches
 * with the determination record, fixings, deliveries — plus the breaks
 * dashboard with aging and ownership. Bulk expiry processing is
 * <b>preview-then-commit</b>; the preview is shown before anything books
 * (demo: commit mutates screen state).
 */
public final class LifecycleWidget
        extends WorkspaceWidget<WorkspaceWidget._None, LifecycleWidget> {

    public static final LifecycleWidget INSTANCE = new LifecycleWidget();

    private LifecycleWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, LifecycleWidget> {}

    @Override protected _Construct<_None, LifecycleWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Lifecycle"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_hidden(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_mono(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_strongest(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn()),
                        FdControlCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    root.appendChild(fdk.el(branch, 'title-1', 'div', fd_title, ",
            "        'LIFECYCLE WORKSTATION'));",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'cut times desk-local, UTC on hover \\u00b7 every event exactly once, journaled'));",
            "",
            "    // The third level of the drill-down: portfolio -> blotter filter ->",
            "    // TRADE. When a trade is selected on the trade bus this pane shows that",
            "    // trade's own journal; with nothing selected it falls back to the",
            "    // desk-wide queue below, which is a different thing and stays separate.",
            "    var tradeHead = fdk.el(branch, 'trade-head', 'div', [fd_header_row, fd_section]);",
            "    root.appendChild(tradeHead);",
            "    var tradeBody = fdk.el(branch, 'trade-body', 'div', null);",
            "    root.appendChild(tradeBody);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Desk events'));",
            "    var items = fdk.el(branch, 'slot-1', 'div', null);",
            "    root.appendChild(items);",
            "",
            "    var bulk = fdk.el(branch, 'cluster-1', 'div', [fd_cluster, fd_section]);",
            "    var preview = fdk.el(branch, 'preview', 'button', fd_btn);",
            "    preview.textContent = 'Preview NY-cut bulk processing';",
            "    bulk.appendChild(preview);",
            "    var bulkNote = fdk.el(branch, 'cap-1-2', 'span', [fd_caption, fd_muted], 'preview-then-commit');",
            "    bulk.appendChild(bulkNote);",
            "    root.appendChild(bulk);",
            "    preview.onclick = function () {",
            "        bulkNote.textContent = 'preview: 214 expiries \\u00b7 197 auto-exercise \\u00b7 2 manual decisions'",
            "            + ' \\u00b7 P&L impact shown per position \\u2192 [commit] (demo)';",
            "        css.removeClass(bulkNote, fd_muted);",
            "    };",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Breaks \\u2014 aging \\u00b7 ownership'));",
            "    var breaks = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(breaks);",
            "    root.appendChild(fdk.el(branch, 'cap-1-3', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'amendments are journaled events: re-price + re-explain automatic, P&L impact shown before commit,'",
            "        + ' materiality-based four-eyes'));",
            "",
            "    function itemRow(rk, i) {",
            "        var line = fdk.el(branch, 'row-1-' + rk, 'div', [fd_row, fd_rule]);",
            "        line.appendChild(fdk.chip(branch, 'chip-1-' + rk, i.severity, i.kind));",
            "        var main = fdk.el(branch, 'spacer-1-' + rk, 'div', fd_spacer);",
            "        main.appendChild(fdk.el(branch, 'strong-1-' + rk, 'div', fd_strong, i.desc));",
            "        main.appendChild(fdk.el(branch, 'cap-2-' + rk, 'div', fd_caption, i.state));",
            "        line.appendChild(main);",
            "        line.appendChild(fdk.el(branch, 'due-' + rk, 'span', [fd_muted, fd_caption], i.due));",
            "        return line;",
            "    }",
            "",
            "    function breakRow(rk, b) {",
            "        var line = fdk.el(branch, 'row-2-' + rk, 'div', [fd_row, fd_rule]);",
            "        line.appendChild(fdk.chip(branch, 'chip-2-' + rk, b.severity, 'aging ' + b.age));",
            "        var main = fdk.el(branch, 'spacer-2-' + rk, 'div', fd_spacer);",
            "        main.appendChild(fdk.el(branch, 'strong-2-' + rk, 'div', fd_strong, b.desc));",
            "        main.appendChild(fdk.el(branch, 'cap-3-' + rk, 'div', fd_caption, ",
            "            'vs ' + b.vs + ' \\u00b7 owner: ' + b.owner));",
            "        line.appendChild(main);",
            "        return line;",
            "    }",
            "",
            "    fetch('/fx/lifecycle')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            for (var i = 0; i < d.items.length; i++) items.appendChild(itemRow(i, d.items[i]));",
            "            for (i = 0; i < d.breaks.length; i++) breaks.appendChild(breakRow(i, d.breaks[i]));",
            "        })",
            "        .catch(function (e) {",
            "            items.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'lifecycle load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    // ---- the selected trade's own lifecycle ---------------------------",
            "    // Fetched, not computed. /fx/trade-lifecycle picks the scheme from the",
            "    // product, seeds from the trade's own identity and applies the",
            "    // per-trade branches; this pane draws whatever comes back and knows",
            "    // nothing about barriers or fixings (P5 — UIs are consumers, not",
            "    // calculators). Adding a product means changing the simulator, not this.",
            "    var tradeBranch = null;",
            "",
            "    // Each render owns a branch, and the fetch it starts captures that",
            "    // branch. Selections can outrun the network — holding an arrow key does",
            "    // it easily — so a reply that comes back after its branch was dissolved",
            "    // must be dropped: painting it would stack a stale journal on top of the",
            "    // trade now on screen. Comparing the captured branch against the live one",
            "    // is the whole test.",
            "    function renderTrade(t) {",
            "        if (tradeBranch) tradeBranch.dissolve();",
            "        tradeBranch = branch.createBranch('trade');",
            "        tradeBranch.activate(root);",
            "        var b = tradeBranch;",
            "        if (!t) {",
            "            tradeHead.appendChild(fdk.el(b, 'none', 'span', [fd_caption, fd_muted],",
            "                'no trade selected \\u00b7 pick one in the Trade Blotter to see its journal'));",
            "            return;",
            "        }",
            "        tradeHead.appendChild(fdk.el(b, 'id', 'span', fd_strongest, t.id));",
            "        tradeHead.appendChild(fdk.el(b, 'ticket', 'span', [fd_caption, fd_mono], t.ticket));",
            "",
            "        // One status line rather than a placeholder that has to be hidden:",
            "        // it says what is happening, then becomes the scheme description.",
            "        var note = fdk.el(b, 'note', 'div', [fd_caption, fd_muted],",
            "            'loading lifecycle\\u2026');",
            "        tradeBody.appendChild(note);",
            "",
            "        fetch('/fx/trade-lifecycle?id=' + encodeURIComponent(t.id))",
            "            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "            .then(function (d) { if (b === tradeBranch) paint(b, note, d); })",
            "            .catch(function (e) {",
            "                if (b !== tradeBranch) return;",
            "                note.textContent = 'lifecycle load failed: ' + (e && e.message ? e.message : e);",
            "                css.setClass(note, fd_error_text);",
            "            });",
            "    }",
            "",
            "    function paint(b, note, d) {",
            "        if (d.error) {",
            "            note.textContent = d.error;",
            "            css.setClass(note, fd_error_text);",
            "            return;",
            "        }",
            "        note.textContent = d.schemeNote;",
            "        tradeHead.appendChild(fdk.chip(b, 'scheme', 'neutral', d.scheme.toLowerCase()));",
            "        tradeHead.appendChild(fdk.chip(b, 'outcome',",
            "            /pending|knocked/.test(d.outcome) ? 'warn' : 'good', d.outcome));",
            "",
            "        for (var i = 0; i < d.events.length; i++) {",
            "            var e = d.events[i];",
            "            var row = fdk.el(b, 'ev-' + i, 'div', [fd_row, fd_rule]);",
            "            row.appendChild(fdk.chip(b, 'ev-chip-' + i, e.severity, e.kind));",
            "            var m = fdk.el(b, 'ev-main-' + i, 'div', fd_spacer);",
            "            m.appendChild(fdk.el(b, 'ev-label-' + i, 'div', fd_strong, e.label));",
            "            m.appendChild(fdk.el(b, 'ev-detail-' + i, 'div', fd_caption, e.detail));",
            "            row.appendChild(m);",
            "            row.appendChild(fdk.el(b, 'ev-at-' + i, 'span', [fd_caption, fd_muted],",
            "                e.at + ' \\u00b7 ' + e.actor));",
            "            tradeBody.appendChild(row);",
            "        }",
            "",
            "        tradeBody.appendChild(fdk.el(b, 'replay', 'div', [fd_caption, fd_muted, fd_section],",
            "            'simulated from the trade\\u2019s own facts \\u00b7 the same trade replays'",
            "            + ' the same journal every time (P4)'));",
            "    }",
            "    renderTrade(null);",
            "",
            "    var tradeParty = (workspaceCtx && workspaceCtx.tradeParty) ? workspaceCtx.tradeParty : null;",
            "    var tradeActorId = null;",
            "    if (tradeParty) {",
            "        tradeActorId = 'mo/lifecycle-' + Math.random().toString(36).slice(2, 8);",
            "        tradeParty.joinActor({",
            "            id: tradeActorId,",
            "            parentSecretary: 'trade',",
            "            reactors: {",
            "                TradeChanged: function (msg) { renderTrade(msg.trade || null); }",
            "            }",
            "        });",
            "        // Late join: a trade may already be selected when this tab opens.",
            "        tradeParty.tellFrom(tradeActorId, { kind: 'CurrentTradeRequested' });",
            "    }",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {",
            "            if (tradeActorId && tradeParty) {",
            "                try { tradeParty.leave(tradeActorId); } catch (e) {}",
            "            }",
            "        }",
            "    };");
    }
}
