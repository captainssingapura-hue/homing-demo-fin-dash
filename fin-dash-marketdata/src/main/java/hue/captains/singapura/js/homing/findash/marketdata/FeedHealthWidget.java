package hue.captains.singapura.js.homing.findash.marketdata;

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
import hue.captains.singapura.js.homing.findash.core.css.FdDataCss;

import java.util.List;

/**
 * The feed &amp; quality console (study §6, W5 pattern) — the system's
 * early-warning radar: the source × quote-kind health grid (state never by
 * color alone — icon + note, P2), composite/arb monitors, and the quarantine
 * review queue with one-gesture release/extend (Ring 3, reason framing).
 * Clicking a degraded cell shows its <b>downstream impact</b> — "this stale
 * forward feeds these three surfaces" — the study's direct-pivot requirement.
 * Demo: queue actions mutate screen state, not a journal.
 */
public final class FeedHealthWidget
        extends WorkspaceWidget<WorkspaceWidget._None, FeedHealthWidget> {

    public static final FeedHealthWidget INSTANCE = new FeedHealthWidget();

    private FeedHealthWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, FeedHealthWidget> {}

    @Override protected _Construct<_None, FeedHealthWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Feed & Quality"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_hidden(),
                        new FdFrameCss.fd_scroll(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_critical(),
                        new FdStatusCss.fd_status_good(),
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn_ghost(),
                        new FdControlCss.fd_clickable()),
                        FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdDataCss.fd_table(),
                        new FdDataCss.fd_td(),
                        new FdDataCss.fd_th()),
                        FdDataCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var party = (workspaceCtx && workspaceCtx.deskParty) ? workspaceCtx.deskParty : null;",
            "    var actorId = null;",
            "    if (party) {",
            "        actorId = 'marketdata/feeds-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'FEED & QUALITY'));",
            "    var summarySlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(summarySlot);",
            "    root.appendChild(head);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Feed health (source \\u00d7 kind)'));",
            "    var gridSlot = fdk.el(branch, 'grid-slot', 'div', fd_scroll);",
            "    root.appendChild(gridSlot);",
            "    var impact = fdk.el(branch, 'impact', 'div', [fd_dense, fd_muted, fd_section],",
            "        'click a degraded cell for its downstream impact');",
            "    root.appendChild(impact);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Monitors'));",
            "    var monitors = fdk.el(branch, 'monitors', 'div', fd_dense);",
            "    root.appendChild(monitors);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-3', 'Quarantine review queue \\u2014 Ring 3, one gesture'));",
            "    var queue = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(queue);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'release/extend are reason-captured and journaled \\u00b7 demo: screen state only'));",
            "",
            "    // Each feed state is an icon, a label and a status class — the colour",
            "    // itself belongs to the theme, and the icon means the heatmap is still",
            "    // readable without it (P2: never colour alone).",
            "    var STATE = {",
            "        ok:          { icon: '\\u25cf', cls: fd_status_good,     label: 'healthy' },",
            "        stale:       { icon: '\\u25cb', cls: fd_status_warn,     label: 'stale' },",
            "        quarantined: { icon: '\\u2715', cls: fd_status_critical, label: 'quarantined' },",
            "        none:        { icon: '\\u2014', cls: fd_muted,           label: 'not provided' }",
            "    };",
            "",
            "    function renderGrid(d) {",
            "        var sources = [];",
            "        for (var i = 0; i < d.cells.length; i++) {",
            "            if (sources.indexOf(d.cells[i].source) < 0) sources.push(d.cells[i].source);",
            "        }",
            "        var table = fdk.el(branch, 'hm', 'table', [fd_table, fd_dense]);",
            "        var hr = fdk.el(branch, 'hm-head', 'tr', null);",
            "        hr.appendChild(fdk.el(branch, 'hm-corner', 'th', fd_th));",
            "        for (i = 0; i < d.kinds.length; i++) {",
            "            hr.appendChild(fdk.el(branch, 'hm-th-' + i, 'th', fd_th, d.kinds[i]));",
            "        }",
            "        table.appendChild(hr);",
            "        for (var s = 0; s < sources.length; s++) {",
            "            var tr = fdk.el(branch, 'hm-tr-' + s, 'tr', null);",
            "            tr.appendChild(fdk.el(branch, 'hm-src-' + s, 'td', [fd_td, fd_strong], sources[s]));",
            "            for (var k = 0; k < d.kinds.length; k++) {",
            "                (function (src, kind, s2, k2) {",
            "                    var cell = null;",
            "                    for (var c = 0; c < d.cells.length; c++) {",
            "                        if (d.cells[c].source === src && d.cells[c].kind === kind) cell = d.cells[c];",
            "                    }",
            "                    var st = STATE[cell ? cell.state : 'none'];",
            "                    var td = fdk.el(branch, 'hm-' + s2 + '-' + k2, 'td',",
            "                        [fd_td, fd_strong, st.cls], st.icon);",
            "                    td.title = st.label + (cell && cell.note ? ' \\u2014 ' + cell.note : '');",
            "                    if (cell && (cell.note || cell.downstream)) {",
            "                        css.addClass(td, fd_clickable);",
            "                        td.onclick = function () {",
            "                            css.removeClass(impact, fd_muted);",
            "                            impact.textContent = src + ' ' + kind + ': ' + st.label",
            "                                + (cell.note ? ' \\u2014 ' + cell.note : '')",
            "                                + (cell.downstream ? '  \\u2192  ' + cell.downstream : '');",
            "                        };",
            "                    }",
            "                    tr.appendChild(td);",
            "                })(sources[s], d.kinds[k], s, k);",
            "            }",
            "            table.appendChild(tr);",
            "        }",
            "        gridSlot.appendChild(table);",
            "    }",
            "",
            "    function queueRow(qi, q) {",
            "        var line = fdk.el(branch, 'q-' + qi, 'div', [fd_row, fd_rule]);",
            "        line.appendChild(fdk.chip(branch, 'q-sev-' + qi, q.severity, q.since));",
            "        var main = fdk.el(branch, 'q-main-' + qi, 'div', fd_spacer);",
            "        var name = fdk.el(branch, 'q-name-' + qi, 'div', [fd_strong, fd_clickable], q.instrument);",
            "        name.onclick = function () {",
            "            if (party && actorId) party.tellFrom(actorId,",
            "                { kind: 'InstrumentSelected', instrument: { pair: q.pair } });",
            "        };",
            "        main.appendChild(name);",
            "        main.appendChild(fdk.el(branch, 'q-reason-' + qi, 'div', fd_caption, q.reason));",
            "        line.appendChild(main);",
            "        // The outcome strip replaces the controls by swapping fd_hidden,",
            "        // rather than tearing the row's children out from under the branch.",
            "        var outcome = fdk.el(branch, 'q-outcome-' + qi, 'div', [fd_cluster, fd_hidden]);",
            "        var acts = fdk.el(branch, 'q-acts-' + qi, 'div', fd_cluster);",
            "        function act(key, label, resultText) {",
            "            var b = fdk.el(branch, 'qact-' + key + '-' + qi, 'button', fd_btn_ghost, label);",
            "            b.onclick = function () {",
            "                css.addClass(acts, fd_hidden);",
            "                css.removeClass(outcome, fd_hidden);",
            "                outcome.appendChild(fdk.chip(branch, 'q-chip-' + qi, 'neutral', resultText));",
            "                outcome.appendChild(fdk.el(branch, 'q-note-' + qi, 'span', [fd_caption, fd_muted],",
            "                    q.instrument + ' \\u00b7 reason-captured \\u00b7 journaled (demo)'));",
            "            };",
            "            return b;",
            "        }",
            "        acts.appendChild(act('release', 'release', 'released'));",
            "        acts.appendChild(act('extend', 'extend', 'extended 30 min'));",
            "        line.appendChild(acts);",
            "        line.appendChild(outcome);",
            "        return line;",
            "    }",
            "",
            "    fetch('/fx/feeds')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            var bad = 0;",
            "            for (var i = 0; i < d.cells.length; i++) {",
            "                if (d.cells[i].state === 'stale' || d.cells[i].state === 'quarantined') bad++;",
            "            }",
            "            summarySlot.appendChild(bad",
            "                ? fdk.chip(branch, 'summary', 'warn', bad + ' degraded feeds')",
            "                : fdk.chip(branch, 'summary-2', 'good'));",
            "            renderGrid(d);",
            "            for (i = 0; i < d.monitors.length; i++) {",
            "                monitors.appendChild(fdk.el(branch, 'mon-' + i, 'div', null,",
            "                    '2022 ' + d.monitors[i]));",
            "            }",
            "            for (i = 0; i < d.quarantine.length; i++) queue.appendChild(queueRow(i, d.quarantine[i]));",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'feeds load failed: ' + (e && e.message ? e.message : e)));",
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
