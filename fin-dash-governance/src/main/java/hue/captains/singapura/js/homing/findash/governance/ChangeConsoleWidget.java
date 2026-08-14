package hue.captains.singapura.js.homing.findash.governance;

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
 * W6 — the change console: the Ring-2 promotion workflow rendered as a queue.
 * Every pending change arrives as a <b>package</b>: the semantic diff in
 * domain terms, the golden-replay impact report with materiality flags, the
 * requester's rationale, and the approval-chain state — structural four-eyes,
 * the approval itself journaled, activation <b>staged to a named epoch, never
 * "now"</b> (P3). Approve/reject mutate the chain on screen; demo, not
 * journaled.
 */
public final class ChangeConsoleWidget
        extends WorkspaceWidget<WorkspaceWidget._None, ChangeConsoleWidget> {

    public static final ChangeConsoleWidget INSTANCE = new ChangeConsoleWidget();

    private ChangeConsoleWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, ChangeConsoleWidget> {}

    @Override protected _Construct<_None, ChangeConsoleWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Change Console"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_good(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_body(),
                        new FdTextCss.fd_dense(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strongest(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn(),
                        new FdControlCss.fd_btn_danger(),
                        new FdControlCss.fd_btn_ghost(),
                        new FdControlCss.fd_clickable(),
                        new FdControlCss.fd_selected()),
                        FdControlCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_card(),
                        new FdSurfaceCss.fd_rule_left()),
                        FdSurfaceCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'CHANGE CONSOLE \\u2014 Ring-2 promotions'));",
            "    var queueSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(queueSlot);",
            "    root.appendChild(head);",
            "",
            "    var wrap = fdk.el(branch, 'wrap', 'div', [fd_header_row, fd_section]);",
            "    root.appendChild(wrap);",
            "    var queue = fdk.el(branch, 'queue', 'div', null);",
            "    wrap.appendChild(queue);",
            "    var detail = fdk.el(branch, 'detail', 'div', [fd_spacer, fd_rule_left]);",
            "    wrap.appendChild(detail);",
            "",
            "    var state = { changes: [], selected: 0, decided: {} };",
            "",
            "    // Queue and detail both re-render on selection and on decisions, so",
            "    // each owns a dissolvable sub-branch — a branch element name may only",
            "    // be claimed once, and dissolve() detaches the previous DOM.",
            "    var queueBranch = null, detailBranch = null;",
            "",
            "    function renderQueue() {",
            "        if (queueBranch) queueBranch.dissolve();",
            "        queueBranch = branch.createBranch('queue');",
            "        queueBranch.activate(root);",
            "        var b = queueBranch;",
            "        queue.appendChild(fdk.sectionTitle(b, 'sect-pending', 'Pending'));",
            "        for (var i = 0; i < state.changes.length; i++) {",
            "            (function (i2) {",
            "                var c = state.changes[i2];",
            "                var item = fdk.el(b, 'q-' + i2, 'div', [fd_card, fd_clickable, fd_row]);",
            "                if (i2 === state.selected) css.addClass(item, fd_selected);",
            "                var top = fdk.el(b, 'q-top-' + i2, 'div', fd_cluster);",
            "                top.appendChild(fdk.el(b, 'q-id-' + i2, 'span', fd_strongest, c.id));",
            "                top.appendChild(fdk.el(b, 'q-title-' + i2, 'span', fd_dense, c.title));",
            "                item.appendChild(top);",
            "                var sub = fdk.el(b, 'q-sub-' + i2, 'div', fd_cluster);",
            "                var dec = state.decided[c.id];",
            "                sub.appendChild(dec",
            "                    ? fdk.chip(b, 'q-chip-' + i2, dec === 'approved' ? 'good' : 'neutral', dec)",
            "                    : fdk.chip(b, 'q-chip-' + i2,",
            "                        c.severity === 'serious' ? 'serious' : (c.severity === 'warn' ? 'warn' : 'neutral'),",
            "                        'aging ' + c.aging));",
            "                item.appendChild(sub);",
            "                item.onclick = function () { state.selected = i2; renderQueue(); renderDetail(); };",
            "                queue.appendChild(item);",
            "            })(i);",
            "        }",
            "    }",
            "",
            "    function lines(b, key, title, arr) {",
            "        var box = fdk.el(b, key, 'div', null);",
            "        box.appendChild(fdk.sectionTitle(b, key + '-title', title));",
            "        for (var i = 0; i < arr.length; i++) {",
            "            box.appendChild(fdk.el(b, key + '-' + i, 'div', fd_dense, '\\u2022 ' + arr[i]));",
            "        }",
            "        return box;",
            "    }",
            "",
            "    function decideBtn(b, key, label, klass, onClick) {",
            "        var el = fdk.el(b, key, 'button', klass, label);",
            "        el.onclick = onClick;",
            "        return el;",
            "    }",
            "",
            "    function renderDetail() {",
            "        if (detailBranch) detailBranch.dissolve();",
            "        detailBranch = branch.createBranch('detail');",
            "        detailBranch.activate(root);",
            "        var b = detailBranch;",
            "        var c = state.changes[state.selected];",
            "        if (!c) return;",
            "        detail.appendChild(fdk.el(b, 'd-title', 'div', [fd_strongest, fd_body],",
            "            'Package ' + c.id + ' \\u2014 ' + c.title));",
            "        detail.appendChild(fdk.el(b, 'd-summary', 'div', fd_dense, c.summary));",
            "        detail.appendChild(lines(b, 'd-diff', 'Semantic diff', c.semanticDiff));",
            "        detail.appendChild(lines(b, 'd-replay', 'Golden replay impact', c.replayImpact));",
            "        detail.appendChild(fdk.sectionTitle(b, 'sect-rationale', 'Rationale'));",
            "        detail.appendChild(fdk.el(b, 'd-rationale', 'div', fd_dense, c.rationale));",
            "        detail.appendChild(fdk.sectionTitle(b, 'sect-chain',",
            "            'Approval chain \\u2014 structural four-eyes \\u00b7 journaled'));",
            "        var dec = state.decided[c.id];",
            "        for (var i = 0; i < c.chain.length; i++) {",
            "            var s = c.chain[i];",
            "            var signed = s.state === 'signed' || (dec === 'approved' && s.who === 'you');",
            "            var row = fdk.el(b, 'ch-' + i, 'div', [fd_cluster, fd_dense]);",
            "            // Signed reads as a healthy state; unsigned is simply secondary.",
            "            var mark = fdk.el(b, 'ch-mark-' + i, 'span', fd_strong, signed ? '\\u2713' : '\\u25fb');",
            "            css.addClass(mark, signed ? fd_status_good : fd_muted);",
            "            row.appendChild(mark);",
            "            row.appendChild(fdk.el(b, 'ch-who-' + i, 'span', null, s.role + ' \\u00b7 ' + s.who));",
            "            row.appendChild(fdk.el(b, 'ch-when-' + i, 'span', [fd_caption, fd_muted], ",
            "                signed ? ('signed ' + (s.when || 'now (demo)')) : 'pending'));",
            "            detail.appendChild(row);",
            "        }",
            "        detail.appendChild(fdk.el(b, 'd-activation', 'div', [fd_dense, fd_section],",
            "            'activation: ' + c.activation));",
            "        var acts = fdk.el(b, 'd-acts', 'div', [fd_cluster, fd_section]);",
            "        if (!dec) {",
            "            acts.appendChild(decideBtn(b, 'act-approve', 'Approve', fd_btn, function () {",
            "                state.decided[c.id] = 'approved'; renderQueue(); renderDetail();",
            "            }));",
            "            acts.appendChild(decideBtn(b, 'act-reject', 'Reject', fd_btn_danger, function () {",
            "                state.decided[c.id] = 'rejected'; renderQueue(); renderDetail();",
            "            }));",
            "            acts.appendChild(decideBtn(b, 'act-info', 'Request info', fd_btn_ghost, function () {",
            "                state.decided[c.id] = 'info requested'; renderQueue(); renderDetail();",
            "            }));",
            "            detail.appendChild(acts);",
            "            detail.appendChild(fdk.el(b, 'd-note', 'div', [fd_caption, fd_muted], ",
            "                'this approval will be journaled \\u00b7 demo: screen state only'));",
            "            return;",
            "        }",
            "        acts.appendChild(fdk.chip(b, 'd-chip', dec === 'approved' ? 'good' : 'neutral', dec));",
            "        acts.appendChild(fdk.el(b, 'd-outcome', 'span', [fd_caption, fd_muted], ",
            "            dec === 'approved' ? 'journaled \\u00b7 activation stays staged to its named epoch (demo)'",
            "                : 'journaled (demo)'));",
            "        detail.appendChild(acts);",
            "    }",
            "",
            "    fetch('/fx/changes')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            state.changes = d.changes;",
            "            var aging = 0;",
            "            for (var i = 0; i < d.changes.length; i++) {",
            "                if (d.changes[i].severity === 'serious') aging++;",
            "            }",
            "            queueSlot.appendChild(fdk.chip(branch, 'queue-chip',",
            "                aging ? 'warn' : 'neutral',",
            "                'queue: ' + d.changes.length + ' pending' + (aging ? ' \\u00b7 ' + aging + ' aging > 5 d' : '')));",
            "            renderQueue(); renderDetail();",
            "        })",
            "        .catch(function (e) {",
            "            detail.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'changes load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
