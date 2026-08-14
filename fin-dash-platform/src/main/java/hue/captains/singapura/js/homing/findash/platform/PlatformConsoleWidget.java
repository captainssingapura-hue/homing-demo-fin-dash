package hue.captains.singapura.js.homing.findash.platform;

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
 * W5 — the platform console: end-to-end latency SLOs as live budget meters
 * (burn alerts at 75%), journal health, the nightly replay-determinism
 * scoreboard, and the heaviest-framed Ring-3 controls in the stack — every
 * control states its <b>blast radius in consumer terms before commit</b>
 * ("3 a.m. decisions are made at the level of what the button says"),
 * four-eyes where configured. Demo: arming a control shows the framing;
 * commit mutates screen state only.
 */
public final class PlatformConsoleWidget
        extends WorkspaceWidget<WorkspaceWidget._None, PlatformConsoleWidget> {

    public static final PlatformConsoleWidget INSTANCE = new PlatformConsoleWidget();

    private PlatformConsoleWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, PlatformConsoleWidget> {}

    @Override protected _Construct<_None, PlatformConsoleWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Platform Console"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_header_row(),
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
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'PLATFORM CONSOLE'));",
            "    var healthSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(healthSlot);",
            "    root.appendChild(head);",
            "    var replayLine = fdk.el('div', 'color:var(--color-text-primary);font-size:11.5px;margin:2px 0 4px;');",
            "    root.appendChild(replayLine);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Latency SLOs (p99, live) \\u2014 budget-burn alerts at 75%'));",
            "    var slos = fdk.el('div', 'max-width:420px;');",
            "    root.appendChild(slos);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Journals'));",
            "    var journals = fdk.el('div', 'display:flex;gap:10px;align-items:center;font-size:12px;color:var(--color-text-primary);');",
            "    root.appendChild(journals);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-3', 'Controls \\u2014 Ring 3, heavy framing'));",
            "    var controls = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(controls);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'every control states consumer impact before commit \\u00b7 demo: screen state only'));",
            "",
            "    function sloRow(s) {",
            "        var row = fdk.el('div', 'display:flex;gap:10px;align-items:center;padding:3px 0;font-size:12.5px;');",
            "        row.appendChild(fdk.el('span', 'min-width:120px;color:var(--color-text-primary);', s.path));",
            "        row.appendChild(fdk.el('span', 'min-width:60px;text-align:right;'",
            "            + 'font-variant-numeric:tabular-nums;font-weight:600;', s.p99));",
            "        row.appendChild(fdk.meter(branch, 'meter-1', s.budgetFrac, { width: 90 }));",
            "        if (s.budgetFrac >= 0.75) row.appendChild(fdk.chip(branch, 'chip-1', 'warn', Math.round(s.budgetFrac * 100) + '% budget'));",
            "        return row;",
            "    }",
            "",
            "    function controlRow(c) {",
            "        var box = fdk.el('div', 'border:1px solid var(--color-border);border-radius:8px;padding:8px 10px;'",
            "            + 'margin-bottom:6px;');",
            "        var top = fdk.el('div', 'display:flex;gap:10px;align-items:center;flex-wrap:wrap;');",
            "        var arm = document.createElement('button');",
            "        arm.textContent = c.label + ' \\u25b8';",
            "        arm.style.cssText = 'padding:5px 12px;font-size:12px;font-weight:600;color:var(--color-surface);'",
            "            + 'background:#a32e2e;border:none;border-radius:6px;cursor:pointer;';",
            "        top.appendChild(arm);",
            "        top.appendChild(fdk.el(branch, 'cap-2', 'span', fd_caption, c.scope));",
            "        if (c.fourEyes) top.appendChild(fdk.chip(branch, 'chip-2', 'warn', 'four-eyes'));",
            "        box.appendChild(top);",
            "        box.appendChild(fdk.el('div', 'color:var(--color-text-muted);font-size:11.5px;margin-top:3px;', ",
            "            'blast radius: ' + c.blastRadius));",
            "        var confirm = fdk.el('div', 'display:none;margin-top:6px;gap:8px;align-items:center;');",
            "        confirm.appendChild(fdk.el('span', 'color:#a32e2e;font-size:12px;font-weight:600;', ",
            "            'Commit \\u2192 ' + c.blastRadius + (c.fourEyes ? '  (second approver required)' : '')));",
            "        var go = document.createElement('button');",
            "        go.textContent = 'commit';",
            "        go.style.cssText = 'margin-left:8px;padding:3px 10px;font-size:11px;border:1px solid #a32e2e;'",
            "            + 'border-radius:4px;background:#f9e4e4;color:#a32e2e;cursor:pointer;font-weight:600;';",
            "        confirm.appendChild(go);",
            "        box.appendChild(confirm);",
            "        arm.onclick = function () {",
            "            confirm.style.display = confirm.style.display === 'none' ? 'flex' : 'none';",
            "        };",
            "        go.onclick = function () {",
            "            while (confirm.firstChild) confirm.removeChild(confirm.firstChild);",
            "            confirm.appendChild(fdk.chip(branch, 'chip-3', 'critical', c.label + ' ACTIVE'));",
            "            confirm.appendChild(fdk.el('span', 'margin-left:8px;color:var(--color-text-muted);font-size:11.5px;', ",
            "                'journaled \\u00b7 ' + (c.fourEyes ? 'four-eyes: ops B \\u2713 \\u00b7 ' : '') + 'demo'));",
            "        };",
            "        return box;",
            "    }",
            "",
            "    fetch('/fx/platform')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            var warns = 0;",
            "            for (var i = 0; i < d.slos.length; i++) if (d.slos[i].budgetFrac >= 0.75) warns++;",
            "            healthSlot.appendChild(warns ? fdk.chip('warn', 'healthy, ' + warns + ' warning')",
            "                : fdk.chip('good'));",
            "            replayLine.textContent = 'nightly replay determinism: ' + d.replayDeterminism;",
            "            for (i = 0; i < d.slos.length; i++) slos.appendChild(sloRow(d.slos[i]));",
            "            journals.appendChild(fdk.el('span', null, d.journalHealth + ' \\u00b7 quota'));",
            "            journals.appendChild(fdk.meter(d.journalQuotaFrac, { width: 70 }));",
            "            journals.appendChild(fdk.el(branch, 'muted-1', 'span', fd_muted, ",
            "                Math.round(d.journalQuotaFrac * 100) + '%'));",
            "            for (i = 0; i < d.controls.length; i++) controls.appendChild(controlRow(d.controls[i]));",
            "        })",
            "        .catch(function (e) {",
            "            slos.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'platform load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
