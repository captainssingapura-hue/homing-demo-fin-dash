package hue.captains.singapura.js.homing.findash.risk;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.findash.core.kit.FinDashGridModule;
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
 * Risk views (study §8): the book re-cut by risk's own hierarchy with limits
 * and utilization inline, and the breach worklist with acknowledgment. Risk
 * consumes the same epochs as everyone (P5) but on <b>snapshot cadence</b> —
 * the as-of is explicit and loud, so a snapshot is never mistaken for live.
 * No mark or model controls (independence is the point); what risk does get
 * is <b>annotation</b> — attach a note to any row, visible to the desk,
 * journaled (demo: screen state).
 */
public final class RiskViewsWidget
        extends WorkspaceWidget<WorkspaceWidget._None, RiskViewsWidget> {

    public static final RiskViewsWidget INSTANCE = new RiskViewsWidget();

    private RiskViewsWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, RiskViewsWidget> {}

    @Override protected _Construct<_None, RiskViewsWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Risk Views"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(List.of(new FinDashKitModule.fdk()), FinDashKitModule.INSTANCE),
                new ModuleImports<>(List.of(new FinDashGridModule.fdGrid()), FinDashGridModule.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdFrameCss.fd_hidden(),
                        new FdFrameCss.fd_cluster(),
                        new FdFrameCss.fd_header_row(),
                        new FdFrameCss.fd_row(),
                        new FdFrameCss.fd_section(),
                        new FdFrameCss.fd_spacer(),
                        new FdFrameCss.fd_widget_root()),
                        FdFrameCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdStatusCss.fd_status_warn(),
                        new FdStatusCss.fd_error_text()),
                        FdStatusCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdTextCss.fd_caption(),
                        new FdTextCss.fd_muted(),
                        new FdTextCss.fd_strong(),
                        new FdTextCss.fd_title()),
                        FdTextCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdSurfaceCss.fd_rule()),
                        FdSurfaceCss.INSTANCE),
                new ModuleImports<>(List.of(
                        new FdControlCss.fd_btn_ghost()),
                        FdControlCss.INSTANCE));
    }

    @Override
    protected List<String> constructBodyJs() {
        return List.of(
            "    var root = branch.createElement('root', 'div');",
            "    css.setClass(root, fd_widget_root);",
            "",
            "    var head = fdk.el(branch, 'head-1', 'div', fd_header_row);",
            "    head.appendChild(fdk.el(branch, 'title-1', 'span', fd_title, ",
            "        'RISK VIEWS \\u2014 limits & utilization'));",
            "    var asOfSlot = fdk.el(branch, 'slot-1', 'span', null);",
            "    head.appendChild(asOfSlot);",
            "    root.appendChild(head);",
            "",
            "    var gridSlot = fdk.el(branch, 'sect-1', 'div', fd_section);",
            "    root.appendChild(gridSlot);",
            "",
            "    root.appendChild(fdk.sectionTitle(branch, 'sect-1-2', 'Breach / warning worklist'));",
            "    var worklist = fdk.el(branch, 'slot-2', 'div', null);",
            "    root.appendChild(worklist);",
            "    root.appendChild(fdk.el(branch, 'cap-1', 'div', [fd_caption, fd_muted, fd_section], ",
            "        'no mark or model controls here \\u2014 independence of the view is the point \\u00b7 '",
            "        + 'annotations are journaled and visible to the desk (demo)'));",
            "",
            "    function breachRow(i, b) {",
            "        var line = fdk.el(branch, 'br-' + i, 'div', [fd_row, fd_rule]);",
            "        line.appendChild(fdk.chip(branch, 'br-chip-' + i, b.severity,",
            "            b.severity === 'serious' ? 'breach risk' : 'warning'));",
            "        var main = fdk.el(branch, 'br-main-' + i, 'div', fd_spacer);",
            "        main.appendChild(fdk.el(branch, 'br-what-' + i, 'div', fd_strong, b.what));",
            "        main.appendChild(fdk.el(branch, 'br-detail-' + i, 'div', fd_caption, b.detail));",
            "        line.appendChild(main);",
            "        if (b.state === 'unacknowledged') {",
            "            var ack = fdk.el(branch, 'br-ack-' + i, 'button', fd_btn_ghost, 'acknowledge');",
            "            var acked = fdk.el(branch, 'br-acked-' + i, 'span', [fd_caption, fd_muted, fd_hidden],",
            "                'acknowledged now \\u00b7 journaled (demo)');",
            "            line.appendChild(acked);",
            "            ack.onclick = function () {",
            "                css.addClass(ack, fd_hidden);",
            "                css.removeClass(acked, fd_hidden);",
            "            };",
            "            line.appendChild(ack);",
            "        } else {",
            "            line.appendChild(fdk.el(branch, 'br-state-' + i, 'span', [fd_caption, fd_muted], b.state));",
            "        }",
            "        return line;",
            "    }",
            "",
            "    fdk.load('/fx/risk', { branch: branch, host: gridSlot, what: 'risk' }, function (d) {",
            "            asOfSlot.appendChild(fdk.chip(branch, 'chip-2', 'neutral', 'as-of ' + d.asOf + ' \\u2014 NOT live'));",
            "            var grid = fdGrid(branch, {",
            "                compact: true,",
            "                columns: [",
            "                    { key: 'book', label: 'Book' },",
            "                    { key: 'metric', label: 'Metric' },",
            "                    { key: 'usage', label: 'Usage', align: 'right' },",
            "                    { key: 'limit', label: 'Limit', align: 'right' },",
            "                    { key: 'frac', label: 'Utilization', render: function (v, row, b, i) {",
            "                        var cell = fdk.el(b, 'util-' + i, 'span', fd_cluster);",
            "                        cell.appendChild(fdk.meter(b, 'util-meter-' + i, v));",
            "                        cell.appendChild(fdk.el(b, 'util-pct-' + i, 'span', fd_caption,",
            "                            Math.round(v * 100) + '%'));",
            "                        return cell;",
            "                    } },",
            "                    { key: 'note', label: 'Note', render: function (v, row, b, i) {",
            "                        var cell = fdk.el(b, 'note-' + i, 'span', fd_cluster);",
            "                        if (v) {",
            "                            cell.appendChild(fdk.el(b, 'note-text-' + i, 'span',",
            "                                [fd_caption, fd_status_warn], v));",
            "                        }",
            "                        var note = fdk.el(b, 'note-btn-' + i, 'button', fd_btn_ghost, '+ note');",
            "                        note.onclick = function () {",
            "                            note.textContent = 'noted \\u2713 (journaled, visible to desk)';",
            "                            note.disabled = true;",
            "                        };",
            "                        cell.appendChild(note);",
            "                        return cell;",
            "                    } }",
            "                ],",
            "                rows: d.limits",
            "            });",
            "            gridSlot.appendChild(grid.root);",
            "            for (var i = 0; i < d.breaches.length; i++) worklist.appendChild(breachRow(i, d.breaches[i]));",
"    });",
            "",
            "    return {",
            "        root: root,",
            "        setActive: function (active) {},",
            "        partyDeregister: function () {}",
            "    };");
    }
}
