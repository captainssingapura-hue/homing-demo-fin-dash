package hue.captains.singapura.js.homing.findash.summary;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashKitModule;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import hue.captains.singapura.js.homing.findash.core.css.FdFrameCss;
import hue.captains.singapura.js.homing.findash.core.css.FdStatusCss;
import hue.captains.singapura.js.homing.findash.core.css.FdTextCss;
import hue.captains.singapura.js.homing.findash.core.css.FdSurfaceCss;

import java.util.List;

/**
 * The management summary (study §13): a glanceable rollup — P&amp;L, risk vs
 * limits, quoting posture, open overrides past threshold age, pending Ring-2
 * approvals, system health. Every tile is a <b>projection of the owning
 * persona's data</b> (P5 — no numbers computed specially for management) and
 * names the workspace that owns it; the landing page is the door.
 */
public final class SummaryDashboardWidget
        extends WorkspaceWidget<WorkspaceWidget._None, SummaryDashboardWidget> {

    public static final SummaryDashboardWidget INSTANCE = new SummaryDashboardWidget();

    private SummaryDashboardWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, SummaryDashboardWidget> {}

    @Override protected _Construct<_None, SummaryDashboardWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Summary"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(new ModuleImports<>(
                List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_grid_auto(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_display(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
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
            "        'MANAGEMENT SUMMARY'));",
            "    var stampSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(stampSlot);",
            "    root.appendChild(head);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_section], ",
            "        'every tile a projection of the owning persona\\u2019s data (P5) \\u00b7 the landing page is the door'));",
            "",
            "    var gridBox = fdk.el(branch, 'grid-1', 'div', fd_grid_auto);",
            "    root.appendChild(gridBox);",
            "",
            "    function tileCard(rk, t) {",
            "        var card = fdk.el(branch, 'el-1-' + rk, 'div', [fd_card, fd_panel]);",
            "        card.appendChild(fdk.el(branch, 'cap-1-' + rk, 'div', [fd_muted, fd_strong, fd_caption], t.title));",
            "        var v = fdk.el(branch, 'cluster-1-' + rk, 'div', [fd_cluster, fd_section]);",
            "        v.appendChild(fdk.el(branch, 'display-1-' + rk, 'span', fd_display, t.value));",
            "        v.appendChild(fdk.chip(branch, 'chip-1-' + rk, t.severity, t.severity === 'good' ? 'ok' : (t.severity === 'serious' ? 'attend' : 'watch')));",
            "        card.appendChild(v);",
            "        card.appendChild(fdk.el(branch, 'cap-2-' + rk, 'div', fd_caption, t.detail));",
            "        card.appendChild(fdk.el(branch, 'tile-note-' + rk, 'div', [fd_muted, fd_caption, fd_section], ",
            "            'owned by: ' + t.owner));",
            "        return card;",
            "    }",
            "",
            "    fetch('/fx/summary')",
            "        .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })",
            "        .then(function (d) {",
            "            stampSlot.appendChild(fdk.stamp(branch, 'stamp-1', { slice: d.slice }));",
            "            for (var i = 0; i < d.tiles.length; i++) gridBox.appendChild(tileCard(i, d.tiles[i]));",
            "        })",
            "        .catch(function (e) {",
            "            gridBox.appendChild(fdk.el(branch, 'err-1', 'div', fd_error_text, ",
            "                'summary load failed: ' + (e && e.message ? e.message : e)));",
            "        });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
