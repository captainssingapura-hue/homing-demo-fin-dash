package hue.captains.singapura.js.homing.findash.etrading;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;

import java.util.List;

/**
 * W3 — the e-trading quoting console: one row per pair (streaming /
 * auto-widened / pulled, with the reason when automatic), spread vs base,
 * inventory skew, quote age, hit ratio, session volume. All controls Ring 3
 * (P3): entitled, reason-captured, expiring — and rendered that way. The
 * <b>master kill is deliberately prominent and deliberately unceremonious</b>
 * (one press, no confirm — pressable at 3 a.m.); ceremony belongs to the
 * un-kill (reason required, four-eyes above the duration threshold).
 * Automatic state changes and manual actions land in the <b>same event
 * stream</b>, so "why were we wide for 40 minutes" has a scrollable answer.
 * Demo: actions mutate screen state and the stream, not a journal.
 *
 * <p>Clicking a pair name publishes {@code InstrumentSelected} on the desk
 * party.</p>
 */
public final class QuotingConsoleWidget
        extends WorkspaceWidget<WorkspaceWidget._None, QuotingConsoleWidget> {

    public static final QuotingConsoleWidget INSTANCE = new QuotingConsoleWidget();

    private QuotingConsoleWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, QuotingConsoleWidget> {}

    @Override protected _Construct<_None, QuotingConsoleWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Quoting Console"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_section(),
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
            "        actorId = 'etrading/console-' + Math.random().toString(36).slice(2, 8);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var state = { master: 'STREAMING', pairs: [], events: [], grid: null };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "    function now() { var d = new Date(); function p(x){ return (x < 10 ? '0' : '') + x; }",
            "        return p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds()); }",
            "",
            "    // -- header: master state + the unceremonious kill ------------------",
            "    var head = fdk.el('div', 'display:flex;align-items:center;gap:12px;flex-wrap:wrap;');",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'E-TRADING CONSOLE'));",
            "    var masterSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(masterSlot);",
            "    var kill = document.createElement('button');",
            "    kill.style.cssText = 'margin-left:auto;padding:8px 18px;font-size:13px;font-weight:700;'",
            "        + 'color:var(--color-surface);background:#d03b3b;border:none;border-radius:8px;cursor:pointer;'",
            "        + 'letter-spacing:0.5px;';",
            "    head.appendChild(kill);",
            "    root.appendChild(head);",
            "",
            "    var unkillBar = fdk.el('div', 'display:none;gap:8px;align-items:center;margin-top:8px;'",
            "        + 'padding:8px 10px;background:#f9e4e4;border-radius:8px;font-size:12px;');",
            "    root.appendChild(unkillBar);",
            "",
            "    var stampSlot = fdk.el('div', 'margin:6px 0 2px;');",
            "    root.appendChild(stampSlot);",
            "",
            "    var evTitle = fdk.sectionTitle(branch, 'sect-1', 'Event stream — automatic + manual, one stream');",
            "    root.appendChild(evTitle);",
            "    var evList = fdk.el('div', 'font-size:11.5px;line-height:1.7;max-height:110px;overflow:auto;'",
            "        + 'border:1px solid var(--color-border);border-radius:8px;padding:6px 10px;');",
            "    root.appendChild(evList);",
            "",
            "    var gridTitle = fdk.sectionTitle(branch, 'sect-2', 'Pairs');",
            "    root.appendChild(gridTitle);",
            "    var gridSlot = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(gridSlot);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'all actions Ring 3: entitled \\u00b7 reason-captured \\u00b7 auto-expiring \\u00b7 journaled \\u2014 '",
            "        + 'resume requires reason; > 30 min pulled requires second approver (four-eyes) \\u00b7 demo: not journaled'));",
            "",
            "    function stateChip(p) {",
            "        if (state.master === 'KILLED') return fdk.chip(branch, 'chip-1', 'critical', 'killed');",
            "        if (p.state === 'streaming')    return fdk.chip(branch, 'chip-2', 'live', 'streaming');",
            "        if (p.state === 'auto-widened') return fdk.chip(branch, 'chip-3', 'warn', 'auto-widened');",
            "        return fdk.chip(branch, 'chip-4', 'neutral', 'pulled');",
            "    }",
            "",
            "    function addEvent(kind, text) {",
            "        state.events.unshift({ time: now(), kind: kind, text: text });",
            "        renderEvents();",
            "    }",
            "    function renderEvents() {",
            "        clear(evList);",
            "        for (var i = 0; i < state.events.length; i++) {",
            "            var e = state.events[i];",
            "            var row = fdk.el(branch, 'slot-3', 'div', null);",
            "            row.appendChild(fdk.el('span', 'color:var(--color-text-muted);font-variant-numeric:tabular-nums;', e.time + '  '));",
            "            row.appendChild(fdk.el('span', 'font-weight:600;color:'",
            "                + (e.kind === 'auto' ? '#1c5cab' : '#9a6b1f') + ';', '[' + e.kind + '] '));",
            "            row.appendChild(fdk.el(branch, 'txt-1', 'span', null, e.text));",
            "            evList.appendChild(row);",
            "        }",
            "    }",
            "",
            "    function actBtn(label, onClick) {",
            "        var b = document.createElement('button');",
            "        b.textContent = label;",
            "        b.style.cssText = 'padding:2px 8px;font-size:11px;border:1px solid var(--color-border);'",
            "            + 'border-radius:4px;background:var(--color-surface);color:var(--color-text-primary);cursor:pointer;margin-right:4px;';",
            "        b.onclick = function (ev) { ev.stopPropagation(); onClick(); };",
            "        return b;",
            "    }",
            "",
            "    function renderMaster() {",
            "        clear(masterSlot);",
            "        masterSlot.appendChild(state.master === 'KILLED'",
            "            ? fdk.chip(branch, 'chip-5', 'critical', 'master: KILLED') : fdk.chip(branch, 'chip-6', 'live', 'master: STREAMING'));",
            "        kill.textContent = state.master === 'KILLED' ? 'KILLED \\u23fb' : 'MASTER KILL \\u23fb';",
            "        kill.disabled = state.master === 'KILLED';",
            "        unkillBar.style.display = state.master === 'KILLED' ? 'flex' : 'none';",
            "        renderGrid();",
            "    }",
            "",
            "    kill.onclick = function () {",
            "        if (state.master === 'KILLED') return;",
            "        state.master = 'KILLED';",
            "        addEvent('manual', 'MASTER KILL \\u2014 all pairs pulled \\u00b7 one gesture, no ceremony (un-kill carries the ceremony)');",
            "        renderMaster();",
            "    };",
            "",
            "    // un-kill: the ceremonious direction — reason + four-eyes framing.",
            "    unkillBar.appendChild(fdk.el('span', 'font-weight:600;color:#a32e2e;', 'Un-kill requires:'));",
            "    var reason = document.createElement('select');",
            "    ['incident resolved', 'false trigger', 'risk sign-off received'].forEach(function (r) {",
            "        var o = document.createElement('option'); o.textContent = r; reason.appendChild(o);",
            "    });",
            "    reason.style.cssText = 'padding:3px 6px;font-size:12px;border:1px solid var(--color-border);border-radius:4px;';",
            "    unkillBar.appendChild(reason);",
            "    unkillBar.appendChild(fdk.el(branch, 'txt-2', 'span', null, '+ second approver (four-eyes)'));",
            "    var unkill = actBtn('resume streaming', function () {",
            "        state.master = 'STREAMING';",
            "        addEvent('manual', 'master resumed \\u00b7 reason: ' + reason.value + ' \\u00b7 four-eyes: supervisor B \\u2713');",
            "        renderMaster();",
            "    });",
            "    unkillBar.appendChild(unkill);",
            "",
            "    function pairActions(row) {",
            "        var cell = fdk.el('span', 'white-space:nowrap;');",
            "        if (state.master === 'KILLED') return cell;",
            "        if (row.state === 'pulled') {",
            "            cell.appendChild(actBtn('resume*', function () {",
            "                row.state = 'streaming'; row.stateNote = null;",
            "                addEvent('manual', row.pair + ' resumed \\u00b7 reason: supervisor (demo) \\u00b7 was pulled > 30 min \\u2192 four-eyes: pending');",
            "                renderGrid();",
            "            }));",
            "        } else {",
            "            cell.appendChild(actBtn('widen', function () {",
            "                row.state = 'auto-widened'; row.stateNote = 'manual widen \\u00d72.0 \\u00b7 expires 17:00';",
            "                addEvent('manual', row.pair + ' widened \\u00d72.0 \\u00b7 reason: supervisor (demo) \\u00b7 auto-expires 17:00');",
            "                renderGrid();",
            "            }));",
            "            cell.appendChild(actBtn('pull', function () {",
            "                row.state = 'pulled'; row.stateNote = 'manual';",
            "                addEvent('manual', row.pair + ' pulled \\u00b7 reason: supervisor (demo)');",
            "                renderGrid();",
            "            }));",
            "        }",
            "        return cell;",
            "    }",
            "",
            "    function renderGrid() {",
            "        clear(gridSlot);",
            "        state.grid = fdGrid(branch, {",
            "            compact: true,",
            "            columns: [",
            "                { key: 'pair', label: 'Pair', render: function (v, row) {",
            "                    var s = fdk.el('span', 'font-weight:600;cursor:pointer;', v);",
            "                    s.onclick = function () {",
            "                        if (party && actorId) party.tellFrom(actorId,",
            "                            { kind: 'InstrumentSelected', instrument: { pair: v } });",
            "                    };",
            "                    return s;",
            "                } },",
            "                { key: 'state', label: 'State', render: function (v, row) {",
            "                    var cell = fdk.el(branch, 'cluster-1', 'span', fd_cluster);",
            "                    cell.appendChild(stateChip(row));",
            "                    if (row.stateNote && state.master !== 'KILLED') {",
            "                        cell.appendChild(fdk.el(branch, 'cap-2', 'span', [fd_caption, fd_muted], row.stateNote));",
            "                    }",
            "                    return cell;",
            "                } },",
            "                { key: 'spread', label: 'Spread', align: 'right' },",
            "                { key: 'skew', label: 'Skew', align: 'right' },",
            "                { key: 'age', label: 'Age', align: 'right' },",
            "                { key: 'hitPct', label: 'Hit%', align: 'right' },",
            "                { key: 'volume', label: 'Today', align: 'right' },",
            "                { key: 'pair', label: 'Actions', render: function (v, row) { return pairActions(row); } }",
            "            ],",
            "            rows: state.pairs",
            "        });",
            "        gridSlot.appendChild(state.grid.root);",
            "    }",
            "",
            "    fetch('/fx/quoting')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            state.master = d.master;",
            "            state.pairs = d.pairs;",
            "            state.events = d.events;",
            "            stampSlot.appendChild(fdk.stamp(branch, 'stamp-1', { slice: d.slice, model: d.model }));",
            "            renderEvents();",
            "            renderMaster();",
            "        })",
            "        .catch(function (e) {",
            "            gridSlot.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'quoting load failed: ' + (e && e.message ? e.message : e)));",
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
