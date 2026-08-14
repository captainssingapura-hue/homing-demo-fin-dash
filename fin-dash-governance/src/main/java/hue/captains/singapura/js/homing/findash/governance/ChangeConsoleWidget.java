package hue.captains.singapura.js.homing.findash.governance;

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
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strongest(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE));
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
            "    var wrap = fdk.el('div', 'display:flex;gap:14px;margin-top:10px;flex-wrap:wrap;');",
            "    root.appendChild(wrap);",
            "    var queue = fdk.el('div', 'min-width:220px;flex:0 0 auto;');",
            "    wrap.appendChild(queue);",
            "    var detail = fdk.el('div', 'flex:1;min-width:320px;border-left:1px solid var(--color-border);'",
            "        + 'padding-left:14px;');",
            "    wrap.appendChild(detail);",
            "",
            "    var state = { changes: [], selected: 0, decided: {} };",
            "    function clear(n) { while (n.firstChild) n.removeChild(n.firstChild); }",
            "",
            "    function renderQueue() {",
            "        clear(queue);",
            "        queue.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Pending'));",
            "        for (var i = 0; i < state.changes.length; i++) {",
            "            (function (i2) {",
            "                var c = state.changes[i2];",
            "                var item = fdk.el('div', 'padding:7px 8px;border-radius:8px;cursor:pointer;'",
            "                    + 'margin-bottom:4px;border:1px solid '",
            "                    + (i2 === state.selected ? '#2a78d6;background:var(--color-surface-raised);' : 'var(--color-border);'));",
            "                var top = fdk.el('div', 'display:flex;gap:8px;align-items:center;');",
            "                top.appendChild(fdk.el(branch, 'strong-1', 'span', fd_strongest, c.id));",
            "                top.appendChild(fdk.el('span', 'color:var(--color-text-primary);font-size:12px;', c.title));",
            "                item.appendChild(top);",
            "                var sub = fdk.el('div', 'display:flex;gap:6px;align-items:center;margin-top:2px;');",
            "                var dec = state.decided[c.id];",
            "                sub.appendChild(dec ? fdk.chip(dec === 'approved' ? 'good' : 'neutral', dec)",
            "                    : fdk.chip(branch, 'chip-1', c.severity === 'serious' ? 'serious' : (c.severity === 'warn' ? 'warn' : 'neutral'),",
            "                        'aging ' + c.aging));",
            "                item.appendChild(sub);",
            "                item.onclick = function () { state.selected = i2; renderQueue(); renderDetail(); };",
            "                queue.appendChild(item);",
            "            })(i);",
            "        }",
            "    }",
            "",
            "    function lines(title, arr) {",
            "        var box = fdk.el(branch, 'slot-2', 'div', null);",
            "        box.appendChild(fdk.sectionTitle(branch, 'sect-2', title));",
            "        for (var i = 0; i < arr.length; i++) {",
            "            box.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:12px;line-height:1.6;', ",
            "                '\\u2022 ' + arr[i]));",
            "        }",
            "        return box;",
            "    }",
            "",
            "    function decideBtn(label, bg, onClick) {",
            "        var b = document.createElement('button');",
            "        b.textContent = label;",
            "        b.style.cssText = 'padding:5px 14px;font-size:12px;font-weight:600;color:var(--color-surface);'",
            "            + 'background:' + bg + ';border:none;border-radius:6px;cursor:pointer;margin-right:6px;';",
            "        b.onclick = onClick;",
            "        return b;",
            "    }",
            "",
            "    function renderDetail() {",
            "        clear(detail);",
            "        var c = state.changes[state.selected];",
            "        if (!c) return;",
            "        detail.appendChild(fdk.el('div', 'font-weight:700;font-size:13px;', ",
            "            'Package ' + c.id + ' \\u2014 ' + c.title));",
            "        detail.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:12px;', c.summary));",
            "        detail.appendChild(lines('Semantic diff', c.semanticDiff));",
            "        detail.appendChild(lines('Golden replay impact', c.replayImpact));",
            "        detail.appendChild(fdk.sectionTitle(branch, 'sect-3', 'Rationale'));",
            "        detail.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:12px;line-height:1.6;', c.rationale));",
            "        detail.appendChild(fdk.sectionTitle(branch, 'sect-4', 'Approval chain \\u2014 structural four-eyes \\u00b7 journaled'));",
            "        var dec = state.decided[c.id];",
            "        for (var i = 0; i < c.chain.length; i++) {",
            "            var s = c.chain[i];",
            "            var signed = s.state === 'signed' || (dec === 'approved' && s.who === 'you');",
            "            var row = fdk.el('div', 'display:flex;gap:8px;align-items:center;padding:2px 0;font-size:12.5px;');",
            "            row.appendChild(fdk.el('span', 'font-weight:600;color:'",
            "                + (signed ? '#006300' : 'var(--color-text-muted)') + ';', signed ? '\\u2713' : '\\u25fb'));",
            "            row.appendChild(fdk.el(branch, 'txt-1', 'span', null, s.role + ' \\u00b7 ' + s.who));",
            "            row.appendChild(fdk.el(branch, 'cap-1', 'span', [fd_caption, fd_muted], ",
            "                signed ? ('signed ' + (s.when || 'now (demo)')) : 'pending'));",
            "            detail.appendChild(row);",
            "        }",
            "        detail.appendChild(fdk.el('div', 'color:var(--color-text-primary);font-size:12px;margin-top:4px;', ",
            "            'activation: ' + c.activation));",
            "        var acts = fdk.el('div', 'margin-top:10px;');",
            "        if (!dec) {",
            "            acts.appendChild(decideBtn('Approve', '#2a78d6', function () {",
            "                state.decided[c.id] = 'approved'; renderQueue(); renderDetail();",
            "            }));",
            "            acts.appendChild(decideBtn('Reject', '#d03b3b', function () {",
            "                state.decided[c.id] = 'rejected'; renderQueue(); renderDetail();",
            "            }));",
            "            acts.appendChild(decideBtn('Request info', 'var(--color-text-muted)', function () {",
            "                state.decided[c.id] = 'info requested'; renderQueue(); renderDetail();",
            "            }));",
            "            acts.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11px;margin-top:4px;', ",
            "                'this approval will be journaled \\u00b7 demo: screen state only'));",
            "        } else {",
            "            acts.appendChild(fdk.chip(branch, 'chip-2', dec === 'approved' ? 'good' : 'neutral', dec));",
            "            acts.appendChild(fdk.el('span', 'margin-left:8px;color:var(--color-text-muted);font-size:11.5px;', ",
            "                dec === 'approved' ? 'journaled \\u00b7 activation stays staged to its named epoch (demo)'",
            "                    : 'journaled (demo)'));",
            "        }",
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
            "            queueSlot.appendChild(fdk.chip(aging ? 'warn' : 'neutral',",
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
