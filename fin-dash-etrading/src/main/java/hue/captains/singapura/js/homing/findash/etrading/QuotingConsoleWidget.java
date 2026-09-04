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
import hue.captains.singapura.js.homing.findash.core.css.FdControlCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

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
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_hidden(),
                        new FdFrameCss.fd_scroll(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_critical(),
                        new FdStatusCss.fd_status_critical_bg(),
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_num(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn_danger(),
                        new FdControlCss.fd_btn_ghost(),
                        new FdControlCss.fd_clickable(),
                        new FdControlCss.fd_input()),
                        FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_card(),
                        new FdSurfaceCss.fd_panel()),
                        FdSurfaceCss.INSTANCE));
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
            "        actorId = fdk.actorId('etrading/console', branch);",
            "        party.joinActor({ id: actorId, parentSecretary: 'desk', reactors: {} });",
            "    }",
            "",
            "    var state = { master: 'STREAMING', pairs: [], events: [], grid: null };",
            "    function now() { var d = new Date(); function p(x){ return (x < 10 ? '0' : '') + x; }",
            "        return p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds()); }",
            "",
            "    // Four regions re-render on their own cadence — master state, the event",
            "    // stream, the pair grid, and the un-kill bar's controls. Each owns a",
            "    // dissolvable sub-branch, since a branch name is claimable only once.",
            "    var masterBranch = null, evBranch = null, gridBranch = null;",
            "",
            "    // -- header: master state + the unceremonious kill ------------------",
            "    var head = fdk.el(branch, 'head', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'E-TRADING CONSOLE'));",
            "    var masterSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(masterSlot);",
            "    head.appendChild(fdk.el(branch, 'spacer', 'span', fd_spacer));",
            "    var kill = fdk.el(branch, 'kill', 'button', fd_btn_danger);",
            "    head.appendChild(kill);",
            "    root.appendChild(head);",
            "",
            "    var unkillBar = fdk.el(branch, 'unkill-bar', 'div',",
            "        [fd_cluster, fd_panel, fd_status_critical_bg, fd_dense, fd_hidden]);",
            "    root.appendChild(unkillBar);",
            "",
            "    var stampSlot = fdk.el(branch, 'stamp-slot', 'div', fd_section);",
            "    root.appendChild(stampSlot);",
            "",
            "    var evTitle = fdk.sectionTitle(branch, 'sect-1', 'Event stream — automatic + manual, one stream');",
            "    root.appendChild(evTitle);",
            "    var evList = fdk.el(branch, 'ev-list', 'div', [fd_card, fd_scroll, fd_caption]);",
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
            "    function stateChip(b, key, p) {",
            "        if (state.master === 'KILLED') return fdk.chip(b, key, 'critical', 'killed');",
            "        if (p.state === 'streaming')    return fdk.chip(b, key, 'live', 'streaming');",
            "        if (p.state === 'auto-widened') return fdk.chip(b, key, 'warn', 'auto-widened');",
            "        return fdk.chip(b, key, 'neutral', 'pulled');",
            "    }",
            "",
            "    function addEvent(kind, text) {",
            "        state.events.unshift({ time: now(), kind: kind, text: text });",
            "        renderEvents();",
            "    }",
            "    function renderEvents() {",
            "        if (evBranch) evBranch.dissolve();",
            "        evBranch = branch.createBranch('events');",
            "        evBranch.activate(root);",
            "        var b = evBranch;",
            "        for (var i = 0; i < state.events.length; i++) {",
            "            var e = state.events[i];",
            "            var row = fdk.el(b, 'ev-' + i, 'div', null);",
            "            row.appendChild(fdk.el(b, 'ev-time-' + i, 'span', [fd_muted, fd_num], e.time + '  '));",
            "            // Manual intervention is the one worth spotting in the stream;",
            "            // automatic entries stay in ordinary emphasis.",
            "            var kindEl = fdk.el(b, 'ev-kind-' + i, 'span', fd_strong, '[' + e.kind + '] ');",
            "            if (e.kind !== 'auto') css.addClass(kindEl, fd_status_warn);",
            "            row.appendChild(kindEl);",
            "            row.appendChild(fdk.el(b, 'ev-text-' + i, 'span', null, e.text));",
            "            evList.appendChild(row);",
            "        }",
            "    }",
            "",
            "    function actBtn(b, key, label, onClick) {",
            "        var el = fdk.el(b, key, 'button', fd_btn_ghost, label);",
            "        el.onclick = function (ev) { ev.stopPropagation(); onClick(); };",
            "        return el;",
            "    }",
            "",
            "    function renderMaster() {",
            "        if (masterBranch) masterBranch.dissolve();",
            "        masterBranch = branch.createBranch('master');",
            "        masterBranch.activate(root);",
            "        masterSlot.appendChild(state.master === 'KILLED'",
            "            ? fdk.chip(masterBranch, 'master-chip', 'critical', 'master: KILLED')",
            "            : fdk.chip(masterBranch, 'master-chip', 'live', 'master: STREAMING'));",
            "        kill.textContent = state.master === 'KILLED' ? 'KILLED \\u23fb' : 'MASTER KILL \\u23fb';",
            "        kill.disabled = state.master === 'KILLED';",
            "        // Visibility is a class, not a style write.",
            "        if (state.master === 'KILLED') css.removeClass(unkillBar, fd_hidden);",
            "        else                           css.addClass(unkillBar, fd_hidden);",
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
            "    unkillBar.appendChild(fdk.el(branch, 'unkill-label', 'span',",
            "        [fd_strong, fd_status_critical], 'Un-kill requires:'));",
            "    var reason = fdk.el(branch, 'unkill-reason', 'select', fd_input);",
            "    ['incident resolved', 'false trigger', 'risk sign-off received'].forEach(function (r, ri) {",
            "        reason.appendChild(fdk.el(branch, 'unkill-opt-' + ri, 'option', null, r));",
            "    });",
            "    unkillBar.appendChild(reason);",
            "    unkillBar.appendChild(fdk.el(branch, 'txt-2', 'span', null, '+ second approver (four-eyes)'));",
            "    var unkill = actBtn(branch, 'unkill-go', 'resume streaming', function () {",
            "        state.master = 'STREAMING';",
            "        addEvent('manual', 'master resumed \\u00b7 reason: ' + reason.value + ' \\u00b7 four-eyes: supervisor B \\u2713');",
            "        renderMaster();",
            "    });",
            "    unkillBar.appendChild(unkill);",
            "",
            "    // b/i are the grid's row branch and row index — the action buttons are",
            "    // rebuilt per row per render, so they must be owned by the branch that",
            "    // gets dissolved and keyed by the row.",
            "    function pairActions(b, i, row) {",
            "        var cell = fdk.el(b, 'act-' + i, 'span', fd_cluster);",
            "        if (state.master === 'KILLED') return cell;",
            "        if (row.state === 'pulled') {",
            "            cell.appendChild(actBtn(b, 'act-resume-' + i, 'resume*', function () {",
            "                row.state = 'streaming'; row.stateNote = null;",
            "                addEvent('manual', row.pair + ' resumed \\u00b7 reason: supervisor (demo) \\u00b7 was pulled > 30 min \\u2192 four-eyes: pending');",
            "                renderGrid();",
            "            }));",
            "        } else {",
            "            cell.appendChild(actBtn(b, 'act-widen-' + i, 'widen', function () {",
            "                row.state = 'auto-widened'; row.stateNote = 'manual widen \\u00d72.0 \\u00b7 expires 17:00';",
            "                addEvent('manual', row.pair + ' widened \\u00d72.0 \\u00b7 reason: supervisor (demo) \\u00b7 auto-expires 17:00');",
            "                renderGrid();",
            "            }));",
            "            cell.appendChild(actBtn(b, 'act-pull-' + i, 'pull', function () {",
            "                row.state = 'pulled'; row.stateNote = 'manual';",
            "                addEvent('manual', row.pair + ' pulled \\u00b7 reason: supervisor (demo)');",
            "                renderGrid();",
            "            }));",
            "        }",
            "        return cell;",
            "    }",
            "",
            "    function renderGrid() {",
            "        if (gridBranch) gridBranch.dissolve();",
            "        gridBranch = branch.createBranch('grid');",
            "        gridBranch.activate(root);",
            "        state.grid = fdGrid(gridBranch, {",
            "            compact: true,",
            "            columns: [",
            "                { key: 'pair', label: 'Pair', render: function (v, row, b, i) {",
            "                    var s = fdk.el(b, 'pair-' + i, 'span', [fd_strong, fd_clickable], v);",
            "                    s.onclick = function () {",
            "                        if (party && actorId) party.tellFrom(actorId,",
            "                            { kind: 'InstrumentSelected', instrument: { pair: v } });",
            "                    };",
            "                    return s;",
            "                } },",
            "                { key: 'state', label: 'State', render: function (v, row, b, i) {",
            "                    var cell = fdk.el(b, 'st-' + i, 'span', fd_cluster);",
            "                    cell.appendChild(stateChip(b, 'st-chip-' + i, row));",
            "                    if (row.stateNote && state.master !== 'KILLED') {",
            "                        cell.appendChild(fdk.el(b, 'st-note-' + i, 'span', [fd_caption, fd_muted], row.stateNote));",
            "                    }",
            "                    return cell;",
            "                } },",
            "                { key: 'spread', label: 'Spread', align: 'right' },",
            "                { key: 'skew', label: 'Skew', align: 'right' },",
            "                { key: 'age', label: 'Age', align: 'right' },",
            "                { key: 'hitPct', label: 'Hit%', align: 'right' },",
            "                { key: 'volume', label: 'Today', align: 'right' },",
            "                { key: 'pair', label: 'Actions', render: function (v, row, b, i) { return pairActions(b, i, row); } }",
            "            ],",
            "            rows: state.pairs",
            "        });",
            "        gridSlot.appendChild(state.grid.root);",
            "    }",
            "",
            "    fdk.load('/fx/quoting', { branch: branch, host: gridSlot, what: 'quoting' }, function (d) {",
            "            state.master = d.master;",
            "            state.pairs = d.pairs;",
            "            state.events = d.events;",
            "            stampSlot.appendChild(fdk.stamp(branch, 'stamp-1', { slice: d.slice, model: d.model }));",
            "            renderEvents();",
            "            renderMaster();",
"    });",
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
