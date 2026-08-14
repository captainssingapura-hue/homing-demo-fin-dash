package hue.captains.singapura.js.homing.findash.marketdata;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

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
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
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
            "    var gridSlot = fdk.el('div', 'overflow-x:auto;');",
            "    root.appendChild(gridSlot);",
            "    var impact = fdk.el('div', 'margin-top:6px;font-size:12px;color:var(--color-text-primary);min-height:18px;', ",
            "        'click a degraded cell for its downstream impact');",
            "    impact.style.color = 'var(--color-text-muted)';",
            "    root.appendChild(impact);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Monitors'));",
            "    var monitors = fdk.el('div', 'color:var(--color-text-primary);font-size:12px;line-height:1.7;');",
            "    root.appendChild(monitors);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-3', 'Quarantine review queue \\u2014 Ring 3, one gesture'));",
            "    var queue = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(queue);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'release/extend are reason-captured and journaled \\u00b7 demo: screen state only'));",
            "",
            "    var STATE = {",
            "        ok:          { icon: '\\u25cf', color: '#006300', label: 'healthy' },",
            "        stale:       { icon: '\\u25cb', color: '#9a6b1f', label: 'stale' },",
            "        quarantined: { icon: '\\u2715', color: '#a32e2e', label: 'quarantined' },",
            "        none:        { icon: '\\u2014', color: 'var(--color-text-muted)', label: 'not provided' }",
            "    };",
            "",
            "    function renderGrid(d) {",
            "        var sources = [];",
            "        for (var i = 0; i < d.cells.length; i++) {",
            "            if (sources.indexOf(d.cells[i].source) < 0) sources.push(d.cells[i].source);",
            "        }",
            "        var table = document.createElement('table');",
            "        table.style.cssText = 'border-collapse:collapse;font-size:12px;';",
            "        var hr = document.createElement('tr');",
            "        var corner = document.createElement('th');",
            "        corner.style.cssText = 'padding:4px 10px;';",
            "        hr.appendChild(corner);",
            "        for (i = 0; i < d.kinds.length; i++) {",
            "            var th = document.createElement('th');",
            "            th.style.cssText = 'padding:4px 10px;font-size:10.5px;font-weight:600;'",
            "                + 'color:var(--color-text-muted);text-align:center;';",
            "            th.textContent = d.kinds[i];",
            "            hr.appendChild(th);",
            "        }",
            "        table.appendChild(hr);",
            "        for (var s = 0; s < sources.length; s++) {",
            "            var tr = document.createElement('tr');",
            "            var name = document.createElement('td');",
            "            name.style.cssText = 'padding:4px 10px;font-weight:600;color:var(--color-text-primary);'",
            "                + 'border-top:1px solid var(--color-border);';",
            "            name.textContent = sources[s];",
            "            tr.appendChild(name);",
            "            for (var k = 0; k < d.kinds.length; k++) {",
            "                (function (src, kind) {",
            "                    var cell = null;",
            "                    for (var c = 0; c < d.cells.length; c++) {",
            "                        if (d.cells[c].source === src && d.cells[c].kind === kind) cell = d.cells[c];",
            "                    }",
            "                    var st = STATE[cell ? cell.state : 'none'];",
            "                    var td = document.createElement('td');",
            "                    td.style.cssText = 'padding:4px 10px;text-align:center;color:' + st.color",
            "                        + ';border-top:1px solid var(--color-border);font-weight:600;'",
            "                        + (cell && (cell.note || cell.downstream) ? 'cursor:pointer;' : '');",
            "                    td.textContent = st.icon;",
            "                    td.title = st.label + (cell && cell.note ? ' \\u2014 ' + cell.note : '');",
            "                    if (cell && (cell.note || cell.downstream)) {",
            "                        td.onclick = function () {",
            "                            impact.style.color = 'var(--color-text-primary)';",
            "                            impact.textContent = src + ' ' + kind + ': ' + st.label",
            "                                + (cell.note ? ' \\u2014 ' + cell.note : '')",
            "                                + (cell.downstream ? '  \\u2192  ' + cell.downstream : '');",
            "                        };",
            "                    }",
            "                    tr.appendChild(td);",
            "                })(sources[s], d.kinds[k]);",
            "            }",
            "            table.appendChild(tr);",
            "        }",
            "        gridSlot.appendChild(table);",
            "    }",
            "",
            "    function queueRow(q) {",
            "        var line = fdk.el(branch, 'row-1', 'div', fd_row",
            "            + 'border-bottom:1px solid var(--color-border);font-size:12.5px;');",
            "        line.appendChild(fdk.chip(branch, 'chip-1', q.severity, q.since));",
            "        var main = fdk.el(branch, 'spacer-1', 'div', fd_spacer);",
            "        main.appendChild(fdk.el('div', 'font-weight:600;cursor:pointer;', q.instrument));",
            "        main.firstChild.onclick = function () {",
            "            if (party && actorId) party.tellFrom(actorId,",
            "                { kind: 'InstrumentSelected', instrument: { pair: q.pair } });",
            "        };",
            "        main.appendChild(fdk.el(branch, 'cap-2', 'div', fd_caption, q.reason));",
            "        line.appendChild(main);",
            "        function act(label, resultText) {",
            "            var b = document.createElement('button');",
            "            b.textContent = label;",
            "            b.style.cssText = 'padding:2px 8px;font-size:11px;border:1px solid var(--color-border);'",
            "                + 'border-radius:4px;background:var(--color-surface);color:var(--color-text-primary);cursor:pointer;margin-left:4px;';",
            "            b.onclick = function () {",
            "                while (line.firstChild) line.removeChild(line.firstChild);",
            "                line.appendChild(fdk.chip(branch, 'chip-2', 'neutral', resultText));",
            "                line.appendChild(fdk.el('span', 'margin-left:8px;color:var(--color-text-muted);font-size:11.5px;', ",
            "                    q.instrument + ' \\u00b7 reason-captured \\u00b7 journaled (demo)'));",
            "            };",
            "            return b;",
            "        }",
            "        line.appendChild(act('release', 'released'));",
            "        line.appendChild(act('extend', 'extended 30 min'));",
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
            "            summarySlot.appendChild(bad ? fdk.chip('warn', bad + ' degraded feeds') : fdk.chip('good'));",
            "            renderGrid(d);",
            "            for (i = 0; i < d.monitors.length; i++) {",
            "                monitors.appendChild(fdk.el('div', null, '\\u2022 ' + d.monitors[i]));",
            "            }",
            "            for (i = 0; i < d.quarantine.length; i++) queue.appendChild(queueRow(d.quarantine[i]));",
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
