package hue.captains.singapura.js.homing.findash.platform;

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
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_hidden(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_critical(),
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
                        new FdControlCss.fd_btn_ghost()),
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
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'PLATFORM CONSOLE'));",
            "    var healthSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(healthSlot);",
            "    root.appendChild(head);",
            "    var replayLine = fdk.el(branch, 'replay', 'div', [fd_caption, fd_section]);",
            "    root.appendChild(replayLine);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1', 'Latency SLOs (p99, live) \\u2014 budget-burn alerts at 75%'));",
            "    var slos = fdk.el(branch, 'slos', 'div', null);",
            "    root.appendChild(slos);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-2', 'Journals'));",
            "    var journals = fdk.el(branch, 'journals', 'div', [fd_cluster, fd_dense]);",
            "    root.appendChild(journals);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-3', 'Controls \\u2014 Ring 3, heavy framing'));",
            "    var controls = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(controls);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'every control states consumer impact before commit \\u00b7 demo: screen state only'));",
            "",
            "    function sloRow(i, s) {",
            "        var row = fdk.el(branch, 'slo-' + i, 'div', [fd_row, fd_dense]);",
            "        row.appendChild(fdk.el(branch, 'slo-path-' + i, 'span', null, s.path));",
            "        row.appendChild(fdk.el(branch, 'slo-p99-' + i, 'span', [fd_strong, fd_num], s.p99));",
            "        row.appendChild(fdk.meter(branch, 'slo-meter-' + i, s.budgetFrac));",
            "        if (s.budgetFrac >= 0.75) {",
            "            row.appendChild(fdk.chip(branch, 'slo-chip-' + i, 'warn',",
            "                Math.round(s.budgetFrac * 100) + '% budget'));",
            "        }",
            "        return row;",
            "    }",
            "",
            "    function controlRow(i, c) {",
            "        var box = fdk.el(branch, 'ctl-' + i, 'div', [fd_card, fd_panel]);",
            "        var top = fdk.el(branch, 'ctl-top-' + i, 'div', fd_header_row);",
            "        // Arming a platform-wide control is destructive by nature — it reads",
            "        // that way before it is pressed, not only after (P3).",
            "        var arm = fdk.el(branch, 'ctl-arm-' + i, 'button', fd_btn_danger, c.label + ' \\u25b8');",
            "        top.appendChild(arm);",
            "        top.appendChild(fdk.el(branch, 'ctl-scope-' + i, 'span', fd_caption, c.scope));",
            "        if (c.fourEyes) top.appendChild(fdk.chip(branch, 'ctl-4e-' + i, 'warn', 'four-eyes'));",
            "        box.appendChild(top);",
            "        box.appendChild(fdk.el(branch, 'ctl-blast-' + i, 'div', [fd_caption, fd_muted],",
            "            'blast radius: ' + c.blastRadius));",
            "        var confirm = fdk.el(branch, 'ctl-confirm-' + i, 'div',",
            "            [fd_cluster, fd_section, fd_hidden]);",
            "        confirm.appendChild(fdk.el(branch, 'ctl-warn-' + i, 'span',",
            "            [fd_strong, fd_dense, fd_status_critical],",
            "            'Commit \\u2192 ' + c.blastRadius + (c.fourEyes ? '  (second approver required)' : '')));",
            "        var go = fdk.el(branch, 'ctl-go-' + i, 'button', fd_btn_ghost, 'commit');",
            "        css.addClass(go, fd_status_critical);",
            "        confirm.appendChild(go);",
            "        var done = fdk.el(branch, 'ctl-done-' + i, 'div', [fd_cluster, fd_section, fd_hidden]);",
            "        box.appendChild(confirm);",
            "        box.appendChild(done);",
            "        arm.onclick = function () { css.toggleClass(confirm, fd_hidden); };",
            "        go.onclick = function () {",
            "            css.addClass(confirm, fd_hidden);",
            "            css.removeClass(done, fd_hidden);",
            "            done.appendChild(fdk.chip(branch, 'ctl-live-' + i, 'critical', c.label + ' ACTIVE'));",
            "            done.appendChild(fdk.el(branch, 'ctl-note-' + i, 'span', [fd_caption, fd_muted],",
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
            "            healthSlot.appendChild(warns",
            "                ? fdk.chip(branch, 'health', 'warn', 'healthy, ' + warns + ' warning')",
            "                : fdk.chip(branch, 'health-2', 'good'));",
            "            replayLine.textContent = 'nightly replay determinism: ' + d.replayDeterminism;",
            "            for (i = 0; i < d.slos.length; i++) slos.appendChild(sloRow(i, d.slos[i]));",
            "            journals.appendChild(fdk.el(branch, 'jrn-label', 'span', null,",
            "                d.journalHealth + ' \\u00b7 quota'));",
            "            journals.appendChild(fdk.meter(branch, 'jrn-meter', d.journalQuotaFrac));",
            "            journals.appendChild(fdk.el(branch, 'jrn-pct', 'span', fd_muted, ",
            "                Math.round(d.journalQuotaFrac * 100) + '%'));",
            "            for (i = 0; i < d.controls.length; i++) controls.appendChild(controlRow(i, d.controls[i]));",
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
