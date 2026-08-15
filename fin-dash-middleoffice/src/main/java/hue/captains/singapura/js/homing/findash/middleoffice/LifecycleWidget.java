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
            "    // Built from the trade itself: a trade's journal IS its lifecycle",
            "    // (booked, then each amendment, then where it stands). Nothing in",
            "    // /fx/lifecycle references a trade id, so inventing a linkage would be",
            "    // false precision — the desk queue below stays what it is.",
            "    var tradeBranch = null;",
            "",
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
            "        tradeHead.appendChild(fdk.chip(b, 'status',",
            "            t.amendments && t.amendments.length ? 'warn' : 'good',",
            "            t.amendments && t.amendments.length",
            "                ? 'amended (' + t.amendments.length + ')' : t.status));",
            "",
            "        // booked",
            "        var booked = fdk.el(b, 'ev-booked', 'div', [fd_row, fd_rule]);",
            "        booked.appendChild(fdk.chip(b, 'ev-booked-chip', 'good', 'booked'));",
            "        var bm = fdk.el(b, 'ev-booked-main', 'div', fd_spacer);",
            "        bm.appendChild(fdk.el(b, 'ev-booked-what', 'div', fd_strong,",
            "            t.time + ' \\u00b7 ' + t.trader + ' \\u00b7 ' + t.notional));",
            "        bm.appendChild(fdk.el(b, 'ev-booked-pv', 'div', fd_caption,",
            "            'PV at booking ' + t.pvAtBooking));",
            "        booked.appendChild(bm);",
            "        booked.appendChild(fdk.stamp(b, 'ev-booked-stamp', { slice: t.stamp }));",
            "        tradeBody.appendChild(booked);",
            "",
            "        // each amendment, in journal order",
            "        var ams = t.amendments || [];",
            "        for (var i = 0; i < ams.length; i++) {",
            "            var a = ams[i];",
            "            var row = fdk.el(b, 'ev-am-' + i, 'div', [fd_row, fd_rule]);",
            "            row.appendChild(fdk.chip(b, 'ev-am-chip-' + i, 'warn', 'amended'));",
            "            var m = fdk.el(b, 'ev-am-main-' + i, 'div', fd_spacer);",
            "            m.appendChild(fdk.el(b, 'ev-am-what-' + i, 'div', fd_strong, a.what));",
            "            m.appendChild(fdk.el(b, 'ev-am-who-' + i, 'div', fd_caption,",
            "                a.when + ' \\u00b7 ' + a.who + ' \\u00b7 ' + a.approval));",
            "            row.appendChild(m);",
            "            row.appendChild(fdk.el(b, 'ev-am-pnl-' + i, 'span', [fd_caption, fd_num],",
            "                a.pnlImpact));",
            "            tradeBody.appendChild(row);",
            "        }",
            "",
            "        tradeBody.appendChild(fdk.el(b, 'note', 'div', [fd_caption, fd_muted, fd_section],",
            "            ams.length",
            "                ? 're-price + re-explain are automatic on each amendment \\u00b7 P&L impact shown before commit'",
            "                : 'no amendments \\u00b7 the booking is the whole journal so far'));",
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
